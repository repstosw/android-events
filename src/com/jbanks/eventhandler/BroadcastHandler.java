package com.jbanks.eventhandler;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jbanks on 12/16/14.
 */
public class BroadcastHandler {

    private static final String TAG = "BroadcastHandler";

    Context mContext;
    Object mMethodObject;

    // Broadcast intent mapping
    Map<String, Method> mBroadcastMap = new HashMap<String, Method>();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            String action = intent.getAction();
            Bundle bundle = intent.getExtras();

            Method method = mBroadcastMap.get(action);

            // In the highly unlikely event that the method doesn't exist in the map
            if (method == null) {
                return;
            }

            method.setAccessible(true);

            Class<?>[] parameters = method.getParameterTypes();

            try {
                if (parameters.length == 1) {
                    method.invoke(mMethodObject, bundle);
                }
                else if (parameters.length == 0) {
                    method.invoke(mMethodObject);
                }


            }
            catch (IllegalAccessException e) {

            }
            catch (InvocationTargetException e) {

            }
        }
    };


    /**
     * Convenience constructor for BroadcastHandler(context, context)
     *
     * @param context - Context containing annotated methods to call upon broadcasts
     */
    public BroadcastHandler(Context context) {
        this(context, context);
    }

    /**
     * Constructor
     *
     * Takes a Context (required to register broadcast receivers, and an Object containing
     * annotated methods to call.
     *
     * In the event that the Context represents an Activity or Service, it is entirely feasible that the Context and
     * the Object are the same. In this case, use the constructor BroadcastHandler(context)
     *
     * @param context - Context to use for receiving broadcasts
     * @param object - Object containing annotated methods to call upon broadcasts.
     */
    public BroadcastHandler(Context context, Object object) {

        mContext = context;
        Class klass = object.getClass();
        mMethodObject = object;

        for (Method method : klass.getDeclaredMethods()) {

            if (method.isAnnotationPresent(Broadcast.class)) {

                Log.d(TAG, "Processing method: " + method.getName());

                // Grab method parameters
                Class<?>[] parameters = method.getParameterTypes();

                // Ensure return type is void
                if (!method.getReturnType().equals(Void.TYPE)) {
                    throw new IllegalArgumentException("Return type other than void in method: " + method.getName());
                }

                // Ensure method can take a Bundle, if it has a parameter
                if(parameters.length == 1) {
                    if(!parameters[0].equals(Bundle.class)) {
                        throw new IllegalArgumentException("Parameter other than Bundle in method: " + method.getName());
                    }
                }
                else if (parameters.length > 1) {
                    throw new IllegalArgumentException("Too many parameters in method: " + method.getName());
                }

                Broadcast broadcastAnnotation = method.getAnnotation(Broadcast.class);

                String broadcastName = broadcastAnnotation.value();

                mBroadcastMap.put(broadcastName, method);
            }

        }

    }

    /**
     * Register broadcast receivers based upon annotated methods in this context
     *
     * Methods should either take no parameters, or take one parameter of type Bundle
     *
     */
    public void registerReceivers() {

        IntentFilter intentFilter = new IntentFilter();
        // Loop through all keys in the broadcast map and add to intent filter
        for (String receiver: mBroadcastMap.keySet()) {
            intentFilter.addAction(receiver);
        }

        mContext.registerReceiver(mReceiver, intentFilter);
    }

    /**
     * Unregister broadcast receivers
     */
    public void unRegisterReceivers() {
        mContext.unregisterReceiver(mReceiver);
    }
}
