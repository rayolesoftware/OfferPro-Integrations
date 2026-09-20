class MegaOffer {
  final int id;
  final String name;
  final String imageUrl;
  final double rewardCoins;
  final String taskTypeName;
  final String directOfferLink;

  MegaOffer({
    required this.id,
    required this.name,
    required this.imageUrl,
    required this.rewardCoins,
    required this.taskTypeName,
    required this.directOfferLink,
  });

  factory MegaOffer.fromJson(Map<String, dynamic> json) {
    return MegaOffer(
      id: json['id'],
      name: json['name'] ?? '',
      imageUrl: json['offer_image'] ?? '',
      rewardCoins: (json['reward_coins'] ?? 0).toDouble(),
      taskTypeName: json['task_type']?['name'] ?? '',
      directOfferLink: json['direct_offer_link'] ?? '',
    );
  }

  Map<String, dynamic> toJson() {
    return {
      'id': id,
      'name': name,
      'offer_image': imageUrl,
      'reward_coins': rewardCoins,
      'task_type': {
        'name': taskTypeName,
      },
      'direct_offer_link': directOfferLink,
    };
  }
}
