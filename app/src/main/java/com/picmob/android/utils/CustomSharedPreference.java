package com.picmob.android.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.internal.LinkedTreeMap;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class CustomSharedPreference {
    private static final String filename = "Picmob_SP";
    private static CustomSharedPreference store;
    public static int theme = 1;

    private final SharedPreferences SP;

    private CustomSharedPreference(Context context) {
        this.SP = context.getApplicationContext().getSharedPreferences(filename, 0);
    }

    public static CustomSharedPreference getInstance(Context context) {
        if (store == null) {
            store = new CustomSharedPreference(context);
        }
        return store;
    }

    public void putString(String key, String value) {
        SharedPreferences.Editor editor = this.SP.edit();
        editor.putString(key, value);
        editor.apply();
    }


    public void putList(String key, List<Object> list) {

//        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (list.size() > 0) {
            SharedPreferences.Editor editor = SP.edit();
            Gson gson = new Gson();
            String json = gson.toJson(list);
            editor.putString(key, json);
            editor.apply();
        }
    }


    public void getList(String key) {

        Gson gson = new Gson();
        String json = SP.getString(key, "");
        Type type = new TypeToken<List<Object>>() {
        }.getType();
        List<Object> arrayList = gson.fromJson(json, type);
    }

    public void putLinkedTreeMap(String key, LinkedTreeMap<String, String> mList) {
        if (mList != null && mList.size() > 0) {
            String value = new Gson().toJson(mList);
            SharedPreferences.Editor editor = this.SP.edit();
            editor.putString(key, value);
            editor.apply();
        }
    }

    public LinkedTreeMap getLinkedTreeMap(String key) {
        LinkedTreeMap<String, String> mList = new LinkedTreeMap<>();
        String strJson = this.SP.getString(key, null);
        if (strJson == null) {
            return mList;
        }
        return (LinkedTreeMap) new Gson().fromJson(strJson, new TypeToken<LinkedTreeMap<String, String>>() {
        }.getType());
    }

   /* public UserAuthPojo getData(Class tClass,String key) {
        return new Gson().fromJson(this.SP.getString(key, null), UserAuthPojo.class);
    }

    public void putData(String key, UserAuthPojo data) {
        String value = new Gson().toJson(data);
        SharedPreferences.Editor editor = this.SP.edit();
        editor.putString(key, value);
        editor.apply();
    }*/

    public String getString(String key) {
        return this.SP.getString(key, null);
    }

    public int getInt(String key) {
        return this.SP.getInt(key, 0);
    }

    public void putInt(String key, int num) {
        SharedPreferences.Editor editor = this.SP.edit();
        editor.putInt(key, num);
        editor.apply();
    }

    public void putBoolean(String key, boolean value) {
        SharedPreferences.Editor editor = this.SP.edit();
        editor.putBoolean(key, value);
        editor.apply();
    }

    public boolean getBoolean(String key) {
        return this.SP.getBoolean(key, false);
    }

    public void clear() {
        SharedPreferences.Editor editor = this.SP.edit();
        editor.clear();
        editor.apply();
    }

    public void remove() {
        SharedPreferences.Editor editor = this.SP.edit();
        editor.remove(filename);
        editor.apply();
    }

}
