package info.fetter.logstashforwarder.util;


import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.List;

public abstract class StringHelper {
    private static final String CHARSET = "UTF-8";

    public StringHelper() {
    }

    public static String appendSlant(String url) {
        if(url == null) {
            return null;
        } else {
            url = url.replace("\\", "/");
            return url.endsWith("/")?url:url + "/";
        }
    }

    public static byte[] getBytes(String s) {
        if(s == null) {
            return new byte[0];
        } else {
            try {
                return s.getBytes("UTF-8");
            } catch (UnsupportedEncodingException var2) {
                return new byte[0];
            }
        }
    }

    public static String getString(byte[] bytes) {
        if(bytes == null) {
            return null;
        } else {
            try {
                return new String(bytes, "UTF-8");
            } catch (UnsupportedEncodingException var2) {
                return "";
            }
        }
    }

    public static String[] mergeArray(String[] a, String[] b) {
        if(a == null) {
            return b;
        } else if(b == null) {
            return a;
        } else {
            String[] newArray = new String[a.length + b.length];
            System.arraycopy(a, 0, newArray, 0, a.length);
            System.arraycopy(b, 0, newArray, a.length, b.length);
            return newArray;
        }
    }

    public static String[] emptyArray() {
        return new String[0];
    }

    public static String emptyString() {
        return "";
    }

    public static boolean isEmpty(String s) {
        return s == null || s.trim().length() == 0;
    }

    public static boolean isEmpty(String[] s) {
        return s == null || s.length == 0;
    }

    /*public static String[] checkEmpty(String[] s) {
        return ListHelper.isEmpty(s)?emptyArray():s;
    }*/

    public static String isEmpty(String value, String defaultValue) {
        return isEmpty(value)?defaultValue:value;
    }

    public static String[] split(String s) {
        return isEmpty(s)?new String[0]:s.split(",|;|:");
    }

    public static List<String> splitToList(String s) {
        return Arrays.asList(split(s));
    }
}

