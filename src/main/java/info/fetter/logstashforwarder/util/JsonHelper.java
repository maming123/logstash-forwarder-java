package info.fetter.logstashforwarder.util;


import com.google.gson.Gson;

import java.lang.reflect.Type;

public abstract class JsonHelper {
    private static final Gson GSON = new Gson();
    private static Gson gsonBuilder = GsonBuilderHelper.newObjectMapper().create();
    private static Gson gsonDateBuilder = GsonBuilderHelper.newDateMapper().create();

    public JsonHelper() {
    }

    public static byte[] toBytes(Object object) {
        return StringHelper.getBytes(pureToJson(object));
    }

    public static byte[] toBytes(Object object, Type type) {
        return StringHelper.getBytes(pureToJson(object, type));
    }

    public static void setGson(Gson gson) {
        gsonBuilder = gson;
    }

    public static String toJson(Object object) {
        return object == null?null:gsonDateBuilder.toJson(object);
    }

    public static String pureToJson(Object object) {
        return object == null?null:gsonBuilder.toJson(object);
    }

    public static String toJson(Object object, Type type) {
        return object == null?null:gsonDateBuilder.toJson(object, type);
    }

    public static String pureToJson(Object object, Type type) {
        return object == null?null:gsonBuilder.toJson(object, type);
    }

    public static <T> T pureFromJson(String json, Class<T> clazz) {
        return StringHelper.isEmpty(json)?null:gsonBuilder.fromJson(json.trim(), clazz);
    }

    /*public static <T> T pureFromJson(String json, Type typeOfT) {
        return StringHelper.isEmpty(json)?null:gsonBuilder.fromJson(json.trim(), typeOfT);
    }*/

    public static <T> T fromJson(String json, Class<T> clazz) {
        return StringHelper.isEmpty(json)?null:gsonDateBuilder.fromJson(json.trim(), clazz);
    }

    /*public static <T> T fromJson(String json, Type typeOfT) {
        return StringHelper.isEmpty(json)?null:gsonDateBuilder.fromJson(json.trim(), typeOfT);
    }*/

//    public static <T> T mapFromJson(Object object, Class<T> clazz) {
//        String json = toJson(object);
//        return !StringHelper.isEmpty(json)?fromJson(json.trim(), clazz):null;
//    }

//    public static <T> T mapFromJson(Object object, Type typeOfT) {
//        String json = toJson(object);
//        return !StringHelper.isEmpty(json)?fromJson(json.trim(), typeOfT):null;
//    }

    public static <T> T fromJson(byte[] bytes, Class<T> clazz) {
        return fromJson(StringHelper.getString(bytes), clazz);
    }
}
