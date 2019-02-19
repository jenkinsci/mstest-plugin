package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.Util;
import java.io.File;
import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

public abstract class TestHelper {

    protected FilePath workspace;
    File parentFile;

    void createWorkspace() throws Exception {
        parentFile = Util.createTempDir();
        workspace = new FilePath(parentFile);
        if (workspace.exists()) {
            workspace.deleteRecursive();
        }
        workspace.mkdirs();
    }

    void deleteWorkspace() throws Exception {
        workspace.deleteRecursive();
    }


    Mockery getClassMock() {
        Mockery classContext;
        classContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
        return classContext;
    }

    Mockery getMock() {
        return new Mockery();
    }

    String[] resolve(String testFile) {
        return new FileResolver(null).FindMatchingMSTestReports(testFile, workspace);
    }
}
