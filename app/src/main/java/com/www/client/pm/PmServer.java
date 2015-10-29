package com.www.client.pm;

import android.os.AsyncTask;
import android.os.Handler;
import android.util.Log;

import com.www.client.Globals;
import com.www.client.Utils;

import java.io.*;
import java.net.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

//public class PmServer extends AsyncTask {

public class PmServer implements Runnable {
    ServerSocket ss;
    PrivacyMechanism pm;

    public PmServer(PrivacyMechanism pm) {
        String TAG = getClass().getName() + "";
        this.pm = pm;
        try {
            this.ss = new ServerSocket(Globals.pm_port);
            Log.wtf(TAG, "Socket opened");
        } catch (IOException e) {
//            Log.wtf(TAG, e.getMessage());
            e.printStackTrace();
        }

    }


    @Override
    public void run() {
        String TAG = getClass().getName() + "@run";

        while (!Thread.currentThread().isInterrupted()) {
            try {
                Log.wtf(TAG, "Waiting for client...");

                Socket s = ss.accept();

                try {
                    // Read from the InputStream
                    Log.wtf(TAG, "1/6");

                    InputStream is = s.getInputStream();

                    Log.wtf(TAG, "2/6");

                    pm.aggregateData(is);

                    Log.wtf(TAG, "3/6");

                    Utils.close(is);

                    Log.wtf(TAG, "4/6");

                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        s.close();

                        Log.wtf(TAG, "5/6");

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

            } catch (IOException e) {
                try {

                    Log.wtf(TAG, "6/6");

                    if (ss != null && !ss.isClosed()) {
                        ss.close();
                    }
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
                e.printStackTrace();
                break;
            }
        }
    }

}
