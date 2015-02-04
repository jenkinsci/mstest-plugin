package hudson.plugins.mstest;

import hudson.model.BuildListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Transforms a MSTest report into a JUnit report.
 */
public class MSTestReportConverter implements Serializable {

    private static final String JUNIT_FILE_POSTFIX = ".xml";
    private static final String JUNIT_FILE_PREFIX = "TEST-";
    private static final String TEMP_JUNIT_FILE_STR = "temp-junit.xml";
    public static final String MSTEST_TO_JUNIT_XSLFILE_STR = "mstest-to-junit.xsl";
    private static final String MSTESTCOVERAGE_TO_EMMA_XSLFILE_STR = "MSTestCoverageToEmma.xsl";
    private static final String EMMA_FILE_STR = "emma" + File.separator + "coverage.xml";
    private static final String MSTESTCOVERAGE_FILE_STR = "mstest-coverage.xml";

    private transient int fileCount;

    /**
     * Transform the MSTest TRX file into a junit XML file in the output path
     *
     * @param file the mstest file to transform
     * @param junitOutputPath the output path to put all junit files
     * @throws java.io.FileNotFoundException
     * @throws IOException thrown if there was any problem with the transform.
     * @throws TransformerException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    public void transform(String file, File junitOutputPath, BuildListener listener)
            throws FileNotFoundException, IOException, TransformerException,
            SAXException, ParserConfigurationException {
        File f = new File(file);
        FileInputStream fileStream = null;
        try {
            fileStream = new FileInputStream(f);
            transform(fileStream, junitOutputPath);
        } finally {
            if (fileStream != null) {
                fileStream.close();
            }
        }

        File c = new File(f.getParent(), MSTESTCOVERAGE_FILE_STR);
        if (c.exists()) {
            File emmaTargetFile = new File(f.getParent(), EMMA_FILE_STR);
            emmaTargetFile.getParentFile().mkdirs();
            listener.getLogger().printf("mstest xml coverage: transforming '%s' to '%s'\n", c.getAbsolutePath(), emmaTargetFile.getAbsolutePath());
            try {
                fileStream = new FileInputStream(c);
                XslTransformer.FromResource(MSTESTCOVERAGE_TO_EMMA_XSLFILE_STR).transform(fileStream, emmaTargetFile);
            } finally {
                if (fileStream != null) {
                    fileStream.close();
                }
            }
        } else {
            listener.getLogger().printf("mstest xml coverage report file not found: %s\n", c.getAbsolutePath());
        }
    }

    /**
     * Transform the MSTest TRX file into a junit XML file in the output path
     *
     * @param mstestFileStream the mstest file stream to transform
     * @param junitOutputPath the output path to put all junit files
     * @throws IOException thrown if there was any problem with the transform.
     * @throws TransformerException
     * @throws SAXException
     * @throws ParserConfigurationException
     */
    private void transform(InputStream mstestFileStream, File junitOutputPath)
            throws IOException, TransformerException,
            SAXException, ParserConfigurationException {
        File junitTargetFile = new File(junitOutputPath, TEMP_JUNIT_FILE_STR);
        XslTransformer.FromResource(MSTEST_TO_JUNIT_XSLFILE_STR).transform(mstestFileStream, junitTargetFile);
        splitJUnitFile(junitTargetFile, junitOutputPath);
        junitTargetFile.delete();
    }

    private DocumentBuilder getDocumentBuilder()
            throws TransformerFactoryConfigurationError, TransformerConfigurationException,
            ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        return factory.newDocumentBuilder();
    }

    /**
     * Splits the junit file into several junit files in the output path
     *
     * @param junitFile report containing one or more junit test suite tags
     * @param junitOutputPath the path to put all junit files
     * @throws IOException
     * @throws SAXException
     * @throws TransformerException
     */
    private void splitJUnitFile(File junitFile, File junitOutputPath) throws SAXException, IOException,
            TransformerException, TransformerFactoryConfigurationError, TransformerConfigurationException, ParserConfigurationException {
        Document document = getDocumentBuilder().parse(junitFile);

        NodeList elementsByTagName = ((Element) document.getElementsByTagName("testsuites").item(0)).getElementsByTagName("testsuite");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element element = (Element) elementsByTagName.item(i);
            DOMSource source = new DOMSource(element);

            String filename = JUNIT_FILE_PREFIX + (fileCount++) + JUNIT_FILE_POSTFIX;
            File junitOutputFile = new File(junitOutputPath, filename);
            try {
                new XslTransformer().transform(source, junitOutputFile);
            } catch (TransformerConfigurationException ex) {
                Logger.getLogger(MSTestReportConverter.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ParserConfigurationException ex) {
                Logger.getLogger(MSTestReportConverter.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
}
