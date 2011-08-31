/*
 * Copyright 2011 David Simmons
 * http://cafbit.com/
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/**
 * 
 */
package com.cafbit.motelib.settings;

import java.util.ArrayList;
import java.util.List;

import com.cafbit.motelib.MoteContext;
import com.cafbit.motelib.dao.DeviceDao;
import com.cafbit.motelib.model.Device;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class DeviceListAdapter extends BaseAdapter {
    
    private Activity activity;
    private LayoutInflater inflater;
    private List<Device> devices;
    
    public DeviceListAdapter(Activity activity) {
        init(activity, null);
    }

    public DeviceListAdapter(Activity activity, List<Device> devices) {
        init(activity, devices);
    }

    private void init(Activity activity, List<Device> devices) {
        this.activity = activity;
        this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
        if (devices == null) {
            loadFromDao();
        } else {
            this.devices = new ArrayList<Device>(devices);
        }
    }
    
    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public Object getItem(int position) {
        return this.devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        String line1, line2;
        View view;
        TextView text;

        if (convertView == null) {
            view = inflater.inflate(android.R.layout.simple_list_item_2, parent, false);
        } else {
            view = convertView;
        }
        
        int numDevices = this.devices.size();
        if ((position >= 0) && (position < numDevices)) {
            line1 = this.devices.get(position).getHeadline();
            line2 = "" + this.devices.get(position).getDescription();
        } else {
            // this never happens
            line1 = "";
            line2 = "";
        }
        
        text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(line1);
        
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        text2.setText(line2);

        return view;
    }
    
    public void addDevice(Device device) {
        int idx;
        if ((idx = devices.indexOf(device)) != -1) {
            // overwrite an existing element, if the specified
            // device already matches one in our list.
            devices.set(idx, device);
        } else {
            devices.add(device);
        }
        notifyDataSetChanged();
    }
    
    public void clear() {
        devices.clear();
        notifyDataSetChanged();
    }
    
    public void loadFromDao() {
        DeviceDao deviceDao = MoteContext.getInstance(activity).getDeviceDao();
        this.devices = new ArrayList<Device>(deviceDao.getAllDevices());
        notifyDataSetChanged();
    }
}