package com.hw.tcc.serialize;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Json序列化器
 *
 * @author chengwei11
 * @date 2019/1/18
 */
public class JsonTccSerializer implements TccSerializer {
    private static Gson gson = null;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gson = gsonBuilder.setPrettyPrinting().serializeNulls().create();
    }

    public String serialize(Object transactionData) {
        return gson.toJson(transactionData);
    }

    public <T> T deserialize(String transactionData, Class<T> clz) {
        return gson.fromJson(transactionData, clz);
    }
}
