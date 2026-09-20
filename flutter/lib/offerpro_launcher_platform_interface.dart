import 'package:plugin_platform_interface/plugin_platform_interface.dart';

import 'offerpro_launcher_method_channel.dart';

abstract class OfferproLauncherPlatform extends PlatformInterface {
  /// Constructs a OfferproLauncherPlatform.
  OfferproLauncherPlatform() : super(token: _token);

  static final Object _token = Object();

  static OfferproLauncherPlatform _instance = MethodChannelOfferproLauncher();

  /// The default instance of [OfferproLauncherPlatform] to use.
  ///
  /// Defaults to [MethodChannelOfferproLauncher].
  static OfferproLauncherPlatform get instance => _instance;

  /// Platform-specific implementations should set this with their own
  /// platform-specific class that extends [OfferproLauncherPlatform] when
  /// they register themselves.
  static set instance(OfferproLauncherPlatform instance) {
    PlatformInterface.verifyToken(instance, _token);
    _instance = instance;
  }

  Future<String?> getPlatformVersion() {
    throw UnimplementedError('platformVersion() has not been implemented.');
  }
}
