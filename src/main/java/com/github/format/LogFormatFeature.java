package com.github.format;

import org.apache.log4j.spi.LoggingEvent;

/**
 * 自定义日志写入redis接口
 * 实现该接口将返回的对象通过fastjson转换为JSON字符串存入redis
 */
public interface LogFormatFeature {

    Object logFormat(LoggingEvent loggingEvent);

}
