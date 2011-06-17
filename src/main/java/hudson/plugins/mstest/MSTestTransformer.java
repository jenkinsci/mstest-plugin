package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;
import hudson.Util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.xml.sax.SAXException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;

/**
 * Class responsible for transforming the MSTest build report into a JUnit file and then
 * record it in the JUnit result archive.
 * 
 * @author Antonio Marques
 */
public class MSTestTransformer implements FilePath.FileCallable<Boolean>, Serializable {

	private static final long serialVersionUID = 1L;

	public static final String JUNIT_REPORTS_PATH = "temporary-junit-reports";
	private BuildListener listener;

    // Build related objects
    private final String testResultsFile;

    private MSTestReportConverter unitReportTransformer;

    public MSTestTransformer(String testResults, MSTestReportConverter unitReportTransformer, BuildListener listener) throws TransformerException {
        this.testResultsFile = testResults;
        this.unitReportTransformer = unitReportTransformer;
        this.listener = listener;
    }

    /** {@inheritDoc} */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
        String[] mstestFiles = findMSTestReports(ws);

        if (mstestFiles.length == 0) {
            listener.fatalError("No MSTest TRX test report files were found. Configuration error?");
           return Boolean.FALSE;
        }


        File junitOutputPath = new File(ws, JUNIT_REPORTS_PATH);
        junitOutputPath.mkdirs();

        for (String mstestFile : mstestFiles)
        {
            listener.getLogger().println(mstestFile);
                FileInputStream fileStream = new FileInputStream(new File(ws, mstestFile));
            try {
                unitReportTransformer.transform(fileStream, junitOutputPath);
            } catch (TransformerException te) {
                throw new IOException2(
                    "Could not transform the MSTest report. Please report this issue to the plugin author", te);
            } catch (SAXException se) {
                throw new IOException2(
                    "Could not transform the MSTest report. Please report this issue to the plugin author", se);
            } catch (ParserConfigurationException pce) {
                throw new IOException2(
                    "Could not initalize the XML parser. Please report this issue to the plugin author", pce);
            } finally {
                if(fileStream != null)
                    fileStream.close();
            }
        }

        return true;
    }

    /**
     * Returns all MSTest report files matching the pattern given in configuration
     *
     * @param workspacePath Workspace Path
     * @return an array of strings containing filenames of MSTest report files
     */
    private String[] findMSTestReports(File workspacePath) {
        if (workspacePath == null)
            return new String[]{};

        FileSet fs = Util.createFileSet(workspacePath, testResultsFile);
        DirectoryScanner ds = fs.getDirectoryScanner();

        String[] mstestFiles = ds.getIncludedFiles();
        if (mstestFiles.length == 0) {
            listener.fatalError("No MSTest test report files were found.");
        }

        return mstestFiles;
    }
}
