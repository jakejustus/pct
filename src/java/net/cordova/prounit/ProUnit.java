package net.cordova.prounit;

import java.io.File;
import java.text.MessageFormat;

import org.apache.tools.ant.BuildException;

import com.phenix.pct.Messages;
import com.phenix.pct.PCTRun;

/**
 * Ant task for ProUnit tests. For more details about ProUnit, see
 * <a href="http://sourceforge.net/projects/prounit/">the SourceForge project's page</a>
 * or <a href="http://www.mycgiserver.com/~flaviocordova/prounit/index.htm">ProUnit's website</a>.
 * 
 * @author <a href="mailto:justus_phenix@users.sourceforge.net">Gilles QUERRET</a>
 * @version $Revision$
 */
public class ProUnit extends PCTRun {
    private File project = null;
    private File result = null;
    private String template = null;
    private boolean verbose = false;

    /**
     * Path to the XML file saved using ProUnit GUI version.
     * 
     * @param project Mandatory
     */
    public void setProject(File project) {
        this.project = project;
    }

    /**
     * Path to the resulting execution file (XML).
     * 
     * @param result Optional
     */
    public void setResult(File result) {
        this.result = result;
    }

    /**
     * Template used to format the result file.
     * 
     * @param template Optional
     */
    public void setTemplate(String template) {
        this.template = template;
    }

    /**
     * Configures execution to be silent or to show execution progress and results
     * 
     * @param verbose Optional, defaults to false
     */
    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public ProUnit() {
        super();
    }

    public void execute() throws BuildException {
        // This parameter is mandatory (to get a return-value)
        StringBuffer sb = new StringBuffer("-runningAnt=true");

        // 
        if ((this.project == null) || !this.project.isFile()) {
            this.cleanup();
            throw new BuildException(MessageFormat.format(
                    Messages.getString("ProUnit.0"), new Object[]{"project"})); //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            sb.append(" -projectFile=").append(this.project.getAbsolutePath());
        }

        if (this.result != null) {
            sb.append(" -resultFile=").append(this.result.getAbsolutePath());
        }

        if (this.template != null) {
            sb.append(" -resultTemplate=").append(this.template);
        }

        if (this.verbose) {
            // Doesn't seem to change anything in batch mode...
            sb.append(" -verbose=true");
        }

        this.setProcedure("batchRunner.p"); //$NON-NLS-1$
        this.setParameter(sb.toString());

        super.execute();

    }

}
