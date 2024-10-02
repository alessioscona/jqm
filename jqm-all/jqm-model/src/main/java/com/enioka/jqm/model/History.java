/**
 * Copyright © 2013 enioka. All rights reserved
 * Authors: Marc-Antoine GOUILLART (marc-antoine.gouillart@enioka.com)
 *          Pierre COPPEE (pierre.coppee@enioka.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.enioka.jqm.model;

import java.io.Serializable;
import java.util.Calendar;

import com.enioka.jqm.jdbc.DbConn;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * Persistence class for storing the execution log. All finished {@link JobInstance}s end up in this table (and are purged from
 * {@link JobInstance}).
 */
public class History implements Serializable
{
    private static final long serialVersionUID = -5249529794213078668L;

    private long id;

    /************/
    /* Identity */

    private Long jd;
    private String applicationName;

    private Long queue_id;
    private String queueName;

    // null if cancelled before running
    private Long node;
    private String nodeName;

    private boolean highlander = false;

    /***********/
    /* RESULTS */

    private State status = State.SUBMITTED;

    private Integer return_code;

    private Integer progress;

    /***********/
    /* TIME */

    private Calendar enqueue_Date;

    private Calendar attributionDate;

    private Calendar execution_date;

    private Calendar end_date;

    /***************************/
    /* Instance classification */

    private String session_id;

    private String userName;

    private String email;

    private Long parent_job_id;

    private String instance_application;

    private String instance_module;

    private String instance_keyword1;

    private String instance_keyword2;

    private String instance_keyword3;

    /**************************/
    /* Job Def classification */
    private String keyword1;

    private String keyword2;

    private String keyword3;

    private String application;

    private String module;

    /**
     * This is both the ID (PK) of the {@link History} (i.e. log) object and the ID of the {@link JobInstance} which was the source of this
     * log. Therefore it is NOT generated by the database and must be set.
     */
    public long getId()
    {
        return id;
    }

    /**
     * See {@link #getId()}
     */
    public void setId(final long id)
    {
        this.id = id;
    }

    /**
     * Time at which the execution request was committed inside the database.
     */
    public Calendar getEnqueueDate()
    {
        return enqueue_Date;
    }

    /**
     * See {@link #getEnqueueDate()}
     */
    public void setEnqueueDate(final Calendar enqueueDate)
    {
        this.enqueue_Date = enqueueDate;
    }

    /**
     * Time at which the execution request entered the RUNNING status - a few milliseconds before actual execution.
     */
    public Calendar getExecutionDate()
    {
        return execution_date;
    }

    /**
     * See {@link #getExecutionDate()}
     */
    public void setExecutionDate(final Calendar executionDate)
    {
        this.execution_date = executionDate;
    }

    /**
     * Time at which the payload (i.e. user code) returned.
     */
    public Calendar getEndDate()
    {
        return end_date;
    }

    /**
     * See {@link #getEndDate()}
     */
    public void setEndDate(final Calendar endDate)
    {
        this.end_date = endDate;
    }

    /**
     * The {@link Queue} on which the {@link JobInstance} run took place. the actual queue, not necessarily the one defined inside
     * {@link JobDef} as it can be overloaded in the execution request)
     */
    public Long getQueue()
    {
        return queue_id;
    }

    /**
     * See {@link #getQueue()}
     */
    public void setQueue(final Long queue)
    {
        this.queue_id = queue;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getUserName()
    {
        return userName;
    }

    /**
     * See {@link #getUserName()}
     */
    public void setUserName(final String userName)
    {
        this.userName = userName;
    }

    /**
     * The actual {@link Node} (i.e. JQM engine) that has run the {@link JobInstance}.
     */
    public Long getNode()
    {
        return node;
    }

    /**
     * See {@link #getNode()}
     */
    public void setNode(final Long node)
    {
        this.node = node;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getSessionId()
    {
        return session_id;
    }

    /**
     * See {@link #getSessionId()}
     */
    public void setSessionId(final String sessionId)
    {
        this.session_id = sessionId;
    }

    /**
     * The {@link JobDef} that was run (i.e. the {@link JobInstance} is actually an instance of this {@link JobDef}).
     */
    public Long getJd()
    {
        return jd;
    }

    /**
     * See {@link #getJd()}
     */
    public void setJd(Long jd)
    {
        this.jd = jd;
    }

    /**
     * Comes directly from the execution request. If specified, an e-mail is sent to this address at run end. Stored in {@link History} only
     * for the sake of being able to duplicate a launch.
     */
    public String getEmail()
    {
        return email;
    }

    /**
     * See {@link #getEmail()}
     */
    public void setEmail(String email)
    {
        this.email = email;
    }

    /**
     * Only set when a job request is created by a running job, in which case it contains the job ID.
     */
    public Long getParentJobId()
    {
        return parent_job_id;
    }

    /**
     * See {@link #getParentJobId()}
     */
    public void setParentJobId(Long parentJobId)
    {
        this.parent_job_id = parentJobId;
    }

    /**
     * The end status of the job (CRASHED, ENDED, ...)
     */
    public State getStatus()
    {
        return status;
    }

    /**
     * See {@link #getStatus()}
     */
    public State getState()
    {
        return status;
    }

    /**
     * See {@link #getStatus()}
     */
    public void setStatus(State status)
    {
        this.status = status;
    }

    /**
     * An optional classification tag which can be specified inside the {@link JobDef} (default is NULL).
     */
    public String getKeyword1()
    {
        return keyword1;
    }

    /**
     * See {@link #getKeyword1()}
     */
    public void setKeyword1(String keyword1)
    {
        this.keyword1 = keyword1;
    }

    /**
     * An optional classification tag which can be specified inside the {@link JobDef} (default is NULL).
     */
    public String getKeyword2()
    {
        return keyword2;
    }

    /**
     * See {@link #getKeyword2()}
     */
    public void setKeyword2(String keyword2)
    {
        this.keyword2 = keyword2;
    }

    /**
     * An optional classification tag which can be specified inside the {@link JobDef} (default is NULL).
     */
    public String getKeyword3()
    {
        return keyword3;
    }

    /**
     * See {@link #getKeyword3()}
     */
    public void setKeyword3(String keyword3)
    {
        this.keyword3 = keyword3;
    }

    /**
     * An optional classification tag which can be specified inside the {@link JobDef} (default is NULL).
     */
    public String getApplication()
    {
        return application;
    }

    /**
     * See {@link #getApplication()}
     */
    public void setApplication(String application)
    {
        this.application = application;
    }

    /**
     * An optional classification tag which can be specified inside the {@link JobDef} (default is NULL).
     */
    public String getModule()
    {
        return module;
    }

    /**
     * See {@link #getModule()}
     */
    public void setModule(String module)
    {
        this.module = module;
    }

    /**
     * User code may signal its progress through this integer. Purely optional.
     */
    public Integer getProgress()
    {
        return progress;
    }

    /**
     * See {@link #getProgress()}
     */
    public void setProgress(Integer progress)
    {
        this.progress = progress;
    }

    /**
     * True if the {@link JobInstance} was run in Highlander mode (i.e. never more than one concurrent execution of the same {@link JobDef}
     * inside the whole cluster)
     */
    public boolean isHighlander()
    {
        return highlander;
    }

    /**
     * See {@link #isHighlander()}
     */
    public void setHighlander(boolean highlander)
    {
        this.highlander = highlander;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getInstanceApplication()
    {
        return instance_application;
    }

    /**
     * See {@link #getInstanceApplication()}
     */
    public void setInstanceApplication(String instanceApplication)
    {
        this.instance_application = instanceApplication;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getInstanceModule()
    {
        return instance_module;
    }

    /**
     * See {@link #setInstanceModule(String)}
     */
    public void setInstanceModule(String instanceModule)
    {
        this.instance_module = instanceModule;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getInstanceKeyword1()
    {
        return instance_keyword1;
    }

    /**
     * See {@link #setInstanceKeyword1(String)}
     */
    public void setInstanceKeyword1(String instanceKeyword1)
    {
        this.instance_keyword1 = instanceKeyword1;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getInstanceKeyword2()
    {
        return instance_keyword2;
    }

    /**
     * See {@link #setInstanceKeyword2(String)}
     */
    public void setInstanceKeyword2(String instanceKeyword2)
    {
        this.instance_keyword2 = instanceKeyword2;
    }

    /**
     * An optional classification tag which can be specified inside the execution request (default is NULL).
     */
    public String getInstanceKeyword3()
    {
        return instance_keyword3;
    }

    /**
     * See {@link #setInstanceKeyword3(String)}
     */
    public void setInstanceKeyword3(String instanceKeyword3)
    {
        this.instance_keyword3 = instanceKeyword3;
    }

    /**
     * Time at which the job execution request (the {@link JobInstance}) was taken by an engine.
     */
    public Calendar getAttributionDate()
    {
        return attributionDate;
    }

    /**
     * See {@link #getAttributionDate()}
     */
    public void setAttributionDate(Calendar attributionDate)
    {
        this.attributionDate = attributionDate;
    }

    /**
     * The applicative key of the {@link JobDef} that has run. {@link JobDef} are always retrieved through this name.<br>
     * Max length is 100.
     */
    public String getApplicationName()
    {
        return this.applicationName;
    }

    /**
     * See {@link #getApplicationName()}
     */
    public void setApplicationName(String applicationName)
    {
        this.applicationName = applicationName;
    }

    /**
     * Functional key. Queues are specified by name inside all APIs. Must be unique.<br>
     * Max length is 50.
     */
    public String getQueueName()
    {
        return queueName;
    }

    /**
     * See {@link #getQueueName()}
     */
    public void setQueueName(String queueName)
    {
        this.queueName = queueName;
    }

    /**
     * The functional key of the node. It is unique.<br>
     * Max length is 100.
     */
    public String getNodeName()
    {
        return nodeName;
    }

    /**
     * See {@link #getName()}
     */
    public void setNodeName(final String nodeName)
    {
        this.nodeName = nodeName;
    }

    /**
     * @return the return_code
     */
    public Integer getReturnCode()
    {
        return return_code;
    }

    /**
     * @param return_code
     *                        the return_code to set
     */
    public void setReturnCode(Integer return_code)
    {
        this.return_code = return_code;
    }

    /**
     * Create an History object from a {@link JobInstance}.
     *
     */
    public static void create(DbConn cnx, JobInstance ji, State finalState, Calendar endDate)
    {
        JobDef jd = ji.getJD();
        Node n = ji.getNode();
        Queue q = ji.getQ();

        if (endDate == null)
        {
            cnx.runUpdate("history_insert", ji.getId(), jd.getApplication(), jd.getApplicationName(), ji.getAttributionDate(),
                    ji.getEmail(), ji.getCreationDate(), ji.getExecutionDate(), jd.isHighlander(), ji.getApplication(), ji.getKeyword1(),
                    ji.getKeyword2(), ji.getKeyword3(), ji.getModule(), jd.getKeyword1(), jd.getKeyword2(), jd.getKeyword3(),
                    jd.getModule(), n == null ? null : n.getName(), ji.getParentId(), ji.getProgress(), q == null ? null : q.getName(), 0,
                    ji.getSessionID(), finalState.toString(), ji.getUserName(), ji.getJdId(), n == null ? null : n.getId(), ji.getQueue(),
                    ji.isFromSchedule(), ji.getPriority(), ji.getNotBefore());
        }
        else
        {
            cnx.runUpdate("history_insert_with_end_date", ji.getId(), jd.getApplication(), jd.getApplicationName(), ji.getAttributionDate(),
                    ji.getEmail(), endDate, ji.getCreationDate(), ji.getExecutionDate(), jd.isHighlander(), ji.getApplication(),
                    ji.getKeyword1(), ji.getKeyword2(), ji.getKeyword3(), ji.getModule(), jd.getKeyword1(), jd.getKeyword2(),
                    jd.getKeyword3(), jd.getModule(), n.getName(), ji.getParentId(), ji.getProgress(), q.getName(), 0, ji.getSessionID(),
                    finalState.toString(), ji.getUserName(), ji.getJdId(), ji.getNode().getId(), ji.getQueue(), ji.isFromSchedule(),
                    ji.getPriority(), ji.getNotBefore());
        }
    }

    /**
     * Create an History object from a {@link JobInstance}. (if it does not exist, exception).
     *
     */
    public static void create(DbConn cnx, long launchId, State finalState, Calendar endDate)
    {
        JobInstance ji = JobInstance.select_id(cnx, launchId);
        create(cnx, ji, finalState, endDate);

    }
}
