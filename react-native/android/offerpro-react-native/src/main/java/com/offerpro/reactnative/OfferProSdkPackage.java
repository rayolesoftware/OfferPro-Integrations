package com.offerpro.reactnative;
import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import java.util.Collections;
import java.util.List;
public final class OfferProSdkPackage implements ReactPackage {
    @Override public List<NativeModule> createNativeModules(ReactApplicationContext c) { return Collections.singletonList(new OfferProSdkModule(c)); }
    @Override public List<ViewManager> createViewManagers(ReactApplicationContext c) { return Collections.emptyList(); }
}
