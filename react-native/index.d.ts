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
  fetchMegaOffer(): Promise<MegaOffer | null>;
  showMegaOffer(url: string): Promise<void>;
};
export default OfferPro;
