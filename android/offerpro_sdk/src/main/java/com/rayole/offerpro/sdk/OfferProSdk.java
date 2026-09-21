package com.rayole.offerpro.sdk;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Single native integration surface used by Android, Flutter, and React Native. */
public final class OfferProSdk {
    public static final String SDK_VERSION = "2.0.1";
    public static final String DEFAULT_WALL_URL = "https://sdk.offerpro.io";
    private static final String API_URL = "https://server.offerpro.io/api";
    private static final ExecutorService IO = Executors.newFixedThreadPool(2);
    private static volatile OfferProSdk instance;
    private Context appContext;
    private volatile SdkConfig config;

    private OfferProSdk() {}
    public static OfferProSdk getInstance() {
        if (instance == null) synchronized (OfferProSdk.class) {
            if (instance == null) instance = new OfferProSdk();
        }
        return instance;
    }
    public synchronized void initialize(Context context, SdkConfig sdkConfig) {
        if (context == null || sdkConfig == null) throw new IllegalArgumentException("context and config are required");
        appContext = context.getApplicationContext(); config = sdkConfig;
    }
    public boolean isInitialized() { return appContext != null && config != null; }
    private void requireInitialized() {
        if (!isInitialized()) throw new IllegalStateException("OfferProSdk.initialize() must be called first");
    }
    public SdkConfig getConfig() { requireInitialized(); return config; }

    public void openWall(Activity activity) {
        requireInitialized();
        try {
            SdkConfig snapshot = getConfig();
            String encrypted = Encryptor.encryptData(userPayload(snapshot), snapshot.encKey);
            openUrl(activity, DEFAULT_WALL_URL + "?enc=" + Uri.encode(encrypted) + "&app_id=" + snapshot.appId);
        } catch (Exception e) { throw new IllegalStateException("Unable to create OfferPro wall URL", e); }
    }
    private void openUrl(Activity activity, String url) {
        requireInitialized();
        if (url == null || !"https".equals(Uri.parse(url).getScheme()) || Uri.parse(url).getHost() == null)
            throw new IllegalArgumentException("A valid HTTPS URL is required");
        IO.execute(() -> {
            java.util.List<String> reasons = DeviceIntegrity.getBlockedReasons(appContext);
            String target = url;
            if (!reasons.isEmpty()) {
                // Always route a blocked device through the trusted wall's existing BlockGate.
                target = Uri.parse(DEFAULT_WALL_URL).buildUpon()
                        .appendQueryParameter("blocked", reasons.get(0)).build().toString();
            }
            final String launch = target;
            activity.runOnUiThread(() -> {
                if (activity.isFinishing() || activity.isDestroyed()) return;
                if (!MainActivity.trusted(launch)) {
                    try { activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(launch))); }
                    catch (android.content.ActivityNotFoundException e) {
                        android.widget.Toast.makeText(activity, "No browser available to open this offer", android.widget.Toast.LENGTH_SHORT).show();
                    }
                    return;
                }
                Intent intent = new Intent(activity, MainActivity.class);
                intent.putExtra(MainActivity.EXTRA_START_URL, launch); activity.startActivity(intent);
            });
        });
    }

    private boolean hasUsageAccess() { requireInitialized(); return AppUsageUtils.hasUsageAccess(appContext); }
    private void openUsageAccessSettings() { requireInitialized(); AppUsageUtils.openUsageAccessSettings(appContext); }
    private long getUsageTimeMs(String packageName, long fromMs, long toMs) {
        requireInitialized(); return AppUsageUtils.getUsageMs(appContext, packageName, fromMs, toMs);
    }
    private boolean isInstalled(String packageName) {
        requireInitialized(); return new ItkrBridge(null, appContext, config.encKey).isInstalled(appContext, packageName);
    }
    private String validateInstall(String packageName) throws Exception {
        requireInitialized(); return new ItkrBridge(null, appContext, config.encKey).validateInstall(packageName);
    }
    private String validateAppUsage(String packageName, long fromMs, long toMs) throws Exception {
        requireInitialized();
        return new ItkrBridge(null, appContext, config.encKey)
                .validateAppUsage(packageName, String.valueOf(fromMs), String.valueOf(toMs));
    }

    public interface MegaOfferCallback { void onResult(MegaOffer offer); }
    public void fetchMegaOffer(MegaOfferCallback callback) { fetchFirst(callback); }
    public void openMegaWall(Activity activity, String url) { openUrl(activity, url); }

    private Map<String, Object> userPayload(SdkConfig config) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("device_id", value(config.deviceId));
        payload.put("advertising_id", value(config.advertisingId));
        payload.put("user_email", value(config.userEmail));
        payload.put("user_id", value(config.userId));
        payload.put("app_id", config.appId);
        payload.put("user_country", value(config.userCountry));
        payload.put("sdk_version", SDK_VERSION); return payload;
    }
    private static String value(String value) { return value == null ? "" : value; }

    private void fetchFirst(MegaOfferCallback callback) {
        requireInitialized();
        final SdkConfig snapshot = getConfig();
        IO.execute(() -> {
            MegaOffer result = null;
            HttpURLConnection connection = null;
            try {
                String encrypted = Encryptor.encryptData(userPayload(snapshot), snapshot.encKey);
                connection = (HttpURLConnection) new URL(
                        API_URL + "/tasks/list_mega_games/?ordering=-cpc&no_pagination=false&page=1").openConnection();
                connection.setRequestMethod("POST"); connection.setConnectTimeout(15_000);
                connection.setReadTimeout(15_000); connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json");
                JSONObject body = new JSONObject(); body.put("enc", encrypted); body.put("app_id", snapshot.appId);
                body.put("device_id", value(snapshot.deviceId));
                try (OutputStream out = connection.getOutputStream();
                     OutputStreamWriter writer = new OutputStreamWriter(out, StandardCharsets.UTF_8)) { writer.write(body.toString()); }
                if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                    Object response = new org.json.JSONTokener(read(connection.getInputStream())).nextValue();
                    JSONArray array = response instanceof JSONArray ? (JSONArray) response : ((JSONObject) response).getJSONArray("results");
                    if (array.length() > 0) {
                        JSONObject item = array.getJSONObject(0);
                        result = MegaOffer.fromJson(item);
                    }
                }
            } catch (Exception ignored) { }
            finally { if (connection != null) connection.disconnect(); }
            MegaOffer finalResult = result;
            new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> callback.onResult(finalResult));
        });
    }
    private static String read(InputStream input) throws Exception {
        StringBuilder value = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
            String line; while ((line = reader.readLine()) != null) value.append(line);
        }
        return value.toString();
    }
}
