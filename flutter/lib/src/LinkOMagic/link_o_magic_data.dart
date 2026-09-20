class LinkOMagicOfferData {
  final int id;
  final String name;
  final String description;
  final String offerImage;
  final String estTime;
  final double rewardCoins;
  final String directOfferLink;

  LinkOMagicOfferData({
    required this.id,
    required this.name,
    required this.description,
    required this.offerImage,
    required this.estTime,
    required this.rewardCoins,
    required this.directOfferLink,
  });

  factory LinkOMagicOfferData.fromJson(Map<String, dynamic> json) {
    return LinkOMagicOfferData(
      id: (json['id'] ?? 0) as int,
      name: (json['name'] ?? '') as String,
      description: (json['description'] ?? '') as String,
      offerImage: (json['offer_image'] ?? '') as String,
      estTime: (json['est_time'] ?? '') as String,
      rewardCoins: double.tryParse((json['reward_coins'] ?? 0).toString()) ?? 0.0,
      directOfferLink: (json['direct_offer_link'] ?? '') as String,
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'description': description,
      'offer_image': offerImage,
      'est_time': estTime,
      'reward_coins': rewardCoins,
      'direct_offer_link': directOfferLink,
    };
  }
}
