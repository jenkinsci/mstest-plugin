package hudson.plugins.mstest;

import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.xml.sax.InputSource;

/**
 * Unit tests for MSTestReportConverter class
 * 
 * @author Antonio Marques
 */
public class MSTestReportConverterTest { 
    @Before
    public void setUp() {
        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
    }

    @Test
    public void testConversionTwoTestsOneClass() throws Exception {

        Transform myTransform = new Transform(new InputSource(this.getClass().getResourceAsStream("mstest_2_tests_1_class.trx")),
                new InputSource(this.getClass().getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("junit_mstest_2_tests_1_class.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testConversionFourTestsTwoClasses() throws Exception {

        Transform myTransform = new Transform(new InputSource(this.getClass().getResourceAsStream("mstest_4_tests_2_classes.trx")),
                new InputSource(this.getClass().getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("junit_mstest_4_tests_2_classes.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }


    private String readXmlAsString(String resourceName) throws IOException {
        String xmlString = "";

        BufferedReader reader = new BufferedReader(new InputStreamReader(this.getClass().getResourceAsStream(resourceName)));
        String line = reader.readLine();
        while (line != null) {
            xmlString += line + "\n";
            line = reader.readLine();
        }
        reader.close();

        return xmlString;
    }
}
