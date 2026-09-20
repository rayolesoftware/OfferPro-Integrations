import 'package:flutter/services.dart';
import 'package:flutter_test/flutter_test.dart';
import 'package:offerpro_launcher/offerpro_launcher.dart';

void main() {
  TestWidgetsFlutterBinding.ensureInitialized();
  const channel = MethodChannel('offerpro_sdk');
  final calls = <MethodCall>[];
  setUp(() {
    calls.clear();
    TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
        .setMockMethodCallHandler(channel, (call) async {
      calls.add(call);
      if (call.method == 'getUsageTimeMs') return 1200;
      return null;
    });
  });
  tearDown(() => TestDefaultBinaryMessengerBinding.instance.defaultBinaryMessenger
      .setMockMethodCallHandler(channel, null));
  test('initialization forwards the required advertising ID and integer app ID', () async {
    await OfferProLauncher.initialize(userEmail: 'test@example.com', userId: 'test',
      appId: 42, userCountry: 'IN', encKey: '12345678901234567890123456789012',
      advertisingId: 'test-advertising-id');
    expect(calls.single.method, 'initialize');
    expect(calls.single.arguments['appId'], 42);
    expect(calls.single.arguments['advertisingId'], 'test-advertising-id');
  });
  test('usage interval crosses the channel without losing milliseconds', () async {
    const from = 1750000000000;
    expect(await OfferProLauncher.getUsageTimeMs('com.example.app', from, from + 2000), 1200);
    expect(calls.single.arguments, {'packageName': 'com.example.app', 'fromMs': from, 'toMs': from + 2000});
  });
}
