package io.github.pixeldungeonmultiplayer.shattered.server.utils;

import com.watabou.utils.DeviceCompat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Log {

    public static void w(String tag, String s, Object... params) {
        print("W", tag,  s, params);
    }

    public static void wtf(String tag, String s, Object... params) {
        print("WTF", tag,  s, params);
    }

    public static void e(String tag, String s, Object... params) {
        print("E", tag,  s, params);
    }

    public static void i(String tag, String s, Object... params) {
        print("I", tag, s, params);
    }

    private static void print(String logLevel, String tag, String s, Object... params)
    {
        String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
        String message;
        try {
            message = (params != null && params.length > 0) ? String.format(s, params) : s;
        } catch (Exception e) {
            StringBuilder q = new  StringBuilder();
            q.append(s);
            for (Object o : params) {
                q.append(" ");
                q.append(o);
            }
            message = q.toString();
        }
        String formattedMessage = String.format("[%s] [%s] %s", time, logLevel, message);
        DeviceCompat.log(tag, formattedMessage);
    }
}
