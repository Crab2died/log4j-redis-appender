package com.github.log4j;

import org.apache.log4j.Category;
import org.apache.log4j.Level;
import org.apache.log4j.Priority;
import org.apache.log4j.spi.LocationInfo;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.Date;
import java.util.Map;

/**
 * LOG信息的扩展类，主要是继承{@link org.apache.log4j.spi.LoggingEvent}
 * 并对其日志信息进行扩展补充
 */
public class LoggingEventX extends LoggingEvent {

    private Date logTime;

    private String logLevel;

    protected LoggingEventX(String fqnOfCategoryClass, Category logger, Priority level, Object message, Throwable
            throwable) {
        super(fqnOfCategoryClass, logger, level, message, throwable);
    }

    protected LoggingEventX(String fqnOfCategoryClass, Category logger, long timeStamp, Priority level, Object message,
                         Throwable throwable) {
        super(fqnOfCategoryClass, logger, timeStamp, level, message, throwable);
    }

    protected LoggingEventX(String fqnOfCategoryClass, Category logger, long timeStamp, Level level, Object message,
                  String threadName, ThrowableInformation throwable, String ndc, LocationInfo info, Map
                          properties) {
        super(fqnOfCategoryClass, logger, timeStamp, level, message, threadName, throwable, ndc, info, properties);
    }

    public Date getLogTime() {
        return logTime;
    }

    public void setLogTime(Date logTime) {
        this.logTime = logTime;
    }

    public String getLogLevel() {
        return logLevel;
    }

    public void setLogLevel(String logLevel) {
        this.logLevel = logLevel;
    }
}
