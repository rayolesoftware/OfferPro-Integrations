import 'package:flutter/material.dart';
import 'package:offerpro_launcher/offerpro_launcher.dart';

void main() {
  WidgetsFlutterBinding.ensureInitialized(); // 👈 add this
  runApp(const MyApp());
}

class MyApp extends StatefulWidget {
  const MyApp({super.key});

  @override
  State<MyApp> createState() => _MyAppState();
}

class _MyAppState extends State<MyApp> {
  MegaOffer? _mega;
  bool _loadingMega = false;
  String? _error;

  @override
  void initState() {
    super.initState();
    WidgetsBinding.instance.addPostFrameCallback((_) async {
      debugPrint('>>> initState: calling offerProInit');
      await offerProInit();
    });
  }

  Future<void> offerProInit() async {
    try {
      await OfferProLauncher.initialize(
        userEmail: const String.fromEnvironment('OFFERPRO_EMAIL'),
        userId: const String.fromEnvironment('OFFERPRO_USER_ID'),
        appId: const int.fromEnvironment('OFFERPRO_APP_ID'),
        advertisingId: const String.fromEnvironment('OFFERPRO_ADVERTISING_ID'),
        userCountry: "IN",
        encKey: const String.fromEnvironment('OFFERPRO_ENC_KEY')
      );
      debugPrint('>>> OfferPro initialized');
    } catch (e, st) {
      debugPrint('>>> ERROR in OfferProLauncher.initialize: $e');
      debugPrint('$st');
      if (mounted) {
        setState(() {
          _error = 'Init error: $e';
        });
      }
      // DO NOT rethrow.
    }
  }

  Future<void> _fetchMega() async {
    setState(() {
      _loadingMega = true;
      _error = null;
    });
    try {
      debugPrint('>>> Fetching mega offer from SDK...');
      final mega = await OfferProLauncher.fetchMegaOffers();
      setState(() {
        _mega = mega;
      });
      debugPrint(">>> mega offer -> ${mega?.toJson()}");
    } catch (e) {
      setState(() {
        _error = e.toString();
      });
      debugPrint('>>> ERROR fetching mega offer: $e');
    } finally {
      setState(() {
        _loadingMega = false;
      });
    }
  }

  Future<void> _launchMega() async {
    if (_mega == null) {
      ScaffoldMessenger.of(
        context,
      ).showSnackBar(const SnackBar(content: Text('No MegaOffer loaded yet')));
      return;
    }
    await OfferProLauncher.showMegaOffer(_mega!.directOfferLink);
  }

  @override
  Widget build(BuildContext context) {
    debugPrint('>>> BUILD called');
    return MaterialApp(
      home: Scaffold(
        appBar: AppBar(title: const Text('OfferPro Launcher Test')),
        body: Center(
          child: Column(
            mainAxisAlignment: MainAxisAlignment.center,
            children: [
              ElevatedButton(
                onPressed: () async {
                  debugPrint('>>> Launch OfferPro pressed');
                  await OfferProLauncher.showOfferPro();
                },
                child: const Text('Launch OfferPro'),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _loadingMega ? null : _fetchMega,
                child: Text(
                  _loadingMega ? 'Fetching MegaOffer...' : 'Fetch MegaOffer',
                ),
              ),
              const SizedBox(height: 20),
              ElevatedButton(
                onPressed: _launchMega,
                child: const Text('Launch MegaOffer'),
              ),
              const SizedBox(height: 20),
              if (_mega != null) ...[
                Text('Loaded MegaOffer: ${_mega!.name}'),
                Text(_mega!.directOfferLink, textAlign: TextAlign.center),
              ],
              if (_error != null) ...[
                const SizedBox(height: 10),
                Text(
                  'Error: $_error',
                  style: const TextStyle(color: Colors.red),
                  textAlign: TextAlign.center,
                ),
              ],
            ],
          ),
        ),
      ),
    );
  }
}
