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

package com.cafbit.motelib;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.res.Resources;

import com.cafbit.motelib.dao.DeviceDao;
import com.cafbit.motelib.model.Device;
import com.cafbit.motelib.model.DeviceClass;
import com.cafbit.xmlfoo.XmlFoo;

public abstract class MoteContext {
    
    protected Context context;
    private DeviceDao deviceDao;
    
    /**
     * Return the MoteContext singleton for this application.
     * This singleton object is fully implemented by each application
     * to allow per-app customizations.
     * 
     * We ask for an activity so we can fetch the Application object
     * via getApplication().  The application object must implement
     * the MoteApplication interface.
     * 
     * We don't use a Context parameter, since it's not clear to me
     * that the Context.getApplicationContext() method will always
     * return the Application object in the future.  (Even though it
     * seems to, today.)
     * 
     * @param activity
     * @return The MoteContext singleton for the application.
     */
    public static MoteContext getInstance(Activity activity) {
        
        Application application = activity.getApplication();
        if (! (application instanceof MoteApplication)) {
            throw new RuntimeException("Application singleton must implement MoteApplication.");
        }
        MoteApplication moteApplication = (MoteApplication)application;
        return moteApplication.getMoteContext();
    }
    
    public MoteContext(Context context) {
        this.context = context;
        
        Resources r = context.getResources();
    }
    
    public String getDeviceWord(DeviceClass deviceClass, boolean plural, boolean capitalized) {
        String word;
        if (plural) {
            if (capitalized) {
                word = "Devices";
            } else {
                word = "devices";
            }
        } else {
            if (capitalized) {
                word = "Device";
            } else {
                word = "device";
            }
        }
        if (deviceClass != null) {
            return deviceClass.getDeviceName()+" "+word;
        } else {
            return word;
        }
    }
    
    public DeviceDao getDeviceDao() {
        if (deviceDao == null) {
            deviceDao = new DeviceDao(context,this);
        }
        return deviceDao;
    }
    
    public void coldReset() {
        getDeviceDao().coldReset();
        System.out.println("cold reset");
    }
    
    private XmlFoo xmlFoo = null;
    public XmlFoo getXmlFoo() {
        if (xmlFoo != null) {
            return xmlFoo;
        }
        
        xmlFoo = new XmlFoo();
        for (DeviceClass dc : getAllDeviceClasses()) {
            xmlFoo.addDiscriminatorClass(Device.class, dc.getDeviceCode(), dc.getDeviceType());
            xmlFoo.addSingleton(dc);
        }
        
        return xmlFoo;
    }
    
    // Poor man's object proxy system to send an object from
    // one activity to another.  This violates the.. ahem...
    // "intent" of the Intent system and breaks inter-process
    // Activity transitions.  However, for in-app Activity
    // transitions, this should be sufficient.
    private int nextStorageId = 1;
    private Map<Integer,Object> storage = new HashMap<Integer,Object>();
    public int storeObject(Object object) {
        int id = nextStorageId++;
        storage.put(id, object);
        return id;
    }
    public Object retreiveObject(int id) {
        return storage.remove(id);
    }


    public abstract List<DeviceClass> getAllDeviceClasses();
    public abstract int getSettingsXmlResource();
}
