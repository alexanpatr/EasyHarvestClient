package com.www.client;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.List;

public class ItemArrayAdapter extends ArrayAdapter<JSONObject> {

    private static final String TAG = "ItemArrayAdapter";
    Context context;

    public ItemArrayAdapter(Context context, int resourceId, List<JSONObject> items) {
        super(context, resourceId, items);
        this.context = context;
    }

    /*private view holder class*/
    private class ViewHolder {
        TextView textView;
        ImageView imageView;
    }

    public View getView(int position, View view, ViewGroup group) {
        ViewHolder holder = null;
        JSONObject item = getItem(position);

        LayoutInflater inflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (view == null) {
            view = inflater.inflate(R.layout.item, null);
            holder = new ViewHolder();
            holder.textView = (TextView) view.findViewById(R.id.text);
            view.setTag(holder);
        }
        else {
            holder = (ViewHolder) view.getTag();
        }

        try {
            holder.textView.setText(item.getString("label"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return view;
    }
}