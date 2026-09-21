# OfferPro publisher integration — 2.0.1

Add the OfferPro offerwall to your Android, Flutter, or React Native app using the packages in the public [OfferPro Integrations repository](https://github.com/rayolesoftware/OfferPro-Integrations). All three integrations support **Android only**.

- [Android](#android)
- [Flutter](#flutter)
- [React Native](#react-native)
- [Optional publisher APIs](#optional-publisher-apis)
- [Troubleshooting](#troubleshooting)

## Before you start

Use your OfferPro App ID and encryption key, together with the signed-in user's details. The examples below use variables that you must replace with values from your app.

| Configuration | Value |
| --- | --- |
| `appId` | Your positive integer OfferPro App ID. |
| `encKey` | Your OfferPro encryption key, exactly 32 UTF-8 bytes. |
| `userId` | A non-empty, stable identifier for the current user. |
| `userEmail` | The current user's non-empty email address. |
| `userCountry` | The user's two-letter country code, for example `IN`. |
| `advertisingId` | The Android advertising ID supplied by your app's identifier/consent flow. Required; the SDK does not collect it for you. |
| `deviceId` | Optional device identifier; omit it if unused. |

Initialize before opening the offerwall or calling other SDK methods. Initialize again when the signed-in user changes and after a fresh app process starts. Do not log the encryption key or user configuration. If an advertising ID is unavailable, defer initialization rather than substitute a fabricated identifier.

| Integration | Minimum Android SDK | Compile SDK |
| --- | --- | --- |
| Native Android | 21 | 35 or newer |
| Flutter | 24 | 35 or newer |
| React Native | 24 | 35 or newer |

Use JDK 17 or newer for Android builds. Keep Android System WebView up to date on devices running the offerwall.

## Android

### 1. Add the SDK

Clone the public repository:

```sh
git clone https://github.com/rayolesoftware/OfferPro-Integrations.git
```

Copy `OfferPro-Integrations/dist/maven` into your Android project as `offerpro-maven`. In the project's `settings.gradle.kts`, add the repository to your existing dependency repositories:

```kotlin
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = uri("$rootDir/offerpro-maven") }
    }
}
```

Add the dependency in `app/build.gradle.kts`:

```kotlin
dependencies {
    implementation("io.offerpro:offerpro-sdk:2.0.1")
}
```

Set `minSdk` to at least 21 and `compileSdk` to at least 35, then sync Gradle. The bundled Maven repository includes dependency metadata so AndroidX WebKit resolves automatically.

If you received only `offerpro-sdk-release.aar`, place it in `app/libs` and use these dependencies instead:

```kotlin
dependencies {
    implementation(files("libs/offerpro-sdk-release.aar"))
    implementation("androidx.webkit:webkit:1.12.1")
}
```

### 2. Initialize

From your application or activity, once the current user's details are available:

```java
import com.rayole.offerpro.sdk.OfferProSdk;
import com.rayole.offerpro.sdk.SdkConfig;

OfferProSdk sdk = OfferProSdk.getInstance();
sdk.initialize(getApplicationContext(), new SdkConfig.Builder()
    .appId(appId)
    .userId(userId)
    .userEmail(userEmail)
    .userCountry(userCountry)
    .advertisingId(advertisingId)
    .encKey(encKey)
    .build());
```

Invalid configuration throws `IllegalArgumentException`; handle it before enabling your offerwall button.

### 3. Open the offerwall

Call this from a foreground activity, for example in your button's click handler. Here `activity` is your current Android `Activity`:

```java
OfferProSdk.getInstance().openWall(activity);
```

The SDK manifest supplies the offerwall activity and Internet permission through manifest merging.

## Flutter

### 1. Install the plugin

Add the plugin directly from GitHub to your `pubspec.yaml`:

```yaml
dependencies:
  flutter:
    sdk: flutter
  offerpro_launcher:
    git:
      url: https://github.com/rayolesoftware/OfferPro-Integrations.git
      ref: main
      path: flutter
```

For a reproducible release, replace `main` with the full commit SHA you have tested and commit your app's `pubspec.lock`. Git must be installed for Flutter to fetch the dependency. See [Dart Git dependencies](https://dart.dev/tools/pub/dependencies#git-packages).

Use a Flutter installation with Dart 3.9.2 or a compatible later Dart 3 release, as required by the package. Set your Android app's `minSdk` to at least 24 and `compileSdk` to at least 35.

```sh
flutter pub get
flutter run
```

Perform a full Android rebuild after adding the plugin; hot reload cannot register its native code. The plugin includes the Android SDK in `android/maven`.

### 2. Initialize and open the offerwall

Run initialization after Flutter has started and the current user's details are available. If initializing before `runApp`, first call `WidgetsFlutterBinding.ensureInitialized()`.

```dart
import 'package:flutter/services.dart';
import 'package:offerpro_launcher/offerpro_launcher.dart';

Future<void> openOfferwall() async {
  try {
    await OfferProLauncher.initialize(
      appId: appId,
      userId: userId,
      userEmail: userEmail,
      userCountry: userCountry,
      advertisingId: advertisingId,
      encKey: encKey,
    );
    await OfferProLauncher.showOfferPro();
  } on PlatformException {
    // Show an appropriate error in your app and allow the user to retry.
  }
}
```

Call `openOfferwall()` from your offerwall button while the app is in the foreground. Do not call the plugin on iOS.

### Centralized Android repositories

The plugin adds its own Maven repository. If your app uses `PREFER_SETTINGS` or `FAIL_ON_PROJECT_REPOS`, use `PREFER_SETTINGS` and register the bundled repository in `android/settings.gradle.kts`:

```kotlin
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url = uri("/absolute/path/to/resolved/offerpro_launcher/android/maven") }
    }
}
```

Replace the placeholder with the resolved Git package directory in your Pub cache (listed for `offerpro_launcher` in `.dart_tool/package_config.json`), followed by `/android/maven`. This path can differ between machines and commits. With standard project repositories, no manual path is needed. Keep any other repositories your app needs.

## React Native

### 1. Install the package

The React Native package lives in the repository's `react-native` subdirectory. Standard npm Git dependencies install the repository root, which currently has no `package.json`, so a direct `npm install github:rayolesoftware/OfferPro-Integrations` will not work.

From your React Native app directory, clone the repository and install its package:

```sh
git clone https://github.com/rayolesoftware/OfferPro-Integrations.git vendor/OfferPro-Integrations
npm install ./vendor/OfferPro-Integrations/react-native
```

Keep this checkout available at the same relative path on development and CI machines. For reproducible releases, check out the full commit SHA you have tested before installing. Direct npm Git installation would require a package at the repository root; a published npm package or package tarball would also avoid the local checkout.

The package is named `@offerpro/react-native`. Autolinking registers its native module; do not also register it manually. Set your Android app's `minSdkVersion` to at least 24 and `compileSdkVersion` to at least 35, then rebuild:

```sh
npx react-native run-android
```

The package declares React Native 0.71+ and React 17+ peer dependencies. It uses the legacy native-module bridge and has been compile-checked with React Native 0.76.9; check compatibility in your app when using other versions or New Architecture interop. Expo apps require a native development build; Expo Go does not include this module. There is no iOS implementation.

### 2. Initialize and open the offerwall

```javascript
import OfferPro from '@offerpro/react-native';

async function openOfferwall() {
  try {
    await OfferPro.initialize({
      appId,
      userId,
      userEmail,
      userCountry,
      advertisingId,
      encKey,
    });
    await OfferPro.showOfferPro();
  } catch (error) {
    // Show an appropriate error in your app and allow the user to retry.
  }
}
```

Call `openOfferwall()` from a button while the Android app is in the foreground. `appId` must be a number; the remaining supplied fields are strings. Await initialization before launching the wall.

### Centralized Android repositories

The package adds its own Maven repository. If your app centralizes dependency repositories, use `PREFER_SETTINGS` and add the following to your existing `android/settings.gradle` configuration:

```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
        maven { url uri('../node_modules/@offerpro/react-native/android/maven') }
    }
}
```

Keep any other repositories your app needs, and adjust the path for a different workspace layout.

## Optional publisher APIs

The standard offerwall integration only needs initialization and launch. If your app provides custom offer entry points, these methods are also available after initialization:

| Action | Android (`sdk`) | Flutter (`OfferProLauncher`) | React Native (`OfferPro`) |
| --- | --- | --- | --- |
| Fetch a Mega Offer | `fetchMegaOffer(callback)` | `fetchMegaOffers()` | `fetchMegaOffer()` |
| Open a Mega Offer URL | `openMegaWall(activity, url)` | `showMegaOffer(url)` | `showMegaOffer(url)` |

Flutter methods return Futures and React Native methods return Promises. Offer fetches may return `null` when no offer is available or a request fails. Open the URL returned by the offer; external offers may launch a browser or another app.

Installation checks, usage validation, and Usage Access prompts are handled internally by the offerwall SDK. They are not exposed as publisher APIs.

## Troubleshooting

- **SDK dependency cannot be found:** confirm the entire bundled Maven directory is present and its path is registered in your app's active dependency repositories.
- **Flutter or React Native module is missing:** rebuild and reinstall the Android app after installing the package.
- **Initialization fails:** check the positive integer App ID, required user fields, advertising ID, and encryption key length of exactly 32 UTF-8 bytes.
- **Offerwall will not open:** await initialization and launch from a foreground activity. Initialize again after an app process restart.
- **WebView update message:** update Android System WebView on the device before retrying.
- **Device blocked:** VPN, root, emulator, ADB, or developer-options checks can block launch. Test on a supported physical device with these disabled.
