package net.chaoc.blescanner.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by yejun on 10/29/16.
 * Copyright (C) 2016 qinyejun
 */

public class CacheUtil {

    private static final String KEY_CACHE = "ble_station_cache";

    private CacheUtil() {
    }

    private static CacheUtil instance;

    public synchronized static CacheUtil getInstance() {
        if (null == instance) {
            instance = new CacheUtil();
        }
        return instance;
    }

    private Context context;

    public void init(Context context) {
        this.context = context;
    }

    /**
     * 设置用户登录状态
     * @param isLogin
     */
    public void setLogin(boolean isLogin) {
        if (null == context) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        sp.edit().putBoolean("is_login", isLogin).commit();
    }

    /**
     * 获取用户登录状态
     * @return
     */
    public boolean isLogin() {
        if (null == context) {
            return false;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        return sp.getBoolean("is_login", false);
    }

    /**
     * 保存登录token
     * @param token
     */
    public void setToken(String token) {
        if (null == context) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        sp.edit().putString("token", token).commit();
    }

    /**
     * 获取登录token
     * @return
     */
    public String getToken() {
        if (null == context) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        return sp.getString("token", null);
    }


    /**
     * 保存云巴alias
     * @param alias
     */
    public void setYunbaAlias(String alias) {
        if (null == context) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        sp.edit().putString("alias", alias).commit();
    }



    /**
     * 获取云巴alias
     * @return
     */
    public String getYunbaAlias() {
        if (null == context) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        return sp.getString("alias","");
    }

    /**
     * 保存云巴 topic
     * @param topic
     */
    public void setYunbaTopic(String topic) {
        if (null == context) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        sp.edit().putString("topic", topic).commit();
    }



    /**
     * 获取云巴topic
     * @return
     */
    public String getYunbaTopic() {
        if (null == context) {
            return null;
        }
        SharedPreferences sp = context.getSharedPreferences(
                KEY_CACHE, Context.MODE_PRIVATE);
        return sp.getString("topic","");
    }


    /**
     * 设置网络配置版本号
     * @param netConfigVersion
     */
    public void setNetConfigVersion(int netConfigVersion) {
        if (null == context) {
            return;
        }
        SharedPreferences sp = context.getSharedPreferences(KEY_CACHE, Context.MODE_PRIVATE);
        sp.edit().putInt("net_config_version", netConfigVersion).commit();
    }

    /**
     * 获取网络配置版本号
     * @return
     */
    public int getNetConfigVersion() {
        if (null == context) {
            return 0;
        }
        SharedPreferences sp = context.getSharedPreferences(KEY_CACHE, Context.MODE_PRIVATE);
        return sp.getInt("net_config_version", 0);
    }



    /**
     * 加密存入缓存
     *
     * @param cacheObj
     */
    public void saveObjCache(Object cacheObj) {
        if (null != cacheObj) {
            Gson gson = new Gson();
            String json = gson.toJson(cacheObj);
            String key = cacheObj.getClass().getSimpleName();
            try {
                String encryptedData = Base64Encoder.encode(json);// base 64加密
                SharedPreferences sp = context.getSharedPreferences(KEY_CACHE,
                        Context.MODE_PRIVATE);
                sp.edit().putString(key, encryptedData).commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 解密取出缓存对象
     *
     * @param cacheObj
     * @return
     */
    public Object getObjCache(Object cacheObj) {
        if (null == cacheObj) {
            return null;
        }
        if (null != cacheObj) {
            SharedPreferences sp = context.getSharedPreferences(KEY_CACHE,
                    Context.MODE_PRIVATE);
            String key = cacheObj.getClass().getSimpleName();
            String value = "";
            String encryptedData = sp.getString(key, "");
            if (!TextUtils.isEmpty(encryptedData)) {
                try {
                    value = Base64Decoder.decode(encryptedData);
                    Gson gson = new Gson();
                    cacheObj = gson.fromJson(value, cacheObj.getClass());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
        return cacheObj;
    }

    /**
     *  存入集合缓存的通用方法
     * @param key
     * @param cacheList
     * @param <T>
     */
    public <T> void saveListCache(String key,
                                  ArrayList<T> cacheList) {
        if (null != cacheList && cacheList.size() >= 0) {
            Gson gson = new Gson();
            String json = gson.toJson(cacheList);
            try {
                String encryptedData = Base64Encoder.encode(json);// base
                // 64加密
                SharedPreferences sp = context.getSharedPreferences(KEY_CACHE, Context.MODE_PRIVATE);
                sp.edit().putString(key, encryptedData).commit();
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("info", "saveListCache Exception:" + e);
            }
        }
    }

    /**
     * 取集合缓存的通用方法
     * @param key
     * @return
     */
    public String getListStrCache(String key) {
        SharedPreferences sp = context.getSharedPreferences(KEY_CACHE,
                Context.MODE_PRIVATE);
        String value = "";
        String encryptedData = sp.getString(key, "");
        if (!TextUtils.isEmpty(encryptedData)) {
            try {
                value = Base64Decoder.decode(encryptedData);// base 64解密
            } catch (Exception e) {
                e.printStackTrace();
                Log.e("info", "getListCache Exception:" + e);
            }
        }
        return value;
    }

}
