package com.www.client.pm;

import com.www.client.Globals;
import com.www.client.R;

import android.content.*;
import android.net.NetworkInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.preference.PreferenceManager;
import android.util.*;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.*;

public  class EasyPrivacy
        extends BroadcastReceiver
        implements PeerListListener, ConnectionInfoListener {

    Context context;

    public static SharedPreferences shrPrf = null;

    private final IntentFilter intentFilter;
    Channel channel;
    WifiP2pManager manager;
    private boolean wiFiP2pStatus;
    private List<Object> peers;

    private ServerSocket serverSocket;
    private ServerSocket clientSocket;

    public EasyPrivacy(Context c) {
        String TAG = getClass().getName() + "@EasyPrivacy: ";

        Log.wtf(TAG, "...");

        context = c;
        shrPrf = PreferenceManager.getDefaultSharedPreferences(c);

        intentFilter = new IntentFilter();

        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);

        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);

        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);

        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        manager = (WifiP2pManager) c.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(c, c.getMainLooper(), null);

        peers = new ArrayList<>();

        Log.wtf(TAG, "OK");

    }




    public void onStart() {
        String TAG = getClass().getName() + "@onStart: ";

        Log.wtf(TAG, "...");

        context.registerReceiver(this, intentFilter);

        // Detect available peers that are in range. The call to this function is
        // asynchronous and a success or failure is communicated to the application
        // with onSuccess() and onFailure(). The onSuccess() method only notifies you
        // that the discovery process succeeded and does not provide any information
        // about the actual peers that it discovered, if any.
        manager.discoverPeers(channel, new ActionListener() {

            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                String TAG = getClass().getName() + "@onSuccess: ";

                Log.wtf(TAG, "OK");

            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                String TAG = getClass().getName() + "@onFailure: ";

                Log.wtf(TAG, "Oops!");

            }

        });

        Log.wtf(TAG, "OK");

    }

    public void onStop() {
        String TAG = getClass().getName() + "@onStop: ";

        Log.wtf(TAG, "...");

        context.unregisterReceiver(this);

        Log.wtf(TAG, "OK");

    }



    // Register the PM service
    // TODO to be called in PrivacyMecanism() if pm is collaborative
    // Create a string map containing information about your service.
    // Map record = new HashMap();
    // record.put("listenport", String.valueOf(SERVER_PORT));
    // record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));
    // record.put("available", "visible");
//    public void startRegistration(Map record) {
    public void startRegistration() {

        // Create a string map containing information about your service.
        // TODO pass it as a parameter
        Map record = new HashMap();

//         record.put("listenport", String.valueOf(SERVER_PORT));

        try {
            serverSocket = new ServerSocket(0);
        } catch (IOException e) {
            e.printStackTrace();
        }
        record.put("port", String.valueOf(serverSocket.getLocalPort()));

//        record.put("buddyname", "John Doe" + (int) (Math.random() * 1000));

        record.put("usr", shrPrf.getString(Globals.shr_user, "user") + shrPrf.getString(Globals.shr_dev_id, "0"));
        record.put("dev", shrPrf.getString(Globals.shr_dev_id, "0"));
        record.put("pm", shrPrf.getString(Globals.pm_id, "0"));
        record.put("prv", shrPrf.getString(Globals.privacy_level, "0"));

//        record.put("available", "visible");

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        WifiP2pDnsSdServiceInfo serviceInfo =
                WifiP2pDnsSdServiceInfo.newInstance("_test", "_presence._tcp", record);

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        manager.addLocalService(channel, serviceInfo, new ActionListener() {

            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                String TAG = getClass().getName() + "@onSuccess: ";

                Log.wtf(TAG, "OK");

            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                String TAG = getClass().getName() + "@onFailure: ";

                Log.wtf(TAG, "Oops!");

            }

        });

    }









    // Get notified about WifiP2P network changes.
    //  - Peers have changed
    //     -> Request the new peer list
    //        (get it at onPeersAvailable)
    //  - A connection has been established
    //     -> Request connection info
    //        (get it at onConnectionInfoAvailable)
    @Override
    public void onReceive(Context c, Intent intent) {
        String TAG = getClass().getName() + "@onReceive: ";

        String action = intent.getAction();

        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
            // Determine if Wifi P2P mode is enabled or not, alert the Activity.
            Log.wtf(TAG, "WifP2P state changed...");

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);

            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                Log.wtf(TAG, "WifiP2P enabled");

                this.setWiFiP2pStatus(true);

            } else {
                Log.wtf(TAG, "WifiP2P disabled");

                this.setWiFiP2pStatus(false);

            }

        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {
            // The peer list has changed!  We should probably do something about that.
            Log.wtf(TAG, "WifiP2P peers changed...");

            // Request available peers from the wifi p2p manager. This is an
            // asynchronous call and the calling activity is notified with a
            // callback on PeerListListener.onPeersAvailable()
            if (manager != null) {
                manager.requestPeers(channel, this);
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

                    manager.requestConnectionInfo(channel, this);
                }

            }

        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            Log.wtf(TAG, "WifiP2P this device changed...");

        }
    }

    // Tne WifiP2pManager.requestPeers(Channel, PeerListListener) returns the new
    // WifiP2pDeviceList.
    //
    // Updates the:
    //  - Peers list
    //  - Service (PM) list
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        String TAG = getClass().getName() + "@onPeersAvailable: ";

        Log.wtf(TAG, "...");

        // Out with the old, in with the new.
        this.peers.clear();

        // TODO
        // Update the peer list
        this.peers.addAll(peers.getDeviceList());
        // TODO

        // If an AdapterView is backed by this data, notify it
        // of the change.  For instance, if you have a ListView of available
        // peers, trigger an update.

        if (this.peers.size() == 0) {
            Log.wtf(TAG, "No devices found");
        } else {
            Log.wtf(TAG, this.peers.toString());
        }

    }

    // Tne WifiP2pManager.requestConnectionInfo(Channel, ConnectionInfoListener)
    // returns connection info.
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        String TAG = getClass().getName() + "@onConnectionInfoAvailable: ";

        Log.wtf(TAG, "...");

        // InetAddress from WifiP2pInfo struct.
//        InetAddress groupOwnerAddress = info.groupOwnerAddress.getHostAddress();

        // After the group negotiation, we can determine the group owner.
        if (info.groupFormed && info.isGroupOwner) {
            // Do whatever tasks are specific to the group owner.
            // One common case is creating a server thread and accepting
            // incoming connections.
        } else if (info.groupFormed) {
            // The other device acts as the client. In this case,
            // you'll want to create a client thread that connects to the group
            // owner.
        }
    }













    private void setWiFiP2pStatus(boolean status) {
        wiFiP2pStatus = status;
    }

    private boolean isWiFiP2pEnabled(boolean status) {
        return wiFiP2pStatus;
    }

}
