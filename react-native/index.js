import { NativeModules, Platform } from 'react-native';

const Native = new Proxy({}, { get: (_, method) => (...args) => {
  if (Platform.OS !== 'android') return Promise.reject(new Error('OfferPro supports Android only'));
  if (!NativeModules.OfferProSdk) return Promise.reject(new Error('OfferPro native module is missing; rebuild the Android app after installation'));
  return NativeModules.OfferProSdk[method](...args);
} });

export const OfferPro = {
  initialize: (config) => Native.initialize(config),
  showOfferPro: () => Native.openWall(),
  openUrl: (url) => Native.openUrl(url),
  fetchMegaOffer: () => Native.fetchMegaOffer(),
  showMegaOffer: (url) => Native.openMegaWall(url),
  hasUsageAccess: () => Native.hasUsageAccess(),
  openUsageAccessSettings: () => Native.openUsageAccessSettings(),
  getUsageTimeMs: (packageName, fromMs, toMs) => Native.getUsageTimeMs(packageName, fromMs, toMs),
  isInstalled: (packageName) => Native.isInstalled(packageName),
  validateInstall: (packageName) => Native.validateInstall(packageName),
  validateAppUsage: (packageName, fromMs, toMs) => Native.validateAppUsage(packageName, fromMs, toMs),
};

export default OfferPro;
