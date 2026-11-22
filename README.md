# Traffic Scotland – Mobile Platform Development coursework

## Joseph Welsh - S2364595 

Android app (Java/XML) showing Traffic Scotland incidents and roadworks with list, detail, road and distance search, map, auto-refresh and offline handling.

## Running the project
1. Open the project in Android Studio (File → Open, or Get from VCS with this repository's URL).
2. Add these lines to `local.properties` in the project root (not committed, as they hold credentials):
       TS_CLIENT_ID=your_traffic_scotland_client_id
       TS_CLIENT_KEY=your_traffic_scotland_client_key
       MAPS_API_KEY=your_google_maps_android_key
3. Sync Gradle and run on an emulator or device with Google Play (Android 8.0+).

Without the keys the project still builds: the feed shows "Can't load traffic data" and the map is grey.
