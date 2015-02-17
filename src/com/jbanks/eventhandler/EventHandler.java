package com.jbanks.eventhandler;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by jbanks on 12/12/14.
 */
public class EventHandler {

    private static final String TAG = "EventHandler";

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            Method method;
            try {
                method = mMethodIndex.get(msg.what);
            } catch (ArrayIndexOutOfBoundsException e) {
                return;
            }

            try {

                method.setAccessible(true);

                // Invoke method with object only if present in method parameters
                if (method.getParameterTypes().length == 0)  {
                    method.invoke(mClassInstance);
                }
                else {
                    method.invoke(mClassInstance, msg.obj);
                }

            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }

        }
    };


    // Event message mapping
    ConcurrentMap<String, Integer> mMethodMap = new ConcurrentHashMap<String, Integer>();
    List<Method> mMethodIndex = new ArrayList<Method>();


    Class mKlass;
    Object mClassInstance;


    /**
     * Constructor
     *
     * Takes an Object containing annotated methods to use as event handlers.
     *
     * @param object - Object containing Event annotated methods
     */
    public EventHandler(Object object) {

        mClassInstance = object;
        mKlass = object.getClass();

        for (Method method : mKlass.getDeclaredMethods()) {

            Log.v(TAG, "Processing method: " + method.getName());

            // Grab method parameters
            Class<?>[] parameters = method.getParameterTypes();

            if (method.isAnnotationPresent(Event.class)) {

                // Ensure return type is void
                if (!method.getReturnType().equals(Void.TYPE)) {
                    throw new IllegalArgumentException("Return type other than void in method: " + method.getName());
                }

                // Methods should always have zero or one argument
                if (parameters.length > 1) {
                    throw new IllegalArgumentException("More than one argument present in method: " + method.getName());
                }

                // If method has a parameter, it should be extended from type Object (i.e. no primitive types)
                if (parameters.length > 0) {
                    if (!Object.class.isAssignableFrom(parameters[0])) {
                        throw new IllegalArgumentException("Parameter must be a type extended from Object in method: " + method.getName());
                    }
                }

                Event eventAnnotation = method.getAnnotation(Event.class);

                String eventName = eventAnnotation.value();
                mMethodMap.put(eventName, mMethodIndex.size());
                mMethodIndex.add(method);
            }

        }

    }


    /**
     * Send an event to the message queue associated with this EventHandler.
     *
     * If a method exists that has an annotation with the value matching the argument event,
     * this method will be called upon dispatch by the message queue.
     *
     * The method must accept one parameter of type Object.
     *
     * @param event - String matching the annotation value of the desired method
     * @param obj - Object to pass to the method
     */
    public synchronized void sendEvent(String event, Object obj) {

        int what = mMethodMap.get(event);
        Message message = mHandler.obtainMessage(what, obj);

        mHandler.dispatchMessage(message);

    }

    /**
     * Send an event to the message queue associated with this EventHandler.
     *
     * If a method exists that has an annotaion with the value matching the argument event,
     * this method will be called upon by dispatch by the message queue.
     *
     * The method must have no parameters.
     *
     * @param event - String matching the annotation value of the desired method
     *
     */
    public synchronized void sendEvent(String event) {
        sendEvent(event, null);
    }

    /**
     * Send an event in the future to the message queue associated with this EventHandler.
     *
     * The method must accept one parameter of type Object.
     *
     * @param event - String matching the annotation value of the desired method
     * @param obj - Object to pass to the method
     * @param milliSeconds - Delay in milliseconds
     */
    public synchronized void sendEventDelayed(String event, Object obj, Long milliSeconds) {

        int what = mMethodMap.get(event);
        Message message = mHandler.obtainMessage(what, obj);

        mHandler.sendMessageDelayed(message, milliSeconds);
    }


    /**
     * Send an event in the future to the message queue associated with this EventHandler.
     *
     * The method must have no parameters.
     *
     * @param event - String matching the annotation value of the desired method
     * @param milliSeconds - Delay in milliseconds
     */
    public synchronized void sendEventDelayed(String event, Long milliSeconds) {
        sendEventDelayed(event, milliSeconds);
    }

    /**
     * Remove all pending events from the queue.
     */
    public synchronized void removeAllEvents() {

        for (int what: mMethodMap.values()) {
            mHandler.removeMessages(what);
        }

    }
}
