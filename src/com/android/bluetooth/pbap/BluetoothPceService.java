/*
 * Copyright (C) 2014 Copyright (C) Tieto Corporation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.bluetooth.pbap;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothPce;
import android.bluetooth.IBluetoothPce;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import com.android.bluetooth.Utils;

import java.io.IOException;

public class BluetoothPceService extends Service {
    private static final String TAG = "BluetoothPceService";

    public static final boolean DEBUG = false;

    public static final boolean VERBOSE = false;

    private static final String BLUETOOTH_PERM = android.Manifest.permission.BLUETOOTH;

    private static final String BLUETOOTH_ADMIN_PERM = android.Manifest.permission.BLUETOOTH_ADMIN;

    private BluetoothAdapter mAdapter;

    private BluetoothDevice mRemoteDevice = null;

    private int mState;

    private int mStartId = -1;

    private PbapObexClient mPbapObexClient;

    public BluetoothPceService() {
        mState = BluetoothPce.STATE_DISCONNECTED;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (VERBOSE)
            Log.v(TAG, "Pce Service onCreate");
        mAdapter = BluetoothAdapter.getDefaultAdapter();

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        mStartId = startId;
        if (mAdapter == null) {
            Log.w(TAG, "Stopping BluetoothPceService: "
                    + "device does not have BT or device is not ready");
            // Release all resources
            closeService();
        }
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        if (VERBOSE)
            Log.v(TAG, "Pce Service onDestroy");

        super.onDestroy();
        setState(BluetoothPce.STATE_DISCONNECTED);
        closeService();
    }

    @Override
    public IBinder onBind(Intent intent) {
        if (VERBOSE)
            Log.v(TAG, "Pce Service onBind");
        return mBinder;
    }

    private final void closeService() {
        if (VERBOSE)
            Log.v(TAG, "Pce Service closeService in");

        if (mPbapObexClient != null) {
            mPbapObexClient.disconnect();
            mState = BluetoothPce.STATE_DISCONNECTED;
        }
    }

    private void setState(int state) {
        mState = state;
    }

    /**
     * Handlers for incoming service calls
     */
    private final IBluetoothPce.Stub mBinder = new IBluetoothPce.Stub() {
        @Override
        public int getState() {
            if (DEBUG)
                Log.d(TAG, "getState " + mState);

            if (!Utils.checkCaller()) {
                Log.w(TAG, "getState(): not allowed for non-active user");
                return BluetoothPce.STATE_DISCONNECTED;
            }

            enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            return mState;
        }

        @Override
        public BluetoothDevice getPse() {
            if (DEBUG)
                Log.d(TAG, "getClient" + mRemoteDevice);

            if (!Utils.checkCaller()) {
                Log.w(TAG, "getClient(): not allowed for non-active user");
                return null;
            }

            enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            if (mState == BluetoothPce.STATE_DISCONNECTED) {
                return null;
            }
            return mRemoteDevice;
        }

        @Override
        public boolean isConnected(BluetoothDevice device) {
            if (!Utils.checkCaller()) {
                Log.w(TAG, "isConnected(): not allowed for non-active user");
                return false;
            }

            enforceCallingOrSelfPermission(BLUETOOTH_PERM, "Need BLUETOOTH permission");
            return mState == BluetoothPce.STATE_CONNECTED && mRemoteDevice.equals(device);
        }

        @Override
        public boolean connect(BluetoothDevice device) {
            if (!Utils.checkCaller()) {
                Log.w(TAG, "connect(): not allowed for non-active user");
                return false;
            }

            enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH_ADMIN permission");
            mRemoteDevice = device;
            if (mPbapObexClient == null) {
                mPbapObexClient = new PbapObexClient(device);
            }
            try {
                return mPbapObexClient.connect();
            } catch (IOException e) {
                Log.e(TAG, "connect(): exception " + e.getMessage());
                return false;
            }
        }

        @Override
        public void disconnect() {
            if (DEBUG)
                Log.d(TAG, "disconnect");

            if (!Utils.checkCaller()) {
                Log.w(TAG, "disconnect(): not allowed for non-active user");
                return;
            }

            enforceCallingOrSelfPermission(BLUETOOTH_ADMIN_PERM, "Need BLUETOOTH_ADMIN permission");
            if (mPbapObexClient != null) {
                mPbapObexClient.disconnect();
            }
            mState = BluetoothPce.STATE_DISCONNECTED;
        }

        @Override
        public String pullPhonebook(String nameOfPath, int propSelector) throws RemoteException {
            if (mPbapObexClient != null) {
                try {
                    return mPbapObexClient.pullPhonebook(nameOfPath, propSelector);
                } catch (IOException e) {
                    Log.e(TAG, "pullPhonebook: exception " + e.getMessage());
                    return null;
                }
            }
            return null;
        }
    };
}
