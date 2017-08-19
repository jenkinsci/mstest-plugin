package hudson.plugins.mstest;

import hudson.*;
import hudson.FilePath.FileCallable;
import hudson.model.*;
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
import java.util.ArrayList;
import java.util.Collection;

import javax.annotation.Nonnull;
import javax.xml.transform.TransformerException;

import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;

/**
 * Class that records MSTest test reports into Hudson.
 *
 * @author Antonio Marques
 */
public class MSTestPublisher extends Recorder implements Serializable, SimpleBuildStep {

    private String testResultsFile = DescriptorImpl.defaultTestResultsFile;
    private String resolvedFilePath;
    private long buildTime;
    private boolean failOnError = DescriptorImpl.defaultFailOnError;
    private boolean keepLongStdio = DescriptorImpl.defaultKeepLongStdio;

    @DataBoundConstructor
    public MSTestPublisher(){
        this(DescriptorImpl.defaultTestResultsFile, DescriptorImpl.defaultFailOnError, DescriptorImpl.defaultKeepLongStdio);
    }

    // used by the unit tests
    @Deprecated
    public MSTestPublisher(String testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    @Deprecated
    public MSTestPublisher(String testResultsFile, boolean failOnError, boolean keepLongStdio) {
        this.testResultsFile = testResultsFile;
        this.failOnError = failOnError;
        this.keepLongStdio = keepLongStdio;
    }

    @Nonnull
    public String getTestResultsFile() {
        return testResultsFile;
    }

    @DataBoundSetter
    public final void setTestResultsFile(String testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    public String getResolvedFilePath() {
        return resolvedFilePath;
    }
    
    public boolean getFailOnError(){
        return failOnError; 
    }

    @DataBoundSetter
    public final void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @DataBoundSetter
    public final void setKeepLongStdio(boolean keepLongStdio) {
        this.keepLongStdio = keepLongStdio;
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
    
    @Override
    public Collection<Action> getProjectActions(AbstractProject<?, ?> project) {
        Collection<Action> actions = new ArrayList<Action>();
        Action action = this.getProjectAction(project);
        if (action != null)
            actions.add(action);
        return actions;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {
        perform(build, build.getWorkspace(), launcher, listener);
        return true;
    }

    @Override
    public void perform(final Run<?, ?> build, FilePath workspace, Launcher launcher, final TaskListener listener)
            throws InterruptedException, IOException {
        boolean result = true;
        buildTime = build.getTimestamp().getTimeInMillis();
        try {
            resolveFilePath(build, listener);

            listener.getLogger().println("[MSTEST-PLUGIN] Processing test results in file(s) " + resolvedFilePath);
            MSTestTransformer transformer = new MSTestTransformer(resolvedFilePath, new MSTestReportConverter(), listener, failOnError);
            result = workspace.act(transformer);

            if (result) {
                // Run the JUnit test archiver
                recordTestResult(MSTestTransformer.JUNIT_REPORTS_PATH + "/TEST-*.xml", build, workspace, listener);
                workspace.child(MSTestTransformer.JUNIT_REPORTS_PATH).deleteRecursive();
            }

        } catch (TransformerException te) {
            throw new AbortException("[MSTEST-PLUGIN] Could not read the XSL XML file. Please report this issue to the plugin author.");
        }
    }

    private void resolveFilePath(Run<?, ?> build, TaskListener listener) throws IOException, InterruptedException {
        EnvVars env = build.getEnvironment(listener);
        resolvedFilePath = testResultsFile;
        String expanded = env.expand(resolvedFilePath);
        if (expanded == null ? resolvedFilePath != null : !expanded.equals(resolvedFilePath)) {
            resolvedFilePath = expanded;
        }
    }

    /**
     * Record the test results into the current build.
     *
     * @param junitFilePattern
     * @param build
     * @param listener
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    private boolean recordTestResult(String junitFilePattern, Run<?, ?> build, FilePath workspace, TaskListener listener)
            throws InterruptedException, IOException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        try {
            TestResult existingTestResults = null;
            if (existingAction != null) {
                existingTestResults = existingAction.getResult();
            }
            TestResult result = getTestResult(junitFilePattern, workspace, existingTestResults);

            if(result == null) {
                return true;
            }
            
            if (existingAction == null) {
                action = new TestResultAction((Run)build, result, listener);
            } else {
                action = existingAction;
                action.setResult(result, listener);
            }
            if (result.getPassCount() == 0 && result.getFailCount() == 0) {
                throw new AbortException("[MSTEST-PLUGIN] None of the test reports contained any result.");
            }
        } catch (AbortException e) {
            if (build.getResult() == Result.FAILURE)
            {
                return true;
            }

            listener.getLogger().println(e.getMessage());
            build.setResult(Result.FAILURE);
            return true;
        }

        if (existingAction == null) {
            build.addAction(action);
        }

        if (action.getResult().getFailCount() > 0) {
            build.setResult(Result.UNSTABLE);
        }

        return true;
    }

    /**
     * Collect the test results from the files
     *
     * @param junitFilePattern
     * @param workspace
     * @param existingTestResults existing test results to add results to
     * @return a test result
     * @throws IOException
     * @throws InterruptedException
     */
    private TestResult getTestResult
        (final String junitFilePattern, FilePath workspace, final TestResult existingTestResults)
            throws IOException, InterruptedException
    {
        TestResult result = workspace.act(new FileCallable<TestResult>() {
            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                FileSet fs = Util.createFileSet(ws, junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] files = ds.getIncludedFiles();
                
                if (files.length == 0) {
                    if(failOnError) {
                        throw new AbortException("[MSTEST-PLUGIN] No test report files were found. (Have you specified a pattern matching any file in your workspace ?)");
                    } else {
                        return null;
                    }
                }
                if (existingTestResults == null) {
                    return new TestResult(buildTime, ds, keepLongStdio);
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
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    public static class DescriptorImpl extends BuildStepDescriptor<Publisher> {

        public static final String defaultTestResultsFile = "**/*.trx";
        public static final boolean defaultKeepLongStdio = false;
        public static final boolean defaultFailOnError = true;

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
            MSTestPublisher publisher = new MSTestPublisher();
            publisher.setTestResultsFile(req.getParameter("mstest_reports.pattern"));

            if (req.getParameter("failOnError") == null) {
                publisher.setFailOnError(defaultFailOnError);
            } else if (!req.getParameter("failOnError").equals("on")) {
                publisher.setFailOnError(false);
            }

            if (req.getParameter("keepLongStdio") == null) {
                publisher.setKeepLongStdio(defaultKeepLongStdio);
            } else if (req.getParameter("keepLongStdio").equals("on")) {
                publisher.setKeepLongStdio(true);
            }

            return publisher;
        }
    }
}
