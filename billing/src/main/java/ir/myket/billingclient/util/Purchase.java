package ir.myket.billingclient.util;

import org.json.JSONException;
import org.json.JSONObject;

public class Purchase {

    private static final String ORDER_ID_KEY = "orderId";
    private static final String PACKAGE_NAME_KEY = "packageName";
    private static final String PRODUCT_ID_KEY = "productId";
    private static final String PURCHASE_TIME_KEY = "purchaseTime";
    private static final String PURCHASE_STATE_KEY = "purchaseState";
    private static final String DEVELOPER_PAYLOAD_KEY = "developerPayload";
    private static final String PURCHASE_TOKEN_KEY = "purchaseToken";
    private static final String TOKEN_KEY = "token";
    private static final String SIGNATURE_KEY = "signature";
    private static final String ORIGINAL_JSON_KEY = "originalJson";
    private static final String ITEM_TYPE_KEY = "itemType";

    String mItemType;
    String mOrderId;
    String mPackageName;
    String mSku;
    long mPurchaseTime;
    int mPurchaseState;
    String mDeveloperPayload;
    String mToken;
    String mOriginalJson;
    String mOriginalJsonWithSignature;
    String mSignature;

    public Purchase(final String itemType, final String jsonPurchaseInfo, final String signature) throws
            JSONException {
        this.mItemType = itemType;
        this.mOriginalJson = jsonPurchaseInfo;
        final JSONObject jsonObj = new JSONObject(mOriginalJson);
        mOrderId = jsonObj.optString(ORDER_ID_KEY);
        mPackageName = jsonObj.optString(PACKAGE_NAME_KEY);
        mSku = jsonObj.optString(PRODUCT_ID_KEY);
        mPurchaseTime = jsonObj.optLong(PURCHASE_TIME_KEY);
        mPurchaseState = jsonObj.optInt(PURCHASE_STATE_KEY);
        mDeveloperPayload = jsonObj.optString(DEVELOPER_PAYLOAD_KEY);
        mToken = jsonObj.optString(TOKEN_KEY, jsonObj.optString(PURCHASE_TOKEN_KEY));
        if (signature == null) {
            mSignature = "NO SIGNATURE RETURNED FROM PLAY";
        } else {
            mSignature = signature;
        }
        jsonObj.put(SIGNATURE_KEY, (Object) mSignature);
        jsonObj.put(ORIGINAL_JSON_KEY, (Object) mOriginalJson);
        jsonObj.put(ITEM_TYPE_KEY, (Object) mItemType);
        this.mOriginalJsonWithSignature = jsonObj.toString();
    }

    @Override
    public boolean equals(final Object obj) {
        return this.mSku.equals(((Purchase) obj).mSku) &&
                this.mPackageName.equals(((Purchase) obj).mPackageName);
    }

    public String getItemType() {
        return mItemType;
    }

    public String getOrderId() {
        return mOrderId;
    }

    public String getPackageName() {
        return mPackageName;
    }

    public String getSku() {
        return mSku;
    }

    public long getPurchaseTime() {
        return mPurchaseTime;
    }

    public int getPurchaseState() {
        return mPurchaseState;
    }

    public String getDeveloperPayload() {
        return mDeveloperPayload;
    }

    public String getToken() {
        return this.mToken;
    }

    public String getOriginalJson() {
        return mOriginalJson;
    }

    public String getSignature() {
        return mSignature;
    }

    @Override
    public String toString() {
        return "PurchaseInfo(type:" + mItemType + "):" + mOriginalJson;
    }

    public String toJson() {
        return mOriginalJsonWithSignature;
    }
}
