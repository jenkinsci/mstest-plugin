package hudson.plugins.mstest;

import java.util.logging.Level;
import org.junit.Test;
import hudson.plugins.mstest.MsTestLogger;

import static org.junit.Assert.assertTrue;

public class MsTestLoggerTest {

    @Test
    public void testConstructor() {
        // arrange
        System.clearProperty(MsTestLogger.HUDSON_PLUGINS_MSTEST_LEVEL);

        // act
        MsTestLogger logger = new MsTestLogger(null);

        // assert
        Level logLevel = logger.getConfiguredLogLevel();
        assertTrue(logLevel == Level.INFO);
    }

    @Test(expected = RuntimeException.class)
    public void testConstructorShouldThrowExceptionWhenUnknownLevel() {
        // arrange
        System.setProperty(MsTestLogger.HUDSON_PLUGINS_MSTEST_LEVEL, "INVALID_LEVEL");

        // act
        MsTestLogger logger = new MsTestLogger(null);
    }

    @Test
    public void testConstructorShouldParseLogLevel() {
        // arrange
        System.setProperty(MsTestLogger.HUDSON_PLUGINS_MSTEST_LEVEL, "WARNING");

        // act
        MsTestLogger logger = new MsTestLogger(null);

        // assert
        Level logLevel = logger.getConfiguredLogLevel();
        assertTrue(logLevel == Level.WARNING);
    }
}