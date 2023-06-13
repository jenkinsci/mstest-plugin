package hudson.plugins.mstest;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.custommonkey.xmlunit.Diff;
import org.custommonkey.xmlunit.Transform;
import org.custommonkey.xmlunit.XMLUnit;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import static org.junit.Assert.assertTrue;

/**
 * Unit tests for MSTestReportConverter class
 *
 * @author Antonio Marques
 */
public class MSTestReportConverterTest {

    @Before
    public void setUp() throws ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
        factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_STYLESHEET, "");

        XMLUnit.setIgnoreWhitespace(true);
        XMLUnit.setNormalizeWhitespace(true);
        XMLUnit.setIgnoreComments(true);
        XMLUnit.setControlDocumentBuilderFactory(factory);
    }

    @Test
    public void testConversionTwoTestsOneClass() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("mstest_2_tests_1_class.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("junit_mstest_2_tests_1_class.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testConversionFourTestsTwoClasses() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("mstest_4_tests_2_classes.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("junit_mstest_4_tests_2_classes.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testConversionTwoTestsFromDifferentAssemblies() throws Exception {

        Transform myTransform = new Transform(new InputSource(
            this.getClass().getResourceAsStream("mstest_2_tests_from_different_assemblies.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(
            readXmlAsString("junit_mstest_2_tests_from_different_assemblies.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testConversionMSTest2010Schema() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("mstest_vs_2010.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("mstest_vs_2010.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testConversionMSTest2012Schema() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("mstest_vs_2012.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("mstest_vs_2012.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    void saveDocumentToFile(Document myDocument, String outputFile) throws TransformerException {
        Transformer transformer = TransformerFactory.newInstance().newTransformer();
        Result output = new StreamResult(new File(outputFile));
        Source input = new DOMSource(myDocument);
        transformer.transform(input, output);
    }

    @Test
    public void testTextMessages() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("JENKINS-17506.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("JENKINS-17506.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testOutputMessages() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("JENKINS-13862.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("JENKINS-13862.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testErrorCount() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("kozl-unit-tests-missing.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("kozl-unit-tests-missing.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testIgnoredTests() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(
                this.getClass().getResourceAsStream("nilleb_HOST18468 2015-03-18 08_03_36.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("nilleb_HOST18468 2015-03-18 08_03_36.xml"),
            myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testStdOut() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("JENKINS-25533+19384.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("JENKINS-25533+19384.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testConversionTestsWithDurationLongerThanOneMinute() throws Exception {

        Transform myTransform = new Transform(new InputSource(
            this.getClass().getResourceAsStream("mstest_more_than_one_minute_test.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("junit_mstest_more_than-one_minute_test.xml"),
            myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testReportingOptions2008() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("ReportingOptions.2008.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("ReportingOptions.2008.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testReportingOptions2013() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("ReportingOptions.2013.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("ReportingOptions.2013.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testDataDriven2008() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("DataDriven.2008.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("DataDriven.2008.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testDataDriven2013() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("DataDriven.2013.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("DataDriven.2013.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void testInconclusive() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("DATAFEED.Tests.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("DATAFEED.Tests.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    @Test
    public void webTest() throws Exception {

        Transform myTransform = new Transform(
            new InputSource(this.getClass().getResourceAsStream("webTestResult.trx")),
            new InputSource(this.getClass()
                .getResourceAsStream(MSTestReportConverter.MSTEST_TO_JUNIT_XSLFILE_STR)));

        Diff myDiff = new Diff(readXmlAsString("webTestResult.xml"), myTransform);
        assertTrue("XSL transformation did not work" + myDiff, myDiff.similar());
    }

    private String readXmlAsString(String resourceName) throws IOException {
        StringBuilder xmlString = new StringBuilder();

        try (BufferedReader reader = new BufferedReader(
            new InputStreamReader(this.getClass().getResourceAsStream(resourceName),
                com.google.common.base.Charsets.UTF_8))) {
            String line = reader.readLine();
            while (line != null) {
                xmlString.append(line).append("\n");
                line = reader.readLine();
            }
        }

        return xmlString.toString();
    }
}
