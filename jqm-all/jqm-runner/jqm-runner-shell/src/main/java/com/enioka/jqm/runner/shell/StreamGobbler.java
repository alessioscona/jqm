package com.enioka.jqm.runner.shell;

import java.io.*;
import java.util.concurrent.Semaphore;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Inspired by jChronix and https://www.javaworld.com/article/2071275/core-java/when-runtime-exec---won-t.html<br>
 * <br>
 * A thread eating the output (stdout, stderr) from a process so as to avoid buffer being filled and subsequent locks.
 */
class StreamGobbler extends Thread
{
    private static Logger jqmlogger = LoggerFactory.getLogger(StreamGobbler.class);
    private static Logger alljobslogger = (Logger) LoggerFactory.getLogger("alljobslogger");

    InputStream is;
    OutputStream os;
    Semaphore end = new Semaphore(0);
    String threadName;
    boolean useCommonLogFile = false;

    private StreamGobbler(InputStream is, OutputStream os)
    {
        this.is = is;
        this.os = os;
        this.threadName = Thread.currentThread().getName(); // Share the name with parent thread, so as to share stdout.
    }

    static Waiter plumbProcess(Process p)
    {
        Waiter res = new Waiter();
        res.stdout = new StreamGobbler(p.getInputStream(), System.out);
        res.stderr = new StreamGobbler(p.getErrorStream(), System.err);
        res.stdout.start();
        res.stderr.start();
        return res;
    }

    static Waiter plumbProcess(Process p, OutputStream out, OutputStream err, boolean alsoWriteToCommonLog)
    {
        Waiter res = new Waiter();
        res.stdout = new StreamGobbler(p.getInputStream(), out);
        res.stdout.useCommonLogFile = alsoWriteToCommonLog;
        res.stderr = new StreamGobbler(p.getErrorStream(), err);
        res.stderr.useCommonLogFile = alsoWriteToCommonLog;
        res.stdout.start();
        res.stderr.start();
        return res;
    }

    public void run()
    {
        Thread.currentThread().setName(this.threadName);
        try(PrintWriter pw = new PrintWriter(this.os))
        {
            InputStreamReader isr = new InputStreamReader(is);
            BufferedReader br = new BufferedReader(isr);

            String line = null;
            while ((line = br.readLine()) != null)
            {
                pw.println(line);

                if (useCommonLogFile)
                {
                    alljobslogger.info(line);
                }
            }

            // Remember to flush before exiting!
            pw.flush();
            is.close();
        }
        catch (IOException ioe)
        {
            jqmlogger.error("Standard flow reading failure", ioe);
        }
        finally
        {
            end.release();
        }
    }

    public void waitForEnd()
    {
        try
        {
            end.acquire();
        }
        catch (InterruptedException e)
        {
            // Nothing to do.
        }
    }
}

class Waiter
{
    StreamGobbler stdout, stderr;

    public void waitForEnd()
    {
        stderr.waitForEnd();
        stdout.waitForEnd();
    }
}
