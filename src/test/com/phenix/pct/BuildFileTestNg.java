package com.phenix.pct;
import java.io.File;
import java.io.PrintStream;
import java.net.URL;

import org.apache.tools.ant.BuildEvent;
import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.BuildListener;
import org.apache.tools.ant.Project;
import org.apache.tools.ant.ProjectHelper;
import org.testng.Assert;
import org.testng.annotations.AfterTest;

public class BuildFileTestNg {
    private String fName;
    protected Project project;

    private StringBuffer logBuffer;
    private StringBuffer fullLogBuffer;
    private StringBuffer outBuffer;
    private StringBuffer errBuffer;
    private BuildException buildException;

    public BuildFileTestNg() {

    }

    public BuildFileTestNg(String name) {
        fName = name;
    }

    @AfterTest
    public void tearDown() {
        if (project.getTargets().containsKey("tearDown")) {
            project.executeTarget("tearDown");
        }
    }

    /**
     * Executes a target we have set up
     * 
     * @pre configureProject has been called
     * @param targetName target to run
     */
    public void executeTarget(String targetName) {
        PrintStream sysOut = System.out;
        PrintStream sysErr = System.err;
        try {
            sysOut.flush();
            sysErr.flush();
            outBuffer = new StringBuffer();
            PrintStream out = new PrintStream(new AntOutputStream(outBuffer));
            System.setOut(out);
            errBuffer = new StringBuffer();
            PrintStream err = new PrintStream(new AntOutputStream(errBuffer));
            System.setErr(err);
            logBuffer = new StringBuffer();
            fullLogBuffer = new StringBuffer();
            buildException = null;
            project.executeTarget(targetName);
        } finally {
            System.setOut(sysOut);
            System.setErr(sysErr);
        }

    }

    /**
     * run a target, expect for any build exception
     * 
     * @param target target to run
     * @param cause information string to reader of report
     */
    public void expectBuildException(String target, String cause) {
        expectSpecificBuildException(target, cause, null);
    }

    /**
     * Runs a target, wait for a build exception.
     * 
     * @param target target to run
     * @param cause information string to reader of report
     * @param msg the message value of the build exception we are waiting for set to null for any
     *            build exception to be valid
     */
    public void expectSpecificBuildException(String target, String cause, String msg) {
        try {
            executeTarget(target);
        } catch (org.apache.tools.ant.BuildException ex) {
            buildException = ex;
            if ((null != msg) && (!ex.getMessage().equals(msg))) {
                Assert.fail("Should throw BuildException because '" + cause + "' with message '"
                        + msg + "' (actual message '" + ex.getMessage() + "' instead)");
            }
            return;
        }
        Assert.fail("Should throw BuildException because: " + cause);
    }

    /**
     * assert that a property equals "true".
     * 
     * @param property property name
     */
    public void assertPropertySet(String property) {
        assertPropertyEquals(property, "true");
    }

    /**
     * assert that a property is null.
     * 
     * @param property property name
     */
    public void assertPropertyUnset(String property) {
        assertPropertyEquals(property, null);
    }

    /**
     * assert that a property equals a value; comparison is case sensitive.
     * 
     * @param property property name
     * @param value expected value
     */
    public void assertPropertyEquals(String property, String value) {
        String result = project.getProperty(property);
        Assert.assertEquals(value, result, "property " + property);
    }

    /**
     * Get the project which has been configured for a test.
     * 
     * @return the Project instance for this test.
     */
    public Project getProject() {
        return project;
    }

    /**
     * Gets the directory of the project.
     * 
     * @return the base dir of the project
     */
    public File getProjectDir() {
        return project.getBaseDir();
    }

    /**
     * Gets the log the BuildFileTest object.
     * 
     * Only valid if configureProject() has been called.
     * 
     * @pre fullLogBuffer!=null
     * @return The log value
     */
    public String getFullLog() {
        return fullLogBuffer.toString();
    }
    /**
     * Assert that only the given message has been logged with a priority &lt;= INFO when running
     * the given target.
     */
    public void expectLog(String target, String log) {
        executeTarget(target);
        String realLog = getLog();
        Assert.assertEquals(log, realLog);
    }

    public String getOutput() {
        return cleanBuffer(outBuffer);
    }

    public String getError() {
        return cleanBuffer(errBuffer);
    }
    /**
     * Retrieve a resource from the caller classloader to avoid assuming a vm working directory. The
     * resource path must be relative to the package name or absolute from the root path.
     * 
     * @param resource the resource to retrieve its url.
     * @throws junit.framework.AssertionFailedError if the resource is not found.
     */
    public URL getResource(String resource) {
        URL url = getClass().getResource(resource);
        Assert.assertNotNull(url, "Could not find resource :" + resource);
        return url;
    }

    public BuildException getBuildException() {
        return buildException;
    }
    public String getLog() {
        return logBuffer.toString();
    }
    private String cleanBuffer(StringBuffer buffer) {
        StringBuffer cleanedBuffer = new StringBuffer();
        boolean cr = false;
        for (int i = 0; i < buffer.length(); i++) {
            char ch = buffer.charAt(i);
            if (ch == '\r') {
                cr = true;
                continue;
            }

            if (!cr) {
                cleanedBuffer.append(ch);
            } else {
                cleanedBuffer.append(ch);
            }
        }
        return cleanedBuffer.toString();
    }

    /**
     * Sets up to run the named project
     * 
     * @param filename name of project file to run
     */
    public void configureProject(String filename) throws BuildException {
        configureProject(filename, Project.MSG_DEBUG);
    }

    /**
     * Sets up to run the named project
     * 
     * @param filename name of project file to run
     */
    public void configureProject(String filename, int logLevel) throws BuildException {
        logBuffer = new StringBuffer();
        fullLogBuffer = new StringBuffer();
        project = new Project();
        project.init();
        File antFile = new File(System.getProperty("root"), filename);
        project.setUserProperty("ant.file", antFile.getAbsolutePath());
        project.addBuildListener(new AntTestListener(logLevel));
        ProjectHelper.configureProject(project, antFile);
    }

    /**
     * an output stream which saves stuff to our buffer.
     */
    private static class AntOutputStream extends java.io.OutputStream {
        private StringBuffer buffer;

        public AntOutputStream(StringBuffer buffer) {
            this.buffer = buffer;
        }

        public void write(int b) {
            buffer.append((char) b);
        }
    }
    /**
     * Our own personal build listener.
     */
    private class AntTestListener implements BuildListener {
        private int logLevel;

        /**
         * Constructs a test listener which will ignore log events above the given level.
         */
        public AntTestListener(int logLevel) {
            this.logLevel = logLevel;
        }

        /**
         * Fired before any targets are started.
         */
        public void buildStarted(BuildEvent event) {
        }

        /**
         * Fired after the last target has finished. This event will still be thrown if an error
         * occurred during the build.
         * 
         * @see BuildEvent#getException()
         */
        public void buildFinished(BuildEvent event) {
        }

        /**
         * Fired when a target is started.
         * 
         * @see BuildEvent#getTarget()
         */
        public void targetStarted(BuildEvent event) {
            // System.out.println("targetStarted " + event.getTarget().getName());
        }

        /**
         * Fired when a target has finished. This event will still be thrown if an error occurred
         * during the build.
         * 
         * @see BuildEvent#getException()
         */
        public void targetFinished(BuildEvent event) {
            // System.out.println("targetFinished " + event.getTarget().getName());
        }

        /**
         * Fired when a task is started.
         * 
         * @see BuildEvent#getTask()
         */
        public void taskStarted(BuildEvent event) {
            // System.out.println("taskStarted " + event.getTask().getTaskName());
        }

        /**
         * Fired when a task has finished. This event will still be throw if an error occurred
         * during the build.
         * 
         * @see BuildEvent#getException()
         */
        public void taskFinished(BuildEvent event) {
            // System.out.println("taskFinished " + event.getTask().getTaskName());
        }

        /**
         * Fired whenever a message is logged.
         * 
         * @see BuildEvent#getMessage()
         * @see BuildEvent#getPriority()
         */
        public void messageLogged(BuildEvent event) {
            if (event.getPriority() > logLevel) {
                // ignore event
                return;
            }

            if (event.getPriority() == Project.MSG_INFO || event.getPriority() == Project.MSG_WARN
                    || event.getPriority() == Project.MSG_ERR) {
                logBuffer.append(event.getMessage());
            }
            fullLogBuffer.append(event.getMessage());
        }
    }

}
