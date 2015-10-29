package com.www.client.pm;

import android.os.AsyncTask;
import android.util.Log;

import com.www.client.Globals;
import com.www.client.Utils;

import java.io.*;
import java.net.*;
import java.util.Map;

//public class PmClient extends AsyncTask {

public class PmClient implements Runnable {


    Map<String, String> peer;
    PrivacyMechanism pm;

    public PmClient(Map<String, String> peer, PrivacyMechanism privacyMechanism) {
        this.peer = peer;
        this.pm = privacyMechanism;
    }

    @Override
    public void run() {
        String TAG = getClass().getName()  + "@doInBackground: ";

        Log.wtf(TAG, "...");

        Socket socket = new Socket();

        Log.wtf(TAG, peer.toString());

        try {
            socket.bind(null);
            socket.connect((new InetSocketAddress(peer.get("ip"),
//                                                    Integer.valueOf(peer.get("port")))), 500);
                                                    Globals.pm_port)), 500);

            OutputStream os = socket.getOutputStream();

            File data = new File(Globals.pms_dir + "/" + pm.getId() + "/" + peer.get("dev") + ".dat");

            if(data.exists()) {
                Log.wtf(TAG, "Sending " + data.getPath());

                RandomAccessFile raf = new RandomAccessFile(data, "r");
                Utils.returnPart(raf, os, 0);

                Utils.close(os);
                Utils.close(raf);

                data.delete();
            } else {
                Log.wtf(TAG, "No data " + data.getPath());
            }

        } catch (FileNotFoundException e) {
            Log.wtf(TAG, e.getMessage());
        } catch (IOException e) {
            Log.wtf(TAG, e.getMessage());
        } finally {
            if (socket != null) {
                if (socket.isConnected()) {
                    try {
                        Log.wtf(TAG, "Closing socket...");
                        socket.close();
                    } catch (IOException e) {
                        Log.wtf(TAG, e.getMessage());
                    }
                }
            }
        }
    }

//    @Override
//    protected Object doInBackground(Object[] params) {
//        String TAG = getClass().getName()  + "@doInBackground: ";
//
//        Log.wtf(TAG, "...");
//
//        Socket socket = new Socket();
//
//        Log.wtf(TAG, peer.toString());
//
//        try {
//            socket.bind(null);
//            socket.connect((new InetSocketAddress(peer.get("ip"),
//                    Integer.valueOf(peer.get("port")))), 500);
//
//            OutputStream os = socket.getOutputStream();
//
////            String dir = Globals.pms_dir + "/" + pm.getId();
////            File data = null;
////            for(File file : new File(dir).listFiles()) {
////                if(file.getName().endsWith(".dat.tmp")) {
////                    data = file;
////                    break;
////                }
////            }
//
//            File data = new File(Globals.pms_dir + "/" + pm.getId() + "/" + peer.get("dev") + ".dat");
//
//            if(data.exists()) {
//                Log.wtf(TAG, "Sending " + data.getPath());
//
//                RandomAccessFile raf = new RandomAccessFile(data, "r");
//                Utils.returnPart(raf, os, 0);
//
//                Utils.close(os);
//                Utils.close(raf);
//
//                data.delete();
//            } else {
//                Log.wtf(TAG, "No data " + data.getPath());
//            }
//
//        } catch (FileNotFoundException e) {
//            Log.wtf(TAG, e.getMessage());
//        } catch (IOException e) {
//            Log.wtf(TAG, e.getMessage());
//        }
//
//        finally {
//            if (socket != null) {
//                if (socket.isConnected()) {
//                    try {
//                        socket.close();
//                    } catch (IOException e) {
//                        Log.wtf(TAG, e.getMessage());
//                    }
//                }
//            }
//        }
//
//        return null;
//    }

}
