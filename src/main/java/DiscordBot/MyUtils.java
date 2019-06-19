package DiscordBot;

import java.io.InputStream;

public class MyUtils {
    public static String inputStreamToString(InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
}
