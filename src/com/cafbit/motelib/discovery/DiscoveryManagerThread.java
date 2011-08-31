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

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.ArrayList;
import java.util.List;

import com.cafbit.motelib.MoteContext;
import com.cafbit.motelib.model.DeviceClass;
import com.cafbit.netlib.AbstractDatagramManagerThread;
import com.cafbit.netlib.MDNSPacketEntry;
import com.cafbit.netlib.MDNSReceiverThread;
import com.cafbit.netlib.ReceiverThread;
import com.cafbit.netlib.ipc.Command;
import com.cafbit.netlib.ipc.CommandHandler;
import com.cafbit.netlib.ipc.CommandListener;
import com.cafbit.netlib.ipc.ErrorCommand;
import com.cafbit.netlib.ipc.QuitCommand;

import android.content.Context;
import android.util.Log;

/**
 * This thread runs in the background while the user has our
 * program in the foreground, and handles sending mDNS queries
 * and processing incoming mDNS packets.
 * @author simmons
 */
public class DiscoveryManagerThread extends AbstractDatagramManagerThread implements CommandListener {
    
    public static final String TAG = "Mote";
    
    private DiscoveryActivity activity;
    private MDNSReceiverThread mdnsThread;
    private List<MDNSDiscoveryHandler> mdnsDiscoveryHandlers =
        new ArrayList<MDNSDiscoveryHandler>();
    
    /**
     * Construct the network thread.
     * @param activity
     */
    public DiscoveryManagerThread(DiscoveryActivity activity, CommandHandler handler) {
        super("discovery-manager", (Context)(activity.getApplicationContext()), handler);
        this.activity = activity;
    }

    @Override
    public void init() throws IOException {
        MoteContext moteContext = MoteContext.getInstance(activity);
        List<DeviceClass> deviceClasses = moteContext.getDeviceDao().getAllDeviceClasses();
        for (DeviceClass dc : deviceClasses) {
            // custom discovery systems
            ReceiverThread receiverThread = dc.getCustomDiscoveryReceiverThread(this);
            if (receiverThread != null) {
                addReceiverThread(receiverThread);
            }
            // mDNS discovery handlers
            MDNSDiscoveryHandler mdnsDiscoveryHandler =
                dc.getMDNSDiscoveryHandler(this);
            if (mdnsDiscoveryHandler != null) {
                // start an mDNS thread if at least one handler
                // is present.
                if (mdnsThread == null) {
                    mdnsThread = new MDNSReceiverThread(this);
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    addReceiverThread(mdnsThread);
                }
                // add the mdns discovery handler
                mdnsDiscoveryHandlers.add(mdnsDiscoveryHandler);
            }
        }
    }
    
    protected void beforeLoop() {
        for (MDNSDiscoveryHandler handler : mdnsDiscoveryHandlers) {
            handler.start();
        }
    }

    protected void afterLoop() {
        for (MDNSDiscoveryHandler handler : mdnsDiscoveryHandlers) {
            handler.stop();
        }
    }
    
    @Override
    protected void handleIncoming(DatagramSocket socket, DatagramPacket response) {
        /*
        Log.v(TAG, String.format("received: offset=0x%04X (%d) length=0x%04X (%d)", response.getOffset(), response.getOffset(), response.getLength(), response.getLength()));
        Log.v(TAG, Util.hexDump(response.getData(), response.getOffset(), response.getLength()));
        */
    }
    
    /**
     * Transmit an mDNS query on the local network.
     * @param host
     * @throws IOException
     */
    private void sendMDNSquery(String host) throws IOException {
        if (mdnsThread != null) {
            System.out.println("mDNS QUERY: "+host);
            mdnsThread.sendQuery(host);
        }
    }

    ////////////////////////////////////////////////////////////
    // inter-process communication
    ////////////////////////////////////////////////////////////
    
    public class IPCHelper {
        public void sendMDNSQuery(String host) {
            if (handler != null) {
                handler.sendCommand(new MDNSQueryCommand(host));
            }
        }
        public void quit() {
            if (handler != null) {
                handler.sendCommand(new QuitCommand());
            }
        }
    }
    public IPCHelper ipc = new IPCHelper();
    
    private static class MDNSQueryCommand implements Command {
        public String host;
        public MDNSQueryCommand(String host) { this.host = host; }
    }
    // COMMAND:
    @Override
    public void onCommand(Command command) {
        super.onCommand(command);
        if (command instanceof MDNSQueryCommand) {
            try {
                sendMDNSquery(((MDNSQueryCommand)command).host);
            } catch (IOException e) {
                upstreamHandler.sendCommand(new ErrorCommand(new RuntimeException(e)));
                Log.e(TAG, "onCommand(query): ",e);
            }
        } else if (command instanceof DiscoveryActivity.DeviceCommand) {
            this.upstreamHandler.sendCommand(command);
        } else if (command instanceof MDNSPacketEntry) {
            for (MDNSDiscoveryHandler handler : mdnsDiscoveryHandlers) {
                handler.handleMessage(((MDNSPacketEntry)command).message);
            }
        }
    }

}
