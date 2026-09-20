# OfferPro Flutter plugin (Android)

Add the local package:
```yaml
dependencies:
  offerpro_launcher:
    path: /path/to/Integrations/flutter
```
Run `flutter pub get` and rebuild the Android app. Use minSdk 24 and compileSdk 35 or newer. The plugin contains a local Maven repository with SDK 2.0.1 and its dependency metadata.

```dart
import 'package:offerpro_launcher/offerpro_launcher.dart';
await OfferProLauncher.initialize(
  userEmail: email, userId: userId, appId: appId, userCountry: country,
  encKey: encryptionKey, advertisingId: advertisingId,
);
await OfferProLauncher.showOfferPro();
```
The host must provide its advertising ID; it is required by the backend. `deviceId` is optional. `appId` is an integer. Invalid configuration and native errors produce PlatformException.

Other methods: `fetchMegaOffers`, `showMegaOffer`, `fetchLinkOMagic`, `showLinkOMagic`, `openUrl`, `isInstalled`, `validateInstall`, `hasUsageAccess`, `openUsageAccessSettings`, `getUsageTimeMs`, `validateAppUsage`. Usage timestamps are epoch milliseconds. Verification methods return backend-compatible encrypted strings.

Standard Flutter hosts use project repositories, which the plugin configures. If your host centralizes repositories using PREFER_SETTINGS or FAIL_ON_PROJECT_REPOS, register the plugin's `android/maven` directory in the host settings and use PREFER_SETTINGS:
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google(); mavenCentral()
        maven { url uri('/absolute/path/to/offerpro_launcher/android/maven') }
    }
}
```
See the [publisher integration guide](../README.md#flutter) for complete setup, optional APIs, and troubleshooting. This plugin has no iOS implementation.
