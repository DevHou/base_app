package com.common.utils;

import android.text.TextUtils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by houlijiang on 2014/9/20.
 * 
 * json相关操作，只是对gson的包装
 */
public class JsonUtils {

    private static final String TAG = JsonUtils.class.getSimpleName();

    // 支持转换成Date 和 Calendar类型
    public static Gson gson = createDefaultBuilder(null).create();

    public static class CalendarAdapter implements JsonSerializer<Calendar>, JsonDeserializer<Calendar> {

        private String format;
        private SimpleDateFormat formatter;

        public CalendarAdapter(String format) {
            this.format = format;
            if (!TextUtils.isEmpty(format)) {
                formatter = new SimpleDateFormat(format);
            }
        }

        public Calendar deserialize(JsonElement json, Type type, JsonDeserializationContext context)
            throws JsonParseException {
            Calendar calendar = Calendar.getInstance();
            if (TextUtils.isEmpty(format)) {
                // 如果没传入格式文本则作为时间戳处理
                calendar.setTime(new Date(json.getAsJsonPrimitive().getAsLong()));
            } else {
                try {
                    String dateStr = json.getAsString();
                    Date date = formatter.parse(dateStr);
                    calendar.setTime(date);
                } catch (ParseException e) {
                    AppLog.e(TAG, "parse date error, e:" + e.getLocalizedMessage());
                }

            }
            return calendar;
        }

        public JsonElement serialize(Calendar calendar, Type type, JsonSerializationContext context) {
            if (TextUtils.isEmpty(format)) {
                return new JsonPrimitive(calendar.getTimeInMillis());
            } else {
                return new JsonPrimitive(formatter.format(calendar.getTime()));
            }
        }

    }

    private static GsonBuilder createDefaultBuilder(String format) {
        return new GsonBuilder().registerTypeAdapter(Date.class, new JsonDeserializer<Date>() {
            @Override
            public Date deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
                throws JsonParseException {
                return new Date(json.getAsJsonPrimitive().getAsLong());
            }
        }).registerTypeHierarchyAdapter(Calendar.class, new CalendarAdapter(format));
    }

    /**
     * 设置自定义的时间字符串格式
     * 
     * @param format 格式如"yyyy-MM-dd HH:mm:ss"
     */
    public static void setDateFormat(String format) {
        gson = createDefaultBuilder(format).create();
    }

    public static <T> T parseString(String result, Class<T> classOfT) {
        if (result == null || classOfT == null) {
            return null;
        }
        return getModel(result, classOfT);
    }

    public static String toString(Object obj) {
        if (obj == null) {
            return "";
        }
        return gson.toJson(obj);
    }

    public static int getInt(JsonObject jsonObject, String key, int defaultValue) {
        if (isEmpty(jsonObject, key))
            return defaultValue;

        try {
            return jsonObject.get(key).getAsInt();
        } catch (Exception e) {
            AppLog.e(TAG, "parse int e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static int getInt(String jsonString, String key, int defaultValue) {
        if (isEmpty(jsonString, key))
            return defaultValue;

        try {
            return getInt(parseObject(jsonString), key, defaultValue);
        } catch (Exception e) {
            AppLog.e(TAG, "parse int e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static long getLong(JsonObject jsonObject, String key, long defaultValue) {
        if (isEmpty(jsonObject, key))
            return defaultValue;

        try {
            return jsonObject.get(key).getAsLong();
        } catch (Exception e) {
            AppLog.e(TAG, "parse long e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static long getLong(String jsonString, String key, long defaultValue) {
        if (isEmpty(jsonString, key))
            return defaultValue;

        try {
            return getLong(parseObject(jsonString), key, defaultValue);
        } catch (Exception e) {
            AppLog.e(TAG, "parse long e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static float getFloat(JsonObject jsonObject, String key, float defaultValue) {
        if (isEmpty(jsonObject, key))
            return defaultValue;

        try {
            return jsonObject.get(key).getAsFloat();
        } catch (Exception e) {
            AppLog.e(TAG, "parse float e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static float getFloat(String jsonString, String key, float defaultValue) {
        if (isEmpty(jsonString, key))
            return defaultValue;

        try {
            return getFloat(parseObject(jsonString), key, defaultValue);
        } catch (Exception e) {
            AppLog.e(TAG, "parse float e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static double getDouble(JsonObject jsonObject, String key, double defaultValue) {
        if (isEmpty(jsonObject, key))
            return defaultValue;

        try {
            return jsonObject.get(key).getAsDouble();
        } catch (Exception e) {
            AppLog.e(TAG, "parse double e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static double getDouble(String jsonString, String key, double defaultValue) {
        if (isEmpty(jsonString, key))
            return defaultValue;

        try {
            return getDouble(parseObject(jsonString), key, defaultValue);
        } catch (Exception e) {
            AppLog.e(TAG, "parse double e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static boolean getBoolean(JsonObject jsonObject, String key, boolean defaultValue) {
        if (isEmpty(jsonObject, key))
            return defaultValue;

        try {
            return jsonObject.get(key).getAsBoolean();
        } catch (Exception e) {
            AppLog.e(TAG, "parse boolean e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static boolean getBoolean(String jsonString, String key, boolean defaultValue) {
        if (isEmpty(jsonString, key))
            return defaultValue;

        try {
            return getBoolean(parseObject(jsonString), key, defaultValue);
        } catch (Exception e) {
            AppLog.e(TAG, "parse boolean e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static String getString(JsonObject jsonObject, String key, String defaultValue) {
        if (isEmpty(jsonObject, key))
            return defaultValue;

        try {
            return jsonObject.get(key).getAsString();
        } catch (Exception e) {
            AppLog.e(TAG, "parse string e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static String getString(String jsonString, String key, String defaultValue) {
        if (isEmpty(jsonString, key))
            return defaultValue;

        try {
            return getString(parseObject(jsonString), key, defaultValue);
        } catch (Exception e) {
            AppLog.e(TAG, "parse string e:" + e.getLocalizedMessage());
        }

        return defaultValue;
    }

    public static JsonObject getJsonObject(JsonObject jsonObject, String key) {
        if (isEmpty(jsonObject, key))
            return null;

        try {
            return jsonObject.get(key).getAsJsonObject();
        } catch (Exception e) {
            AppLog.e(TAG, "parse json object e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static JsonObject getJsonObject(String jsonString, String key) {
        if (isEmpty(jsonString, key))
            return null;

        try {
            return getJsonObject(parseObject(jsonString), key);
        } catch (Exception e) {
            AppLog.e(TAG, "parse json object e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static JsonArray getJsonArray(JsonObject jsonObject, String key) {
        if (isEmpty(jsonObject, key))
            return null;

        try {
            return jsonObject.get(key).getAsJsonArray();
        } catch (Exception e) {
            AppLog.e(TAG, "parse json array e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static JsonArray getJsonArray(String jsonString, String key) {
        if (isEmpty(jsonString, key))
            return null;

        try {
            return getJsonArray(parseObject(jsonString), key);
        } catch (Exception e) {
            AppLog.e(TAG, "parse json array e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static JsonObject parseObject(String jsonString) {
        try {
            return new JsonParser().parse(jsonString).getAsJsonObject();
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, "parse string to object e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static JsonArray parseArray(String jsonString) {
        try {
            return new JsonParser().parse(jsonString).getAsJsonArray();
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, "parse string to json array e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static JsonObject getJsonObject(JsonArray jsonArray, int index) {
        return jsonArray.get(index).getAsJsonObject();
    }

    public static String getString(JsonArray jsonArray, int index) {
        return toString(jsonArray.get(index));
    }

    public static <T> T getModel(String jsonString, Class<T> clazz) {
        try {
            return gson.fromJson(jsonString, clazz);
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, "parse get model e:" + e.getLocalizedMessage() + " json:" + jsonString);
        }

        return null;
    }

    public static <T> T getModel(JsonObject jsonObject, Class<T> clazz) {
        try {
            return gson.fromJson(jsonObject, clazz);
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, "parse get model e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static <T> List<T> getModelList(JsonArray jsonArray, TypeToken<List<T>> typeToken) {
        try {
            return gson.fromJson(jsonArray, typeToken.getType());
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, "parse get model list e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static <T> List<T> getModelList(String jsonString, TypeToken<List<T>> typeToken) {
        try {
            return gson.fromJson(parseArray(jsonString), typeToken.getType());
        } catch (JsonSyntaxException e) {
            AppLog.e(TAG, "parse get model list e:" + e.getLocalizedMessage());
        }

        return null;
    }

    public static void addString(JsonObject jsonObject, String property, String value) {
        if (isEmpty(jsonObject, property))
            return;
        try {
            jsonObject.addProperty(property, value);
        } catch (Exception e) {
            AppLog.e(TAG, "parse add string e:" + e.getLocalizedMessage());
        }
    }

    public static void addBoolean(JsonObject jsonObject, String property, boolean value) {
        if (isEmpty(jsonObject, property))
            return;
        try {
            jsonObject.addProperty(property, value);
        } catch (Exception e) {
            AppLog.e(TAG, "parse add boolean e:" + e.getLocalizedMessage());
        }
    }

    private static boolean isEmpty(JsonObject jsonObject, String key) {
        return jsonObject == null || TextUtils.isEmpty(key);
    }

    private static boolean isEmpty(String jsonString, String key) {
        return TextUtils.isEmpty(jsonString) || TextUtils.isEmpty(key);
    }

}
