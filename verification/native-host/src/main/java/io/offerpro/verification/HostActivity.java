package io.offerpro.verification;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowInsets;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.rayole.offerpro.sdk.OfferProSdk;
import com.rayole.offerpro.sdk.SdkConfig;

public final class HostActivity extends Activity {
    private EditText appId, encryptionKey, userId, email, country, advertisingId, deviceId;
    private Button launch;
    private TextView status;
    private boolean ready;
    private final OfferProSdk sdk = OfferProSdk.getInstance();

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        getWindow().setStatusBarColor(Color.BLACK);
        getWindow().setNavigationBarColor(Color.BLACK);
        if (Build.VERSION.SDK_INT >= 30) getWindow().setDecorFitsSystemWindows(false);
        else getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);

        LinearLayout root = new LinearLayout(this);
        root.setBackgroundColor(Color.BLACK);
        root.setOnApplyWindowInsetsListener((view, insets) -> {
            if (Build.VERSION.SDK_INT >= 30) {
                android.graphics.Insets safe = insets.getInsets(WindowInsets.Type.systemBars()
                        | WindowInsets.Type.displayCutout() | WindowInsets.Type.ime());
                root.setPadding(safe.left, safe.top, safe.right, safe.bottom);
            } else {
                root.setPadding(insets.getSystemWindowInsetLeft(), insets.getSystemWindowInsetTop(),
                        insets.getSystemWindowInsetRight(), insets.getSystemWindowInsetBottom());
            }
            return insets.consumeSystemWindowInsets();
        });
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        LinearLayout form = new LinearLayout(this);
        form.setOrientation(LinearLayout.VERTICAL);
        form.setPadding(dp(24), dp(24), dp(24), dp(32));
        scroll.addView(form);
        root.addView(scroll, new LinearLayout.LayoutParams(-1, -1));
        setContentView(root);
        root.requestApplyInsets();

        label(form, "OfferPro SDK Tester", 26);
        label(form, "SDK " + OfferProSdk.SDK_VERSION + " · Android", 14);
        label(form, "Enter your app credentials and test user, initialize, then launch the offer wall.", 16);
        appId = field(form, "App ID", InputType.TYPE_CLASS_NUMBER);
        encryptionKey = field(form, "Encryption key (32 UTF-8 bytes)",
                InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        userId = field(form, "User ID", InputType.TYPE_CLASS_TEXT);
        email = field(form, "User email", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        country = field(form, "Country code (e.g. IN)", InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);
        advertisingId = field(form, "Advertising ID", InputType.TYPE_CLASS_TEXT);
        deviceId = field(form, "Device ID (optional)", InputType.TYPE_CLASS_TEXT);

        Button initialize = new Button(this);
        initialize.setText("Initialize SDK");
        form.addView(initialize, new LinearLayout.LayoutParams(-1, dp(56)));
        launch = new Button(this);
        launch.setText("Launch Offerwall");
        launch.setEnabled(false);
        form.addView(launch, new LinearLayout.LayoutParams(-1, dp(56)));
        status = label(form, "Not initialized", 16);
        status.setAccessibilityLiveRegion(View.ACCESSIBILITY_LIVE_REGION_POLITE);
        label(form, "Credentials are kept in memory only. Editing any field requires initialization again.", 13);
        label(form, "Integrity checks remain enabled. Emulators, VPN, root, ADB and developer options can block the wall.", 13);

        // Recover an initialized session on Activity recreation without writing credentials to disk.
        if (sdk.isInitialized()) {
            SdkConfig config = sdk.getConfig();
            appId.setText(String.valueOf(config.appId)); encryptionKey.setText(config.encKey);
            userId.setText(config.userId); email.setText(config.userEmail);
            country.setText(config.userCountry); advertisingId.setText(config.advertisingId);
            deviceId.setText(config.deviceId);
            ready = true; launch.setEnabled(true); showStatus("SDK initialized. Ready to launch.", false);
        }
        TextWatcher changed = new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) { }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                ready = false; launch.setEnabled(false);
                showStatus("Initialize SDK to apply these credentials.", false);
            }
            @Override public void afterTextChanged(Editable e) { }
        };
        for (EditText input : new EditText[]{appId, encryptionKey, userId, email, country, advertisingId, deviceId})
            input.addTextChangedListener(changed);

        initialize.setOnClickListener(v -> initialize());
        launch.setOnClickListener(v -> {
            if (!ready) return;
            hideKeyboard();
            try {
                sdk.openWall(this);
                showStatus("Launch requested. The SDK is checking device integrity.", false);
            } catch (Exception error) { showStatus("Launch failed: " + error.getMessage(), true); }
        });
    }

    private void initialize() {
        ready = false; launch.setEnabled(false);
        try {
            int id;
            try { id = Integer.parseInt(value(appId)); }
            catch (NumberFormatException e) { appId.setError("Enter a positive numeric app ID"); appId.requestFocus(); return; }
            String countryCode = value(country).toUpperCase(java.util.Locale.ROOT);
            if (!countryCode.matches("[A-Z]{2}")) {
                country.setError("Enter a two-letter country code"); country.requestFocus(); return;
            }
            SdkConfig config = new SdkConfig.Builder().appId(id)
                    .encKey(encryptionKey.getText().toString())
                    .userId(value(userId)).userEmail(value(email)).userCountry(countryCode)
                    .advertisingId(value(advertisingId)).deviceId(value(deviceId)).build();
            sdk.initialize(getApplicationContext(), config);
            ready = true; launch.setEnabled(true); hideKeyboard();
            showStatus("SDK initialized. Ready to launch.", false);
        } catch (Exception error) { showStatus("Initialization failed: " + error.getMessage(), true); }
    }

    private EditText field(LinearLayout form, String title, int inputType) {
        TextView caption = label(form, title, 14);
        EditText input = new EditText(this);
        input.setId(View.generateViewId());
        caption.setLabelFor(input.getId());
        input.setInputType(inputType);
        input.setSingleLine(true);
        input.setTextColor(Color.WHITE);
        input.setSaveEnabled(false);
        if (Build.VERSION.SDK_INT >= 26) input.setImportantForAutofill(View.IMPORTANT_FOR_AUTOFILL_NO);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(52));
        params.bottomMargin = dp(12);
        form.addView(input, params);
        return input;
    }
    private TextView label(LinearLayout form, String text, int size) {
        TextView label = new TextView(this);
        label.setText(text); label.setTextSize(size); label.setTextColor(Color.LTGRAY);
        label.setPadding(0, dp(8), 0, dp(8));
        form.addView(label, new LinearLayout.LayoutParams(-1, -2));
        return label;
    }
    private void showStatus(String message, boolean error) {
        status.setText(message); status.setTextColor(error ? Color.rgb(255, 130, 130) : Color.rgb(140, 220, 180));
    }
    private String value(EditText input) { return input.getText().toString().trim(); }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    private void hideKeyboard() {
        InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
        if (keyboard != null) keyboard.hideSoftInputFromWindow(appId.getWindowToken(), 0);
    }
}
