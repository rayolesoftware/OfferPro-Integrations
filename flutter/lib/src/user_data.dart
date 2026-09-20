class UserData {
  final String userEmail;
  final String userId;
  final int appId;
  final String userCountry;

  UserData({
    required this.userEmail,
    required this.userId,
    required this.appId,
    required this.userCountry,
  });

  /// Converts UserData to a Map<String, dynamic> for encryption
  Map<String, dynamic> toMap() {
    return {
      "user_email": userEmail,
      "user_id": userId,
      "app_id": appId,
      "user_country": userCountry,
    };
  }

  /// Factory method to create UserData from a Map<String, dynamic>
  factory UserData.fromMap(Map<String, dynamic> map) {
    return UserData(
      userEmail: map["user_email"] ?? "",
      userId: map["user_id"] ?? "",
      appId: map["app_id"] ?? 0,
      userCountry: map["user_country"] ?? "",
    );
  }
}
