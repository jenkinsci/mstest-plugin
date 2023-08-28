package hudson.plugins.mstest;

import hudson.model.TaskListener;
import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

class MsTestLogger implements Serializable {

    private static String prefix = "[MSTEST-PLUGIN]";
    private TaskListener listener;
    private Level configuredLevel;
    public static String HUDSON_PLUGINS_MSTEST_LEVEL = "hudson.plugins.mstest.level";
    private static String ERROR_LEVEL = "ERROR";
    private static String WARNING_LEVEL = "WARNING";
    private static String DEBUG_LEVEL = "DEBUG";
    private static String INFO_LEVEL = "INFO";

    MsTestLogger(TaskListener listener) {
        this.listener = listener;
        this.configuredLevel = parseLevel(System.getProperty(HUDSON_PLUGINS_MSTEST_LEVEL));
    }

    static MsTestLogger getLogger() {
        return new MsTestLogger(null);
    }

    static String format(String message) {
        return String.format("%s %s", MsTestLogger.prefix, message);
    }

    void debug(String format, Object... args) {
        printf(Level.FINE, format, args);
    }

    void info(String format, Object... args) {
        printf(Level.INFO, format, args);
    }

    void warn(String format, Object... args) {
        printf(Level.WARNING, format, args);
    }

    void error(String format, Object... args) {
        printf(Level.SEVERE, format, args);
    }

    private void printf(Level level, String format, Object... args) {
        String messageFormat = String
            .format("%s %s %s%n", MsTestLogger.prefix, level.getName(), format);
        if (listener != null) {
            if(shouldLog(level)){
                listener.getLogger().printf(messageFormat, args);
            }
        } else {
            Logger logger = Logger.getLogger(MSTestReportConverter.class.getName());
            logger.setLevel(this.configuredLevel);
            logger.log(level, messageFormat, args);
        }
    }

    private boolean shouldLog(Level level){
        if(this.configuredLevel != null){
            return level.intValue() >= configuredLevel.intValue();
        } else {
            return true;
        }
    }

    public static Level parseLevel(String level) throws RuntimeException {
        if(ERROR_LEVEL.equals(level)){
            return Level.SEVERE;
        } else if (WARNING_LEVEL.equals(level)){
            return Level.WARNING;
        } else if (DEBUG_LEVEL.equals(level)){
            return Level.FINE;
        } else if (INFO_LEVEL.equals(level)){
            return Level.INFO;
        } else {
            throw new RuntimeException(String.format("Unknown [%s] level provided!", level));
        }
    }
}
