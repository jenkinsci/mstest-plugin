/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.mstest;

import hudson.EnvVars;
import hudson.Launcher;
import hudson.model.AbstractBuild;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;
import org.jvnet.hudson.test.TestBuilder;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * @author nilleb
 */
public class MSTestPublisherJenkinsRuleTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    private MSTestPublisher getTestedPublisher(String pattern) {
        MSTestPublisher publisher = new MSTestPublisher();
        publisher.setTestResultsFile(pattern);
        publisher.setFailOnError(false);
        publisher.setKeepLongStdio(false);
        return publisher;
    }

    @Test
    public void testResolveEnvironmentVariables() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("TRX", "build.trx");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        project.getPublishersList().add(getTestedPublisher("$TRX"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        if (build != null) {
            String s = FileUtils.readFileToString(build.getLogFile());
            assertFalse(s.contains("Processing tests results in file(s) $TRX"));
            assertTrue(s.contains("build.trx"));
        }
    }

    @Test
    public void testResolveMultipleEnvironmentVariables() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("TRX", "build.trx");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        project.getPublishersList().add(getTestedPublisher("$WORKSPACE/$TRX"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();

        if (build != null) {
            String s = FileUtils.readFileToString(build.getLogFile());
            assertFalse(s.contains("Processing tests results in file(s) $TRX"));
            assertTrue(s.contains("/build.trx"));
        }
    }

    @Test
    public void testExecuteOnRealTrx() throws Exception {
        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("TRX", "results-example-mstest.trx");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        project.getPublishersList().add(getTestedPublisher("$WORKSPACE/$TRX"));
        project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                File f = new File(
                    "src/test/resources/hudson/plugins/mstest/results-example-mstest.trx");
                assertTrue(f.exists());
                File dest = new File(build.getWorkspace().getRemote());
                assertTrue(dest.exists());
                FileUtils.copyFileToDirectory(f, dest);
                assertTrue(build.getWorkspace().child("results-example-mstest.trx").exists());
                return true;
            }
        });

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        if (build != null) {
            String s = FileUtils.readFileToString(build.getLogFile());
            assertTrue(s.contains(File.separator + "results-example-mstest.trx"));
        }
    }

    @Test
    public void testExecuteOnRealTrx_usingAntFileSet() throws Exception {
        FreeStyleProject project = j.createFreeStyleProject();
        project.getPublishersList().add(getTestedPublisher("**/*.trx"));
        assertTrue(project.getBuildersList().add(new TestBuilder() {
            public boolean perform(AbstractBuild<?, ?> build, Launcher launcher,
                BuildListener listener) throws InterruptedException, IOException {
                File f = new File(
                    "src/test/resources/hudson/plugins/mstest/results-example-mstest.trx");
                assertTrue(f.exists());
                File dest = new File(build.getWorkspace().getRemote(),
                    "TestResults_51310ef0-5d36-47cc-a1a9-b21d6f3e2072");
                assertTrue(dest.mkdir());
                FileUtils.copyFileToDirectory(f, dest);
                assertTrue(build.getWorkspace().child(
                    "TestResults_51310ef0-5d36-47cc-a1a9-b21d6f3e2072/results-example-mstest.trx")
                    .exists());
                return true;
            }
        }));

        FreeStyleBuild build = project.scheduleBuild2(0).get();
        if (build != null) {
            String s = FileUtils.readFileToString(build.getLogFile());
            assertTrue(s == null || s.contains(File.separator + "results-example-mstest.trx"));
        }
    }
}
