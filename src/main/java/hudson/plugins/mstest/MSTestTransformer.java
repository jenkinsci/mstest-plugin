package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;

/**
 * Class responsible for transforming the MSTest build report into a JUnit file
 * and then record it in the JUnit result archive.
 *
 * @author Antonio Marques
 */
public class MSTestTransformer implements FilePath.FileCallable<Boolean>, Serializable {

    private static final long serialVersionUID = 1L;

    public static final String JUNIT_REPORTS_PATH = "temporary-junit-reports";
    private final BuildListener listener;
    private final boolean failOnError;

    // Build related objects
    private final String testResultsFile;

    private final MSTestReportConverter unitReportTransformer;

    
    public MSTestTransformer(String testResults, MSTestReportConverter unitReportTransformer, BuildListener listener) throws TransformerException {
        this(testResults, unitReportTransformer, listener, true);
    }
    
    public MSTestTransformer(String testResults, MSTestReportConverter unitReportTransformer, BuildListener listener, boolean failOnError) throws TransformerException {
        this.testResultsFile = testResults;
        this.unitReportTransformer = unitReportTransformer;
        this.listener = listener;
        this.failOnError = failOnError;
    }

    /**
     * {@inheritDoc}
     * @param ws
     * @param channel
     * @return 
     * @throws java.io.IOException
     */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
        String[] mstestFiles = findMSTestReports(ws);

        if (mstestFiles.length == 0) {
            if(!failOnError){
                listener.getLogger().println("MSTest: No MSTest TRX test report files were found. Ignoring.");
                return Boolean.TRUE;
            }
            listener.fatalError("MSTest: No MSTest TRX test report files were found. Configuration error?");
            return Boolean.FALSE;
        }

        File junitOutputPath = new File(ws, JUNIT_REPORTS_PATH);
        junitOutputPath.mkdirs();

        for (String mstestFile : mstestFiles) {
            listener.getLogger().println("MSTest: " + mstestFile);
            try {
                new ContentCorrector(mstestFile).fix();
                unitReportTransformer.transform(mstestFile, junitOutputPath, listener);
            } catch (TransformerException te) {
                throw new IOException(
                        "MSTest: Could not transform the MSTest report. Please report this issue to the plugin author", te);
            } catch (SAXException se) {
                throw new IOException(
                        "MSTest: Could not transform the MSTest report. Please report this issue to the plugin author", se);
            } catch (ParserConfigurationException pce) {
                throw new IOException(
                        "MSTest: Could not initalize the XML parser. Please report this issue to the plugin author", pce);
            }
        }

        return true;
    }

    /**
     * Returns all MSTest report files matching the pattern given in
     * configuration
     *
     * @param workspacePath Workspace Path
     * @return an array of strings containing filenames of MSTest report files
     */
    private String[] findMSTestReports(File workspacePath) {
        if (workspacePath == null) {
            return new String[]{};
        }
        File f = new File(testResultsFile);
        if (f.isAbsolute() && f.exists()) {
            return new String[]{f.getAbsolutePath()};
        }
        FilePath ws = new FilePath(workspacePath);
        ArrayList<String> fileNames = new ArrayList<String>();
        try {
            for (FilePath x : ws.list(testResultsFile)) {
                fileNames.add(x.getRemote());
            }
        } catch (IOException ioe) {
        } catch (InterruptedException inte) {
        }
        return fileNames.toArray(new String[fileNames.size()]);
    }
}
