package ir.myket.billingclient.util;

import ir.myket.billingclient.IabHelper;

public class IabResult {
    int mResponse;
    String mMessage;

    public IabResult(final int response, final String message) {
        this.mResponse = response;
        if (message == null || message.trim().length() == 0) {
            this.mMessage = IabHelper.getResponseDesc(response);
        } else {
            this.mMessage = message + " (response: " + IabHelper.getResponseDesc(response) + ")";
        }
    }

    public int getResponse() {
        return this.mResponse;
    }

    public String getMessage() {
        return this.mMessage;
    }

    public boolean isSuccess() {
        return this.mResponse == 0;
    }

    public boolean isFailure() {
        return !this.isSuccess();
    }

    @Override
    public String toString() {
        return "IabResult: " + this.getMessage();
    }
}
