# OfferPro SDK test app

Open this directory in Android Studio, select `native-host`, and run it on Android.

Enter App ID, the 32-byte encryption key, user ID, email, two-letter country code,
advertising ID, and optionally device ID. Tap **Initialize SDK**, then **Launch
Offerwall**. Editing any field disables launching until initialization succeeds again.
The app displays initialization errors without logging credentials. Credentials
are held in memory only, not saved to preferences or Activity state.

The SDK's integrity checks remain enabled: use a physical device, and disable
ADB/developer options and VPN before launching the wall if they trigger a block.
You can install the APK, disconnect debugging, and launch the test app manually.

Build with JDK 17+ and ANDROID_HOME configured:

```sh
../android/gradlew :native-host:assembleDebug
```

APK: `native-host/build/outputs/apk/debug/native-host-debug.apk`.

The sibling Flutter and React Native modules in this project are native compile
checks; the runnable test app exercises the shared Android SDK directly.
