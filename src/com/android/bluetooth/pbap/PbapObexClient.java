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

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.UUID;

import javax.obex.ClientOperation;
import javax.obex.ClientSession;
import javax.obex.HeaderSet;
import javax.obex.ObexTransport;

public class PbapObexClient {

    private static final String TAG = "PbapObexClient";
    private static final boolean D = false;
    private static final boolean V = false;

    // Unique UUID for socket connection
    public static final UUID PBAP_UUID = UUID.fromString("0000112f-0000-1000-8000-00805F9B34FB");

    // 128 bit UUID for PBAP
    private static final byte[] PBAP_TARGET = new byte[] { 0x79, 0x61, 0x35, (byte) 0xf0,
            (byte) 0xf0, (byte) 0xc5, 0x11, (byte) 0xd8, 0x09, 0x66, 0x08, 0x00, 0x20, 0x0c,
            (byte) 0x9a, 0x66 };

    private ObexTransport mTransport;
    private ClientSession mClientSession;
    BluetoothDevice mRemoteDevice;

    // connection state
    private int mState;

    // Constants that indicate the current connection state
    public static final int STATE_NONE = 0; // we're doing nothing
    public static final int STATE_CONNECTING = 1; // now initiating an outgoing
                                                  // connection
    public static final int STATE_CONNECTED = 2; // now connected to a remote
                                                 // device

    public PbapObexClient(BluetoothDevice remoteDevice) {
        if (remoteDevice == null) {
            throw new NullPointerException("Remote device is null for Obex transport");
        }
        mRemoteDevice = remoteDevice;
    }

    /**
     * Set the current state of the PBAP connection
     * 
     * @param state
     *            An integer defining the current connection state
     */
    private synchronized void setState(int state) {
        if (D)
            Log.d(TAG, "setState() " + mState + " -> " + state);
        mState = state;
    }

    /**
     * Return the current connection state.
     */
    public synchronized int getState() {
        return mState;
    }

    /**
     * Return the whether current is connected to remote device.
     */
    public boolean isConnected() {
        return mState == STATE_CONNECTED;
    }

    /**
     * Shutdown the PBAP OBEX connection.
     */
    public void shutdown() {
        /* Disconnect if connected */
        disconnect();
    }

    private HeaderSet mHsConnect = null;

    /**
     * Initiate a connection to a remote device.
     * 
     * @param device
     *            The BluetoothDevice to connect
     * @throws IOException
     */
    public synchronized boolean connect() throws IOException {
        if (mRemoteDevice == null) {
            Log.e(TAG, "No avaiable remote device  ");
            throw new IOException("Remote device is null for Obex connect");
        }
        if (mState == STATE_CONNECTED) {
            Log.e(TAG, "Already connected  ");
            throw new IOException("Already connected");
        } else if (mState == STATE_CONNECTING) {
            throw new IOException("Last connect request is ongoing");
        }
        boolean connected = false;
        BluetoothSocket btSocket = null;

        try {
            setState(STATE_CONNECTING);
            btSocket = mRemoteDevice.createRfcommSocketToServiceRecord(PBAP_UUID);
            btSocket.connect();
            mTransport = new BluetoothPbapRfcommTransport(btSocket);
            mClientSession = new ClientSession(mTransport);
            if (mClientSession != null) {
                HeaderSet hs = new HeaderSet();
                hs.setHeader(HeaderSet.TARGET, PBAP_TARGET);
                mHsConnect = mClientSession.connect(hs);
                connected = true;
                if (D)
                    Log.d(TAG, "OBEX session created");
            }
        } finally {
            if (connected) {
                setState(STATE_CONNECTED);
            } else {
                setState(STATE_NONE);
            }
        }
        return connected;
    }

    /**
     * get content from remote device in vCard.
     * 
     * @param request
     *            The Headerset of obex request
     * @throws IOException
     */
    public synchronized String getContent(HeaderSet requestHeader) throws IOException {
        String responseStr = null;
        // ClientSession clientSession = mClientSession;

        if ((mState != STATE_CONNECTED) || (mClientSession == null)) {
            Log.e(TAG, "can't get content, connection lost " + mState);
            throw new IOException("Connection lost");
            // return responseStr;
        }

        ClientOperation getOperation = null;
        InputStream inputStream = null;

        // Send the header first and then the body
        try {
            getOperation = (ClientOperation) mClientSession.get(requestHeader);
            inputStream = getOperation.openInputStream();
            responseStr = convertStreamToString(inputStream);
            if (V)
                Log.v(TAG, "get Response " + responseStr);

        } catch (IOException e) {
            Log.e(TAG, "Error when put HeaderSet " + e.getMessage());
            try {
                getOperation.abort();
            } catch (IOException e1) {
                Log.e(TAG, "Error abort obex client session " + e1.getMessage());
            }
        } finally {
            if (getOperation != null) {
                try {
                    getOperation.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error close obex client session " + e.getMessage());
                }
            }
        }
        // }
        return responseStr;
    }

    /**
     * Convert Stream to string
     * 
     * @throws IOException
     */

    private String convertStreamToString(InputStream is) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;
        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                Log.e(TAG, "Error close input stream " + e.getMessage());
            }
        }
        return sb.toString();
    }

    /**
     * Disconnect the connection to PBAP OBEX server. Call this when the PBAP
     * client requests disconnect.
     */
    public void disconnect() {
        try {
            if (mClientSession != null) {
                mClientSession.disconnect(null);
                if (D)
                    Log.d(TAG, "OBEX session disconnected");
            }
        } catch (IOException e) {
            Log.w(TAG, "OBEX session disconnect error " + e.getMessage());
        }
        try {
            if (mClientSession != null) {
                if (D)
                    Log.d(TAG, "OBEX session close mClientSession");
                mClientSession.close();
                mClientSession = null;
                if (D)
                    Log.d(TAG, "OBEX session closed");
            }
        } catch (IOException e) {
            Log.w(TAG, "OBEX session close error:" + e.getMessage());
        }
        if (mTransport != null) {
            try {
                if (D)
                    Log.d(TAG, "Close Obex Transport");
                mTransport.close();
                mTransport = null;
                mState = STATE_NONE;
                if (D)
                    Log.d(TAG, "Obex Transport Closed");
            } catch (IOException e) {
                Log.e(TAG, "mTransport.close error: " + e.getMessage());
            }
        }
    }

    /**
     * Pull phonebook
     * 
     * @param nameOfPath
     *            absolute path in the virtual folders architecture of the
     *            PSE.ex: telecom/pb.vcf,SIM1/telecom/pb.vcf.
     * @param propSelector
     *            indicate the properties contained in the requested vCard
     *            objects.
     * @throws IOException
     */

    public String pullPhonebook(String nameOfPath, int propSelector) throws IOException {
        HeaderSet request = new HeaderSet();

        request.setHeader(HeaderSet.TYPE, "x-bt/phonebook");
        request.setHeader(HeaderSet.NAME, nameOfPath);

        BluetoothPbapAppParams appParams = new BluetoothPbapAppParams();
        appParams.setPropertySelector(propSelector);
        try {
            request.setHeader(HeaderSet.APPLICATION_PARAMETER, appParams.EncodeParams());
        } catch (UnsupportedEncodingException e) {
            throw new IOException("Unsupported encoding");
        }

        if (mHsConnect.mConnectionID != null) {
            request.mConnectionID = new byte[4];
            System.arraycopy(mHsConnect.mConnectionID, 0, request.mConnectionID, 0, 4);
        } else {
            return null;
        }
        String restring = getContent(request);
        return restring;
    }
}
