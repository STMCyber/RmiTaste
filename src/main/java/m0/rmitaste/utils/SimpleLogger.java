package m0.rmitaste.utils;

import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 *
 * @author Marcin Ogorzelski (mzero - @_mzer0)
 */
public class SimpleLogger {
    private static Logger log;
    private static FileHandler fh;
    private static LogFormat formatter;

    public static void init(String logFile) throws IOException {
        SimpleLogger.fh = new FileHandler(logFile);
        SimpleLogger.log = Logger.getLogger(Logger.GLOBAL_LOGGER_NAME);
        SimpleLogger.log.setUseParentHandlers(false);
        SimpleLogger.formatter = new LogFormat();
        // Console output
        //ConsoleHandler handler = new ConsoleHandler();
        //handler.setFormatter(formatter);
        //log.addHandler(handler);
        SimpleLogger.log.addHandler(fh);
        SimpleLogger.fh.setFormatter(formatter);
    }

    public static void logmsg(Level level, String message){
        SimpleLogger.log.log(level, message);
    }

    public static void info(String message){
        SimpleLogger.logmsg(Level.INFO, message);
    }

    public static void warn(String message){
        SimpleLogger.logmsg(Level.WARNING, message);
    }

    public static void error(String message){
        SimpleLogger.logmsg(Level.SEVERE, message);
    }

    public static void logmsg(Level level, String message, Object[] params){
        SimpleLogger.log.log(level, message, params);
    }

    public static void info(String message, Object[] params){
        SimpleLogger.logmsg(Level.INFO, message, params);
    }

    public static void warn(String message, Object[] params){
        SimpleLogger.logmsg(Level.WARNING, message, params);
    }

    public static void error(String message, Object[] params){
        SimpleLogger.logmsg(Level.SEVERE, message, params);
    }

    public static Logger getLog() {
        return SimpleLogger.log;
    }

}
