package info.fetter.logstashforwarder.util;


import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;
import com.google.gson.GsonBuilder;

public class GsonBuilderHelper {
    private GsonBuilderHelper() {
    }

    public static GsonBuilder newObjectMapper() {
        return (new GsonBuilder()).enableComplexMapKeySerialization().serializeNulls().setDateFormat("yyyy-MM-dd HH:mm:ss:SSS").disableHtmlEscaping().setExclusionStrategies(new ExclusionStrategy[]{new ExclusionStrategy() {
            public boolean shouldSkipField(FieldAttributes fieldAttributes) {
                return fieldAttributes.getName().startsWith("_");
            }

            public boolean shouldSkipClass(Class<?> aClass) {
                return false;
            }
        }});
    }

    public static GsonBuilder newDateMapper() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
    }
}
