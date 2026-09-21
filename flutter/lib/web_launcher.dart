import 'package:flutter/services.dart';
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

  static Future<MegaOffer?> fetchMegaOffers() async {
    final value = await _channel.invokeMethod<dynamic>('fetchMegaOffer');
    return value == null ? null : MegaOffer.fromJson(Map<String, dynamic>.from(value));
  }

  static Future<void> showMegaOffer(String url) => _channel.invokeMethod('openMegaWall', {'url': url});
}
