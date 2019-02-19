package hudson.plugins.mstest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 * @author nilleb
 */
class XslTransformer {

    private final transient Transformer xslTransformer;

    XslTransformer()
        throws TransformerConfigurationException, ParserConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        xslTransformer = transformerFactory.newTransformer();
    }

    private XslTransformer(String xslTransform)
        throws TransformerConfigurationException, ParserConfigurationException {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        xslTransformer = transformerFactory
            .newTransformer(new StreamSource(this.getClass().getResourceAsStream(xslTransform)));
    }

    static XslTransformer FromResource(String resourceName)
        throws TransformerConfigurationException, ParserConfigurationException {
        return new XslTransformer(resourceName);
    }

    void transform(InputStream inputStream, File outputFile)
        throws TransformerException, IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            xslTransformer
                .transform(new StreamSource(inputStream), new StreamResult(fileOutputStream));
        } finally {
            fileOutputStream.close();
        }
    }

    void transform(DOMSource source, File outputFile)
        throws TransformerException, IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            xslTransformer.transform(source, new StreamResult(fileOutputStream));
        } finally {
            fileOutputStream.close();
        }
    }
}
