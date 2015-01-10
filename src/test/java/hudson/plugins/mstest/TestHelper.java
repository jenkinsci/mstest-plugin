package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.Util;
import hudson.model.Build;
import hudson.model.FreeStyleProject;
import hudson.tasks.Builder;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import junit.framework.TestCase;

import org.jmock.Mockery;
import org.jmock.lib.legacy.ClassImposteriser;
import org.jvnet.hudson.test.JenkinsRule;

public abstract class TestHelper extends TestCase {

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
