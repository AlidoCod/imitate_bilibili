package org.demo.util;


import org.demo.pojo.base.GlobalRuntimeException;
import org.demo.pojo.User;
import org.demo.pojo.base.ResponseEnum;
import org.demo.pojo.base.Role;

/**
 * 用于存储线程对象
 * ThreadLocal的原理是，每一个线程都有一个map，threadLocal对象是key，Object是value
 * public T get() {
 *         Thread t = Thread.currentThread();
 *         ThreadLocalMap map = getMap(t);
 *         if (map != null) {
 *             ThreadLocalMap.Entry e = map.getEntry(this);
 *             if (e != null) {
 *                 T result = (T)e.value;
 *                 return result;
 *             }
 *         }
 *         return setInitialValue();
 *     }
 */
public class ThreadHolder {

    private static final ThreadLocal<User> threadLocal = new ThreadLocal<>();

    public static void setUser(User user) {
        threadLocal.set(user);
    }

    public static User getUser() {
        return threadLocal.get();
    }

    public static String getUsername() {
        return getUser().getUsername();
    }

    /**
     * 0对应枚举User，1对应枚举Admin
     */
    public static Role getRole() {
        User user = getUser();
        if (user == null) {
            throw GlobalRuntimeException.of(ResponseEnum.HTTP_STATUS_401);
        }
        return user.getRole();
    }


    /**
     * 清除线程里map对应的Entry
     */
    public static void removeUser() {
        threadLocal.remove();
    }

}
