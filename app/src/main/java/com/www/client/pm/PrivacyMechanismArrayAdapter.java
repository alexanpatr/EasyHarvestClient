package com.www.client.pm;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.www.client.ClientActivity;
import com.www.client.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class PrivacyMechanismArrayAdapter extends ArrayAdapter<String> {

    private static final String TAG = "PrivacyMechanismArrayAdapter";
    Context context;

    public PrivacyMechanismArrayAdapter(Context context, int resourceId, List<String> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView nameView;
        TextView userView;
        TextView statusView;
    }

    public View getView(int position, View view, ViewGroup group) {
        ViewHolder holder = null;
        JSONObject item = null;
        try {
            item = new JSONObject(getItem(position));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.privacy_mechanism_item, null);
            holder = new ViewHolder();
            holder.nameView = (TextView) view.findViewById(R.id.name);
            holder.userView = (TextView) view.findViewById(R.id.user);
            holder.statusView = (TextView) view.findViewById(R.id.status);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        try {
            if (item != null) {
                holder.nameView.setText(item.getJSONArray("name").getString(0));
                holder.userView.setText("by " + item.getJSONArray("user").getString(0));
                if(ClientActivity.sharedPref.getString("pmID", null) != null &&
                        item.getJSONArray("id").getString(0).equals(ClientActivity.sharedPref.getString("pmID", null))) {
                    holder.statusView.setText("INSTALLED");
                } else {
                    holder.statusView.setText(" ");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return view;
    }
}
