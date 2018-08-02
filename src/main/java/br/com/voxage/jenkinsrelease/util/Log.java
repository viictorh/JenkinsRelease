package br.com.voxage.jenkinsrelease.util;

import java.io.Serializable;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

public class Log implements Serializable {
    private static final long serialVersionUID = -8036627521945661572L;
    public static Log         log              = new Log();
    Logger                    logger           = null;

    private Log() {
        logger = Logger.getLogger("br.com.voxage.jenkinsrelease.util");
    }

    public void info() {
        logger.info(getMethodName());
    }

    public void info(Object s) {
        logger.info(getMethodName() + validLogSize(s));
    }

    public void debug() {
        logger.debug(getMethodName());
    }

    public void debug(Object s) {
        logger.debug(getMethodName() + validLogSize(s));
    }

    public void trace() {
        logger.trace(getMethodName());
    }

    public void trace(Object s) {
        logger.trace(getMethodName() + validLogSize(s));
    }

    public void error(Object s) {
        logger.error(getMethodName() + validLogSize(s));
    }

    public void error(Object s, Throwable t) {
        logger.error(getMethodName() + validLogSize(s), t);
    }

    public void error(Throwable t) {
        if (t != null) {
            logger.error(getMethodName() + t.getClass().getSimpleName(), t);
        }
    }

    public void stacktrace() {
        String s = "\nLOG STACK TRACE:\n";
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        for (int i = 2; i < stackTrace.length; i++) {
            StackTraceElement stack = stackTrace[i];
            s += "  line: " + stack + "\n";
        }
        logger.info(getMethodName() + s);
    }

    private String getMethodName() {
        StackTraceElement stack = Thread.currentThread().getStackTrace()[3];
        return stack.getClassName().substring(stack.getClassName().lastIndexOf(".") + 1) + "." + stack.getMethodName() + "() ";
    }

    public void changeLevel(Level level) {
        logger.setLevel(level);
    }

    public Object validLogSize(Object s) {
        if (s != null) {
            String value = s.toString();
            if (value.length() > 100000) {
                s = value.substring(0, 100000) + "...";
            }
        }
        return s;
    }

}
