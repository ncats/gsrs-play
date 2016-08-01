package ix.core.util;

import play.Logger;

import java.util.function.Supplier;

/**
 * A utility class that logs messages
 * but creates the messages using a lambda
 * expression that doesn't get evaluated
 * unless we actually perform the log operation.
 *
 * Most log libraries just take Strings as log messages
 * but this can be a performance problem if the String is
 * computationally expensive to create.  That String will always
 * be created at runtime before the Logger is called
 * which may decide not to even log the message because the
 * log level isn't low or high enough.
 *
 * By using the lambda expression, we only perform the computationally
 * expensive String creation after we have already determined that the message
 * should be logged.
 *
 * Created by katzelda on 7/29/16.
 */
public final class LogUtil {

    private LogUtil(){
        //can not instantiate
    }
    /**
     * Log the given message returned by the Supplier only if Debug level is enabled.
     *
     * @param logMessage the Supplier that will create the message to log.
     */
    public static void debug( Supplier<String> logMessage){
        if(Logger.isDebugEnabled()){
            Logger.debug(logMessage.get());
        }
    }
    /**
     * Log the given message returned by the Supplier only if Warn level is enabled.
     *
     * @param logMessage the Supplier that will create the message to log.
     */
    public static void warn( Supplier<String> logMessage){
        if(Logger.isWarnEnabled()){
            Logger.warn(logMessage.get());
        }
    }
    /**
     * Log the given message returned by the Supplier only if Info level is enabled.
     *
     * @param logMessage the Supplier that will create the message to log.
     */
    public static void info( Supplier<String> logMessage){
        if(Logger.isInfoEnabled()){
            Logger.info(logMessage.get());
        }
    }
    /**
     * Log the given message returned by the Supplier only if Trace level is enabled.
     *
     * @param logMessage the Supplier that will create the message to log.
     */
    public static void trace( Supplier<String> logMessage){
        if(Logger.isTraceEnabled()){
            Logger.trace(logMessage.get());
        }
    }
    /**
     * Log the given message returned by the Supplier only if Trace level is enabled.
     *
     * @param logMessage the Supplier that will create the message to log.
     *
     * @param t the Throwable to log
     */
    public static void trace( Supplier<String> logMessage, Throwable t){
        if(Logger.isTraceEnabled()){
            Logger.trace(logMessage.get(),t);
        }
    }
    /**
     * Log the given message returned by the Supplier only if Error level is enabled.
     *
     * @param logMessage the Supplier that will create the message to log.
     */
    public static void error( Supplier<String> logMessage){
        if(Logger.isErrorEnabled()){
            Logger.error(logMessage.get());
        }
    }
    /**
     * Log the given message returned by the Supplier only if Error level is enabled.
     *
     * @param logMessage the Supplier that will create the message to log.
     *
     * @param t the Throwable to log
     */
    public static void error( Supplier<String> logMessage, Throwable t){
        if(Logger.isErrorEnabled()){
            Logger.error(logMessage.get(), t);
        }
    }
}
