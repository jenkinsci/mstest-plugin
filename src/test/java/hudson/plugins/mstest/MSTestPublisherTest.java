package hudson.plugins.mstest;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.AbstractBuild;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.model.BuildListener;
import hudson.tasks.test.TestResultProjectAction;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import org.jmock.Expectations;
import static org.jmock.Expectations.equal;
import static org.jmock.Expectations.returnValue;
import org.jmock.Mockery;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Before;
import org.junit.Test;

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

//    commented out -- since my mocks are not being used except the first one.
//    @Test
//    public void testResolveEnvironmentVariables() throws InterruptedException, IOException, Exception {
//        final AbstractBuild build = classContext.mock(AbstractBuild.class);
//        final BuildListener listener = classContext.mock(BuildListener.class);
//        this.createWorkspace();
//        final FilePath ws = this.workspace;
//        final EnvVars env = new EnvVars();
//        env.put("TRX", "build.trx");
//        classContext.checking(new Expectations() {
//            {
//                one(build).getEnvironment();
//                will(returnValue(env));
//                ignoring(build).getWorkspace();
//                will(returnValue(ws));
//                ignoring(listener).getLogger();
//                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
//            }
//        });
//        MSTestPublisher publisher = new MSTestPublisher("$TRX");
//        publisher.perform(build, null, null);
//        assertEquals(new File(ws.toURI().getPath(), "build.trx"), publisher.getTestResultsTrxFile());
//    }
}
