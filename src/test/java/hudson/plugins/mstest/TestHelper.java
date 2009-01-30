package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.Util;

import java.io.File;

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;

public abstract class TestHelper {

    protected File parentFile;
    protected FilePath workspace;

    public void createWorkspace() throws Exception {
        parentFile = Util.createTempDir();
        workspace = new FilePath(parentFile);
        if (workspace.exists()) {
            workspace.deleteRecursive();
        }
        workspace.mkdirs();
    }

    public void deleteWorkspace() throws Exception {
        workspace.deleteRecursive();
    }
    
    
    protected Mockery getClassMock()
    {
    	Mockery classContext;
        classContext = new Mockery() {
            {
                setImposteriser(ClassImposteriser.INSTANCE);
            }
        };
    	return classContext;
    }
    
    protected Mockery getMock()
    {
    	return new Mockery();
    }
    
}
