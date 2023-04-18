package org.demo;

public class RabbitMQConstant {

    public static final String DEMO_EXCHANGE = "demo_exchange";

    public static final String BARRAGE_QUEUE = "barrage_queue";

    public static final String BARRAGE_ROUTING_KEY = "barrage";

    public static final String ASYNC_ROUTING_KEY = "async";

    public static Long MINUTE = null;

    private static final long NANO_SCALE   = 1L;
    private static final long MICRO_SCALE  = 1000L * NANO_SCALE;
    private static final long MILLI_SCALE  = 1000L * MICRO_SCALE;
    private static final long SECOND_SCALE = 1000L * MILLI_SCALE;
    private static final long MINUTE_SCALE = 60L * SECOND_SCALE;

    /*
      反射获取值
     */
    static {
/*        Class<TimeUnit> clazz = TimeUnit.class;
*//*        try {
            Field secondScale = clazz.getDeclaredField("MINUTE_SCALE");
            secondScale.setAccessible(true);
            MINUTE = secondScale.getLong(null);
            secondScale.setAccessible(false);

        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }*/
        MINUTE = MINUTE_SCALE;
    }
}
