package hudson.plugins.mstest;

import hudson.model.TaskListener;

import java.io.Serializable;
import java.util.logging.Level;
import java.util.logging.Logger;

class MsTestLogger implements Serializable {
    private static String prefix = "[MSTEST-PLUGIN]";
    private TaskListener listener;

    static MsTestLogger getLogger() {
        return new MsTestLogger(null);
    }

    MsTestLogger(TaskListener listener){
        this.listener = listener;
    }

    void debug(String format, Object ... args){
        printf(Level.FINE, format, args);
    }

    void info(String format, Object ... args){
        printf(Level.INFO, format, args);
    }

    void warn(String format, Object ... args){
        printf(Level.WARNING, format, args);
    }

    void error(String format, Object ... args){
        printf(Level.SEVERE, format, args);
    }

    private void printf(Level level, String format, Object ... args){
        String messageFormat = String.format("%s %s %s%n", MsTestLogger.prefix, level.getName(), format);
        if (listener != null) {
            listener.getLogger().printf(messageFormat, args);
        } else {
            Logger logger = Logger.getLogger(MSTestReportConverter.class.getName());
            logger.log(level, messageFormat, args);
        }
    }

    static String format(String message){
        return String.format("%s %s", MsTestLogger.prefix, message);
    }
}
