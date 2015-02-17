package com.jbanks.eventhandler.tests;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.InstrumentationTestCase;
import android.util.Log;
import com.jbanks.eventhandler.Broadcast;
import com.jbanks.eventhandler.BroadcastHandler;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

/**
 * Created by jbanks on 12/16/14.
 */
public class BroadcastHandlerTest extends InstrumentationTestCase {

    private static final String TAG = "BroadcastHandlerTest";

    Context mContext;

    public class TestObject {

    }

    public void testMethodCallback() throws Exception {

        final CountDownLatch cdLatch = new CountDownLatch(1);

        mContext = getInstrumentation().getContext();

        TestObject testObject = new TestObject() {
            @Broadcast("com.jbanks.test")
            public void methodTest() {
                Log.d(TAG, "methodTest");
                cdLatch.countDown();
            }
        };

        BroadcastHandler broadcastHandler = new BroadcastHandler(mContext, testObject);

        broadcastHandler.registerReceivers();
        mContext.sendBroadcast(new Intent("com.jbanks.test"));

        assertTrue(cdLatch.await(500, TimeUnit.MILLISECONDS));
        broadcastHandler.unRegisterReceivers();
    }

    public void testMethodCallbackBundle() throws Exception {

        final CountDownLatch cdLatch = new CountDownLatch(2);

        mContext = getInstrumentation().getContext();

        TestObject testObject = new TestObject() {

            @Broadcast("com.jbanks.bundle")
            public void bundleTest(Bundle bundle) {
                String s = bundle.getString("test.extra");
                assertNotNull(s);
                if (s.equals("test")) {
                    cdLatch.countDown();
                }
            }

            @Broadcast("com.jbanks.bundle2")
            public void bundleTest2() {
                cdLatch.countDown();
            }
        };

        BroadcastHandler broadcastHandler = new BroadcastHandler(mContext, testObject);

        broadcastHandler.registerReceivers();

        Intent intent = new Intent("com.jbanks.bundle");
        intent.putExtra("test.extra", "test");

        mContext.sendBroadcast(intent);

        intent.setAction("com.jbanks.bundle2");
        mContext.sendBroadcast(intent);

        assertTrue(cdLatch.await(500, TimeUnit.MILLISECONDS));

    }

    public void testBadReturnType() throws Exception {

        TestObject testObject = new TestObject() {

            @Broadcast("com.broadcast")
            public int bundleTest() {
                return 0;
            }
        };

        mContext = getInstrumentation().getContext();

        try {
            BroadcastHandler broadcastHandler = new BroadcastHandler(mContext, testObject);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail();

    }

    public void testNonBundleType() throws Exception {

        TestObject testObject = new TestObject() {

            @Broadcast("com.broadcast")
            public void bundleTest(String y) {
            }
        };

        mContext = getInstrumentation().getContext();

        try {
            BroadcastHandler broadcastHandler = new BroadcastHandler(mContext, testObject);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail();

    }

    public void testTooManyParams() throws Exception {

        TestObject testObject = new TestObject() {

            @Broadcast("com.broadcast")
            public void bundleTest(Bundle b, int x) {
            }
        };

        mContext = getInstrumentation().getContext();

        try {
            BroadcastHandler broadcastHandler = new BroadcastHandler(mContext, testObject);
        }
        catch (IllegalArgumentException e) {
            return;
        }

        fail();

    }
}
