package hudson.plugins.mstest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.Extension;
import hudson.FilePath;
import hudson.Launcher;
import hudson.Util;
import hudson.model.AbstractBuild;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.model.Job;
import hudson.model.Result;
import hudson.model.Run;
import hudson.model.TaskListener;
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
import java.util.UUID;
import jenkins.MasterToSlaveFileCallable;
import jenkins.tasks.SimpleBuildStep;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.jenkinsci.Symbol;
import org.kohsuke.stapler.DataBoundConstructor;
import org.kohsuke.stapler.DataBoundSetter;

/**
 * Class that records MSTest test reports into Hudson.
 *
 * @author Antonio Marques
 */
public class MSTestPublisher extends Recorder implements Serializable, SimpleBuildStep {

    private static final long serialVersionUID = 1L;
    @NonNull
    private String testResultsFile = DescriptorImpl.defaultTestResultsFile;
    private boolean failOnError = DescriptorImpl.defaultFailOnError;
    private boolean keepLongStdio = DescriptorImpl.defaultKeepLongStdio;
    private String logLevel = DescriptorImpl.defaultLogLevel;
    private long buildTime;

    @DataBoundConstructor
    public MSTestPublisher() {
    }

    static String[] resolveTestReports(String testReportsPattern, @NonNull Run<?, ?> build,
        @NonNull FilePath workspace, @NonNull TaskListener listener) {
        FileResolver resolver = new FileResolver(listener);
        String resolved = resolver.SafeResolveFilePath(testReportsPattern, build, listener);
        return resolver.FindMatchingMSTestReports(resolved, workspace);
    }

    @NonNull
    public String getTestResultsFile() {
        return testResultsFile;
    }

    @DataBoundSetter
    public final void setTestResultsFile(@NonNull String testResultsFile) {
        this.testResultsFile = testResultsFile;
    }

    public boolean getFailOnError() {
        return failOnError;
    }

    @DataBoundSetter
    public final void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    public boolean getKeepLongStdio() {
        return keepLongStdio;
    }

    @DataBoundSetter
    public final void setKeepLongStdio(boolean keepLongStdio) {
        this.keepLongStdio = keepLongStdio;
    }

    public String getLogLevel() { return logLevel; }

    @DataBoundSetter
    public final void setLogLevel(String logLevel) { this.logLevel = logLevel; }

    @Override
    public Action getProjectAction(AbstractProject<?, ?> project) {
        TestResultProjectAction action = project.getAction(TestResultProjectAction.class);
        if (action == null) {
            return new TestResultProjectAction((Job) project);
        } else {
            return null;
        }
    }

    @Override
    @NonNull
    public Collection<Action> getProjectActions(AbstractProject<?, ?> project) {
        Collection<Action> actions = new ArrayList<>();
        Action action = this.getProjectAction(project);
        if (action != null) {
            actions.add(action);
        }
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
    public void perform(final @NonNull Run<?, ?> build, @NonNull FilePath workspace,
        @NonNull Launcher launcher, final @NonNull TaskListener listener)
        throws InterruptedException, IOException {

        System.setProperty(MsTestLogger.HUDSON_PLUGINS_MSTEST_LEVEL, this.logLevel);

        buildTime = build.getTimestamp().getTimeInMillis();

        String junitReportDir = MSTestTransformer.JUNIT_REPORTS_PATH_DIR_PREFIX + "-" + UUID.randomUUID();
        String[] matchingFiles = resolveTestReports(testResultsFile, build, workspace, listener);
        MSTestReportConverter converter = new MSTestReportConverter(listener);
        MSTestTransformer transformer = new MSTestTransformer(matchingFiles, converter, listener,
            failOnError, junitReportDir);
        boolean result = workspace.act(transformer);

        if (result) {
            // Run the JUnit test archiver
            recordTestResult(junitReportDir + "/TEST-*.xml", build, workspace,
                    listener);
            workspace.child(junitReportDir).deleteRecursive();
        } else {
            throw new AbortException("Unable to transform the MSTest report.");
        }
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
    private void recordTestResult(String junitFilePattern, Run<?, ?> build, FilePath workspace,
        TaskListener listener)
        throws InterruptedException, IOException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        MsTestLogger logger = new MsTestLogger(listener);
        TestResult existingTestResults = null;

        if (existingAction != null) {
            existingTestResults = existingAction.getResult();
        }
        TestResult result = getTestResult(junitFilePattern, workspace, existingTestResults);

        if (result == null) {
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
        throws IOException, InterruptedException {
        return workspace.act(new MasterToSlaveFileCallable<TestResult>() {
            private static final long serialVersionUID = 1L;

            public TestResult invoke(File ws, VirtualChannel channel) throws IOException {
                FileSet fs = Util.createFileSet(ws, junitFilePattern);
                DirectoryScanner ds = fs.getDirectoryScanner();
                String[] files = ds.getIncludedFiles();

                if (files.length == 0) {
                    if (failOnError) {
                        throw new AbortException(MsTestLogger.format(
                            "No test report files were found. (Have you specified a pattern matching any file in your workspace ?)"));
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

    protected Object readResolve() {
        if (logLevel == null) {
            logLevel = DescriptorImpl.defaultLogLevel;
        }
        return this;
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
        public static final String defaultLogLevel = "INFO";

        public DescriptorImpl() {
            super(MSTestPublisher.class);
        }

        @Override
        @NonNull
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
