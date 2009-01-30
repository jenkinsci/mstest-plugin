package hudson.plugins.mstest;

import hudson.FilePath;
import hudson.Util;
import hudson.model.BuildListener;
import hudson.remoting.VirtualChannel;
import hudson.util.IOException2;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.types.FileSet;
import org.xml.sax.SAXException;

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
        Boolean retValue = Boolean.TRUE;

        File junitOutputPath = new File(ws, JUNIT_REPORTS_PATH);
        junitOutputPath.mkdirs();

        FileInputStream fileStream = null;
        try{
        	fileStream = new FileInputStream(new File(ws, testResultsFile));
        }
        catch(FileNotFoundException e)
        {
        	listener.fatalError("No MSTest TRX test report files were found. Configuration error?");
        	return  Boolean.FALSE;
        }
        
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

        return retValue;
    }

    
}
