package hudson.plugins.mstest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import hudson.model.AbstractProject;
import hudson.model.Action;
import hudson.tasks.test.TestResultProjectAction;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MSTestPublisher class
 * 
 * @author Antonio Marques
 */
public class MSTestPublisherTest extends TestHelper{

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
                one(project).getAction(with(equal(TestResultProjectAction.class))); will(returnValue(new TestResultProjectAction(project)));
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
                one(project).getAction(with(equal(TestResultProjectAction.class))); will(returnValue(null));
            }
        });
        MSTestPublisher publisher = new MSTestPublisher("build.trx");
        Action projectAction = publisher.getProjectAction(project);
        assertNotNull("The action was null", projectAction);
        assertEquals("The action type is incorrect", TestResultProjectAction.class, projectAction.getClass());
    }
}
