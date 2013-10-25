package com.enioka.jqm.tools;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import org.apache.log4j.Logger;
import org.sonatype.aether.artifact.Artifact;
import org.sonatype.aether.repository.RemoteRepository;
import org.sonatype.aether.resolution.DependencyResolutionException;
import org.sonatype.aether.util.artifact.DefaultArtifact;

import com.enioka.jqm.api.JobBase;
import com.enioka.jqm.deliverabletools.DeliverableStruct;
import com.enioka.jqm.jpamodel.History;
import com.enioka.jqm.jpamodel.JobInstance;
import com.enioka.jqm.temp.Polling;
import com.jcabi.aether.Aether;

public class Loader implements Runnable
{

	JobInstance job = null;
	Object jobBase = new JobBase();
	ArrayList<DeliverableStruct> s1s = new ArrayList<DeliverableStruct>();
	EntityManager em = Helpers.getNewEm();
	Map<String, ClassLoader> cache = null;
	boolean isInCache = true;
	Logger jqmlogger = Logger.getLogger(this.getClass());
	Polling p = null;

	public Loader(JobInstance job, Map<String, ClassLoader> cache, Polling p)
	{

		this.job = job;
		this.cache = cache;
		this.p = p;
	}

	public void crashedStatus()
	{

		EntityTransaction transac = em.getTransaction();
		transac.begin();

		// STATE UPDATED

		em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", job.getId())
				.setParameter("msg", "CRASHED").executeUpdate();

		// MESSAGE HISTORY UPDATED

		History h = em.createQuery("SELECT h FROM History h WHERE h.id = :j", History.class).setParameter("j", job.getId())
				.getSingleResult();

		Helpers.createMessage("Status updated: ATTRIBUTED", h, em);

		transac.commit();
	}

	@Override
	public void run()
	{

		try
		{
			jqmlogger.debug("TOUT DEBUT LOADER");

			// ---------------- BEGIN: MAVEN DEPENDENCIES ------------------
			File local = new File(System.getProperty("user.home") + "/.m2/repository");
			File jar = new File(job.getJd().getJarPath());
			URL jars = jar.toURI().toURL();
			jqmlogger.debug("Loader will try to launch jar " + job.getJd().getJarPath() + " - " + job.getJd().getJavaClassName());
			ArrayList<URL> tmp = new ArrayList<URL>();
			Collection<Artifact> deps = null;

			// Update of the job status
			em.getTransaction().begin();
			History h = em.createQuery("SELECT h FROM History h WHERE h.jobInstance = :j", History.class).setParameter("j", job)
					.getSingleResult();

			// Update of the execution date

			Calendar executionDate = GregorianCalendar.getInstance(Locale.getDefault());

			em.createQuery("UPDATE History h SET h.executionDate = :date WHERE h.id = :h").setParameter("h", h.getId())
					.setParameter("date", executionDate).executeUpdate();

			// End of the execution date

			jqmlogger.debug("History was updated");

			Helpers.createMessage("Status updated: RUNNING", h, em);
			em.getTransaction().commit();

			EntityTransaction transac = em.getTransaction();
			transac.begin();

			em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j)").setParameter("j", job.getId())
					.setParameter("msg", "RUNNING").executeUpdate();
			transac.commit();
			jqmlogger.debug("JobInstance was updated");

			// if (!cache.containsKey(job.getJd().getApplicationName()))
			// {
			Dependencies dependencies = new Dependencies(job.getJd().getFilePath() + "pom.xml");

			isInCache = false;
			Collection<RemoteRepository> remotes = Arrays.asList(new RemoteRepository("maven-central", "default",
					"http://repo1.maven.org/maven2/"), new RemoteRepository("eclipselink", "default",
					"http://download.eclipse.org/rt/eclipselink/maven.repo/")

			);

			deps = new ArrayList<Artifact>();
			for (int i = 0; i < dependencies.getList().size(); i++)
			{
				jqmlogger.info("Resolving Maven dep " + dependencies.getList().get(i));
				deps.addAll(new Aether(remotes, local).resolve(new DefaultArtifact(dependencies.getList().get(i)), "compile"));
			}

			for (Artifact artifact : deps)
			{
				tmp.add(artifact.getFile().toURI().toURL());
				jqmlogger.info("Artifact: " + artifact.getFile().toURI().toURL());
			}
			// }
			// ------------------- END: MAVEN DEPENDENCIES ---------------

			// We save the actual classloader
			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();
			JarClassLoader jobClassLoader = null;
			URL[] urls = tmp.toArray(new URL[tmp.size()]);

			if (!isInCache)
			{
				jobClassLoader = new JarClassLoader(jars, urls);
				cache.put(job.getJd().getApplicationName(), jobClassLoader);
			}
			else
				jobClassLoader = (JarClassLoader) cache.get(job.getJd().getApplicationName());

			// Change active class loader
			jqmlogger.debug("Setting class loader");
			Thread.currentThread().setContextClassLoader(jobClassLoader);
			jqmlogger.info("Class Loader was set correctly");

			// Go! (launches the main function in the startup class designated in the manifest)
			jqmlogger.debug("+++++++++++++++++++++++++++++++++++++++");
			jqmlogger.debug("Job is running in the thread: " + Thread.currentThread().getName());
			jqmlogger.debug("AVANT INVOKE MAIN");
			jobBase = jobClassLoader.invokeMain(job);
			jqmlogger.debug("ActualNbThread after execution: " + p.getActualNbThread());
			p.setActualNbThread(p.getActualNbThread() - 1);
			jqmlogger.debug("+++++++++++++++++++++++++++++++++++++++");

			// Restore class loader
			Thread.currentThread().setContextClassLoader(contextClassLoader);

			// Update end date

			Calendar endDate = GregorianCalendar.getInstance(Locale.getDefault());

			em.getTransaction().begin();

			em.createQuery("UPDATE History h SET h.executionDate = :date WHERE h.id = :h").setParameter("h", h.getId())
					.setParameter("date", endDate).executeUpdate();

			em.getTransaction().commit();

			// End end date

			// STATE UPDATED
			em.getTransaction().begin();
			em.createQuery("UPDATE JobInstance j SET j.state = :msg WHERE j.id = :j").setParameter("j", job.getId())
					.setParameter("msg", "ENDED").executeUpdate();
			em.getTransaction().commit();
			jqmlogger.debug("LOADER HISTORY: " + h.getId());

			em.getTransaction().begin();
			Helpers.createMessage("Status updated: ENDED", h, em);
			em.getTransaction().commit();

			// Retrieve files created by the job
			Method m = this.jobBase.getClass().getMethod("getSha1s");
			ArrayList<Object> dss = (ArrayList<Object>) m.invoke(jobBase, null);

			for (Object ds : dss)
			{
				try
				{
					em.getTransaction().begin();
					String filePath = (String) ds.getClass().getMethod("getFilePath", null).invoke(ds, null);
					String fileName = (String) ds.getClass().getMethod("getFileName", null).invoke(ds, null);
					String hashPath = (String) ds.getClass().getMethod("getHashPath", null).invoke(ds, null);
					String fileFamily = (String) ds.getClass().getMethod("getFileFamily", null).invoke(ds, null);

					jqmlogger.debug("Job " + job.getId() + " has created a file: " + fileName + " - " + hashPath + " - " + fileFamily);
					Helpers.createDeliverable(filePath, fileName, hashPath, fileFamily, this.job.getId(), em);
					jqmlogger.debug("Job " + job.getId() + " has finished registering file " + fileName);
				} catch (Exception e)
				{
					jqmlogger
							.error("Could not analyse a deliverbale - it may be of an incorrect Java class. Job has run correctly - it's only missing its produce.",
									e);
				} finally
				{
					em.getTransaction().commit();
				}
			}

			jqmlogger.debug("End of loader. Thread will now end");
			jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");
			jqmlogger.debug("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%");

		} catch (DependencyResolutionException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (MalformedURLException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (ClassNotFoundException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (SecurityException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (NoSuchMethodException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (IllegalArgumentException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (InvocationTargetException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (IOException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (InstantiationException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (IllegalAccessException e)
		{
			crashedStatus();
			jqmlogger.info(e);
		} catch (Exception e)
		{
			jqmlogger.error("An error occured during job execution or preparation: " + e.getMessage(), e);
		} finally
		{
			try
			{
				em.close();
			} catch (Exception e)
			{
			}
		}

	}

	public JobInstance getJob()
	{

		return job;
	}

	public void setJob(JobInstance job)
	{

		this.job = job;
	}

	public Object getJobBase()
	{

		return jobBase;
	}

	public void setJobBase(JobBase jobBase)
	{

		this.jobBase = jobBase;
	}
}
