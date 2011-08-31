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

package com.cafbit.motelib.discovery;

import java.util.Collections;

import com.cafbit.motelib.MoteApplication;
import com.cafbit.motelib.MoteContext;
import com.cafbit.motelib.R;
import com.cafbit.motelib.model.Device;
import com.cafbit.motelib.settings.AddDeviceActivity;
import com.cafbit.motelib.settings.DeviceListAdapter;
import com.cafbit.netlib.ipc.Command;
import com.cafbit.netlib.ipc.CommandHandler;
import com.cafbit.netlib.ipc.CommandListener;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.AdapterView.OnItemClickListener;

public class DiscoveryActivity extends Activity implements CommandListener {
    
    public static final String TAG = "Discovery";
    
    private ListView listView;
    private DiscoveryCommandHandler discoveryCommandHandler =
        new DiscoveryCommandHandler(this);
    private DiscoveryManagerThread discoveryManagerThread;
    private DeviceListAdapter deviceListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.discover);
        listView = (ListView)this.findViewById(R.id.list);
        listView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Device device = 
                    (Device) parent.getItemAtPosition(position);

                Intent intent = new Intent(DiscoveryActivity.this, AddDeviceActivity.class);
                
                int deviceId = MoteContext.getInstance(DiscoveryActivity.this).storeObject(device);
                intent.putExtra("discovered_device_ref", deviceId);
                startActivity(intent);
                finish();
            }
        });
    }
    
    /**
     * This is called when the user resumes using the activity
     * after using other programs (and at activity creation time).
     * 
     * We don't keep the network thread running when the user is
     * not running this program in the foreground, so we use this
     * method to initialize the packet list and start the
     * network thread.
     */
    @Override
    protected void onResume() {
        super.onResume();
        
        deviceListAdapter = new DeviceListAdapter(this,Collections.<Device>emptyList());
        listView.setAdapter(deviceListAdapter);

        if (discoveryManagerThread != null) {
            Log.e(TAG, "discoveryManagerThread should be null!");
            discoveryManagerThread.ipc.quit();
        }
        discoveryManagerThread = new DiscoveryManagerThread(this, discoveryCommandHandler);
        discoveryManagerThread.start();
    }

    /**
     * This is called when the user leaves the activity to run
     * another program.  We stop the network thread when this
     * happens.
     */
    @Override
    protected void onPause() {
        super.onPause();
        
        deviceListAdapter.clear();
        deviceListAdapter = null;
        
        if (discoveryManagerThread == null) {
            Log.e(TAG, "discoveryManagerThread should not be null!");
            return;
        }
        discoveryManagerThread.ipc.quit();
        discoveryManagerThread = null;
    }

    
    @Override
    public void onCommand(Command command) {
        if (command instanceof DeviceCommand) {
            DeviceCommand deviceCommand = (DeviceCommand)command;
            deviceListAdapter.addDevice(deviceCommand.device);
        }
    }

    public class DiscoveryCommandHandler extends CommandHandler {
        public DiscoveryCommandHandler(CommandListener commandListener) {
            super(commandListener);
        }
    }
    
    public static class DeviceCommand implements Command {
        public Device device;
        public DeviceCommand(Device device) {
            this.device = device;
        }
    }

}
