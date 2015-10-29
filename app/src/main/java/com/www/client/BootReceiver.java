package com.www.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class BootReceiver extends BroadcastReceiver {
	private static final String TAG = "BootReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive: " + "...");
		if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			//context.startService(new Intent(context, TaskService.class));
			context.startService(new Intent(context, TaskService.class));
        }
		
	}

}