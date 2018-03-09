package com.syarul.rnlocation;

import android.location.Location;
import android.location.LocationManager;
import android.location.LocationListener;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.Log;
import android.os.Bundle;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule.RCTDeviceEventEmitter;

public class RNLocationModule extends ReactContextBaseJavaModule{

  // React Class Name as called from JS
  public static final String REACT_CLASS = "RNLocation";
  // Unique Name for Log TAG
  public static final String TAG = RNLocationModule.class.getSimpleName();

  private static final float RCT_DEFAULT_LOCATION_ACCURACY = 1;
  public static int POSITION_UNAVAILABLE = 2;

  // Save last Location Provided
  private Location mLastLocation;
  private LocationListener mLocationListener;
  private LocationManager locationManager;

  //The React Native Context
  ReactApplicationContext mReactContext;


  // Constructor Method as called in Package
  public RNLocationModule(ReactApplicationContext reactContext) {
    super(reactContext);
    // Save Context for later use
    mReactContext = reactContext;

    locationManager = (LocationManager) mReactContext.getSystemService(Context.LOCATION_SERVICE);
  }


  @Override
  public String getName() {
    return REACT_CLASS;
  }

  private static class LocationOptions {
    private final long timeout;
    private final double maximumAge;
    private final boolean highAccuracy;
    private final float distanceFilter;

    private LocationOptions(
      long timeout,
      double maximumAge,
      boolean highAccuracy,
      float distanceFilter) {
      this.timeout = timeout;
      this.maximumAge = maximumAge;
      this.highAccuracy = highAccuracy;
      this.distanceFilter = distanceFilter;
    }

    private static LocationOptions fromReactMap(ReadableMap map) {
      // precision might be dropped on timeout (double -> int conversion), but that's OK
      long timeout =
        map.hasKey("timeout") ? (long) map.getDouble("timeout") : Long.MAX_VALUE;
      double maximumAge =
        map.hasKey("maximumAge") ? map.getDouble("maximumAge") : Double.POSITIVE_INFINITY;
      boolean highAccuracy = !map.hasKey("enableHighAccuracy") || map.getBoolean("enableHighAccuracy");
      float distanceFilter = map.hasKey("distanceFilter") ?
        (float) map.getDouble("distanceFilter") :
        RCT_DEFAULT_LOCATION_ACCURACY;

      return new LocationOptions(timeout, maximumAge, highAccuracy, distanceFilter);
    }
  }

  /*
   * Location permission request (Not implemented yet)
   */
  @ReactMethod
  public void requestWhenInUseAuthorization(){
    Log.i(TAG, "Requesting authorization");
  }

  @Nullable
  private static String getValidProvider(LocationManager locationManager, boolean highAccuracy) {
    String provider =
      highAccuracy ? LocationManager.GPS_PROVIDER : LocationManager.NETWORK_PROVIDER;
    if (!locationManager.isProviderEnabled(provider)) {
      provider = provider.equals(LocationManager.GPS_PROVIDER)
        ? LocationManager.NETWORK_PROVIDER
        : LocationManager.GPS_PROVIDER;
      if (!locationManager.isProviderEnabled(provider)) {
        return null;
      }
    }
    return provider;
  }

  private void emitError(int code, String message) {
    WritableMap error = Arguments.createMap();
    error.putInt("code", code);

    if (message != null) {
      error.putString("message", message);
    }

    getReactApplicationContext().getJSModule(RCTDeviceEventEmitter.class)
      .emit("geolocationError", error);
  }

  /*
   * Location Callback as called by JS
   */
  @ReactMethod
  public void startUpdatingLocation(ReadableMap options) {
    LocationOptions locationOptions = LocationOptions.fromReactMap(options);
    String provider = getValidProvider(locationManager, locationOptions.highAccuracy);

    if (provider == null) {
      emitError(POSITION_UNAVAILABLE, "No location provider available.");
      return;
    }

    mLocationListener = new LocationListener(){
      @Override
      public void onStatusChanged(String provider,int status,Bundle extras){
        WritableMap params = Arguments.createMap();
        params.putString("provider", provider);
        params.putInt("status", status);

        sendEvent(mReactContext, "providerStatusChanged", params);
      }

      @Override
      public void onProviderEnabled(String provider){
        sendEvent(mReactContext, "providerEnabled", Arguments.createMap());
      }

      @Override
      public void onProviderDisabled(String provider){
        sendEvent(mReactContext, "providerDisabled", Arguments.createMap());
      }

      @Override
      public void onLocationChanged(Location loc){
        mLastLocation = loc;
        if (mLastLocation != null) {
          try {
            double longitude;
            double latitude;
            double speed;
            double altitude;
            double accuracy;
            double course;

            // Receive Longitude / Latitude from (updated) Last Location
            longitude = mLastLocation.getLongitude();
            latitude = mLastLocation.getLatitude();
            speed = mLastLocation.getSpeed();
            altitude = mLastLocation.getAltitude();
            accuracy = mLastLocation.getAccuracy();
            course = mLastLocation.getBearing();

            Log.i(TAG, "Got new location. Lng: " +longitude+" Lat: "+latitude);

            // Create Map with Parameters to send to JS
            WritableMap params = Arguments.createMap();
            params.putDouble("longitude", longitude);
            params.putDouble("latitude", latitude);
            params.putDouble("speed", speed);
            params.putDouble("altitude", altitude);
            params.putDouble("accuracy", accuracy);
            params.putDouble("course", course);

            // Send Event to JS to update Location
            sendEvent(mReactContext, "locationUpdated", params);
          } catch (Exception e) {
            e.printStackTrace();
            Log.i(TAG, "Location services disconnected.");
          }
        }
      }
    };

    locationManager.requestLocationUpdates(provider, 1000, locationOptions.distanceFilter, mLocationListener);
  }

  @ReactMethod
  public void stopUpdatingLocation() {
    try {
      locationManager.removeUpdates(mLocationListener);
      Log.i(TAG, "Location service disabled.");
    }catch(Exception e) {
      e.printStackTrace();
    }
  }

  /*
   * Internal function for communicating with JS
   */
  private void sendEvent(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    if (reactContext.hasActiveCatalystInstance()) {
      reactContext
        .getJSModule(RCTDeviceEventEmitter.class)
        .emit(eventName, params);
    } else {
      Log.i(TAG, "Waiting for CatalystInstance...");
    }
  }
}
