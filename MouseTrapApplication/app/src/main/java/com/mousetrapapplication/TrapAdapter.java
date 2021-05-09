package com.mousetrapapplication;

import android.app.LauncherActivity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class TrapAdapter extends ArrayAdapter<TrapStatus> {
    private Context mContext;
    private int mResource;

    public TrapAdapter(@NonNull Context context, int resource, @NonNull ArrayList<TrapStatus> objects) {
        super(context, resource, objects);

        this.mContext = context;
        this.mResource = resource;

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater layoutInflater = LayoutInflater.from(mContext);

        convertView = layoutInflater.inflate(mResource, parent, false);

        ImageView imageView = (ImageView) convertView.findViewById(R.id.image);

        TextView txtStatus = (TextView) convertView.findViewById(R.id.txtStatus);

        TextView txtDate = (TextView) convertView.findViewById(R.id.txtDate);

        imageView.setImageResource(getItem(position).getImage());

        txtStatus.setText(getItem(position).getStatus());

        txtDate.setText(getItem(position).getDate().toString());

        return convertView;
    }
}
