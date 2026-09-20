// class Offer {
//   final int id;
//   final String name;
//   final String imageUrl;
//   final double rewardCoins;
//   final String taskTypeName;
//
//   Offer({
//     required this.id,
//     required this.name,
//     required this.imageUrl,
//     required this.rewardCoins,
//     required this.taskTypeName,
//   });
//
//   factory Offer.fromJson(Map<String, dynamic> json) {
//     return Offer(
//       id: json['id'],
//       name: json['name'] ?? '',
//       imageUrl: json['offer_image'] ?? '',
//       rewardCoins: (json['reward_coins'] ?? 0).toDouble(),
//       taskTypeName: json['task_type']?['name'] ?? '',
//     );
//   }
// }
