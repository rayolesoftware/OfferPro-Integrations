# OfferPro React Native (Android)

The package is in a subdirectory, so npm cannot install it directly from the repository's Git URL. From your app directory:

```sh
git clone https://github.com/rayolesoftware/OfferPro-Integrations.git vendor/OfferPro-Integrations
npm install ./vendor/OfferPro-Integrations/react-native
```

Keep the checkout at the same relative path on development and CI machines. Pin the checkout to a tested commit for releases.
Autolinking registers `OfferProSdkPackage`; do not also register it manually. Rebuild the Android app after installation. Use minSdk 24 and compileSdk 35+. Expo requires a native development build (not Expo Go).

```js
import OfferPro from '@offerpro/react-native';
await OfferPro.initialize({
  userEmail, userId, appId, userCountry, encKey, advertisingId,
  deviceId: '',
});
await OfferPro.showOfferPro();
```
Advertising ID is required and provided by the host. Other publisher methods are `fetchMegaOffer` and `showMegaOffer`. Installation and usage validation are handled internally by the SDK.

The native module uses the legacy React Native bridge, compile-checked against 0.76.9. Compatibility with other versions/new-architecture interop requires testing in the consuming app. iOS calls reject with a clear unsupported-platform error.

The package's `android/maven` contains SDK 2.0.1. Standard hosts use the plugin's project repository. For centralized repositories use PREFER_SETTINGS and add:
```groovy
maven { url uri('../node_modules/@offerpro/react-native/android/maven') }
```
alongside `google()` and `mavenCentral()` in the host's settings repositories.

See `index.d.ts` for all method types and the [publisher integration guide](../README.md#react-native) for complete setup, optional APIs, and troubleshooting.
