package hudson.plugins.mstest;

import hudson.model.TaskListener;

class MsTestLogger {
    private static String prefix = "[MSTEST-PLUGIN]";
    private TaskListener listener;

    MsTestLogger(TaskListener listener){
        this.listener = listener;
    }

    void debug(String format, Object ... args){
        printf("DEBUG", format, args);
    }

    void info(String format, Object ... args){
        printf("INFO", format, args);
    }

    void warn(String format, Object ... args){
        printf("WARN", format, args);
    }

    void error(String format, Object ... args){
        printf("ERR", format, args);
    }

    private void printf(String level, String format, Object ... args){
        String message_format = String.format("%s %s %s%n", MsTestLogger.prefix, level, format);
        if (listener != null) {
            listener.getLogger().printf(message_format, args);
        }
    }

    static String format(String message){
        return String.format("%s %s", MsTestLogger.prefix, message);
    }
}
