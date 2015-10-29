package com.www.client.pm;

import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.*;
import android.net.wifi.p2p.WifiP2pManager.*;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.*;

import com.www.client.Globals;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.*;
import java.util.concurrent.ThreadPoolExecutor;

public class PmP2p implements PeerListListener, ConnectionInfoListener {

    Context context;
    PrivacyMechanism pm;

    private SharedPreferences shrPrf;

    private PmBroadcastReceiver receiver;
    private IntentFilter intentFilter;
    private Channel channel;
    private WifiP2pManager manager;
    private WifiP2pDnsSdServiceInfo serviceInfo;

    private boolean wifiP2pStatus;
    private boolean connectionStatus;

    private List<Map<String, String>> peers;

    //    ServerSocket serverSocket;
    Map<String, String> serverPeer;
    Thread server, client;

    //
    public PmP2p(Context context, PrivacyMechanism pm) {
        String TAG = getClass().getName() + "@PmP2p: ";

        Log.wtf(TAG, "...");

        this.context = context;
        this.pm = pm;

        Log.wtf(TAG, "1/8");

        shrPrf = PreferenceManager.getDefaultSharedPreferences(context);

        Log.wtf(TAG, "2/8");

        // Create the intent filter
        intentFilter = new IntentFilter();
        // Indicates a change in the Wi-Fi P2P status.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);
        // Indicates a change in the list of available peers.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);
        // Indicates the state of Wi-Fi P2P connectivity has changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
        // Indicates this device's details have changed.
        intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);

        Log.wtf(TAG, "3/8");

        // WifiP2pManager
        manager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        channel = manager.initialize(context, context.getMainLooper(), null);

        Log.wtf(TAG, "4/8");

        // Detect available peers that are in range.
        // get the list @onPeersAvailable
        peers = new ArrayList<>();
        serverPeer = new HashMap<>();
        manager.discoverPeers(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                // Command successful! Code isn't necessarily needed here,
                // Unless you want to update the UI or add logging statements.
                String TAG = getClass().getName() + "discoverPeers@onSuccess: ";

                Log.wtf(TAG, "OK");

            }

            @Override
            public void onFailure(int arg0) {
                // Command failed.  Check for P2P_UNSUPPORTED, ERROR, or BUSY
                String TAG = getClass().getName() + "discoverPeers@onFailure: ";

                Log.wtf(TAG, "Oops!");

            }

        });

        Log.wtf(TAG, "5/8");

        registerService();

        Log.wtf(TAG, "6/8");

        discoverService();

        Log.wtf(TAG, "7/8");

        // BroadcastReceiver
        receiver = new PmBroadcastReceiver(manager, channel, this);
        context.registerReceiver(receiver, intentFilter);

        Log.wtf(TAG, "8/8");

//        TODO
//        try {
//            server = new Thread(new PmServer(this.pm));
//            server.start();
//        } catch (IOException e) {
//            Log.wtf(TAG, e.getMessage());
//        }
//        TODO

        Log.wtf(TAG, "OK");
    }

    //
    public void onStop() {
        String TAG = getClass().getName() + "@onStop: ";

        Log.wtf(TAG, "...");

        disconnect();
////        TODO
//        if(server != null) {
//            Log.wtf(TAG, "server");
//            server.cancel(true);
//            server = null;
//
////            serverSocket = null;
//
//        }

        manager.stopPeerDiscovery(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "stopPeerDiscovery@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "stopPeerDiscovery@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });

        manager.cancelConnect(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "cancelConnect@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "cancelConnect@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });
        manager.removeGroup(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "removeGroup@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "removeGroup@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });

        if (serviceInfo != null) {
            manager.removeLocalService(channel, serviceInfo, new ActionListener() {
                @Override
                public void onSuccess() {
                    String TAG = getClass().getName() + "removeLocalService@onSuccess: ";
                    Log.wtf(TAG, "OK");
                }

                @Override
                public void onFailure(int reason) {
                    String TAG = getClass().getName() + "removeLocalService@onFailure: ";
                    Log.wtf(TAG, "Oops!");
                }
            });
        }

        manager.clearServiceRequests(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "clearServiceRequests@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "clearServiceRequests@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });
        manager.clearLocalServices(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "clearLocalServices@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "clearLocalServices@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });

        context.unregisterReceiver(receiver);

        Log.wtf(TAG, "OK");
    }

    // Register the PM service
    private void registerService() {
        String TAG = getClass().getName() + "@registerService: ";

        Map record = new HashMap();

        Log.wtf(TAG, "...");

//        try {
//            serverSocket = new ServerSocket(0);
//            serverSocket = new ServerSocket(Globals.pm_port);

//            serverSocket = new ServerSocket(); // <-- create an unbound socket first
//            serverSocket.setReuseAddress(true);
//            serverSocket.bind(new InetSocketAddress(Globals.pm_port)); // <-- now bind it

//            Log.wtf(TAG, serverSocket.toString());

//            record.put("port", String.valueOf(serverSocket.getLocalPort()));
        record.put("usr", shrPrf.getString(Globals.shr_user, "user"));
        record.put("dev", shrPrf.getString(Globals.shr_dev_id, "0"));
        record.put("pm", String.valueOf(pm.getId()));
        record.put("pref", String.valueOf(pm.getPreferences()));

        Log.wtf(TAG, record.toString());

//        } catch (IOException e) {
//            Log.wtf(TAG, e.getMessage());
//        }

        // Service information.  Pass it an instance name, service type
        // _protocol._transportlayer , and the map containing
        // information other devices will want once they connect to this one.
        serviceInfo = WifiP2pDnsSdServiceInfo.newInstance("_eh", "_ftp._tcp", record);


        Log.wtf(TAG, "Adding service...");

        // Add the local service, sending the service info, network channel,
        // and listener that will be used to indicate success or failure of
        // the request.
        manager.addLocalService(channel, serviceInfo, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "addLocalService@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "addLocalService@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });

    }

    //
    private void discoverService() {
        /**
         * Register listeners for DNS-SD services. These are callbacks invoked
         * by the system when a service is actually discovered.
         */
        manager.setDnsSdResponseListeners(channel,
                new DnsSdServiceResponseListener() {
                    @Override
                    public void onDnsSdServiceAvailable(String name, String type, WifiP2pDevice device) {
                        // A service has been discovered. Is this our app?
                        String TAG = getClass().getName() + "@onDnsSdServiceAvailable: ";

                        Log.wtf(TAG, "...");

                        Log.wtf(TAG, name + "" + type + "" + device.toString());

                    }
                }, new DnsSdTxtRecordListener() {
                    // A new TXT record is available. Pick up the advertised name.
                    @Override
                    public void onDnsSdTxtRecordAvailable(String domain, Map<String, String> record, WifiP2pDevice device) {
                        String TAG = getClass().getName() + "@onDnsSdTxtRecordAvailable: ";

                        Log.wtf(TAG, "...");

                        Log.wtf(TAG, "Found " + domain.toString() + record.toString() + device.toString());

                        if (record.containsKey("pm") && record.get("pm") != null &&
                                String.valueOf(pm.getId()).equals(record.get("pm"))) {

                            Map<String, String> m = new HashMap<>();
                            m.put("mac", device.deviceAddress);
                            m.putAll(record);

                            Log.wtf(TAG, "Checking " + m);

                            if (!peers.contains(m)) {
                                for (Map p : peers) {
                                    if (p.get("dev").equals(m.get("dev"))) {
                                        Log.wtf(TAG, "Updating " + m);
                                        p.putAll(m);
                                        return;
                                    }
                                }
                                Log.wtf(TAG, "Adding " + m);
                                peers.add(m);
                            }

                            pm.onPeersChanged(peers);
                        }
                    }
                }
        );

        /**
         * After attaching listeners, create a service request and initiate
         * discovery.
         */
        WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
        manager.addServiceRequest(channel, serviceRequest, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "addServiceRequest@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "addServiceRequest@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });
        manager.discoverServices(channel, new ActionListener() {
            @Override
            public void onSuccess() {
                String TAG = getClass().getName() + "discoverServices@onSuccess: ";
                Log.wtf(TAG, "OK");
            }

            @Override
            public void onFailure(int reason) {
                String TAG = getClass().getName() + "discoverServices@onFailure: ";
                Log.wtf(TAG, "Oops!");
            }
        });

    }

    /**
     * Read an input stream and covert it to readable string.
     *
     * @param deviceAddress the MAC address of the peer.
     */
    public void sendToPeer(String deviceAddress) {
        String TAG = getClass().getName() + "@sendToPeer: ";

        Log.wtf(TAG, deviceAddress);

        Map<String, String> peer = getPeer(deviceAddress);

        if (isConnected() && peer != null) {
            Log.wtf(TAG, peer.toString());
            serverPeer.putAll(peer);
            Log.wtf(TAG, serverPeer.toString());

            // client = new PmClient(serverPeer, pm);
            // client.execute();

            client = new Thread(new PmClient(serverPeer, this.pm));
            client.start();
        } else {
            connect(deviceAddress);
        }

    }

    /**
     *
     */
    public void connect(String deviceAddress) {
        String TAG = getClass().getName() + "@connect: ";

        Log.wtf(TAG, "Connecting to " + deviceAddress);

        WifiP2pConfig config = new WifiP2pConfig();
        config.deviceAddress = deviceAddress;
        config.wps.setup = WpsInfo.PBC;

        Log.wtf(TAG, "Config " + config.toString());

        Map<String, String> peer = getPeer(deviceAddress);

        if (peer != null) {
            serverPeer.putAll(peer);

            Log.wtf(TAG, "serverPeer == " + serverPeer.toString());
            manager.connect(channel, config, new ActionListener() {
                @Override
                public void onSuccess() {
                    String TAG = getClass().getName() + "@connect: ";
                    Log.wtf(TAG, "OK");
                }

                @Override
                public void onFailure(int reason) {
                    String TAG = getClass().getName() + "@connect: ";
                    Log.wtf(TAG, "error" + reason);
                }
            });
        }
    }

    /**
     *
     */
    public void disconnect() {
        String TAG = getClass().getName() + "@disconnect: ";

        Log.wtf(TAG, "...");

        setConnectionStatus(false);

        if (client != null) {
            Log.wtf(TAG, "client");
            client.interrupt();
            client = null;

//            serverPeer = null;

        }

        if (server != null) {
            Log.wtf(TAG, "server");
            server.interrupt();
            server = null;

//            serverSocket = null;

        }

    }

    /**
     *
     */
    public void setWifiP2pStatus(boolean status) {
        wifiP2pStatus = status;
    }

    /**
     *
     */
    public boolean isWifiP2pEnabled() {
        return wifiP2pStatus;
    }

    /**
     *
     */
    public void setConnectionStatus(boolean status) {
        this.connectionStatus = status;
    }

    /**
     *
     */
    public boolean isConnected() {
        return this.connectionStatus;
    }

    /**
     * Tne WifiP2pManager.requestConnectionInfo(Channel, ConnectionInfoListener)
     * returns connection info.
     */
    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {
        String TAG = getClass().getName() + "@onConnectionInfoAvailable: ";

        Log.wtf(TAG, "...");

        Log.wtf(TAG, info.toString());

        setConnectionStatus(true);

        // TODO: EVAL
        if (info.isGroupOwner) {
        // if(Build.PRODUCT.equals(Globals.eval_dev)) {
        // TODO: /EVAL
            Log.wtf(TAG, "Connected as server");

            /*
            if (serverSocket != null) {
            Log.wtf(TAG, "Executing server...");
                server = new PmServer(serverSocket, pm);
            server = new PmServer(pm);
            server.execute();
            } else {
                Log.wtf(TAG, "No server socket");
            }
            */

            server = new Thread(new PmServer(this.pm));
            server.start();
        } else {
            Log.wtf(TAG, "Connected as client");

            /*
            if (serverPeer != null) {
                Log.wtf(TAG, "Executing client...");
                serverPeer.put("ip", info.groupOwnerAddress.getHostAddress());
                Log.wtf(TAG, "serverPeer " + serverPeer.toString());
                sendToPeer(info.groupOwnerAddress.getHostAddress());
            } else {
                Log.wtf(TAG, "No server peer");
            }
            */

            if (serverPeer != null) {
                Log.wtf(TAG, "Executing client...");
                // TODO: EVAL
                serverPeer.put("ip", info.groupOwnerAddress.getHostAddress());   //
                // serverPeer.put("ip", Globals.eval_ip);
                Log.wtf(TAG, "serverPeer " + serverPeer.toString());
                sendToPeer(info.groupOwnerAddress.getHostAddress());             //
                // sendToPeer(serverPeer.get("ip"));
                // TODO: /EVAL
            } else {
                Log.wtf(TAG, "No server peer");
            }

        }

    }

    /**
     * Tne WifiP2pManager.requestPeers(Channel, PeerListListener) returns the new
     * WifiP2pDeviceList.
     * <p/>
     * Updates the:
     * - Peers list
     * - Service (PM) list
     */
    @Override
    public void onPeersAvailable(WifiP2pDeviceList peers) {
        String TAG = getClass().getName() + "@onPeersAvailable: ";

        Log.wtf(TAG, "...");

        boolean update = false;

        if (peers.getDeviceList().size() == 0) {
            Log.wtf(TAG, "No devices found");
            this.peers = new ArrayList<>();
            update = true;
        } else {
            // Update the peers list
            for (Map m : this.peers) {
                boolean remove = true;
                for (WifiP2pDevice d : peers.getDeviceList()) {
                    Log.wtf(TAG, m.get("mac") + " - " + d.deviceAddress);
                    if (m.get("mac").equals(d.deviceAddress)) {
                        Log.wtf(TAG, "Validated " + m);
                        remove = false;
                        break;
                    }
                }
                if (remove) {
                    Log.wtf(TAG, "Removing " + m);
                    this.peers.remove(m);
                    update = true;
                }
            }
        }

        if (update) {
            Log.wtf(TAG, "Going to update peers...");
            pm.onPeersChanged(this.peers);
        } else {
            Log.wtf(TAG, "Nothing changed");
        }


    }

    //
    public String getPeerAddress(int id) {
        String TAG = getClass().getName() + "@getPeerAddress: ";

        Log.wtf(TAG, "...");

        for (Map m : this.peers) {

            Log.wtf(TAG, m.get("dev") + " - " + id);

            if (Integer.parseInt((String) m.get("dev")) == id) {

                Log.wtf(TAG, m.get("mac").toString());

                return m.get("mac").toString();
            }
        }
        return "";
    }

    //
    public Map<String, String> getPeer(String deviceAddress) {
        String TAG = getClass().getName() + "@getPeer: ";

        Log.wtf(TAG, "...");
        Log.wtf(TAG, "Finding peer with address " + deviceAddress);

        for (Map m : this.peers) {
            Log.wtf(TAG, "" + deviceAddress + " - " + m.get("mac"));
            if (m.get("mac").equals(deviceAddress)) {
                Log.wtf(TAG, "Returning " + m.toString());
                return m;
            }
        }

        Log.wtf(TAG, "Could not find peer with address " + deviceAddress);
        return null;
    }

}
