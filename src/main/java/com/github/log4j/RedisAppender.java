package com.github.log4j;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SimpleDateFormatSerializer;
import com.github.format.LogFormatFeature;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Date;

public class RedisAppender extends AppenderSkeleton {

    private String host = "localhost";
    private int port = 6379;
    private String password;
    private String key = "log4j-log";
    private int dbIndex;

    // 连接超时
    private int timeout = 10000;
    private long minEvictableIdleTimeMillis = 60000L;
    private long timeBetweenEvictionRunsMillis = 30000L;
    private int numTestsPerEvictionRun = -1;
    private int maxTotal = 8;
    private int maxIdle = 0;
    private int minIdle = 0;
    private boolean blockWhenExhaused = false;
    private String evictionPolicyClassName = "";
    private boolean lifo = false;
    private boolean testOnBorrow = false;
    private boolean testWhileIdle = false;
    private boolean testOnReturn = false;

    private String dateFormatter = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'";
    private String logFormatFeature;

    static private boolean jedisHeath = true;
    static private boolean logFormatHeath = true;

    static private JedisPool jedisPool;

    @Override
    public void activateOptions() {
        super.activateOptions();

        // redis key
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        if (lifo) {
            poolConfig.setLifo(lifo);
        }
        if (testOnBorrow) {
            poolConfig.setTestOnBorrow(testOnBorrow);
        }
        if (isTestWhileIdle()) {
            poolConfig.setTestWhileIdle(isTestWhileIdle());
        }
        if (testOnReturn) {
            poolConfig.setTestOnReturn(testOnReturn);
        }
        if (timeBetweenEvictionRunsMillis > 0) {
            poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        }
        if (evictionPolicyClassName != null && evictionPolicyClassName.length() > 0) {
            poolConfig.setEvictionPolicyClassName(evictionPolicyClassName);
        }
        if (blockWhenExhaused) {
            poolConfig.setBlockWhenExhausted(blockWhenExhaused);
        }
        if (minIdle > 0) {
            poolConfig.setMinIdle(minIdle);
        }
        if (maxIdle > 0) {
            poolConfig.setMaxIdle(maxIdle);
        }
        if (numTestsPerEvictionRun > 0) {
            poolConfig.setNumTestsPerEvictionRun(numTestsPerEvictionRun);
        }
        if (maxTotal != 8) {
            poolConfig.setMaxTotal(maxTotal);
        }
        if (minEvictableIdleTimeMillis > 0) {
            poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        }

        if (password != null && password.length() > 0) {
            jedisPool = new JedisPool(poolConfig, host, port, timeout, password);
        } else {
            jedisPool = new JedisPool(poolConfig, host, port, timeout);
        }

        try {
            Jedis jedis = jedisPool.getResource();
            String info = jedis.select(dbIndex);
        } catch (Exception e) {
            jedisHeath = false;
            LogLog.error("Redis is can not connected", e);
        }

    }

    @Override
    protected void append(LoggingEvent loggingEvent) {
        if (!jedisHeath) return;
        try {
            if (null != layout) {
                pushLog2Redis(layout.format(loggingEvent));
            } else {
                Object logData;
                if (null != logFormatFeature && !"".equals(logFormatFeature) && logFormatHeath) {
                    try {
                        Class<?> clazz = Class.forName(logFormatFeature);
                        LogFormatFeature feature = (LogFormatFeature) clazz.newInstance();
                        logData = feature.logFormat(loggingEvent);
                    } catch (Exception e) {
                        logFormatHeath = false;
                        logData = defaultLogFormat(loggingEvent);
                        LogLog.error("log4j properties [logFormatFeature] can not found the class : " +
                                logFormatFeature, e);
                    }

                } else {
                    logData = defaultLogFormat(loggingEvent);
                }
                // JSON时间格式化
                SerializeConfig mapping = new SerializeConfig();
                mapping.put(Date.class, new SimpleDateFormatSerializer(dateFormatter));
                pushLog2Redis(JSON.toJSONString(logData, mapping));
            }
        } catch (Exception e) {
            LogLog.error("redis append message error : ", e);
        }
    }

    @Override
    public void close() {
        LogLog.debug("do nothing...");
    }

    @Override
    public boolean requiresLayout() {
        return false;
    }

    static private Object defaultLogFormat(LoggingEvent loggingEvent) {

        LoggingEventX eventX = new LoggingEventX(loggingEvent.getFQNOfLoggerClass(), loggingEvent.getLogger()
                , loggingEvent.getTimeStamp(), loggingEvent.getLevel(), loggingEvent.getMessage(),
                loggingEvent.getThreadName(), loggingEvent.getThrowableInformation(), loggingEvent.getNDC(),
                loggingEvent.getLocationInformation(), loggingEvent.getProperties());

        eventX.setLogTime(new Date(loggingEvent.getTimeStamp()));
        eventX.setLogLevel(eventX.getLevel().toString());
        return eventX;
    }

    private void pushLog2Redis(String msg) {
        if (jedisPool != null) {
            Jedis jedis = jedisPool.getResource();
            jedis.select(dbIndex);
            Long listSize = jedis.rpush(key, msg);
            LogLog.error(String.format("[%s] the log list size number is :%s", key, listSize));
            jedis.close();
        }
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public int getDbIndex() {
        return dbIndex;
    }

    public void setDbIndex(int dbIndex) {
        this.dbIndex = dbIndex;
    }

    public int getTimeout() {
        return timeout;
    }

    public void setTimeout(int timeout) {
        this.timeout = timeout;
    }

    public long getMinEvictableIdleTimeMillis() {
        return minEvictableIdleTimeMillis;
    }

    public void setMinEvictableIdleTimeMillis(long minEvictableIdleTimeMillis) {
        this.minEvictableIdleTimeMillis = minEvictableIdleTimeMillis;
    }

    public long getTimeBetweenEvictionRunsMillis() {
        return timeBetweenEvictionRunsMillis;
    }

    public void setTimeBetweenEvictionRunsMillis(long timeBetweenEvictionRunsMillis) {
        this.timeBetweenEvictionRunsMillis = timeBetweenEvictionRunsMillis;
    }

    public int getNumTestsPerEvictionRun() {
        return numTestsPerEvictionRun;
    }

    public void setNumTestsPerEvictionRun(int numTestsPerEvictionRun) {
        this.numTestsPerEvictionRun = numTestsPerEvictionRun;
    }

    public int getMaxTotal() {
        return maxTotal;
    }

    public void setMaxTotal(int maxTotal) {
        this.maxTotal = maxTotal;
    }

    public int getMaxIdle() {
        return maxIdle;
    }

    public void setMaxIdle(int maxIdle) {
        this.maxIdle = maxIdle;
    }

    public int getMinIdle() {
        return minIdle;
    }

    public void setMinIdle(int minIdle) {
        this.minIdle = minIdle;
    }

    public boolean isBlockWhenExhaused() {
        return blockWhenExhaused;
    }

    public void setBlockWhenExhaused(boolean blockWhenExhaused) {
        this.blockWhenExhaused = blockWhenExhaused;
    }

    public String getEvictionPolicyClassName() {
        return evictionPolicyClassName;
    }

    public void setEvictionPolicyClassName(String evictionPolicyClassName) {
        this.evictionPolicyClassName = evictionPolicyClassName;
    }

    public boolean isLifo() {
        return lifo;
    }

    public void setLifo(boolean lifo) {
        this.lifo = lifo;
    }

    public boolean isTestOnBorrow() {
        return testOnBorrow;
    }

    public void setTestOnBorrow(boolean testOnBorrow) {
        this.testOnBorrow = testOnBorrow;
    }

    public boolean isTestWhileIdle() {
        return testWhileIdle;
    }

    public void setTestWhileIdle(boolean testWhileIdle) {
        this.testWhileIdle = testWhileIdle;
    }

    public boolean isTestOnReturn() {
        return testOnReturn;
    }

    public void setTestOnReturn(boolean testOnReturn) {
        this.testOnReturn = testOnReturn;
    }

    public String getDateFormatter() {
        return dateFormatter;
    }

    public void setDateFormatter(String dateFormatter) {
        this.dateFormatter = dateFormatter;
    }

    public String getLogFormatFeature() {
        return logFormatFeature;
    }

    public void setLogFormatFeature(String logFormatFeature) {
        this.logFormatFeature = logFormatFeature;
    }
}
