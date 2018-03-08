package hudson.plugins.mstest;

import hudson.*;
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

import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;
import net.sf.json.JSONObject;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;
import org.kohsuke.stapler.StaplerRequest;
/**
 * Class that records MSTest test reports into Hudson.
 *
 * @author Antonio Marques
 */
public class MSTestPublisher extends Recorder implements Serializable, SimpleBuildStep {

    private static final long serialVersionUID = 1L;
    private @Nonnull String testResultsFile = DescriptorImpl.defaultTestResultsFile;
    private boolean failOnError = DescriptorImpl.defaultFailOnError;
    private boolean keepLongStdio = DescriptorImpl.defaultKeepLongStdio;

    private long buildTime;

    @DataBoundConstructor
    public MSTestPublisher() {
    }

    public @Nonnull
    String getTestResultsFile() {
        return testResultsFile;
    }

    @DataBoundSetter
    public final void setTestResultsFile(@Nonnull String testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    public boolean getFailOnError(){
        return failOnError;
    }

    @DataBoundSetter
    public final void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean getKeepLongStdio(){
        return keepLongStdio;
    }

    @DataBoundSetter
    public final void setKeepLongStdio(boolean keepLongStdio) {
        this.keepLongStdio = keepLongStdio;
    }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        TestResultProjectAction action = project.getAction(TestResultProjectAction.class);
        if (action == null) {
            return new TestResultProjectAction((Job)project);
        } else {
            return null;
        }
    }

    @Override
    public Collection<Action> getProjectActions(AbstractProject<?, ?> project) {
        Collection<Action> actions = new ArrayList<>();
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
        final FilePath workspace = build.getWorkspace();

        if (workspace == null) {
            throw new IllegalArgumentException();
        }

        perform(build, workspace, launcher, listener);
        return true;
    }

    @Override
    public void perform(final @Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull Launcher launcher, final @Nonnull TaskListener listener)
            throws InterruptedException, IOException {
        buildTime = build.getTimestamp().getTimeInMillis();

        String[] matchingFiles = resolveTestReports(testResultsFile, build, workspace, listener);
        MSTestReportConverter converter = new MSTestReportConverter(listener);
        MSTestTransformer transformer = new MSTestTransformer(matchingFiles, converter, listener, failOnError);
        boolean result = workspace.act(transformer);

        if (result) {
            // Run the JUnit test archiver
            recordTestResult(MSTestTransformer.JUNIT_REPORTS_PATH + "/TEST-*.xml", build, workspace, listener);
            workspace.child(MSTestTransformer.JUNIT_REPORTS_PATH).deleteRecursive();
        } else {
            throw new AbortException("Unable to transform the MSTest report.");
        }
    }

    static String[] resolveTestReports(String testReportsPattern, @Nonnull Run<?, ?> build, @Nonnull FilePath workspace, @Nonnull TaskListener listener) {
        FileResolver resolver = new FileResolver(listener);
        String resolved = resolver.SafeResolveFilePath(testReportsPattern, build, listener);
        return resolver.FindMatchingMSTestReports(resolved, workspace);
    }

    /**
     * Record the test results into the current build.
     *
     * @param junitFilePattern the ant file pattern mathing the junit test results file
     * @param build the current build
     * @param listener the log listener
     * @throws InterruptedException workspace/jenkins operations may throw
     * @throws IOException workspace/jenkins operations may throw
     */
    private void recordTestResult(String junitFilePattern, Run<?, ?> build, FilePath workspace, TaskListener listener)
            throws InterruptedException, IOException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        MsTestLogger logger = new MsTestLogger(listener);
        TestResult existingTestResults = null;

        if (existingAction != null) {
            existingTestResults = existingAction.getResult();
        }
        TestResult result = getTestResult(junitFilePattern, workspace, existingTestResults);

        if(result == null) {
            return;
        }

        if (existingAction == null) {
            action = new TestResultAction(build, result, listener);
        } else {
            action = existingAction;
            action.setResult(result, listener);
        }

        if (result.getPassCount() == 0 && result.getFailCount() == 0) {
            String message = "None of the test reports contained any result.";
            if (failOnError) {
                throw new AbortException(message);
            } else {
                logger.error(message);
            }
        }

        if (existingAction == null) {
            build.addAction(action);
        }

        if (action.getResult().getFailCount() > 0) {
            build.setResult(Result.UNSTABLE);
        }
    }

    /**
     * Collect the test results from the files
     *
     * @param junitFilePattern the ant file pattern mathing the junit test results file
     * @param workspace the build workspace
     * @param existingTestResults existing test results to add results to
     * @return a junit TestResult
     * @throws IOException workspace/jenkins operations may throw
     * @throws InterruptedException workspace/jenkins operations may throw
     */
    private TestResult getTestResult
        (final String junitFilePattern, FilePath workspace, final TestResult existingTestResults)
            throws IOException, InterruptedException
    {
        return workspace.act(new MasterToSlaveFileCallable<TestResult>() {
            private static final long serialVersionUID = 1L;

            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                FileSet fs = Util.createFileSet(ws, junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] files = ds.getIncludedFiles();

                if (files.length == 0) {
                    if(failOnError) {
                        throw new AbortException(MsTestLogger.format("No test report files were found. (Have you specified a pattern matching any file in your workspace ?)"));
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
    }

    @Override
    public DescriptorImpl getDescriptor() {
        return (DescriptorImpl) super.getDescriptor();
    }

    @Extension
    @Symbol("mstest")
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
    }
}
