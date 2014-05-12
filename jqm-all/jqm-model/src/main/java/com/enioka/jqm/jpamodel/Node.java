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

package com.enioka.jqm.jpamodel;

import java.util.Calendar;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

/**
 * <strong>Not part of any API - this an internal JQM class and may change without notice.</strong> <br>
 * JPA persistence class for storing the definition of the different nodes that are member of the JMQ cluster.<br>
 * There can be some confusion between terms: an <code>engine</code> is a Java process that represents a {@link Node}. There can only be one
 * engine running the same Node at the same time.<br>
 * <br>
 * A node is the holder of all the parameters needed for the engine to run: a list of {@link Queue}s to poll (through
 * {@link DeploymentParameter}s), the different TCP ports to use, etc.
 */
@Entity
@Table(name = "Node")
public class Node
{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

    @Column(nullable = false, length = 100, name = "nodeName", unique = true)
    private String name;

    @Column(nullable = false, length = 255, name = "dns")
    private String dns = "localhost";

    @Column(nullable = false, name = "port")
    private Integer port;

    @Column(nullable = false, name = "dlRepo", length = 1024)
    private String dlRepo;

    @Column(nullable = false, name = "repo", length = 1024)
    private String repo;

    @Column(nullable = true, name = "exportRepo", length = 1024)
    private String exportRepo = "";

    @Column(nullable = false, name = "stop")
    private boolean stop = false;

    @Column(name = "rootLogLevel", length = 10)
    private String rootLogLevel = "DEBUG";

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "lastSeenAlive", nullable = true)
    private Calendar lastSeenAlive;

    private Integer jmxRegistryPort = 0;

    private Integer jmxServerPort = 0;

    /**
     * A technical ID without any meaning. Generated by the database.
     */
    public Integer getId()
    {
        return id;
    }

    /**
     * See {@link #getId()}
     */
    void setId(final Integer id)
    {
        this.id = id;
    }

    /**
     * The functional key of the node. When starting an engine, it is given this name as its only parameter. It must be unique.<br>
     * Max length is 100.
     */
    public String getName()
    {
        return name;
    }

    /**
     * See {@link #getName()}
     */
    public void setName(final String name)
    {
        this.name = name;
    }

    /**
     * The TCP port used for starting the engine Jetty server (it holds the servlet API as well as the We Service API).
     */
    public Integer getPort()
    {
        return port;
    }

    /**
     * See {@link #getPort()}
     */
    public void setPort(final Integer port)
    {
        this.port = port;
    }

    /**
     * The directory that will store all the {@link Deliverable}s created by job instances.<br>
     * Max length is 1024.
     */
    public String getDlRepo()
    {
        return dlRepo;
    }

    /**
     * See {@link #getDlRepo()}
     */
    public void setDlRepo(final String dlRepo)
    {
        this.dlRepo = dlRepo;
    }

    /**
     * Directory holding the payload repository, i.e. all the jars that can be run by JQM.
     */
    public String getRepo()
    {
        return repo;
    }

    /**
     * See {@link #getRepo()}
     */
    public void setRepo(final String repo)
    {
        this.repo = repo;
    }

    /**
     * That field is polled be the engine: if true, it will stop at once. this is one of the three ways to send a stop order to an engine.
     */
    public boolean isStop()
    {
        return stop;
    }

    /**
     * See {@link #isStop()}
     */
    public void setStop(boolean stop)
    {
        this.stop = stop;
    }

    /**
     * The log level for the jqm.log file. Valid values are TRACE, DEBUG, INFO, WARN, ERROR, FATAL. Default is INFO.
     */
    public String getRootLogLevel()
    {
        return rootLogLevel;
    }

    /**
     * See {@link #getRootLogLevel()
     */
    public void setRootLogLevel(String rootLogLevel)
    {
        this.rootLogLevel = rootLogLevel;
    }

    /**
     * @deprecated was never used
     */
    public String getExportRepo()
    {
        return exportRepo;
    }

    /**
     * @deprecated was never used
     */
    public void setExportRepo(String exportRepo)
    {
        this.exportRepo = exportRepo;
    }

    /**
     * The DNS name on which to create listeners. Default is localhost.
     */
    public String getDns()
    {
        return dns;
    }

    /**
     * See {@link #getDns()}
     */
    public void setDns(String dns)
    {
        this.dns = dns;
    }

    /**
     * Engine will periodically update this field, which can be used for monitoring. It is also used to prevent starting two engine on the
     * same node.
     */
    public Calendar getLastSeenAlive()
    {
        return lastSeenAlive;
    }

    /**
     * See {@link #getLastSeenAlive()}
     */
    public void setLastSeenAlive(Calendar lastSeenAlive)
    {
        this.lastSeenAlive = lastSeenAlive;
    }

    /**
     * The port on which to start the JMX remote registry. No remote JMX item is started if this field or jmxserverport is < 1
     */
    public Integer getJmxRegistryPort()
    {
        return jmxRegistryPort;
    }

    /**
     * See {@link #getJmxRegistryPort()}
     */
    public void setJmxRegistryPort(Integer jmxRegistryPort)
    {
        this.jmxRegistryPort = jmxRegistryPort;
    }

    /**
     * The port on which to start the JMX remote server. No remote JMX item is started if this field or jmxregistryport is < 1
     */
    public Integer getJmxServerPort()
    {
        return jmxServerPort;
    }

    /**
     * See {@link #getJmxServerPort()}
     */
    public void setJmxServerPort(Integer jmxServerPort)
    {
        this.jmxServerPort = jmxServerPort;
    }
}
