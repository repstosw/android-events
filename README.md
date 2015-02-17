# EventHandler Library

This library consists of two modules, one for receiving messages from an Android event loop, and one for receiving broadcasted intents.
The main goal of these modules is to avoid complex switch() statements (for event loops), or complex if..else statements (for broadcasts),
and replace these with direct calls to object methods.

## EventHandler module
The EventHandler is required to be used within an object containing an Android Looper (such as Activity, Service, HandlerThread, and a few others).

### EventHandler Usage
- Instantiate the EventHandler. The EventHandler takes a reference to the object that contains the desired callback methods (usually the same class as where the EventHandler lives).
In addition, the EventHandler expects to be able to grab a reference to a Looper from its internal Handler in the current thread.
```
EventHandler eventHandler = new EventHandler(this);
```

- Declare callback methods with Event annotations. The method name is arbitrary.
```
@Event("myEvent")
public void myEventIsCalled() {
    // do your stuff
}

@Event("myEventWithAnObject")
public void eventWithObject(Object object) {
    // do stuff with the Object (cast to preferred type)
}
```
Annotated methods must have a return type of void, and accept either no parameters or a single Object parameter (this reflects the underlying Android message format).


- Send an event, either with or without an attached object. Attempting to send a primitive type (i.e. not extended from Object) will produce an exception.
```
eventHandler.sendEvent("myEvent"); // will call the myEventIsCalled() method
eventHandler.sendEvent("myEventWithAnObject", "sending a String object"); // will call the eventWithObject method with a String argument 
```
The sendEvent method can be called from anywhere, from any thread (make EventHandler public or declare public methods to use it). However, a given EventHandler will only respond to events sent to itself (there is no universal Handler in the API).

## BroadcastHandler module
The BroadcastHandler module operates similarly to the EventHandler, with the requirement that it accept a reference to a Context due to the Android broadcast intent mechanism.

### BroadcastHandler usage
- Instantiate the BroadcastHandler. The constructor takes two arguments: a Context to register the internal BroadcastReceiver to, and an Object containing the annotated callback methods. In the event that the Context and the Object are the same (an Activity or Service, for example), there is also a single argument constructor that internally calls the two-argument constructor.
```
BroadcastHandler broadcastHandler = new BroadcastHandler(context, object); 
// or
BroadcastHandler broadcastHandler = new BroadcastHandler(this); // if the current class is extended from Context and contains the annotated callbacks
```

- Declare annotated methods. The method name is arbitrary. The method can take either no parameters or a single Bundle parameter that will contain Intent extras attached to the broadcast (if any).
```

@Broadcast("android.intent.action.DEVICE_STORAGE_LOW")
public void onDeviceStorageLow() {
    // do something
}

@Broadcast("android.intent.action.BATTERY_CHANGED")
public void onBatteryChanged(Bundle extras) {
    int batteryLevel = extras.getInt(BatteryManager.EXTRA_LEVEL);
    // do something with this information 
}
```

- Register receivers. Required before anything is received - this matches the Android registerReceiver() methods that need to be tied to the Android Activity/Service lifecycle.
```
broadcastHandler.registerReceivers();
```
When receivers are registered, the methods will start receiving broadcasts for which they are annotated.

- Unregister receivers prior to object(s) being destroyed - this is important. Usually done in an onPause() or onDestroy() method in an Activity or Service.
```
broadcastHandler.unRegisterReceivers();
```

