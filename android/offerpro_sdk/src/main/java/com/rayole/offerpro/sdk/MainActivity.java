package com.rayole.offerpro.sdk;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.webkit.*;
import android.widget.FrameLayout;
import android.widget.Toast;
import androidx.webkit.WebViewCompat;
import androidx.webkit.WebViewFeature;
import java.util.Collections;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.json.JSONArray;
import org.json.JSONObject;

/** Safe-area wall with an origin-checked, main-frame-only asynchronous bridge. */
public final class MainActivity extends Activity {
    public static final String EXTRA_START_URL = "offerpro_start_url";
    private static final int PICK_FILE = 7041;
    private WebView webView;
    private ValueCallback<Uri[]> fileCallback;
    private final ExecutorService bridgeWorker = Executors.newSingleThreadExecutor();
    private final java.util.List<WebView> popups = new java.util.ArrayList<>();
    private ItkrBridge bridge;
    private static final String ORIGIN = "https://sdk.offerpro.io";

    static boolean trusted(String url) {
        if (url == null) return false;
        Uri u = Uri.parse(url);
        return "https".equals(u.getScheme()) && "sdk.offerpro.io".equals(u.getHost())
                && (u.getPort() == -1 || u.getPort() == 443) && u.getUserInfo() == null;
    }

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        // Following process death the publisher must initialize again. Never persist encryption keys.
        if (!OfferProSdk.getInstance().isInitialized()) { finish(); return; }
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        if (Build.VERSION.SDK_INT >= 29) getWindow().setNavigationBarContrastEnforced(false);
        FrameLayout root = new FrameLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setOnApplyWindowInsetsListener((v, insets) -> {
            int l, t, r, b;
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                l = safe.left; t = safe.top; r = safe.right; b = safe.bottom;
            } else {
                l = insets.getSystemWindowInsetLeft(); t = insets.getSystemWindowInsetTop();
                r = insets.getSystemWindowInsetRight(); b = insets.getSystemWindowInsetBottom();
                if (Build.VERSION.SDK_INT >= 28 && insets.getDisplayCutout() != null) {
                    android.view.DisplayCutout c = insets.getDisplayCutout();
                    l = Math.max(l, c.getSafeInsetLeft()); t = Math.max(t, c.getSafeInsetTop());
                    r = Math.max(r, c.getSafeInsetRight()); b = Math.max(b, c.getSafeInsetBottom());
                }
            }
            root.setPadding(l, t, r, b);
            return insets.consumeSystemWindowInsets();
        });
        webView = new WebView(this);
        webView.setBackgroundColor(Color.BLACK);
        root.addView(webView, new FrameLayout.LayoutParams(-1, -1));
        setContentView(root);
        root.requestApplyInsets();
        WebViewUtils.applySecureDefaults(webView);
        CookieManager.getInstance().setAcceptCookie(true);
        CookieManager.getInstance().setAcceptThirdPartyCookies(webView, true);
        bridge = new ItkrBridge(this, OfferProSdk.getInstance().getConfig().encKey);
        if (!WebViewFeature.isFeatureSupported(WebViewFeature.WEB_MESSAGE_LISTENER)
                || !WebViewFeature.isFeatureSupported(WebViewFeature.DOCUMENT_START_SCRIPT)) {
            Toast.makeText(this, "Update Android System WebView to open OfferPro", Toast.LENGTH_LONG).show();
            finish(); return;
        }
        WebViewCompat.addWebMessageListener(webView, "offerProNative", Collections.singleton(ORIGIN),
                (view, message, origin, mainFrame, reply) -> {
                    if (!mainFrame || !trusted(origin.toString())) return;
                    try {
                        JSONObject request = new JSONObject(message.getData());
                        String method = request.getString("method");
                        int id = request.getInt("id");
                        JSONArray args = request.optJSONArray("args");
                        if ("closeOfferWall".equals(method) || "endReached".equals(method)) { finish(); return; }
                        if ("openUsageAccessSettings".equals(method)) {
                            bridge.openUsageAccessSettings();
                            reply.postMessage(new JSONObject().put("id", id).put("value", true).toString());
                            return;
                        }
                        bridgeWorker.execute(() -> {
                            JSONObject result = new JSONObject();
                            try {
                                result.put("id", id);
                                Object value;
                                switch (method) {
                                    case "hasUsageAccess": value = bridge.hasUsageAccess(); break;
                                    case "validateInstall": value = bridge.validateInstall(args.getString(0)); break;
                                    case "validateAppUsage": value = bridge.validateAppUsage(args.getString(0), args.getString(1), args.getString(2)); break;
                                    default: throw new IllegalArgumentException("Unknown bridge method");
                                }
                                result.put("value", value);
                            } catch (Exception e) { try { result.put("error", "Native request failed"); } catch (Exception ignored) {} }
                            runOnUiThread(() -> { if (!isFinishing() && !isDestroyed()) reply.postMessage(result.toString()); });
                        });
                    } catch (Exception ignored) { }
                });
        WebViewCompat.addDocumentStartJavaScript(webView, bridgeScript(), Collections.singleton(ORIGIN));
        webView.setWebViewClient(new WebViewClient() {
            @Override public boolean shouldOverrideUrlLoading(WebView v, String url) { return navigate(url); }
            @Override public boolean shouldOverrideUrlLoading(WebView v, WebResourceRequest r) {
                return r.isForMainFrame() ? navigate(r.getUrl().toString()) : false;
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override public boolean onShowFileChooser(WebView v, ValueCallback<Uri[]> callback, FileChooserParams params) {
                if (!trusted(v.getUrl())) return false;
                if (fileCallback != null) fileCallback.onReceiveValue(null);
                fileCallback = callback;
                try { startActivityForResult(params.createIntent(), PICK_FILE); }
                catch (Exception e) { fileCallback.onReceiveValue(null); fileCallback = null; }
                return true;
            }
            @Override public boolean onCreateWindow(WebView v, boolean dialog, boolean gesture, Message message) {
                if (!gesture) return false;
                WebView popup = new WebView(MainActivity.this);
                popups.add(popup);
                popup.setWebViewClient(new WebViewClient() {
                    @Override public boolean shouldOverrideUrlLoading(WebView p, String url) {
                        if ("about:blank".equals(url)) return false;
                        if (trusted(url)) webView.loadUrl(url); else external(url);
                        p.post(() -> { popups.remove(p); p.destroy(); });
                        return true;
                    }
                });
                ((WebView.WebViewTransport) message.obj).setWebView(popup);
                message.sendToTarget(); return true;
            }
        });
        webView.setDownloadListener((url, ua, disposition, mime, length) -> external(url));
        if (state == null || webView.restoreState(state) == null) {
            String url = getIntent().getStringExtra(EXTRA_START_URL);
            if (!trusted(url)) { if (url != null) external(url); finish(); return; }
            webView.loadUrl(url);
        }
    }

    private boolean navigate(String url) {
        if (trusted(url)) return false;
        external(url); return true;
    }

    private void external(String url) {
        try {
            Uri uri = Uri.parse(url);
            String scheme = uri.getScheme();
            if ("intent".equals(scheme)) {
                Intent parsed = Intent.parseUri(url, Intent.URI_INTENT_SCHEME);
                String fallback = parsed.getStringExtra("browser_fallback_url");
                Uri data = parsed.getData();
                if (data != null && allowedExternal(data.getScheme())) {
                    Intent safe = new Intent(Intent.ACTION_VIEW, data).addCategory(Intent.CATEGORY_BROWSABLE);
                    safe.setPackage(parsed.getPackage());
                    try { startActivity(safe); return; } catch (android.content.ActivityNotFoundException ignored) { }
                }
                if (fallback != null && "https".equals(Uri.parse(fallback).getScheme())) external(fallback);
            } else if (allowedExternal(scheme)) {
                startActivity(new Intent(Intent.ACTION_VIEW, uri).addCategory(Intent.CATEGORY_BROWSABLE));
            }
        } catch (Exception e) { Toast.makeText(this, "No app available to open this link", Toast.LENGTH_SHORT).show(); }
    }

    private static boolean allowedExternal(String s) {
        return "https".equals(s) || "http".equals(s) || "market".equals(s) || "mailto".equals(s) || "tel".equals(s);
    }

    @Override protected void onActivityResult(int request, int result, Intent data) {
        super.onActivityResult(request, result, data);
        if (request == PICK_FILE && fileCallback != null) {
            Uri[] selected = WebChromeClient.FileChooserParams.parseResult(result, data);
            if (selected != null) for (Uri uri : selected) {
                if (!"content".equals(uri.getScheme())) { selected = null; break; }
            }
            fileCallback.onReceiveValue(selected); fileCallback = null;
        }
    }
    @Override protected void onSaveInstanceState(Bundle state) {
        if (webView != null) webView.saveState(state);
        super.onSaveInstanceState(state);
    }
    @Override public void onBackPressed() {
        if (webView == null) { finish(); return; }
        webView.evaluateJavascript("(function(){if(typeof window.onAndroidBackPressed==='function'){window.onAndroidBackPressed();return true;}return false;})()", handled -> {
            if (!"true".equals(handled)) { if (webView.canGoBack()) webView.goBack(); else finish(); }
        });
    }
    @Override protected void onDestroy() {
        if (fileCallback != null) { fileCallback.onReceiveValue(null); fileCallback = null; }
        bridgeWorker.shutdownNow();
        for (WebView popup : popups) popup.destroy();
        popups.clear();
        if (webView != null) { ((ViewGroup) webView.getParent()).removeView(webView); webView.destroy(); }
        super.onDestroy();
    }

    private static String bridgeScript() {
        return "(function(){if(window!==window.top)return;var seq=0,pending={};"
            + "offerProNative.onmessage=function(e){var r=JSON.parse(e.data),p=pending[r.id];if(p){clearTimeout(p.timer);delete pending[r.id];r.error?p.reject(new Error(r.error)):p.resolve(r.value);}};"
            + "function call(method,args){return new Promise(function(resolve,reject){var id=++seq;pending[id]={resolve:resolve,reject:reject,timer:setTimeout(function(){delete pending[id];reject(new Error('Native timeout'));},30000)};offerProNative.postMessage(JSON.stringify({id:id,method:method,args:args}));});}"
            + "window.itkr={validateInstall:function(p){return call('validateInstall',[p]);},validateAppUsage:function(p,f,t){return call('validateAppUsage',[p,String(f),String(t)]);},hasUsageAccess:function(){return call('hasUsageAccess',[]);},openUsageAccessSettings:function(){call('openUsageAccessSettings',[]).catch(function(){});},closeOfferWall:function(){offerProNative.postMessage(JSON.stringify({id:0,method:'closeOfferWall'}));},endReached:function(){offerProNative.postMessage(JSON.stringify({id:0,method:'endReached'}));}};window.itkrNav=window.itkr;})();";
    }
}
