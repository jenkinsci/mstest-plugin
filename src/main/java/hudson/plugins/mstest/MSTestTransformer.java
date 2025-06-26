package hudson.plugins.mstest;

import edu.umd.cs.findbugs.annotations.NonNull;
import hudson.AbortException;
import hudson.FilePath;
import hudson.model.TaskListener;
import hudson.remoting.Channel;
import hudson.remoting.VirtualChannel;
import java.io.File;
import java.io.IOException;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import jenkins.MasterToSlaveFileCallable;
import org.xml.sax.SAXException;

/**
 * Class responsible for transforming the MSTest build report into a JUnit file and then record it
 * in the JUnit result archive.
 *
 * @author Antonio Marques
 */
public class MSTestTransformer extends MasterToSlaveFileCallable<Boolean> {

    static final String JUNIT_REPORTS_PATH_DIR_PREFIX = "temp-junit-reports";
    private static final long serialVersionUID = 1L;
    private final TaskListener listener;
    private final boolean failOnError;
    private final String junitReportDir;

    private final MSTestReportConverter unitReportTransformer;
    private final String[] msTestFiles;

    MSTestTransformer(String[] msTestFiles, @NonNull MSTestReportConverter unitReportTransformer,
        @NonNull TaskListener listener, boolean failOnError) {
        this(msTestFiles, unitReportTransformer, listener, failOnError, null);
    }

    MSTestTransformer(String[] msTestFiles, @NonNull MSTestReportConverter unitReportTransformer,
        @NonNull TaskListener listener, boolean failOnError, String junitReportDir) {
        this.msTestFiles = msTestFiles;
        this.unitReportTransformer = unitReportTransformer;
        this.listener = listener;
        this.failOnError = failOnError;
        this.junitReportDir = (junitReportDir != null) ? junitReportDir : JUNIT_REPORTS_PATH_DIR_PREFIX; // Keep tests calls simple
    }

    /**
     * Performs the computational task on the node where the data is located.
     *
     * <p>
     * All the exceptions are forwarded to the caller.
     *
     * @param ws {@link File} that represents the local file that {@link FilePath} has represented.
     * @param channel The "back pointer" of the {@link Channel} that represents the communication
     * with the node from where the code was sent.
     */
    public Boolean invoke(File ws, VirtualChannel channel) throws IOException {
        MsTestLogger logger = new MsTestLogger(listener);

        if (msTestFiles.length == 0) {
            if (!failOnError) {
                logger.warn("No MSTest TRX test report files were found. Ignoring.");
                return Boolean.TRUE;
            }
            throw new AbortException(MsTestLogger
                .format("No MSTest TRX test report files were found. Configuration error?"));
        }

        File junitOutputPath = new File(ws, junitReportDir);
        boolean success = FileOperator.safeCreateFolder(junitOutputPath, logger);
        if (!success) {
            return Boolean.FALSE;
        }

        for (String mstestFile : msTestFiles) {
            logger.info("processing report file: " + mstestFile);
            try {
                new ContentCorrector(mstestFile).fix();
                unitReportTransformer.transform(mstestFile, junitOutputPath);
            } catch (TransformerException | SAXException te) {
                throw new IOException(
                    MsTestLogger.format(
                        "Unable to transform the MSTest report. Please report this issue to the plugin author"),
                    te);
            } catch (ParserConfigurationException pce) {
                throw new IOException(
                    MsTestLogger.format(
                        "Unable to initalize the XML parser. Please report this issue to the plugin author"),
                    pce);
            }
        }

        return Boolean.TRUE;
    }
}
