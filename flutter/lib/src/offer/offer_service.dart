// import 'dart:convert';
// import 'package:http/http.dart' as http;
// import '../../offerpro_launcher.dart';
//
// Future<List<Offer>> fetchOffers({
//   required UserData userData,
//   required String encKey,
//   int limit = 10,
// }) async {
//   try {
//     final deviceId = await getDeviceUniqueID();
//     final adId = await getGoogleAdID();
//
//     final userMap = userData.toMap()
//       ..['device_id'] = deviceId
//       ..['advertising_id'] = adId;
//
//     final enc = Encryptor.encryptData(userMap, encKey);
//
//     final url = Uri.parse(
//       'https://server.offerpro.io/api/tasks/list_tasks/?ordering=-cpc&no_pagination=false&page_size=$limit',
//     );
//
//     final response = await http.post(
//       url,
//       headers: {'Content-Type': 'application/json'},
//       body: jsonEncode({'enc': enc, 'app_id': userData.appId}),
//     );
//
//     if (response.statusCode == 200) {
//       final data = jsonDecode(response.body);
//       final results = List<Map<String, dynamic>>.from(data['results']);
//       return results.map((json) => Offer.fromJson(json)).toList();
//     } else {
//       // throw Exception('Failed to load offers: ${response.statusCode}');
//       return [];
//     }
//   } on Exception catch (e) {
//     // print('Error fetching offers: $e');
//     return [];
//   }
// }
