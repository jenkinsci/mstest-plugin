package hudson.plugins.mstest;

import static org.junit.Assert.assertFalse;
import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;

import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Unit tests for MSTestTransformer class
 * 
 * @author Antonio Marques
 */
public class MSTestTransformerTest extends TestHelper{

	
    protected File parentFile;
    protected FilePath workspace;
    private BuildListener buildListener;
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

        buildListener = classContext.mock(BuildListener.class);
        converter = classContext.mock(MSTestReportConverter.class);
        virtualChannel = context.mock(VirtualChannel.class);
    }

    @After
    public void tearDown() throws Exception {
    	   deleteWorkspace();
    }

    

    @Test
    public void testReturnWhenNoTRXFileisFound() throws Exception {
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(new PrintStream(new ByteArrayOutputStream())));
                one(buildListener).fatalError(with(any(String.class)));
            }
        });
        transformer = new MSTestTransformer("build.trx", converter, buildListener);
        Boolean result = transformer.invoke(parentFile, virtualChannel);
        assertFalse("The archiver did not return false when it could not find any files", result);
    }
}
