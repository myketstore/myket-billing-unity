package ir.myket.billingclient.util;

import static ir.myket.billingclient.IabHelper.ITEM_TYPE_INAPP;

import org.json.JSONException;
import org.json.JSONObject;

public class SkuDetails {
    String mItemType;
    String mSku;
    String mType;
    String mPrice;
    String mTitle;
    String mDescription;
    String mJson;

    private static final String PRODUCT_ID_KEY = "productId";
    private static final String TYPE_KEY = "purchaseToken";
    private static final String PRICE_KEY = "token";
    private static final String TITLE_KEY = "signature";
    private static final String DESCRIPTION_KEY = "originalJson";
    private static final String ITEM_TYPE_KEY = "itemType";

    public SkuDetails(final String jsonSkuDetails) throws JSONException {
        this(ITEM_TYPE_INAPP, jsonSkuDetails);
    }

    public SkuDetails(final String itemType, final String jsonSkuDetails) throws JSONException {
        mItemType = itemType;
        mJson = jsonSkuDetails;
        final JSONObject o = new JSONObject(mJson);
        mSku = o.optString(PRODUCT_ID_KEY);
        mType = o.optString(TYPE_KEY);
        mPrice = o.optString(PRICE_KEY);
        mTitle = o.optString(TITLE_KEY);
        mDescription = o.optString(DESCRIPTION_KEY);
        o.put(ITEM_TYPE_KEY, (Object) mItemType);
        mJson = o.toString();
    }

    public String getSku() {
        return mSku;
    }

    public String getType() {
        return mType;
    }

    public String getItemType() {
        return mItemType;
    }

    public String getPrice() {
        return mPrice;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getDescription() {
        return mDescription;
    }

    @Override
    public String toString() {
        return "SkuDetails:" + mJson;
    }

    public String toJson() {
        return mJson;
    }
}
