import 'package:flutter/services.dart';
import 'src/LinkOMagic/link_o_magic_data.dart';
import 'src/mega_offer/mega_offer_model.dart';

/// Flutter facade over the native OfferProSdk AAR.
class OfferProLauncher {
  static const MethodChannel _channel = MethodChannel('offerpro_sdk');

  static Future<void> initialize({
    required String userEmail,
    required String userId,
    required int appId,
    required String userCountry,
    required String encKey,
    String deviceId = '',
    required String advertisingId,
  }) => _channel.invokeMethod('initialize', {
        'userEmail': userEmail,
        'userId': userId,
        'appId': appId,
        'userCountry': userCountry,
        'encKey': encKey,
        'deviceId': deviceId,
        'advertisingId': advertisingId,
      });

  static Future<void> showOfferPro() => _channel.invokeMethod('openWall');
  static Future<void> openUrl(String url) => _channel.invokeMethod('openUrl', {'url': url});

  static Future<MegaOffer?> fetchMegaOffers() async {
    final value = await _channel.invokeMethod<dynamic>('fetchMegaOffer');
    return value == null ? null : MegaOffer.fromJson(Map<String, dynamic>.from(value));
  }

  static Future<void> showMegaOffer(String url) => _channel.invokeMethod('openMegaWall', {'url': url});

  static Future<LinkOMagicOfferData?> fetchLinkOMagic() async {
    final value = await _channel.invokeMethod<dynamic>('fetchLinkOMagic');
    return value == null ? null : LinkOMagicOfferData.fromJson(Map<String, dynamic>.from(value));
  }

  static Future<void> showLinkOMagic(String url) => _channel.invokeMethod('showLinkOMagic', {'url': url});
  static Future<bool> hasUsageAccess() => _channel.invokeMethod<bool>('hasUsageAccess').then((v) => v ?? false);
  static Future<void> openUsageAccessSettings() => _channel.invokeMethod('openUsageAccessSettings');
  static Future<int> getUsageTimeMs(String packageName, int fromMs, int toMs) async =>
      (await _channel.invokeMethod<num>('getUsageTimeMs', {
        'packageName': packageName, 'fromMs': fromMs, 'toMs': toMs,
      }))?.toInt() ?? 0;
  static Future<bool> isInstalled(String packageName) => _channel.invokeMethod<bool>('isInstalled', {'packageName': packageName}).then((v) => v ?? false);
  static Future<String> validateInstall(String packageName) => _channel.invokeMethod<String>('validateInstall', {'packageName': packageName}).then((v) => v ?? '');
  static Future<String> validateAppUsage(String packageName, int fromMs, int toMs) => _channel.invokeMethod<String>('validateAppUsage', {
    'packageName': packageName, 'fromMs': fromMs, 'toMs': toMs,
  }).then((v) => v ?? '');
}
