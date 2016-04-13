package hudson.plugins.mstest;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.Util;
import hudson.FilePath.FileCallable;
import hudson.AbortException;
import hudson.Extension;
import hudson.model.*;
import hudson.plugins.emma.EmmaHealthReportThresholds;
import hudson.plugins.emma.EmmaPublisher;
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
public class MSTestPublisher extends Recorder implements Serializable {

    private String testResultsFile = DescriptorImpl.defaultTestResultsFile;
    private String resolvedFilePath;
    private long buildTime;
    private boolean failOnError = DescriptorImpl.defaultFailOnError;
    private boolean keepLongStdio = DescriptorImpl.defaultKeepLongStdio;
    private boolean enableCodeCoverageAnalysis = DescriptorImpl.defaultEnableCodeCoverageAnalysis;

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

    public boolean getEnableCodeCoverageAnalysis(){
        return enableCodeCoverageAnalysis;
    }

    @DataBoundSetter
    public final void setFailOnError(boolean failOnError) {
        this.failOnError = failOnError;
    }

    @DataBoundSetter
    public final void setKeepLongStdio(boolean keepLongStdio) {
        this.keepLongStdio = keepLongStdio;
    }

    @DataBoundSetter
    public final void setEnableCodeCoverageAnalysis(boolean enableCodeCoverageAnalysis) {
        this.enableCodeCoverageAnalysis = enableCodeCoverageAnalysis;
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
        if (enableCodeCoverageAnalysis) {
            action = new EmmaPublisher().getProjectAction(project);
            if (action != null) {
                actions.add(action);
            }
        }
        return actions;
    }

    public BuildStepMonitor getRequiredMonitorService() {
        return BuildStepMonitor.NONE;
    }

    @Override
    public boolean perform(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener)
            throws InterruptedException, IOException {

        boolean result = true;
        buildTime = build.getTimestamp().getTimeInMillis();
        try {
            resolveFilePath(build, listener);

            listener.getLogger().println("[MSTEST-PLUGIN] Processing test results in file(s) " + resolvedFilePath);
            MSTestTransformer transformer = new MSTestTransformer(resolvedFilePath, new MSTestReportConverter(), listener, failOnError);
            result = build.getWorkspace().act(transformer);

            if (result) {
                // Run the JUnit test archiver
                result = recordTestResult(MSTestTransformer.JUNIT_REPORTS_PATH + "/TEST-*.xml", build, listener);
                build.getWorkspace().child(MSTestTransformer.JUNIT_REPORTS_PATH).deleteRecursive();
                createEmmaReport(build, launcher, listener);
            }

        } catch (TransformerException te) {
            throw new AbortException("[MSTEST-PLUGIN] Could not read the XSL XML file. Please report this issue to the plugin author.");
        }

        return result;
    }

    private void createEmmaReport(AbstractBuild<?, ?> build, Launcher launcher, BuildListener listener) throws IOException, InterruptedException {
        if (build.getWorkspace().list("**/emma/coverage.xml").length > 0)
        {
            EmmaPublisher ep = new EmmaPublisher();
            ep.healthReports = new EmmaHealthReportThresholds();
            ep.healthReports.setMaxBlock(80);
            ep.healthReports.setMinBlock(0);
            ep.healthReports.setMaxClass(100);
            ep.healthReports.setMinClass(0);
            ep.healthReports.setMaxCondition(80);
            ep.healthReports.setMinCondition(0);
            ep.healthReports.setMaxMethod(70);
            ep.healthReports.setMinMethod(0);
            ep.healthReports.setMaxLine(80);
            ep.healthReports.setMinLine(0);
            ep.perform(build, launcher, listener);
        }
    }

    private void resolveFilePath(AbstractBuild<?, ?> build, BuildListener listener) throws IOException, InterruptedException {
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
    private boolean recordTestResult(String junitFilePattern, AbstractBuild<?, ?> build, BuildListener listener)
            throws InterruptedException, IOException {
        TestResultAction existingAction = build.getAction(TestResultAction.class);
        TestResultAction action;

        try {
            TestResult existingTestResults = null;
            if (existingAction != null) {
                existingTestResults = existingAction.getResult();
            }
            TestResult result = getTestResult(junitFilePattern, build, existingTestResults);

            if(result == null) {
                return true;
            }
            
            if (existingAction == null) {
                action = new TestResultAction((Run)build, result, (TaskListener)listener);
            } else {
                action = existingAction;
                action.setResult(result, (TaskListener)listener);
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
     * @param build
     * @param existingTestResults existing test results to add results to
     * @return a test result
     * @throws IOException
     * @throws InterruptedException
     */
    private TestResult getTestResult
        (final String junitFilePattern, AbstractBuild<?, ?> build, final TestResult existingTestResults)
            throws IOException, InterruptedException
    {
        TestResult result = build.getWorkspace().act(new FileCallable<TestResult>() {
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
        public static final boolean defaultEnableCodeCoverageAnalysis = false;

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

            if (req.getParameter("enableCodeCoverageAnalysis") == null) {
                publisher.setEnableCodeCoverageAnalysis(defaultEnableCodeCoverageAnalysis);
            } else if (req.getParameter("enableCodeCoverageAnalysis").equals("on")) {
                publisher.setEnableCodeCoverageAnalysis(true);
            }
            return publisher;
        }
    }
}
