package com.rayole.offerpro.sdk;
import org.junit.Test;
import static org.junit.Assert.*;
public class UsageWindowTest {
    @Test public void clipsSessionCrossingStart() {
        UsageWindow w = new UsageWindow(100, 200);
        w.event("a", 50, true); w.event("a", 150, false);
        assertEquals(50, w.finish());
    }
    @Test public void ignoresEarlierUsage() {
        UsageWindow w = new UsageWindow(100, 200);
        w.event("a", 10, true); w.event("a", 90, false);
        assertEquals(0, w.finish());
    }
    @Test public void overlappingActivitiesCountOnce() {
        UsageWindow w = new UsageWindow(100, 200);
        w.event("a", 110, true); w.event("b", 120, true);
        w.event("a", 130, false); w.event("b", 170, false);
        assertEquals(60, w.finish());
    }
    @Test public void screenOffStopsOpenSession() {
        UsageWindow w = new UsageWindow(100, 200);
        w.event("a", 120, true); w.stop(150);
        assertEquals(30, w.finish());
    }
    @Test public void clipsOpenSessionAtEnd() {
        UsageWindow w = new UsageWindow(100, 200);
        w.event("a", 50, true);
        assertEquals(100, w.finish());
    }
    @Test public void missingStartDoesNotInventUsage() {
        UsageWindow w = new UsageWindow(100, 200);
        w.event("a", 150, false);
        assertEquals(0, w.finish());
    }
}
