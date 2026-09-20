package com.rayole.offerpro.sdk;

/** Event-based foreground union, clipped to the requested interval. No daily-bucket fallback. */
final class UsageWindow {
    private final long from, to;
    private final java.util.Set<String> active = new java.util.HashSet<>();
    private long started = -1, total;
    UsageWindow(long from, long to) { this.from = from; this.to = to; }
    void event(String activity, long time, boolean resumed) {
        if (time > to) return;
        if (resumed) {
            if (active.isEmpty()) started = Math.max(from, time);
            active.add(activity);
        } else {
            active.remove(activity);
            if (active.isEmpty()) close(time);
        }
    }
    void stop(long time) { active.clear(); close(time); }
    private void close(long time) {
        if (started >= 0) total += Math.max(0, Math.min(time, to) - started);
        started = -1;
    }
    long finish() { close(to); return Math.max(0, Math.min(to - from, total)); }
}
