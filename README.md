# react-native-gps

Native GPS location support for React Native for Android and IOS. Was inspired in project of [timfpark](https://github.com/timfpark/react-native-location) and  [syarul](https://github.com/syarul/react-native-android-location)

## Installation
Pending: not released yet

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
          timestamp: 1446007304457.029
        }
        */
    }
);
```


## Methods

To access the methods, you need import the `react-native-location` module. This is done through `var Beacons = require('react-native-location')`.

### Location.requestWhenInUseAuthorization
```javascript
Location.requestWhenInUseAuthorization();
```

This method should be called before anything else. It requests location updates while the application is open. If the application is in the background, you will not get location updates.

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
