package hudson.plugins.mstest;

import hudson.EnvVars;
import hudson.FilePath;
import hudson.model.Run;
import hudson.model.TaskListener;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

class FileResolver {
    private final TaskListener listener;

    FileResolver(TaskListener listener){
        this.listener = listener;
    }

    String SafeResolveFilePath(String filePath, Run<?, ?> build, TaskListener listener)
    {
        MsTestLogger logger = new MsTestLogger(listener);
        String resolved = filePath;
        EnvVars env = null;
        try {
            env = build.getEnvironment(listener);
        } catch (IOException ioe) {
            logger.error("caught unexpected IO exception while retrieving environment variables: %s", ioe.getMessage());
        } catch (InterruptedException ie){
            logger.error("caught unexpected interrupted exception while retrieving environment variables: %s", ie.getMessage());
            Thread.currentThread().interrupt();
        }
        if (env != null) {
            resolved = resolveFilePath(filePath, env);
        }
        return resolved;
    }

    private String resolveFilePath(String filePath, EnvVars env) {
        MsTestLogger logger = new MsTestLogger(listener);
        String resolvedFilePath = filePath;
        String expanded = env.expand(resolvedFilePath);
        if (expanded != null) {
            resolvedFilePath = expanded;
        }
        if (!filePath.equals(resolvedFilePath)){
            logger.debug("the path %s has been resolved to %s", filePath, resolvedFilePath);
        }
        logger.info("processing test results in file(s) %s", resolvedFilePath);
        return resolvedFilePath;
    }

    /**
     * Returns all MSTest report files matching the pattern given in
     * configuration
     *
     * @param pattern the pattern the files shall match
     * @param workspace the build's workspace
     * @return an array of strings containing filenames of MSTest report files
     */
     String[] FindMatchingMSTestReports(String pattern, FilePath workspace) {
         MsTestLogger logger = new MsTestLogger(listener);
        if (workspace == null) {
            return new String[]{};
        }
        File f = new File(pattern);
        if (f.isAbsolute() && f.exists()) {
            return new String[]{f.getAbsolutePath()};
        }
        ArrayList<String> fileNames = new ArrayList<String>();
        try {
            for (FilePath x : workspace.list(pattern)) {
                fileNames.add(x.getRemote());
            }
        } catch (IOException ioe) {
            logger.error("while listing workspace files: %s", ioe.getMessage());
        } catch (InterruptedException ie) {
            logger.error("while listing workspace files: %s", ie.getMessage());
            Thread.currentThread().interrupt();
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }
}
