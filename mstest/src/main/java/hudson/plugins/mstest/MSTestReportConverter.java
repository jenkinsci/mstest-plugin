package hudson.plugins.mstest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.xml.sax.SAXException;

/**
 * Transforms a MSTest report into a JUnit report. 
 */
public class MSTestReportConverter implements  Serializable {


    private static final String JUNIT_OUTPUT_FILE_STR = "TEST-mstest.xml";
    public static final String MSTEST_TO_JUNIT_XSLFILE_STR = "mstest-to-junit.xsl";

    private transient boolean xslIsInitialized;
    private transient Transformer mstestTransformer;

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
        
        File junitTargetFile = new File(junitOutputPath, JUNIT_OUTPUT_FILE_STR);
        FileOutputStream fileOutputStream = new FileOutputStream(junitTargetFile);
        try {
            mstestTransformer.transform(new StreamSource(mstestFileStream), new StreamResult(fileOutputStream));
        } finally {
            fileOutputStream.close();
        }

    }

    private void initialize() throws TransformerFactoryConfigurationError, TransformerConfigurationException,
            ParserConfigurationException {
        if (!xslIsInitialized) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            mstestTransformer = transformerFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(MSTEST_TO_JUNIT_XSLFILE_STR)));
                
            xslIsInitialized = true;
        }
    }
}

