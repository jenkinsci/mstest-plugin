/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package hudson.plugins.mstest;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import java.io.IOException;
import org.apache.commons.io.FileUtils;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 *
 * @author ivo
 */
public class MSTestPublisherJenkinsRuleTest {

    @Rule
    public JenkinsRule j = new JenkinsRule();

    @Test
    public void testResolveEnvironmentVariables() throws InterruptedException, IOException, Exception {

        EnvironmentVariablesNodeProperty prop = new EnvironmentVariablesNodeProperty();
        EnvVars envVars = prop.getEnvVars();
        envVars.put("TRX", "build.trx");
        j.jenkins.getGlobalNodeProperties().add(prop);
        FreeStyleProject project = j.createFreeStyleProject();
        project.getPublishersList().add(new MSTestPublisher("$TRX"));
        FreeStyleBuild build = project.scheduleBuild2(0).get();
        FilePath ws = build.getWorkspace();

        String s = FileUtils.readFileToString(build.getLogFile());
        assertFalse(s.contains("Processing tests results in file(s) $TRX"));
        assertTrue(s.contains("/build.trx"));
    }

}
