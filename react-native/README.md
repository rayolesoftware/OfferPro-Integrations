# OfferPro React Native (Android)

```sh
npm install /path/to/Integrations/react-native
```
Autolinking registers `OfferProSdkPackage`; do not also register it manually. Rebuild the Android app after installation. Use minSdk 24 and compileSdk 35+. Expo requires a native development build (not Expo Go).

```js
import OfferPro from '@offerpro/react-native';
await OfferPro.initialize({
  userEmail, userId, appId, userCountry, encKey, advertisingId,
  deviceId: '',
});
await OfferPro.showOfferPro();
const installed = await OfferPro.isInstalled('com.example.app');
const usedMs = await OfferPro.getUsageTimeMs('com.example.app', fromMs, toMs);
```
Advertising ID is required and provided by the host. Usage timestamps are epoch milliseconds. Grant Usage Access through `openUsageAccessSettings()` before reading usage. `validateInstall` and `validateAppUsage` return encrypted backend tokens.

The native module uses the legacy React Native bridge, compile-checked against 0.76.9. Compatibility with other versions/new-architecture interop requires testing in the consuming app. iOS calls reject with a clear unsupported-platform error.

The package's `android/maven` contains SDK 2.0.1. Standard hosts use the plugin's project repository. For centralized repositories use PREFER_SETTINGS and add:
```groovy
maven { url uri('../node_modules/@offerpro/react-native/android/maven') }
```
alongside `google()` and `mavenCentral()` in the host's settings repositories.

See `index.d.ts` for all method types and the [publisher integration guide](../README.md#react-native) for complete setup, optional APIs, and troubleshooting.
