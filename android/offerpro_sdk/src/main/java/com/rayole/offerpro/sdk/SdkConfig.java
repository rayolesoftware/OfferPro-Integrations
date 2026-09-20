// android/offerpro_sdk/src/main/java/com/offerpro/sdk/SdkConfig.java
package com.rayole.offerpro.sdk;

public final class SdkConfig {
    public final String deviceId;
    public final String advertisingId;
    public final String userEmail;
    public final String userId;
    public final int appId;
    public final String userCountry;
    public final String encKey;     // secret – used only to encrypt

    private SdkConfig(Builder b) {
        this.deviceId = b.deviceId;
        this.advertisingId = b.advertisingId;
        this.userEmail = b.userEmail;
        this.userId = b.userId;
        this.appId = b.appId;
        this.userCountry = b.userCountry;
        this.encKey = b.encKey;
    }

    public static class Builder {
        private String deviceId;
        private String advertisingId;
        private String userEmail;
        private String userId;
        private int appId;
        private String userCountry;
        private String encKey;

        public Builder deviceId(String v) { this.deviceId = v; return this; }
        public Builder advertisingId(String v) { this.advertisingId = v; return this; }
        public Builder userEmail(String v) { this.userEmail = v; return this; }
        public Builder userId(String v) { this.userId = v; return this; }
        public Builder appId(int v) { this.appId = v; return this; }
        public Builder userCountry(String v) { this.userCountry = v; return this; }
        public Builder encKey(String v) { this.encKey = v; return this; }

        public SdkConfig build() {
            if (appId <= 0) throw new IllegalArgumentException("appId must be positive");
            if (userId == null || userId.trim().isEmpty()) throw new IllegalArgumentException("userId is required");
            if (userEmail == null || userEmail.trim().isEmpty()) throw new IllegalArgumentException("userEmail is required");
            if (advertisingId == null || advertisingId.trim().isEmpty()) throw new IllegalArgumentException("advertisingId is required by the OfferPro backend");
            if (encKey == null || encKey.getBytes(java.nio.charset.StandardCharsets.UTF_8).length != 32)
                throw new IllegalArgumentException("encKey must contain exactly 32 UTF-8 bytes");
            return new SdkConfig(this);
        }
    }
}
