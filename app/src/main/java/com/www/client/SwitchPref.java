package com.www.client;

import android.content.Context;
import android.preference.SwitchPreference;
import android.util.AttributeSet;

public class SwitchPref extends SwitchPreference {

    public SwitchPref(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public SwitchPref(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwitchPref(Context context) {
        super(context);
    }

    @Override
    protected void onClick() {
        //super.onClick();
    }
}