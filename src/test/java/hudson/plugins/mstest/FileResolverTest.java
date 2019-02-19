package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.model.TaskListener;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.PrintStream;
import org.jmock.Expectations;
import org.jmock.Mockery;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.util.FileCopyUtils;

public class FileResolverTest extends TestHelper {

    private TaskListener buildListener;
    private Mockery classContext;

    @Before
    public void setUp() throws Exception {
        createWorkspace();

        classContext = getClassMock();
        buildListener = classContext.mock(TaskListener.class);
    }

    @After
    public void tearDown() throws Exception {
        deleteWorkspace();
    }


    @Test
    public void testWorkspaceList() throws Exception {
        final PrintStream logger = new PrintStream(new ByteArrayOutputStream());
        classContext.checking(new Expectations() {
            {
                ignoring(buildListener).getLogger();
                will(returnValue(logger));
                ignoring(buildListener);
            }
        });

        File subfolder = new File(parentFile, "subfolder");
        subfolder.mkdirs();
        File testFile = new File(subfolder, "xmlentities-forged.trx");
        if (testFile.exists()) {
            testFile.delete();
        }
        InputStream testStream = this.getClass()
            .getResourceAsStream("JENKINS-23531-xmlentities-forged.trx");
        FileCopyUtils.copy(testStream, new FileOutputStream(testFile));
        FilePath[] list = workspace.list("*.trx");
        Assert.assertEquals(0, list.length);
    }


}
