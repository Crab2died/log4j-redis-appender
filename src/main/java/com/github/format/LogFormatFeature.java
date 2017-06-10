package com.github.format;

import org.apache.log4j.spi.LoggingEvent;

public interface LogFormatFeature {

    Object logFormat(LoggingEvent loggingEvent);

}
