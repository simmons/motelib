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

import java.util.List;

import com.cafbit.motelib.MoteContext;
import com.cafbit.motelib.dao.DeviceDao;
import com.cafbit.motelib.model.DeviceClass;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

class DeviceClassListAdapter extends BaseAdapter {
    
    private Activity activity;
    private LayoutInflater inflater;
    private List<DeviceClass> deviceClasses;
    
    private DeviceDao deviceDao;
    
    public DeviceClassListAdapter(Activity activity) {
        init(activity, false);
    }

    private void init(Activity activity, boolean showAddNewDevices) {
        this.activity = activity;
        this.inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.deviceDao = MoteContext.getInstance(activity).getDeviceDao();
        this.deviceClasses = deviceDao.getAllDeviceClasses();
    }

    @Override
    public int getCount() {
        return this.deviceClasses.size();
    }

    @Override
    public Object getItem(int position) {
        return this.deviceClasses.get(position);
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
        
        int numDevices = this.deviceClasses.size();
        if (position < numDevices) {
            line1 = this.deviceClasses.get(position).getDeviceName();
            line2 = "" + this.deviceClasses.get(position).getDeviceDescription();
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
}