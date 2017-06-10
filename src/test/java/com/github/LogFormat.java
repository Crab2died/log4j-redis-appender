package com.github;

import com.github.format.LogFormatFeature;
import org.apache.log4j.spi.LoggingEvent;

import java.util.HashMap;
import java.util.Map;

public class LogFormat implements LogFormatFeature{

    @Override
    public Object logFormat(LoggingEvent loggingEvent) {
        Map<String, Object> map = new HashMap<>();
        map.put("LEVEL", loggingEvent.getLevel().toString());
        map.put("MESSAGE", loggingEvent.getMessage());
        return map;
    }
}
