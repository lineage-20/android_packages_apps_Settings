/*
 * Copyright 2018 The Android Open Source Project
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
package com.android.settings.connecteddevice;

import android.content.Context;
import android.content.pm.PackageManager;
import androidx.annotation.VisibleForTesting;
import androidx.preference.Preference;
import androidx.preference.PreferenceGroup;
import androidx.preference.PreferenceScreen;
import com.android.settings.bluetooth.AvailableMediaBluetoothDeviceUpdater;
import com.android.settings.bluetooth.BluetoothDeviceUpdater;
import com.android.settings.core.BasePreferenceController;
import com.android.settings.dashboard.DashboardFragment;
import com.android.settingslib.core.lifecycle.LifecycleObserver;
import com.android.settingslib.core.lifecycle.events.OnStart;
import com.android.settingslib.core.lifecycle.events.OnStop;

/**
 * Controller to maintain the {@link androidx.preference.PreferenceGroup} for all
 * available media devices. It uses {@link DevicePreferenceCallback}
 * to add/remove {@link Preference}
 */
public class AvailableMediaDeviceGroupController extends BasePreferenceController
        implements LifecycleObserver, OnStart, OnStop, DevicePreferenceCallback {

    private static final String KEY = "available_device_list";

    @VisibleForTesting
    PreferenceGroup mPreferenceGroup;
    private BluetoothDeviceUpdater mBluetoothDeviceUpdater;

    public AvailableMediaDeviceGroupController(Context context) {
        super(context, KEY);
    }

    @Override
    public void onStart() {
        mBluetoothDeviceUpdater.registerCallback();
    }

    @Override
    public void onStop() {
        mBluetoothDeviceUpdater.unregisterCallback();
    }

    @Override
    public void displayPreference(PreferenceScreen screen) {
        super.displayPreference(screen);
        if (isAvailable()) {
            mPreferenceGroup = (PreferenceGroup) screen.findPreference(KEY);
            mPreferenceGroup.setVisible(false);
            mBluetoothDeviceUpdater.setPrefContext(screen.getContext());
            mBluetoothDeviceUpdater.forceUpdate();
        }
    }

    @Override
    public int getAvailabilityStatus() {
        return mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH)
                ? AVAILABLE
                : DISABLED_UNSUPPORTED;
    }

    @Override
    public String getPreferenceKey() {
        return KEY;
    }

    @Override
    public void onDeviceAdded(Preference preference) {
        if (mPreferenceGroup.getPreferenceCount() == 0) {
            mPreferenceGroup.setVisible(true);
        }
        mPreferenceGroup.addPreference(preference);
    }

    @Override
    public void onDeviceRemoved(Preference preference) {
        mPreferenceGroup.removePreference(preference);
        if (mPreferenceGroup.getPreferenceCount() == 0) {
            mPreferenceGroup.setVisible(false);
        }
    }

    public void init(DashboardFragment fragment) {
        mBluetoothDeviceUpdater = new AvailableMediaBluetoothDeviceUpdater(fragment.getContext(),
                fragment, AvailableMediaDeviceGroupController.this);
    }

    @VisibleForTesting
    public void setBluetoothDeviceUpdater(BluetoothDeviceUpdater bluetoothDeviceUpdater) {
        mBluetoothDeviceUpdater  = bluetoothDeviceUpdater;
    }
}
