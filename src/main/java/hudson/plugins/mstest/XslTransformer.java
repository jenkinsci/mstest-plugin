package hudson.plugins.mstest;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author nilleb
 */
public class XslTransformer {

    private final transient Transformer xslTransformer;
    private final DocumentBuilder xmlDocumentBuilder;
    private final Transformer writerTransformer;

    public XslTransformer(String xslTransform)
            throws TransformerConfigurationException, ParserConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        xslTransformer = transformerFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(xslTransform)));
        writerTransformer = transformerFactory.newTransformer();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        xmlDocumentBuilder = factory.newDocumentBuilder();
    }

    public void transform(InputStream inputStream, File outputFile) 
            throws FileNotFoundException, TransformerException, IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            xslTransformer.transform(new StreamSource(inputStream), new StreamResult(fileOutputStream));
        } finally {
            fileOutputStream.close();
        }
    }
}
