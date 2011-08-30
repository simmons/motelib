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

package com.cafbit.motelib.settings;

import com.cafbit.motelib.MoteContext;
import com.cafbit.motelib.R;
import com.cafbit.motelib.discovery.DiscoveryActivity;
import com.cafbit.motelib.model.Device;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.AdapterView.AdapterContextMenuInfo;

// http://www.kaloer.com/android-preferences

public class DevicesActivity extends ListActivity {
	
	private LayoutInflater inflater;
	private View addNewDeviceView;
	private View discoverDevicesView;
	private DeviceListAdapter adapter;
	private MoteContext moteContext;
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
		this.moteContext = MoteContext.getInstance(this);
        
        this.inflater = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.adapter = new DeviceListAdapter(this);

        // Add custom items
        addNewDeviceView = createHeaderView(
        	"Add a new "+moteContext.getDeviceWord(null, false, false)+"...",
            "Manually add a new "+moteContext.getDeviceWord(null, false, false)+"."
        );
        getListView().addFooterView(addNewDeviceView);
        discoverDevicesView = createHeaderView(
        	"Discover "+moteContext.getDeviceWord(null, true, false)+"...",
            "Discover "+moteContext.getDeviceWord(null, true, false)+" on the local network."
        );
        getListView().addFooterView(discoverDevicesView);
        
        // Bind to our new adapter.
        setListAdapter(adapter);
        
        registerForContextMenu(getListView());
    }
	
	@Override
	public void onContentChanged() {
		super.onContentChanged();
	}
	
	@Override
	protected void onPause() {
		super.onPause();
	}

	@Override
	protected void onResume() {
		super.onResume();
		adapter.loadFromDao();
	}

	private View createHeaderView(String line1, String line2) {
		View view = inflater.inflate(android.R.layout.simple_list_item_2, getListView(), false);
        TextView text = (TextView) view.findViewById(android.R.id.text1);
        text.setText(line1);
        TextView text2 = (TextView) view.findViewById(android.R.id.text2);
        text2.setText(line2);
        return view;
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		
		if ((v.equals(getListView())) && (menuInfo instanceof AdapterContextMenuInfo)) {
			AdapterContextMenuInfo acmu = (AdapterContextMenuInfo)menuInfo;
			if (acmu.id != -1) {
				onDeviceCreateContextMenu((Device)adapter.getItem((int)acmu.id), menu);
			}
		}
		
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
		onDeviceContextSelected((Device)adapter.getItem((int)info.id), item.getItemId());
		return true;
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		if (id == -1) {
			if (v == addNewDeviceView) {
				onAddNewDevice();
			} else if (v == discoverDevicesView) {
				onDiscoverDevices();
			}
		} else {
			onDeviceSelected((Device)adapter.getItem((int)id));
		}
	}
	
	protected void onDeviceSelected(Device device) {
	}
	
	protected void onDeviceCreateContextMenu(Device device, ContextMenu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.devicecontext, menu);
		menu.setHeaderTitle(device.getHeadline());		
	}

	protected void onDeviceContextSelected(Device device, int menuItemId) {
		switch (menuItemId) {
		case R.id.edit:
	    	Intent intent = new Intent(this, AddDeviceActivity.class);
	    	intent.putExtra("device_id", device.id);
	    	startActivity(intent);			
			return;
		case R.id.delete:
			try {
				moteContext.getDeviceDao().removeDevice(device);
			} catch (Exception e) {
				e.printStackTrace();
				alert("Error", "An error occured while deleting the "+moteContext.getDeviceWord(null, false, false)+":\n"+e.getMessage(), null);
				return;
			}
			adapter.loadFromDao();
			return;
	    }
	}
	
	protected void onAddNewDevice() {
    	Intent intent = new Intent(this, AddDeviceActivity.class);
    	startActivity(intent);
	}
	
	protected void onDiscoverDevices() {
    	Intent intent = new Intent(this, DiscoveryActivity.class);
    	startActivity(intent);
	}

	private void alert(String title, String message, OnDismissListener onDismissListener) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setPositiveButton("OK", null);
		AlertDialog alertDialog = builder.create();
		alertDialog.setTitle(title);
		alertDialog.setMessage(message);
		if (onDismissListener != null) {
			alertDialog.setOnDismissListener(onDismissListener);
		}
		alertDialog.show();
		return;		
	}

}
