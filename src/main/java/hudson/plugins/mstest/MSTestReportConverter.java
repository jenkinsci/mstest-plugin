package hudson.plugins.mstest;

import hudson.model.BuildListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FilenameUtils;
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
    private static final String MSTESTCOVERAGE_FILE_STR = "vstest.coveragexml";
    private static final String MSTESTCOVERAGE_FILE_EXT = ".coveragexml";

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

        for (File c: getCoverageFiles(f))
            if (c.exists() && containsData(c)) {
                convertToEmma(listener, f, c);
                break;
            } else {
                listener.getLogger().printf("[MSTEST] XML coverage report file not found: %s\n", c.getAbsolutePath());
            }
    }

    private List<File> getCoverageFiles(File trxFile)
    {
        List<File> coverageFiles = new ArrayList<File>();
        coverageFiles.add(new File(trxFile.getParent(), MSTESTCOVERAGE_FILE_STR));
        coverageFiles.add(getCoverageFile(trxFile));
        return coverageFiles;
    }

    private File getCoverageFile(File trxFile)
    {
        String fileNameWithOutExt = FilenameUtils.removeExtension(FilenameUtils.getBaseName(trxFile.getAbsolutePath()));
        return new File(trxFile.getParentFile(), fileNameWithOutExt + MSTESTCOVERAGE_FILE_EXT);
    }

    private void convertToEmma(BuildListener listener, File f, File c) throws TransformerException, IOException, ParserConfigurationException {
        FileInputStream fileStream = null;
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
    }

    private boolean containsData(File c) throws IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(c);
            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            XPathExpression expr = xpath.compile("count(/CoverageDSPriv/*)");
            Double childCount = (Double) expr.evaluate(doc, XPathConstants.NUMBER);
            return childCount > 0;
        } catch (ParserConfigurationException ex) {
        } catch (org.xml.sax.SAXException ex) {
        } catch (XPathExpressionException ex) {
        }
        return false;
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
