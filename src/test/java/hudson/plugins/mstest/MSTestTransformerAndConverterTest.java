package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

import java.io.*;
/* Java 1.7+
import java.nio.file.Files;
 */

/**
 * Unit tests for MSTestTransformer class
 * 
 * @author Antonio Marques
 */
public class MSTestTransformerAndConverterTest extends TestHelper{

	
    private TaskListener buildListener;
    private Mockery context;
    private Mockery classContext;
    private MSTestTransformer transformer;
    private VirtualChannel virtualChannel;

    
    @Before
    public void setUp() throws Exception {
        createWorkspace();

        context = getMock();
        classContext = getClassMock();
        buildListener = classContext.mock(TaskListener.class);
        virtualChannel = context.mock(VirtualChannel.class);
    }

    @After
    public void tearDown() throws Exception {
        deleteWorkspace();
    }

    @Test
    public void testInvalidXmlCharacters() throws Exception {
        final PrintStream logger = new PrintStream(new ByteArrayOutputStream());
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(logger));
                ignoring(buildListener);
            }
        });
        final String testPath = "xmlentities-forged.trx";
        File testFile = new File(parentFile, testPath);
        if (testFile.exists())
            testFile.delete();
        InputStream testStream = this.getClass().getResourceAsStream("JENKINS-23531-xmlentities-forged.trx");
        FileCopyUtils.copy(testStream, new FileOutputStream(testFile));
        /* Java 1.7+
        Files.copy(testStream, testFile.toPath());*/
        MSTestReportConverter converter = new MSTestReportConverter(buildListener);
        transformer = new MSTestTransformer(resolve(testPath), converter, buildListener, false);
        transformer.invoke(parentFile, virtualChannel);
        FilePath[] list = workspace.list("*.trx");
        Assert.assertEquals(1, list.length);
    }

    @Test
    public void testInvalidXmlCharacters_VishalMane() throws Exception {
        final PrintStream logger = new PrintStream(new ByteArrayOutputStream());
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(logger));
                ignoring(buildListener);
            }
        });
        final String testPath = "vishalMane.trx";
        File testFile = new File(parentFile, testPath);
        if (testFile.exists())
            testFile.delete();
        InputStream testStream = this.getClass().getResourceAsStream("SYSTEM_AD-JENKINS 2015-07-08 10_53_01.trx");
        FileCopyUtils.copy(testStream, new FileOutputStream(testFile));
        /* Java 1.7+
        Files.copy(testStream, testFile.toPath());  */
        MSTestReportConverter converter = new MSTestReportConverter(buildListener);
        transformer = new MSTestTransformer(resolve(testPath), converter, buildListener, false);
        transformer.invoke(parentFile, virtualChannel);
        FilePath[] list = workspace.list("*.trx");
        Assert.assertEquals(1, list.length);
    }

    @Test
    public void testCharset() throws Exception {
        final PrintStream logger = new PrintStream(new ByteArrayOutputStream());
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(logger));
                ignoring(buildListener);
            }
        });
        final String testPath = "charset.trx";
        File testFile = new File(parentFile, testPath);
        if (testFile.exists())
            testFile.delete();
        InputStream testStream = this.getClass().getResourceAsStream("JENKINS-23531-charset.trx");
        FileCopyUtils.copy(testStream, new FileOutputStream(testFile));
        /* Java 1.7+
        Files.copy(testStream, testFile.toPath());*/
        MSTestReportConverter converter = new MSTestReportConverter(buildListener);
        transformer = new MSTestTransformer(resolve(testPath), converter, buildListener, false);
        transformer.invoke(parentFile, virtualChannel);
        FilePath[] list = workspace.list("*.trx");
        Assert.assertEquals(1, list.length);
    }
}
