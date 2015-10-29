package com.www.client.pm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.util.Log;

public class PmBroadcastReceiver extends BroadcastReceiver {

    WifiP2pManager manager;
    Channel channel;
    PmP2p pmP2p;

    //
    public PmBroadcastReceiver(WifiP2pManager manager, Channel channel, PmP2p pmP2p) {
        this.manager = manager;
        this.channel = channel;
        this.pmP2p = pmP2p;
    }

    // Get notified about WifiP2P network changes.
    //  - Peers have changed
    //     -> Request the new peer list
    //        (get it @PmP2p.onPeersAvailable)
    //  - A connection has been established
    //     -> Request connection info
    //        (get it @PmP2p.onConnectionInfoAvailable)
    @Override
    public void onReceive(Context context, Intent intent) {
        String TAG = getClass().getName() + "@onReceive: ";

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert the Activity.
            Log.wtf(TAG, "WifP2P state changed...");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.wtf(TAG, "WifiP2P enabled");
                pmP2p.setWifiP2pStatus(true);
            } else {
                Log.wtf(TAG, "WifiP2P disabled");
                pmP2p.setWifiP2pStatus(false);
            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // The peer list has changed!  We should probably do something about that.
            Log.wtf(TAG, "WifiP2P peers changed...");

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, pmP2p);
            }

        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            // Connection state changed!  We should probably do something about that.
            Log.wtf(TAG, "WifiP2P connection changed...");

            if (manager != null) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);

                if (networkInfo.isConnected()) {
                    // We are connected with the other device, request connection
                    // info to find group owner IP
                    Log.wtf(TAG, "Connected");

                    manager.requestConnectionInfo(channel, pmP2p);
                } else {
                    Log.wtf(TAG, "Disconnected");

                    pmP2p.disconnect();
                }
            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.wtf(TAG, "WifiP2P this device changed...");
        }

    }

}
