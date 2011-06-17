package hudson.plugins.mstest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.transform.dom.DOMSource;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Transforms a MSTest report into a JUnit report. 
 */
public class MSTestReportConverter implements  Serializable {

    private static final String ILLEGAL_FILE_CHARS_REGEX = "[\\*/:<>\\?\\|\\\\\";]+";
    public static final String JUNIT_FILE_POSTFIX = ".xml";
    public static final String JUNIT_FILE_PREFIX = "TEST-";
    private static final String TEMP_JUNIT_FILE_STR = "temp-junit.xml";
    public static final String MSTEST_TO_JUNIT_XSLFILE_STR = "mstest-to-junit.xsl";

    private transient boolean xslIsInitialized;
    private transient Transformer mstestTransformer;

    private transient Transformer writerTransformer;
    private transient DocumentBuilder xmlDocumentBuilder;

    private transient int fileCount;

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
    public void transform(InputStream mstestFileStream, File junitOutputPath) throws IOException, TransformerException,
            SAXException, ParserConfigurationException {
        
        initialize();
        
        File junitTargetFile = new File(junitOutputPath, TEMP_JUNIT_FILE_STR);
        FileOutputStream fileOutputStream = new FileOutputStream(junitTargetFile);
        try {
            mstestTransformer.transform(new StreamSource(mstestFileStream), new StreamResult(fileOutputStream));
        } finally {
            fileOutputStream.close();
        }

        splitJUnitFile(junitTargetFile, junitOutputPath);
	junitTargetFile.delete();
    }

    private void initialize() throws TransformerFactoryConfigurationError, TransformerConfigurationException,
            ParserConfigurationException {
        if (!xslIsInitialized) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            mstestTransformer = transformerFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(MSTEST_TO_JUNIT_XSLFILE_STR)));
                
            writerTransformer = transformerFactory.newTransformer();

            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            xmlDocumentBuilder = factory.newDocumentBuilder();

            xslIsInitialized = true;
        }
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
            TransformerException {
        Document document = xmlDocumentBuilder.parse(junitFile);

        NodeList elementsByTagName = ((Element) document.getElementsByTagName("testsuites").item(0)).getElementsByTagName("testsuite");
        for (int i = 0; i < elementsByTagName.getLength(); i++) {
            Element element = (Element) elementsByTagName.item(i);
            DOMSource source = new DOMSource(element);
	    
            String filename = JUNIT_FILE_PREFIX + (fileCount++) + JUNIT_FILE_POSTFIX;
	    File junitOutputFile = new File(junitOutputPath, filename);
            FileOutputStream fileOutputStream = new FileOutputStream(junitOutputFile);
            try {
                StreamResult result = new StreamResult(fileOutputStream);
                writerTransformer.transform(source, result);
            } finally {
                fileOutputStream.close();
            }
        }
    }
}

