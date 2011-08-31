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

package com.cafbit.motelib.model;

import java.io.IOException;

import com.cafbit.motelib.discovery.DiscoveryManagerThread;
import com.cafbit.motelib.discovery.MDNSDiscoveryHandler;
import com.cafbit.motelib.settings.DeviceSetupState;
import com.cafbit.motelib.settings.OnDeviceChange;
import com.cafbit.netlib.NetworkManagerThread;
import com.cafbit.netlib.ReceiverThread;

import android.content.Context;
import android.view.View;

public interface DeviceClass {
    public String getDeviceCode();
    public String getDeviceName();
    public String getDeviceDescription();
    public Class<? extends Device> getDeviceType();
    public View createDeviceSetupView(Context context, Device device, boolean isUpdate, OnDeviceChange onDeviceChange, DeviceSetupState deviceSetupState);
    public ReceiverThread getCustomDiscoveryReceiverThread(NetworkManagerThread networkManager) throws IOException;
    public MDNSDiscoveryHandler getMDNSDiscoveryHandler(DiscoveryManagerThread networkManager) throws IOException;
}
