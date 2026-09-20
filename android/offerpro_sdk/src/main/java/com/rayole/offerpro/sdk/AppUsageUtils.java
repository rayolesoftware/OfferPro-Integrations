// android/offerpro_sdk/src/main/java/com/offerpro/sdk/AppUsageUtils.java
package com.rayole.offerpro.sdk;

import android.app.AppOpsManager;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

final class AppUsageUtils {
    private AppUsageUtils() {}

    /** True if the app has "Usage Access" permission enabled in Settings. */
    public static boolean hasUsageAccess(Context ctx) {
        try {
            AppOpsManager appOps = (AppOpsManager) ctx.getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    ctx.getPackageName()
            );
            if (mode == AppOpsManager.MODE_DEFAULT) {
                // Note: PACKAGE_USAGE_STATS is a “special” permission; this check returns
                // PERMISSION_GRANTED only on some builds. MODE_ALLOWED is the most reliable signal.
                return ctx.checkCallingOrSelfPermission("android.permission.PACKAGE_USAGE_STATS")
                        == PackageManager.PERMISSION_GRANTED;
            } else {
                return mode == AppOpsManager.MODE_ALLOWED;
            }
        } catch (Throwable t) {
//            Log.d("hasUsageAccess", "hasUsageAccess failed", t);
            return false;
        }
    }

    /** Opens the system screen where the user can grant Usage Access. */
    static void openUsageAccessSettings(Context ctx) {
        String pkg = ctx.getPackageName();

        // 1) Try ACTION_USAGE_ACCESS_SETTINGS with package: Uri (works on many OEMs)
        try {
            Intent perApp = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .setData(Uri.fromParts("package", pkg, null));
            if (perApp.resolveActivity(ctx.getPackageManager()) != null) {
                ctx.startActivity(perApp);
                return;
            }
        } catch (Throwable t) {
            Log.e("openUsageAccessSettings", "Per-app usage access deep link failed", t);
        }

        // 2) Fallback: open the generic “Usage access” list
        try {
            Intent list = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (list.resolveActivity(ctx.getPackageManager()) != null) {
                ctx.startActivity(list);
                return;
            }
        } catch (Throwable t) {
            Log.e("openUsageAccessSettings", "Generic usage access list failed", t);
        }

        // 3) Last resort: App Info page — user can go to “Special app access” from here
        try {
            Intent appInfo = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", pkg, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (appInfo.resolveActivity(ctx.getPackageManager()) != null) {
                ctx.startActivity(appInfo);
            }
        } catch (Throwable t) {
            Log.e("openUsageAccessSettings", "Could not open any usage access screen", t);
        }
    }

    /**
     * Foreground union in [fromMs, toMs), using retained events only.
     * A 24h lookback recovers sessions crossing the start boundary. Missing/expired
     * history is conservatively undercounted; daily totals must never validate rewards.
     */
    public static long getUsageMs(Context ctx, String pkg, long fromMs, long toMs) {
        if (TextUtils.isEmpty(pkg) || fromMs < 0 || toMs <= fromMs)
            throw new IllegalArgumentException("Valid package and increasing timestamps are required");
        if (!hasUsageAccess(ctx)) throw new SecurityException("Usage Access is not granted");
        long end = Math.min(toMs, System.currentTimeMillis());
        if (end <= fromMs) return 0;
        UsageStatsManager manager = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        if (manager == null) return 0;
        UsageEvents events = manager.queryEvents(Math.max(0, fromMs - 86_400_000L), end);
        if (events == null) return 0;
        UsageWindow window = new UsageWindow(fromMs, end);
        UsageEvents.Event event = new UsageEvents.Event();
        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            int type = event.getEventType();
            if (type == UsageEvents.Event.SCREEN_NON_INTERACTIVE
                    || type == UsageEvents.Event.DEVICE_SHUTDOWN) {
                window.stop(event.getTimeStamp());
                continue;
            }
            if (!pkg.equals(event.getPackageName())) continue;
            String key = Build.VERSION.SDK_INT >= 29
                    ? String.valueOf(event.getClassName()) : "package";
            if (type == UsageEvents.Event.MOVE_TO_FOREGROUND)
                window.event(key, event.getTimeStamp(), true);
            else if (type == UsageEvents.Event.MOVE_TO_BACKGROUND)
                window.event(key, event.getTimeStamp(), false);
        }
        return window.finish();
    }

    /** Convenience: parse millis from string; allows empty or non-numeric -> 0. */
    static long parseMillis(String s) {
        if (TextUtils.isEmpty(s)) return 0L;
        try { return Long.parseLong(s.trim()); } catch (NumberFormatException ignored) { return 0L; }
    }
}
