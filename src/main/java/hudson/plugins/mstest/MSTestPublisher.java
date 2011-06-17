package hudson.plugins.mstest;

import hudson.Launcher;
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Result;
import hudson.remoting.VirtualChannel;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.BuildStepMonitor;
import hudson.tasks.Publisher;
import hudson.tasks.Recorder;
import hudson.tasks.junit.TestResult;
import hudson.tasks.junit.TestResultAction;
import hudson.tasks.test.TestResultProjectAction;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.transform.TransformerException;

import net.sf.json.JSONObject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Class that records MSTest test reports into Hudson.
 * 
 * @author Antonio Marques
 */
public class MSTestPublisher extends Recorder implements Serializable {

    private String testResultsFile;


    public MSTestPublisher(String testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    public String getTestResultsTrxFile() {
        return testResultsFile;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        TestResultProjectAction action = project.getAction(TestResultProjectAction.class);
        if (action == null) {
            return new TestResultProjectAction(project);
        } else {
            return null;
        }
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        boolean result = true;
        try {
            listener.getLogger().println("Processing tests results in file(s) " + testResultsFile);
            MSTestTransformer transformer = new MSTestTransformer(testResultsFile, new MSTestReportConverter(), listener);
            result = build.getWorkspace().act(transformer);

            if (result) {
                // Run the JUnit test archiver
                result = recordTestResult(MSTestTransformer.JUNIT_REPORTS_PATH + "/TEST-*.xml", build, listener);                
                build.getWorkspace().child(MSTestTransformer.JUNIT_REPORTS_PATH).deleteRecursive();
            }
            
        } catch (TransformerException te) {
            throw new AbortException("Could not read the XSL XML file. Please report this issue to the plugin author");
        }

        return result;
    }

    /**
     * Record the test results into the current build.
     * @param junitFilePattern
     * @param build
     * @param listener
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean recordTestResult(String junitFilePattern, AbstractBuild<?, ?> build, BuildListener listener)
            throws InterruptedException, IOException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        try {
            final long buildTime = build.getTimestamp().getTimeInMillis();

            TestResult existingTestResults = null;
            if (existingAction != null) {
                existingTestResults = existingAction.getResult();
            }
            TestResult result = getTestResult(junitFilePattern, build, existingTestResults, buildTime);

            if (existingAction == null) {
                action = new TestResultAction(build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }
            if(result.getPassCount()==0 && result.getFailCount()==0)
                new AbortException("None of the test reports contained any result");
        } catch (AbortException e) {
            if(build.getResult()==Result.FAILURE)
                // most likely a build failed before it gets to the test phase.
                // don't report confusing error message.
                return true;

            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        }

        if (existingAction == null) {
            build.getActions().add(action);
        }

        if(action.getResult().getFailCount()>0)
            build.setResult(Result.UNSTABLE);

        return true;
    }

    /**
     * Collect the test results from the files
     * @param junitFilePattern
     * @param build
     * @param existingTestResults existing test results to add results to
     * @param buildTime
     * @return a test result
     * @throws IOException
     * @throws InterruptedException
     */
    private TestResult getTestResult(final String junitFilePattern, AbstractBuild<?, ?> build,
            final TestResult existingTestResults, final long buildTime) throws IOException, InterruptedException {
        TestResult result = build.getWorkspace().act(new FileCallable<TestResult>() {
            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                FileSet fs = Util.createFileSet(ws,junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();

                String[] files = ds.getIncludedFiles();
                if(files.length==0) {
                    // no test result. Most likely a configuration error or fatal problem
                    throw new AbortException("No test report files were found. Configuration error?");
                }
                if (existingTestResults == null) {
                    return new TestResult(buildTime, ds);
                } else {
                    existingTestResults.parse(buildTime, ds);
                    return existingTestResults;
                }
            }
        });
        return result;
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl)super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public DescriptorImpl() {
            super(MSTestPublisher.class);
        }

        @Override
        public String getDisplayName() {
            return Messages.MsTest_Publisher_Name();
        }

        @Override
        public String getHelpFile() {
            return "/plugin/mstest/help.html";
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> jobType) {
            return true;
        }

        @Override
        public Publisher newInstance(StaplerRequest req, JSONObject formData) throws FormException {
            return new MSTestPublisher(req.getParameter("mstest_reports.pattern"));
        }
    }
}
