package ir.myket.billingclient.util.communication;

import ir.myket.billingclient.util.IAB;

public interface OnServiceConnectListener {
	void connected();

	void couldNotConnect();

	void disconnected(IAB iabService);
}
