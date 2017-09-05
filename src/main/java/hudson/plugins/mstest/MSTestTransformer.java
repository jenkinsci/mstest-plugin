package hudson.plugins.mstest;

import hudson.AbortException;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.VirtualChannel;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;

import javax.annotation.Nonnull;
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

    static final String JUNIT_REPORTS_PATH = "temporary-junit-reports";
    private final TaskListener listener;
    private final boolean failOnError;

    private final MSTestReportConverter unitReportTransformer;
    private final String[] msTestFiles;

    MSTestTransformer(String[] msTestFiles, @Nonnull MSTestReportConverter unitReportTransformer, @Nonnull TaskListener listener, boolean failOnError){
        this.msTestFiles = msTestFiles;
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
        MsTestLogger logger = new MsTestLogger(listener);

        if (msTestFiles.length == 0) {
            if(!failOnError){
                logger.warn("No MSTest TRX test report files were found. Ignoring.");
                return Boolean.TRUE;
            }
            throw new AbortException(MsTestLogger.format("No MSTest TRX test report files were found. Configuration error?"));
        }

        File junitOutputPath = new File(ws, JUNIT_REPORTS_PATH);
        boolean success = FileOperator.safeCreateFolder(junitOutputPath, logger);
        if (!success) {
            return Boolean.FALSE;
        }

        for (String mstestFile : msTestFiles) {
            logger.info("processing report file: " + mstestFile);
            try {
                new ContentCorrector(mstestFile).fix();
                unitReportTransformer.transform(mstestFile, junitOutputPath);
            } catch (TransformerException te) {
                throw new IOException(
                        MsTestLogger.format("Unable to transform the MSTest report. Please report this issue to the plugin author"), te);
            } catch (SAXException se) {
                throw new IOException(
                        MsTestLogger.format("Unable to transform the MSTest report. Please report this issue to the plugin author"), se);
            } catch (ParserConfigurationException pce) {
                throw new IOException(
                        MsTestLogger.format("Unable to initalize the XML parser. Please report this issue to the plugin author"), pce);
            }
        }

        return Boolean.TRUE;
    }
}
