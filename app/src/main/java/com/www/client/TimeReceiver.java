package com.www.client;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class TimeReceiver extends BroadcastReceiver {
	private static final String TAG = "TimeReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i(TAG, "onReceive: " + "...");
		if (intent.getAction().equals("TASK_SERVICE_ON")) {
			context.startService(new Intent(context, TaskService.class));
        }
		else if (intent.getAction().equals("TASK_SERVICE_OFF")) {
			context.startService(new Intent(context, TaskService.class));
        }
		
	}

}