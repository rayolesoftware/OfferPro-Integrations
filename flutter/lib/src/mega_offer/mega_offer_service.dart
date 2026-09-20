// import 'dart:convert';
// import 'package:http/http.dart' as http;
// import '../../offerpro_launcher.dart';
//
// Future<MegaOffer?> fetchMegaOffers({
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
//     print("here is mega offer link -> ${userMap}");
//
//     final enc = Encryptor.encryptData(userMap, encKey);
//
//     final url = Uri.parse(
//       'https://server.offerpro.io/api/tasks/list_mega_games/?ordering=-cpc&no_pagination=false&page_size=$limit',
//     );
//
//     print("here is mega offer link -> ${url}");
//     final response = await http.post(
//       url,
//       headers: {'Content-Type': 'application/json'},
//       body: jsonEncode({'enc': enc, 'app_id': userData.appId}),
//     );
//
//     print("here is enc appid -> ${ jsonEncode({'enc': enc, 'app_id': userData.appId})}");
//
//     if (response.statusCode == 200) {
//       final data = jsonDecode(response.body);
//       final results = List<Map<String, dynamic>>.from(data);
//
//       final enrichedResults = results.map((json) {
//         return MegaOffer.fromJson(json);
//       }).toList();
//
//       if (enrichedResults.isEmpty) {
//         return null;
//       }
//
//       return enrichedResults[0];
//     } else {
//       // throw Exception('Failed to load offers: ${response.statusCode}');
//       return null;
//     }
//   } on Exception catch (e) {
//     return null;
//   }
// }
