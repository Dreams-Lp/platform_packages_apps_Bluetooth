package com.android.bluetooth.opp;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.os.Process;
import android.util.Log;

/* This thread is dedicated to handling position updates
 * (used for progress bar display) in parallel of OPP file
 * transfer as a low priority task to preserve an optimum bitrate
 */
public class BluetoothOppObexSessionProgress extends Thread {

    private Uri mContentUri;
    private ContentValues mUpdateValues;
    private Context mCtx;
    private boolean mExit;
    private long mLastTimeStamp;
    private long mFileLength;
    private long mLastPosition;
    private String mTag;

    public BluetoothOppObexSessionProgress(String tag, Uri contentUri, Context ctx, long fileLength){

        super("ContentResolverUpdateThread");

        mContentUri = contentUri;
        mCtx = ctx;
        mExit = false;
        mUpdateValues = null;
        mFileLength = fileLength;
        mTag = tag;
    }

    @Override
    public synchronized void run() {

        /* Set the thread to the lowest possible priority
         * to preserve optimal throughput */
        Process.setThreadPriority(Process.THREAD_PRIORITY_LOWEST);

        mLastTimeStamp = System.currentTimeMillis();
        mLastPosition = 0;

        /* Loop handling position updates when they are available */
        while (!mExit) {

            try {
                /* Wait until updateAvailable or exit notification */
                wait();
            } catch (Exception e) {
                Log.e(mTag, "Unexpected exception in ContentResolverUpdateThread wait() : " + e.getMessage());
            }

            /* If an update is available */
            if (mUpdateValues != null) {
                /* Log byte rate no more than every second */
                long currentTimeStamp = System.currentTimeMillis();
                if (currentTimeStamp - mLastTimeStamp >= 1000) {
                    long currentPosition = mUpdateValues.getAsInteger(BluetoothShare.CURRENT_BYTES);
                    long completion = currentPosition * 100 / mFileLength;
                    long byteRate = (currentPosition - mLastPosition) / (currentTimeStamp - mLastTimeStamp);
                    mLastPosition = currentPosition;
                    mLastTimeStamp = currentTimeStamp;
                    Log.i (mTag, "File transfer " + currentPosition + "/" + mFileLength
                        + " [" + completion + "%] ongoing at " + byteRate + " kB/s");
                }

                /* Update data table */
                mCtx.getContentResolver().update(mContentUri, mUpdateValues,
                        null, null);
                /* Update was treated, remove it */
                mUpdateValues = null;

                Log.d(mTag, "Uri position update done");
            }
        }
        Log.d(mTag, "Exiting ContentResolverUpdateThread thread");
    }

    public synchronized void exit() {

        Log.d(mTag, "ContentResolverUpdateThread thread exit requested");
        mExit = true;
        /* Notify thread so that it can exit wait() and proceed */
        notify();
    }

    public synchronized void updateAvailable(ContentValues updateValues) {

        Log.d(mTag, "ContentResolverUpdateThread position update notified");
        mUpdateValues = updateValues;
        /* Notify thread so that it can exit wait() and proceed */
        notify();
    }
}
