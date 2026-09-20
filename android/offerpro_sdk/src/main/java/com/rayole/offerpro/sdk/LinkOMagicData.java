package com.rayole.offerpro.sdk;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class LinkOMagicData {

    private final int id;
    private final String name;
    private final String description;
    private final String offerImage;
    private final String estTime;
    private final double rewardCoins;
    private final String source;
    private final String offerLink;
    private final String directOfferLink;

    public LinkOMagicData(
            int id,
            String name,
            String description,
            String offerImage,
            String estTime,
            double rewardCoins,
            String source,
            String offerLink,
            String directOfferLink
    ) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.offerImage = offerImage;
        this.estTime = estTime;
        this.rewardCoins = rewardCoins;
        this.source = source;
        this.offerLink = offerLink;
        this.directOfferLink = directOfferLink;
    }

    // ---------------- Getters ----------------

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getOfferImage() { return offerImage; }
    public String getEstTime() { return estTime; }
    public double getRewardCoins() { return rewardCoins; }
    public String getSource() { return source; }
    public String getOfferLink() { return offerLink; }
    public String getDirectOfferLink() { return directOfferLink; }

    // ---------------- JSON helpers ----------------

    public static LinkOMagicData fromJson(JSONObject json) throws JSONException {
        return new LinkOMagicData(
                json.optInt("id", 0),
                json.optString("name", ""),
                json.optString("description", ""),
                json.optString("offer_image", ""),
                json.optString("est_time", ""),
                json.optDouble("reward_coins", 0.0),
                json.optString("source", ""),
                json.optString("offer_link", ""),
                json.optString("direct_offer_link", "")
        );
    }

    /**
     * Converts to Map<String, Object> for Flutter
     */
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("id", id);
        map.put("name", name);
        map.put("description", description);
        map.put("offer_image", offerImage);
        map.put("est_time", estTime);
        map.put("reward_coins", rewardCoins);
        map.put("source", source);
        map.put("offer_link", offerLink);
        map.put("direct_offer_link", directOfferLink);
        return map;
    }
}
