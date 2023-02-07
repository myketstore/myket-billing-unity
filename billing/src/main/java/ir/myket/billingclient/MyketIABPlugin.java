package ir.myket.billingclient;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import ir.myket.billingclient.util.IABLogger;
import ir.myket.billingclient.util.IabResult;
import ir.myket.billingclient.util.Inventory;
import ir.myket.billingclient.util.Purchase;
import ir.myket.billingclient.util.SkuDetails;

public class MyketIABPlugin extends MyketIABPluginBase
        implements IabHelper.QueryInventoryFinishedListener, IabHelper.QuerySkuDetailsFinishedListener,
        IabHelper.QueryPurchasesFinishedListener, IabHelper.OnIabPurchaseFinishedListener,
        IabHelper.OnConsumeFinishedListener, IabHelper.OnConsumeMultiFinishedListener {
    private static final String BILLING_NOT_RUNNING_ERROR =
            "The billing service is not running or billing is not supported. Aborting.";
    private static MyketIABPlugin mInstance;
    private IabHelper mHelper;
    private List<Purchase> mPurchases;
    private List<SkuDetails> mSkus;

    public MyketIABPlugin() {
        mPurchases = new ArrayList<Purchase>();
    }

    public static MyketIABPlugin instance() {
        if (MyketIABPlugin.mInstance == null) {
            MyketIABPlugin.mInstance = new MyketIABPlugin();
        }
        return MyketIABPlugin.mInstance;
    }

    public IabHelper getIabHelper() {
        return mHelper;
    }

    public void enableLogging(final boolean shouldEnable) {
        IABLogger.DEBUG = shouldEnable;
        if (mHelper != null) {
            mHelper.enableDebugLogging(true);
        }
    }

    public void init(final String publicKey) {
        IABLogger.logEntering(getClass().getSimpleName(), "init = ", publicKey);
        mPurchases = new ArrayList<Purchase>();
        (mHelper = new IabHelper((Context) getActivity(), publicKey))
                .startSetup(new IabHelper.OnIabSetupFinishedListener() {
                    @Override
                    public void onIabSetupFinished(final IabResult result) {
                        if (result.isSuccess()) {
                            UnitySendMessage("billingSupported", "");
                        } else {
                            Log.i("[MyketAIB][Plugin]", "billing not supported: " + result.getMessage());
                            UnitySendMessage("billingNotSupported", result.getMessage());
                            mHelper = null;
                        }
                    }
                });
    }

    public void unbindService() {
        IABLogger.logEntering(getClass().getSimpleName(), "unbindService");
        if (mHelper != null) {
            mHelper.dispose();
            mHelper = null;
        }
    }

    public boolean areSubscriptionsSupported() {
        IABLogger.logEntering(getClass().getSimpleName(), "areSubscriptionsSupported");
        return mHelper != null && mHelper.subscriptionsSupported();
    }

    public void queryInventory(final String[] skus) {
        IABLogger.logEntering(getClass().getSimpleName(), "queryInventory", skus);
        if (mHelper == null) {
            Log.i(TAG, MyketIABPlugin.BILLING_NOT_RUNNING_ERROR);
            return;
        }
        runSafelyOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHelper.queryInventoryAsync(true, Arrays.asList(skus), MyketIABPlugin.this);
            }
        }, "queryInventoryFailed");
    }

    @Override
    public void onQueryInventoryFinished(final IabResult result, final Inventory inventory) {
        if (result.isSuccess()) {
            mPurchases = inventory.getAllPurchases();
            mSkus = inventory.getAllSkuDetails();
            UnitySendMessage("queryInventorySucceeded", inventory.getAllSkusAndPurchasesAsJson());
        } else {
            UnitySendMessage("queryInventoryFailed", result.getMessage());
        }
    }

    public void querySkuDetails(final String[] skus) {
        IABLogger.logEntering(getClass().getSimpleName(), "querySkuDetails", skus);
        if (mHelper == null) {
            Log.i(TAG, MyketIABPlugin.BILLING_NOT_RUNNING_ERROR);
            return;
        }
        runSafelyOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHelper.querySkuDetailsAsync(Arrays.asList(skus), MyketIABPlugin.this);
            }
        }, "querySkuDetailsFailed");
    }

    @Override
    public void onQuerySkuDetailsFinished(final IabResult result, final Inventory inventory) {
        if (result.isSuccess()) {
            mSkus = inventory.getAllSkuDetails();
            final String skusStr = inventory.getAllSkusAsJson().toString();
            UnitySendMessage("querySkuDetailsSucceeded", skusStr);
        } else {
            UnitySendMessage("querySkuDetailsFailed", result.getMessage());
        }
    }

    public void queryPurchases() {
        IABLogger.logEntering(getClass().getSimpleName(), "queryPurchases");
        if (mHelper == null) {
            Log.i(TAG, MyketIABPlugin.BILLING_NOT_RUNNING_ERROR);
            return;
        }
        runSafelyOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHelper.queryPurchasesAsync(MyketIABPlugin.this);
            }
        }, "queryInventoryFailed");
    }

    @Override
    public void onQueryPurchasesFinished(final IabResult result, final Inventory inventory) {
        if (result.isSuccess()) {
            mPurchases = inventory.getAllPurchases();
            final String purchasesStr = inventory.getAllPurchasesAsJson().toString();
            UnitySendMessage("queryPurchasesSucceeded", purchasesStr);
        } else {
            UnitySendMessage("queryPurchasesFailed", result.getMessage());
        }
    }

    public void purchaseProduct(final String sku, final String developerPayload) {
        IABLogger.logEntering(getClass().getSimpleName(), "purchaseProduct", new Object[]{sku, developerPayload});
        if (mHelper == null) {
            Log.i(TAG, MyketIABPlugin.BILLING_NOT_RUNNING_ERROR);
            return;
        }
        for (final Purchase p : mPurchases) {
            if (p.getSku().equalsIgnoreCase(sku)) {
                Log.i(TAG,
                        "Attempting to purchase an item that has already been purchased. That is probably not a good idea: " +
                                sku);
            }
        }

        final String f_itemType = "inapp";
        runSafelyOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHelper.launchPurchaseFlow(getActivity(), sku, f_itemType, MyketIABPlugin.this, developerPayload);
            }
        }, "launchPurchaseFlow");
    }

    @Override
    public void onIabPurchaseFinished(final IabResult result, final Purchase info) {
        if (result.isSuccess()) {
            if (!mPurchases.contains(info)) {
                mPurchases.add(info);
            }
            UnitySendMessage("purchaseSucceeded", info.toJson());
        } else {
            UnitySendMessage("purchaseFailed", result.getMessage());
        }
    }

    public void consumeProduct(final String sku) {
        IABLogger.logEntering(getClass().getSimpleName(), "consumeProduct", sku);
        if (mHelper == null) {
            Log.i(TAG, MyketIABPlugin.BILLING_NOT_RUNNING_ERROR);
            return;
        }
        final Purchase purchase = getPurchasedProductForSku(sku);
        if (purchase == null) {
            Log.i(TAG,
                    "Attempting to consume an item that has not been purchased. Aborting to avoid exception. sku: " +
                            sku);
            UnitySendMessage("consumePurchaseFailed", sku +
                    ": you cannot consume a project that has not been purchased or if you have not first queried your inventory to retreive the purchases.");
            return;
        }
        runSafelyOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHelper.consumeAsync(purchase, MyketIABPlugin.this);
            }
        }, "consumePurchaseFailed");
    }

    private Purchase getPurchasedProductForSku(final String sku) {
        for (final Purchase p : mPurchases) {
            if (p.getSku().equalsIgnoreCase(sku)) {
                return p;
            }
        }
        return null;
    }

    @Override
    public void onConsumeFinished(final Purchase purchase, final IabResult result) {
        if (result.isSuccess()) {
            if (mPurchases.contains(purchase)) {
                mPurchases.remove(purchase);
            }
            UnitySendMessage("consumePurchaseSucceeded", purchase.toJson());
        } else {
            final String res = purchase.getSku() + ": " + result.getMessage();
            UnitySendMessage("consumePurchaseFailed", res);
        }
    }

    public void consumeProducts(final String[] skus) {
        IABLogger.logEntering(getClass().getSimpleName(), "consumeProducts", skus);
        if (mHelper == null) {
            Log.i(TAG, MyketIABPlugin.BILLING_NOT_RUNNING_ERROR);
            return;
        }
        if (mPurchases == null || mPurchases.size() == 0) {
            Log.e(TAG, "there are no purchases available to consume");
            return;
        }
        final List<Purchase> confirmedPurchases = new ArrayList<Purchase>();
        for (final String sku : skus) {
            final Purchase purchase = getPurchasedProductForSku(sku);
            if (purchase != null) {
                confirmedPurchases.add(purchase);
            }
        }
        if (confirmedPurchases.size() != skus.length) {
            Log.i(TAG, "Attempting to consume " + skus.length + " item(s) but only " +
                    confirmedPurchases.size() + " item(s) were found to be purchased. Aborting.");
            return;
        }
        runSafelyOnUiThread(new Runnable() {
            @Override
            public void run() {
                mHelper.consumeAsync(confirmedPurchases, MyketIABPlugin.this);
            }
        }, "consumePurchaseFailed");
    }

    @Override
    public void onConsumeMultiFinished(final List<Purchase> purchases, final List<IabResult> results) {
        for (int i = 0; i < results.size(); ++i) {
            final IabResult result = results.get(i);
            final Purchase purchase = purchases.get(i);
            if (result.isSuccess()) {
                if (mPurchases.contains(purchase)) {
                    mPurchases.remove(purchase);
                }

                UnitySendMessage("consumePurchaseSucceeded", purchase.toJson());
            } else {
                final String res = purchase.getSku() + ": " + result.getMessage();
                UnitySendMessage("consumePurchaseFailed", res);
            }
        }
    }

}