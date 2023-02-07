package ir.myket.billingclient;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

import ir.myket.billingclient.util.IABLogger;

public class MyketIABPluginBase {
    protected static final String TAG = "[MyketAIB][Plugin]";
    protected static final String MANAGER_NAME = "MyketPlugin.IABEventManager";
    private Class<?> mUnityPlayerClass;
    private Field mUnityPlayerActivityField;
    private Method mUnitySendMessageMethod;

    public MyketIABPluginBase() {
        try {
            mUnityPlayerClass = Class.forName("com.unity3d.player.UnityPlayer");
            mUnityPlayerActivityField = mUnityPlayerClass.getField("currentActivity");
            mUnitySendMessageMethod = mUnityPlayerClass
                    .getMethod("UnitySendMessage", String.class, String.class, String.class);
        } catch (ClassNotFoundException e) {
            Log.i(TAG, "Could not find UnityPlayer class: " + e.getMessage());
        } catch (NoSuchFieldException e2) {
            Log.i(TAG, "Could not find currentActivity field: " + e2.getMessage());
        } catch (Exception e3) {
            Log.i(TAG, "Unkown exception occurred locating UnitySendMessage(): " + e3.getMessage());
        }
    }

    protected Activity getActivity() {
        if (mUnityPlayerActivityField != null) {
            try {
                final Activity activity = (Activity) mUnityPlayerActivityField.get(mUnityPlayerClass);
                if (activity == null) {
                    Log.e(TAG,
                            "The Unity Activity does not exist. This could be due to a low memory situation");
                }
                return activity;
            } catch (Exception e) {
                Log.i(TAG, "Error getting currentActivity: " + e.getMessage());
            }
        }
        return null;
    }

    protected void UnitySendMessage(final String methodName, String methodParam) {
        if (methodParam == null) {
            methodParam = "";
        }
        if (mUnitySendMessageMethod != null) {
            try {
                mUnitySendMessageMethod.invoke(null, MANAGER_NAME, methodName, methodParam);
            } catch (IllegalArgumentException e) {
                Log.i(TAG, "could not find UnitySendMessage method: " + e.getMessage());
            } catch (IllegalAccessException e2) {
                Log.i(TAG, "could not find UnitySendMessage method: " + e2.getMessage());
            } catch (InvocationTargetException e3) {
                Log.i(TAG, "could not find UnitySendMessage method: " + e3.getMessage());
            }
        } else {
            Toast.makeText((Context) getActivity(),
                    "UnitySendMessage:\n" + methodName + "\n" + methodParam, Toast.LENGTH_LONG).show();
            Log.i(TAG, "UnitySendMessage: MyketIABManager, " + methodName + ", " + methodParam);
        }
    }

    protected void runSafelyOnUiThread(final Runnable r) {
        runSafelyOnUiThread(r, null);
    }

    protected void runSafelyOnUiThread(final Runnable r, final String methodName) {
        getActivity().runOnUiThread((Runnable) new Runnable() {
            @Override
            public void run() {
                try {
                    r.run();
                } catch (Exception e) {
                    if (methodName != null) {
                        UnitySendMessage(methodName, e.getMessage());
                    }
                    Log.e(TAG, "Exception running command on UI thread: " + e.getMessage());
                }
            }
        });
    }

    protected void persist(final String key, final String value) {
        IABLogger.logEntering(getClass().getSimpleName(), "persist", new Object[]{key, value});
        try {
            final SharedPreferences prefs =
                    getActivity().getSharedPreferences("MyketIABPluginPreferences", 0);
            prefs.edit().putString(key, value).apply();
        } catch (Exception e) {
            Log.i(TAG, "error in persist: " + e.getMessage());
        }
    }

    protected String unPersist(final String key, final boolean deleteKeyAfterFetching) {
        IABLogger.logEntering(getClass().getSimpleName(), "unpersist", new Object[]{key, true});
        String val = "";
        try {
            final SharedPreferences prefs =
                    getActivity().getSharedPreferences("MyketIABPluginPreferences", 0);
            val = prefs.getString(key, (String) null);
            if (deleteKeyAfterFetching) {
                prefs.edit().remove(key).apply();
            }
            return val;
        } catch (Exception e) {
            Log.i(TAG, "error in unpersist: " + e.getMessage());
            return val;
        }
    }
}
