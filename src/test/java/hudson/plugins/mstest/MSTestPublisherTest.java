package hudson.plugins.mstest;

import hudson.EnvVars;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.Run;
import hudson.model.TaskListener;
import hudson.tasks.test.TestResultProjectAction;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import java.io.*;

/**
 * Unit tests for MSTestPublisher class
 *
 * @author Antonio Marques
 */
public class MSTestPublisherTest extends TestHelper {

    private Mockery classContext;
    private AbstractProject project;
    private TaskListener buildListener;
    private Run<?, ?> run;

    @Before
    public void setUp() throws Exception {
        classContext = getClassMock();
        project = classContext.mock(AbstractProject.class);
        buildListener = classContext.mock(TaskListener.class);
        run = classContext.mock(Run.class);
    }

    private MSTestPublisher getTestedPublisher()
    {
        return new MSTestPublisher("build.trx", false, false);
    }

    @Test
    public void testGetProjectActionProjectReusing() {
        classContext.checking(new Expectations() {
            {
                oneOf(project).getAction(with(equal(TestResultProjectAction.class)));
                will(returnValue(new TestResultProjectAction(project)));
            }
        });
        MSTestPublisher publisher = getTestedPublisher();
        Action projectAction = publisher.getProjectAction(project);
        Assert.assertNull("The action was not null", projectAction);
    }

    @Test
    public void testGetProjectActionProject() {
        classContext.checking(new Expectations() {
            {
                oneOf(project).getAction(with(equal(TestResultProjectAction.class)));
                will(returnValue(null));
            }
        });
        MSTestPublisher publisher = getTestedPublisher();
        Action projectAction = publisher.getProjectAction(project);
        Assert.assertNotNull("The action was null", projectAction);
        Assert.assertEquals("The action type is incorrect", TestResultProjectAction.class, projectAction.getClass());
    }

    @Test
    public void testNoFileMatchingPattern() throws Exception {
        classContext.checking(new Expectations() {
            {
                oneOf(run).getEnvironment(with(equal(buildListener)));
                will(returnValue(new EnvVars()));
            }
        });
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
                oneOf(buildListener).fatalError(with(any(String.class)));
            }
        });
        File subfolder = new File(parentFile, "subfolder");
        subfolder.mkdirs();
        File testFile = new File(subfolder, "xmlentities-forged.trx");
        if (testFile.exists())
            testFile.delete();
        InputStream testStream = this.getClass().getResourceAsStream("JENKINS-23531-xmlentities-forged.trx");
        FileCopyUtils.copy(testStream, new FileOutputStream(testFile));
        String[] results = MSTestPublisher.resolveTestReports("*.trx", run, workspace, buildListener);
        Assert.assertEquals(0, results.length);
    }
}
