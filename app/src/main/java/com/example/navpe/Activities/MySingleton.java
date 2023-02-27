package com.example.navpe.Activities;

import android.annotation.SuppressLint;
import android.content.Context;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;

import org.json.JSONObject;

public class MySingleton {
    @SuppressLint("StaticFieldLeak")
    private static MySingleton instance;
    private RequestQueue requestQueue;
    private final Context ctx;

    private MySingleton(Context context) {
        ctx = context;
        requestQueue = getRequestQueue();
    }

    public static synchronized MySingleton getInstance(Context context) {
        if (instance == null) {
            instance = new MySingleton(context);
        }
        return instance;
    }

    public RequestQueue getRequestQueue() {
        if (requestQueue == null) {
            requestQueue = Volley.newRequestQueue(ctx.getApplicationContext());
        }
        return requestQueue;
    }

    public void addToRequestQueue(Request<JSONObject> req) {
        getRequestQueue().add(req);
    }
}