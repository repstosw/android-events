package com.jbanks.eventhandler.tests;

import android.os.Looper;
import android.test.AndroidTestCase;
import com.jbanks.eventhandler.Event;
import com.jbanks.eventhandler.EventHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbanks on 12/12/14.
 */
public class EventHandlerTest extends AndroidTestCase {

    private static final String TAG = "EventHandlerTest";

    // Thread class with Looper to test event looping
    public class TestThread extends Thread {

        public EventHandler eventHandler;

        public Looper looper;

        public TestThread() {
            eventHandler = new EventHandler(this);
        }

        @Override
        public void run() {
            Looper.prepare();
            looper = Looper.myLooper();
            Looper.loop();
        }

    }

    private TestThread mTestThread;


    // Check to see if a callback can be called
    public void testCallbackWithParameter() throws Exception {

        final CountDownLatch cdLatch = new CountDownLatch(1);

        mTestThread = new TestThread() {

            @Event("test")
            void onTestEvent(Object object) {
                cdLatch.countDown();
            }
        };

        mTestThread.start();

        // Sleep to allow thread to start
        Thread.sleep(250);

        mTestThread.eventHandler.sendEvent("test", "TEST");

        assertTrue(cdLatch.await(250, TimeUnit.MILLISECONDS));
        mTestThread.looper.quit();
    }

    // Check to see if a callback can be called
    public void testCallbackWithoutParameter() throws Exception {

        final CountDownLatch cdLatch = new CountDownLatch(1);

        mTestThread = new TestThread() {

            @Event("test")
            void onTestEvent() {
                cdLatch.countDown();
            }
        };

        mTestThread.start();

        // Sleep to allow thread to start
        Thread.sleep(250);

        mTestThread.eventHandler.sendEvent("test");

        assertTrue(cdLatch.await(250, TimeUnit.MILLISECONDS));
        mTestThread.looper.quit();
    }


    // See if we can take other object types
    public void testCallbackWithString() throws Exception {

        final CountDownLatch cdLatch = new CountDownLatch(1);

        mTestThread = new TestThread() {
            @Event("stringTest")
            void onStringEvent(String s) {
                cdLatch.countDown();
            }
        };

        mTestThread.start();
        Thread.sleep(250);

        mTestThread.eventHandler.sendEvent("stringTest", "STRING!");
        assertTrue(cdLatch.await(250, TimeUnit.MILLISECONDS));
        mTestThread.looper.quit();
    }

    public void testInvalidReturnType() throws Exception {

        try {
            mTestThread = new TestThread() {

                @Event("badmethod")
                public String onBadMethod(Object object) {
                    return "NO";
                }
            };
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail();
    }

    public void testInvalidMethodParameters() throws Exception {

        try {
            mTestThread = new TestThread() {

                @Event("badmethod")
                public void onBadMethod(Object object, String s, int y) {
                }
            };
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail();

    }

    public void testInvalidParameterType() throws Exception {

        try {
            mTestThread = new TestThread() {

                @Event("badmethod")
                public void onBadMethod(int y) {
                }
            };
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail();
    }

}
