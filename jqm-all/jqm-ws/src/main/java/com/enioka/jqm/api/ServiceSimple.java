package com.enioka.jqm.api;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.SecurityContext;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.enioka.jqm.jpamodel.Deliverable;

/**
 * A minimal API designed to interact well with CLI tools such as schedulers. Some of its methods (file retrieval) are also used by the two
 * other sets of APIs.
 * 
 */
@Path("/simple")
public class ServiceSimple
{
    private static Logger log = LoggerFactory.getLogger(ServiceSimple.class);

    private @Context
    HttpServletResponse res;
    private @Context
    SecurityContext security;

    // ///////////////////////////////////////////////////////////
    // The one and only really simple API
    // ///////////////////////////////////////////////////////////
    @GET
    @Path("status")
    public String getStatus(@QueryParam("id") int id)
    {
        return JqmClientFactory.getClient().getJob(id).getState().toString();
    }

    // ///////////////////////////////////////////////////////////
    // File retrieval
    // ///////////////////////////////////////////////////////////

    @GET
    @Path("stdout")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getLogOut(@QueryParam("id") int id)
    {
        res.setHeader("Content-Disposition", "attachment; filename=" + id + ".stdout.txt");
        return getFile(FilenameUtils.concat("./logs", StringUtils.leftPad("" + id, 10, "0") + ".stdout.log"));
    }

    @GET
    @Path("stderr")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getLogErr(@QueryParam("id") int id)
    {
        res.setHeader("Content-Disposition", "attachment; filename=" + id + ".stderr.txt");
        return getFile(FilenameUtils.concat("./logs", StringUtils.leftPad("" + id, 10, "0") + ".stderr.log"));
    }

    @GET
    @Path("file")
    @Produces(MediaType.APPLICATION_OCTET_STREAM)
    public InputStream getDeliverableStream(@QueryParam("id") String randomId)
    {
        Deliverable d = null;
        EntityManager em = null;
        try
        {
            em = ((HibernateClient) JqmClientFactory.getClient()).getEm();
            d = em.createQuery("SELECT d from Deliverable d WHERE d.randomId = :ii", Deliverable.class).setParameter("ii", randomId)
                    .getSingleResult();
        }
        catch (NoResultException e)
        {
            throw new ErrorDto("Deliverable does not exist", 905, e, Status.BAD_REQUEST);
        }
        catch (Exception e)
        {
            throw new ErrorDto("Could not retrieve Deliverable metadata from database", 906, e, Status.INTERNAL_SERVER_ERROR);
        }
        finally
        {
            if (em != null)
            {
                em.close();
            }
        }

        String ext = FilenameUtils.getExtension(d.getOriginalFileName());
        res.setHeader("Content-Disposition", "attachment; filename=" + d.getFileFamily() + ext);
        return getFile(d.getFilePath());
    }

    public InputStream getFile(String path)
    {
        try
        {
            log.debug("file retrieval service called by user " + getUserName() + " for file " + path);
            return new FileInputStream(path);
        }
        catch (FileNotFoundException e)
        {
            throw new ErrorDto("Could not find the desired file", 901, e, Status.NO_CONTENT);
        }
    }

    private String getUserName()
    {
        if (security != null && security.getUserPrincipal() != null && security.getUserPrincipal().getName() != null)
        {
            return security.getUserPrincipal().getName();
        }
        else
        {
            return "anonymous";
        }
    }

    // ///////////////////////////////////////////////////////////
    // Enqueue - a form service...
    // ///////////////////////////////////////////////////////////

    @POST
    @Path("ji")
    @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
    public String enqueue(@FormParam("applicationname") String applicationName, @FormParam("module") String module,
            @FormParam("mail") String mail, @FormParam("keyword1") String keyword1, @FormParam("keyword2") String keyword2,
            @FormParam("keyword3") String keyword3, @FormParam("parentid") Integer parentId, @FormParam("user") String user,
            @FormParam("sessionid") String sessionId)
    {
        if (user == null && security != null && security.getUserPrincipal() != null)
        {
            user = security.getUserPrincipal().getName();
        }

        JobRequest jd = new JobRequest(applicationName, user);

        jd.setModule(module);
        jd.setEmail(mail);
        jd.setKeyword1(keyword1);
        jd.setKeyword2(keyword2);
        jd.setKeyword3(keyword3);
        jd.setParentID(parentId);
        jd.setSessionID(sessionId);

        /*
         * int j = 0; while (req.getParameter("param_" + j) != null) { j++; jd.addParameter(req.getParameter("param_" + j),
         * req.getParameter("paramvalue_" + j)); }
         */

        Integer i = JqmClientFactory.getClient().enqueue(jd);

        return i.toString();
    }
}
