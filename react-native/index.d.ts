export interface OfferProConfig {
  appId: number;
  userId: string;
  userEmail: string;
  userCountry: string;
  encKey: string;
  advertisingId: string;
  deviceId?: string;
}
export interface MegaOffer {
  id: number; name: string; offer_image: string; reward_coins: number;
  task_type: { name: string }; direct_offer_link: string;
}
export const OfferPro: {
  initialize(config: OfferProConfig): Promise<void>;
  showOfferPro(): Promise<void>;
  openUrl(url: string): Promise<void>;
  fetchMegaOffer(): Promise<MegaOffer | null>;
  showMegaOffer(url: string): Promise<void>;
  hasUsageAccess(): Promise<boolean>;
  openUsageAccessSettings(): Promise<void>;
  getUsageTimeMs(packageName: string, fromMs: number, toMs: number): Promise<number>;
  isInstalled(packageName: string): Promise<boolean>;
  validateInstall(packageName: string): Promise<string>;
  validateAppUsage(packageName: string, fromMs: number, toMs: number): Promise<string>;
};
export default OfferPro;
