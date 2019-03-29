package com.hw.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;

public abstract class JsonUtils {

    public static String toJson(Object o) {
        return JSON.toJSONString(o);
    }

    public static String toJson(Object o, SerializerFeature... features) {
        return JSON.toJSONString(o, features);
    }

    /**
     * 反序列化json
     *
     * @param json
     * @param clazz
     * @return
     */
    public static <T> T fromJson(String json, Class<T> clazz) {
        return JSON.parseObject(json, clazz);
    }

    /**
     * 反序列化json
     *
     * @param text
     * @param type
     * @return
     */
    public static <T> T fromJson(String text, TypeReference<T> type) {
        return JSON.parseObject(text, type);
    }

}
