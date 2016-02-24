# react-native-gps

Native GPS location support for React Native for Android and IOS. This module was inspired in project of [timfpark](https://github.com/timfpark/react-native-location) and  [syarul](https://github.com/syarul/react-native-android-location). For the moment is not compatible for a PR back to one of that repositories because some methods are not implemented yet and some structure changed. Only a few methods are implemented so if you want to contribute, any contribution of new missing methods will be appreciated.

## Installation
#### Install the npm package
```bash
npm i --save react-native-gps
```
## IOS
You then need to add the Objective C part to your XCode project. Drag `RNLocation.xcodeproj` from the `node_modules/react-native-location` folder into your XCode project. Click on the your project in XCode, goto Build Phases then Link Binary With Libraries and add `libRNLocation.a` and `CoreLocation.framework`.

NOTE: Make sure you don't have the `RNLocation` project open separately in XCode otherwise it won't work.

### Android

* In `android/settings.gradle`

```gradle
...
include ':RNLocation'
project(':RNLocation').projectDir = new File(rootProject.projectDir, '../node_modules/react-native-gps')
```

* In `android/app/build.gradle`

```gradle
...
dependencies {
    ...
    compile project(':RNLocation')
}
```

* register module (in MainActivity.java)

```java
import com.syarul.rnalocation.RNLocation;  // <--- import

public class MainActivity extends Activity implements DefaultHardwareBackBtnHandler {
  ......

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mReactRootView = new ReactRootView(this);

    mReactInstanceManager = ReactInstanceManager.builder()
      .setApplication(getApplication())
      .setBundleAssetName("index.android.bundle")
      .setJSMainModuleName("index.android")
      .addPackage(new MainReactPackage())
      .addPackage(new RNLocation()) // <-- Register package here
      .setUseDeveloperSupport(BuildConfig.DEBUG)
      .setInitialLifecycleState(LifecycleState.RESUMED)
      .build();

    mReactRootView.startReactApplication(mReactInstanceManager, "example", null);

    setContentView(mReactRootView);
  }

  ......

}
```

#### Add permissions to your Project

Add this to your AndroidManifest file;

``` xml
// file: android/app/src/main/AndroidManifest.xml

<uses-permission android:name="android.permission.ACCESS_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
```

## Location Usage
```javascript
var React = require('react-native');
var { DeviceEventEmitter } = React;

var { RNLocation: Location } = require('NativeModules');

Location.startUpdatingLocation();

var subscription = DeviceEventEmitter.addListener(
    'locationUpdated',
    (location) => {
        /* Example location returned
        {
          speed: -1,
          longitude: -0.1337,
          latitude: 51.50998,
          accuracy: 5,
          heading: -1,
          altitude: 0,
          altitudeAccuracy: -1
        }
        */
    }
);
```


## Methods

To access the methods, you need import the `react-native-location` module. 

### Location.requestWhenInUseAuthorization
```javascript
Location.requestWhenInUseAuthorization();
```

This method should be called before anything else. It requests location updates while the application is open. If the application is in the background, you will not get location updates (for the moment, background work not implemented yet).

### Location.startUpdatingLocation
```javascript
Location.startUpdatingLocation();
var subscription = DeviceEventEmitter.addListener(
    'locationUpdated',
    (location) => {
        // do something with the location
    }
);
```
## License
MIT, for more information see `LICENSE`
