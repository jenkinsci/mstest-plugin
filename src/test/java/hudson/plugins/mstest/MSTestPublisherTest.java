package hudson.plugins.mstest;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Build;
import hudson.model.BuildListener;
import hudson.model.FreeStyleBuild;
import hudson.model.FreeStyleProject;
import hudson.slaves.EnvironmentVariablesNodeProperty;
import hudson.tasks.test.TestResultProjectAction;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.jmock.Expectations;
import static org.jmock.Expectations.equal;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.jvnet.hudson.test.JenkinsRule;

/**
 * Unit tests for MSTestPublisher class
 *
 * @author Antonio Marques
 */
public class MSTestPublisherTest extends TestHelper {

    private Mockery classContext;
    private AbstractProject project;

    @Before
    public void setUp() throws Exception {
        classContext = getClassMock();
        project = classContext.mock(AbstractProject.class);
    }

    @Test
    public void testGetProjectActionProjectReusing() {
        classContext.checking(new Expectations() {
            {
                one(project).getAction(with(equal(TestResultProjectAction.class)));
                will(returnValue(new TestResultProjectAction(project)));
            }
        });
        MSTestPublisher publisher = new MSTestPublisher("build.trx");
        Action projectAction = publisher.getProjectAction(project);
        assertNull("The action was not null", projectAction);
    }

    @Test
    public void testGetProjectActionProject() {
        classContext.checking(new Expectations() {
            {
                one(project).getAction(with(equal(TestResultProjectAction.class)));
                will(returnValue(null));
            }
        });
        MSTestPublisher publisher = new MSTestPublisher("build.trx");
        Action projectAction = publisher.getProjectAction(project);
        assertNotNull("The action was null", projectAction);
        assertEquals("The action type is incorrect", TestResultProjectAction.class, projectAction.getClass());
    }
}
