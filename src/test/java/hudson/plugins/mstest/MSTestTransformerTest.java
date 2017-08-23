package hudson.plugins.mstest;

import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import java.io.*;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MSTestTransformer class
 * 
 * @author Antonio Marques
 */
public class MSTestTransformerTest extends TestHelper {

	
    private TaskListener buildListener;
    private Mockery context;
    private Mockery classContext;
    private MSTestReportConverter converter;
    private MSTestTransformer transformer;
    private VirtualChannel virtualChannel;

    
    @Before
    public void setUp() throws Exception {
        createWorkspace();

        context = getMock();
        classContext = getClassMock();        

        buildListener = classContext.mock(TaskListener.class);
        converter = classContext.mock(MSTestReportConverter.class);
        virtualChannel = context.mock(VirtualChannel.class);
    }

    @After
    public void tearDown() throws Exception {
       deleteWorkspace();
    }

    @Test
    public void testReturnWhenNoTRXFileIsFound() throws Exception {
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
                oneOf(buildListener).fatalError(with(any(String.class)));
            }
        });

        transformer = new MSTestTransformer(resolve("build.trx"), converter, buildListener, true);
        Boolean result = transformer.invoke(parentFile, virtualChannel);
        Assert.assertFalse("The archiver did not return false when it could not find any files", result);
    }
}
