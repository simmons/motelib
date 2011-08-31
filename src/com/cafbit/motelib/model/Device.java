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

import com.cafbit.xmlfoo.annotations.Discriminator;
import com.cafbit.xmlfoo.annotations.Singleton;

public abstract class Device {
    public int id = 0;
    @Discriminator
    @Singleton
    public DeviceClass deviceClass;
    public String address;
    
    public int getId() {
        return id;
    }
    
    public String getClassDiscriminator() {
        return deviceClass.getDeviceCode();
    }

    public DeviceClass getDeviceClass() {
        return deviceClass;
    }
    
    public String getHostname() {
        return address;
    }
    
    public String getHeadline() {
        return deviceClass.getDeviceName();
    }
    
    public abstract String getDescription();

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result
                + ((deviceClass == null) ? 0 : deviceClass.getClass().hashCode());
        result = prime * result
                + ((address == null) ? 0 : address.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        
        Device other = (Device) obj;
        if (deviceClass == null) {
            if (other.deviceClass != null)
                return false;
        } else if (other.deviceClass == null) {
            return false;
        } else if (!deviceClass.getClass().equals(other.deviceClass.getClass()))
            return false;
        
        if (address == null) {
            if (other.address != null)
                return false;
        } else if (!address.equals(other.address))
            return false;
        return true;
    }
    
    
}
