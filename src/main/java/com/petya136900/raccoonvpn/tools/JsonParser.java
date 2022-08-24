package com.petya136900.raccoonvpn.tools;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public class JsonParser {
	public static String  toJson(Object obj) {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		return gson.toJson(obj);
	}
	public static <T> T   fromJson(String jsonRequest, Class<T> class1) throws JsonSyntaxException {
		GsonBuilder builder = new GsonBuilder();
		Gson gson = builder.create();
		//System.out.println(class1.getName());
		return gson.fromJson(jsonRequest, class1);
	}
	public static boolean isJson(String Json) {
        try {
            new JSONObject(Json);
        } catch (JSONException ex) {
            try {
                new JSONArray(Json);
            } catch (JSONException ex1) {
                return false;
            }
        }
        return true;
    }	
}
