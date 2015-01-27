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
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

/**
 *
 * @author nilleb
 */
public class XslTransformer {

    private final transient Transformer xslTransformer;

    public XslTransformer()
            throws TransformerConfigurationException, ParserConfigurationException
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        xslTransformer = transformerFactory.newTransformer();  
    }

    private XslTransformer(String xslTransform)
            throws TransformerConfigurationException, ParserConfigurationException
    {
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        xslTransformer = transformerFactory.newTransformer(new StreamSource(this.getClass().getResourceAsStream(xslTransform)));  
    }

    public static XslTransformer FromResource(String resourceName)
            throws TransformerConfigurationException, ParserConfigurationException
    {
        return new XslTransformer(resourceName);
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
    
    public void transform(DOMSource source, File outputFile) 
            throws FileNotFoundException, TransformerException, IOException {
        FileOutputStream fileOutputStream = new FileOutputStream(outputFile);
        try {
            xslTransformer.transform(source, new StreamResult(fileOutputStream));
        } finally {
            fileOutputStream.close();
        }
    }
}
