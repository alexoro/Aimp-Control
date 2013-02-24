/*
 * Copyright (c) 2013, Sorokin Alexander (uas.sorokin@gmail.com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *   notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *   notice, this list of conditions and the following disclaimer in the
 *   documentation and/or other materials provided with the distribution.
 *
 * 3. The names of the authors may not be used to endorse or promote products
 *   derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED
 * TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR
 * PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR
 * PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
 * LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package com.uas.media.aimp.mobile;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import com.uas.media.aimp.AimpPlayerInstance;
import com.uas.media.aimp.R;
import com.uas.media.aimp.api.IPlugin;
import com.uas.media.aimp.player.AimpPlayer;
import com.uas.media.aimp.player.ConnectionListener;
import com.uas.media.aimp.player.ConnectionListenerViaHandler;
import com.uas.media.aimp.utils.Settings;

/**
 * User: uas.sorokin@gmail.com
 */
public class SettingsActivity extends SherlockPreferenceActivity {

    private EditTextPreference vHost;
    private EditTextPreference vPort;
    private ListPreference vSyncProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.settings);

        vHost = (EditTextPreference) findPreference(Settings.KEY_HOST);
        vPort = (EditTextPreference) findPreference(Settings.KEY_PORT);
        vSyncProfile = (ListPreference) findPreference(Settings.KEY_SYNC_PROFILE);

        ActionBar ab = getSupportActionBar();
        ab.setDisplayHomeAsUpEnabled(true);

        vPort.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                try {
                    int port = Integer.parseInt((String) newValue);
                    if (port < 1 || port > 65535) {
                        throw new IllegalArgumentException("Port value should be between [1,65535]");
                    }
                    return true;
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showInvalidPortValue();
                    return false;
                }
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUi();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(mSharedPrefsListener);
        AimpPlayerInstance.get().registerConnectionListener(mConnectionListener);
    }

    @Override
    protected void onPause() {
        super.onPause();

        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(mSharedPrefsListener);
        AimpPlayerInstance.get().unregisterConnectionListener(mConnectionListener);
    }


    protected final ConnectionListener mConnectionListener = new ConnectionListenerViaHandler() {
        @Override
        public void onConnectionStatusChanged(IPlugin plugin, AimpPlayer.ConnectionStatus status) {
            updateUi();
        }
    };

    protected SharedPreferences.OnSharedPreferenceChangeListener mSharedPrefsListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
            updateUi();
        }
    };

    protected void updateUi() {
        vHost.setSummary(Settings.getHost());
        vPort.setSummary(String.valueOf(Settings.getPort()));

        switch (Settings.getSyncFrequency()) {
            case Settings.SYNC_FREQUENCY_RARELY:
                vSyncProfile.setSummary(getString(R.string.settings_sync_profile_rarely));
                break;
            case Settings.SYNC_FREQUENCY_OFTEN:
                vSyncProfile.setSummary(getString(R.string.settings_sync_profile_often));
                break;
            default:
                vSyncProfile.setSummary(getString(R.string.settings_sync_profile_optimally));
                break;
        }

        if (AimpPlayerInstance.get().isConnected()) {
            vHost.setEnabled(false);
            vPort.setEnabled(false);
            vSyncProfile.setEnabled(false);
        } else {
            vHost.setEnabled(true);
            vPort.setEnabled(true);
            vSyncProfile.setEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    protected void showInvalidPortValue() {
        Dialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.error)
                .setMessage(R.string.settings_invalid_port_value)
                .setCancelable(true)
                .setNeutralButton(R.string.close, null)
                .create();
        d.show();
    }

}