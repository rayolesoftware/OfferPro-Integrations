package com.offerpro.reactnative;

import android.app.Activity;
import android.content.Context;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.rayole.offerpro.sdk.MegaOffer;
import com.rayole.offerpro.sdk.OfferProSdk;
import com.rayole.offerpro.sdk.SdkConfig;

public final class OfferProSdkModule extends ReactContextBaseJavaModule {
    public OfferProSdkModule(ReactApplicationContext context) { super(context); }
    @Override public String getName() { return "OfferProSdk"; }

    private Activity activity() { return getCurrentActivity(); }

    @ReactMethod public void initialize(ReadableMap args, Promise promise) {
        try {
            SdkConfig config = new SdkConfig.Builder()
                    .deviceId(args.hasKey("deviceId") ? args.getString("deviceId") : "")
                    .advertisingId(args.getString("advertisingId"))
                    .userEmail(args.getString("userEmail"))
                    .userId(args.getString("userId"))
                    .appId(args.getInt("appId"))
                    .userCountry(args.getString("userCountry"))
                    .encKey(args.getString("encKey"))
                    .build();
            OfferProSdk.getInstance().initialize(getReactApplicationContext(), config); promise.resolve(null);
        } catch (Throwable t) { promise.reject("INIT_FAIL", t); }
    }
    @ReactMethod public void openWall(Promise promise) { runActivity(promise, a -> OfferProSdk.getInstance().openWall(a)); }
    @ReactMethod public void openMegaWall(String url, Promise promise) { runActivity(promise, a -> OfferProSdk.getInstance().openMegaWall(a, url)); }

    @ReactMethod public void fetchMegaOffer(Promise p) { try { OfferProSdk.getInstance().fetchMegaOffer(o -> p.resolve(o == null ? null : offer(o))); } catch (Throwable t) { p.reject("MEGA_FAIL", t); } }
    private WritableMap offer(MegaOffer o) {
        WritableMap map = Arguments.createMap();
        map.putInt("id", o.getId()); map.putString("name", o.getName());
        map.putString("offer_image", o.getImageUrl()); map.putDouble("reward_coins", o.getRewardCoins());
        WritableMap type = Arguments.createMap(); type.putString("name", o.getTaskTypeName());
        map.putMap("task_type", type); map.putString("direct_offer_link", o.getDirectOfferLink()); return map;
    }
    private interface ActivityAction { void run(Activity activity); }
    private void runActivity(Promise p, ActivityAction action) {
        getReactApplicationContext().runOnUiQueueThread(() -> {
            Activity a = activity();
            if (a == null || a.isFinishing()) { p.reject("NO_ACTIVITY", "No foreground Activity"); return; }
            try { action.run(a); p.resolve(null); } catch (Exception t) { p.reject("SDK_ERROR", t); }
        });
    }
}
