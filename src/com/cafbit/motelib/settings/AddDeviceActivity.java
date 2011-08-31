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
import com.cafbit.motelib.model.Device;
import com.cafbit.motelib.model.DeviceClass;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnDismissListener;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class AddDeviceActivity extends Activity implements OnDeviceChange {

    private MoteContext moteContext;
    private View setupView = null;
    
    private static class State {
        public int state = 0;
        public boolean isUpdate = false;
        public Device device = null;
        public DeviceClass deviceClass = null;
        public DeviceSetupState deviceSetupState = null;
    };
    private State state = new State();
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.moteContext = MoteContext.getInstance(this);
        
        // check for a saved state which should be restored
        State savedState = (State) getLastNonConfigurationInstance();
        if (savedState != null) {
            this.state = savedState;
        }
        
        // process the intent extras
        if ((getIntent() != null) && (getIntent().getExtras() != null)) {       
            // should we jump directly to a specific device class?
            String deviceClassString = getIntent().getExtras().getString("device_class");
            if (deviceClassString != null) {
                for (DeviceClass dc : moteContext.getAllDeviceClasses()) {
                    if (dc.getDeviceCode().equals(deviceClassString)) {
                        this.state.deviceClass = dc;
                        break;
                    }
                }
            }
            
            // should we just directly to editing a specific device instance?
            int deviceId = getIntent().getExtras().getInt("device_id");
            if (deviceId != 0) {
                Device device = moteContext.getDeviceDao().getDeviceById(deviceId);
                if (savedState == null) {
                    this.state.device = device;
                    this.state.deviceClass = device.getDeviceClass();
                    this.state.isUpdate = true;
                }
            }
        }
    
        // support immediate probing of devices found via discovery
        int discoveredDeviceRef = 0;
        if ((getIntent() != null) && (getIntent().getExtras() != null)) {
            discoveredDeviceRef = getIntent().getExtras().getInt("discovered_device_ref");
        }
        if (discoveredDeviceRef > 0) {
            if (savedState == null) {
                Device device = (Device) moteContext.retreiveObject(discoveredDeviceRef);
                this.state.device = device;
                this.state.deviceClass = device.getDeviceClass();
            }
            transition(1);
        } else {
            if (this.state.deviceClass != null) {
                transition(1);
            } else {
                transition(0);
            }
        }
    }
    
    @Override
    public Object onRetainNonConfigurationInstance() {
        if (setupView instanceof OnSaveDeviceSetupState) {
            state.deviceSetupState = ((OnSaveDeviceSetupState)setupView).onSaveDeviceSetupState();
        }
        return state;
    }
    
    private void createList() {
        setTitle("Select a device type...");
        ListView listView = new ListView(this);
        listView.setAdapter(new DeviceClassListAdapter(this));
        setContentView(listView);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                AddDeviceActivity.this.state.deviceClass = 
                    (DeviceClass) parent.getItemAtPosition(position);
                AddDeviceActivity.this.transition(1);
            }
        });
    }
    
    private void createDeviceSetupView() {
        setTitle("Configure your "+moteContext.getDeviceWord(state.deviceClass, false, false)+"...");
        this.setupView = state.deviceClass.createDeviceSetupView(this, state.device, state.isUpdate, this, state.deviceSetupState);
        setContentView(this.setupView);
    }
    
    private void transition(int newState) {
        state.state = newState;
        switch (state.state) {
        case 0:
            createList();
            break;
        case 1:
            createDeviceSetupView();
            break;
        }
    }

    @Override
    public void onAddDevice(Device device) {
        try {
            moteContext.getDeviceDao().addDevice(device);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Error", "An error occured while saving your new "+moteContext.getDeviceWord(null, false, false)+":\n"+e.getMessage(), null);
            return;
        }
        
        alert(
            moteContext.getDeviceWord(null, false, true)+" added!",
            "Your "+moteContext.getDeviceWord(state.deviceClass, false, false)+" has been added.",
            new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            }
        );
    }
    
    @Override
    public void onUpdateDevice(Device device) {
        try {
            moteContext.getDeviceDao().updateDevice(device);
        } catch (Exception e) {
            e.printStackTrace();
            alert("Error", "An error occured while saving your "+moteContext.getDeviceWord(null, false, false)+":\n"+e.getMessage(), null);
            return;
        }
        
        alert(
            moteContext.getDeviceWord(null, false, true)+" updated!",
            "Your "+moteContext.getDeviceWord(state.deviceClass, false, false)+" has been updated.",
            new OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialogInterface) {
                    finish();
                }
            }
        );      
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
