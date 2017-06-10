package com.github;

import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.concurrent.TimeUnit;

public class AppTest {

    private static Logger logger = Logger.getLogger(AppTest.class);

    static class MyThread implements Runnable {
        @Override
        public void run() {
            logger.debug(Thread.currentThread().getName() + "测试 。。。");
            try {
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    @Test
    public void testRedisAppender() {
        for (int i = 0; i < 100; i++) {
//            try{
//                throw new RuntimeException("err");
//            }catch (Exception e){
//                logger.debug("test ", e);
//            }
            logger.info("---测试。。--");

        }

    }
}
