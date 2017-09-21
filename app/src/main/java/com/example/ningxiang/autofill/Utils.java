package com.example.ningxiang.autofill;

import android.support.v4.util.LruCache;

import java.lang.reflect.Field;
import java.util.Map;

/**
 * Created by ningxiang on 4/6/17.
 */

public class Utils {

    public static <K, V> Map<K, V> getLruCacheMap(LruCache<K, V> cache) {
        Class clazz = LruCache.class;
        Field mapFiled = null;
        try {
            mapFiled = clazz.getDeclaredField("map");
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
        if (mapFiled != null) {
            mapFiled.setAccessible(true);
        }

        try {
            return (Map<K, V>) mapFiled.get(cache);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            return null;
        }
    }
}
