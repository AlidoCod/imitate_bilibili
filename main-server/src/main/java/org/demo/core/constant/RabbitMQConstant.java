package org.demo.core.constant;

import java.lang.reflect.Field;
import java.util.concurrent.TimeUnit;

public class RabbitMQConstant {

    public static final String DEMO_EXCHANGE = "demo_exchange";

    public static final String BARRAGE_QUEUE = "barrage_queue";

    public static final String BARRAGE_ROUTING_KEY = "barrage";

    public static Long MINUTE = null;

    /*
      反射获取值
     */
    static {
        Class<TimeUnit> clazz = TimeUnit.class;
        try {
            Field secondScale = clazz.getDeclaredField("MINUTE_SCALE");
            secondScale.setAccessible(true);
            MINUTE = secondScale.getLong(null);
            secondScale.setAccessible(false);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
