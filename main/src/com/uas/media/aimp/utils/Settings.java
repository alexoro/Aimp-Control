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

package com.uas.media.aimp.utils;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.uas.media.aimp.AppInstance;
import com.uas.media.aimp.player.SyncParams;

/**
 * User: uas.sorokin@gmail.com
 */
public class Settings {

    public static final String KEY_HOST = "host";
    public static final String KEY_PORT = "port";
    public static final String KEY_SYNC_PROFILE = "sync_profile";

    public static final int SYNC_FREQUENCY_RARELY = 1;
    public static final int SYNC_FREQUENCY_OPTIMALLY = 2;
    public static final int SYNC_FREQUENCY_OFTEN = 3;

    protected static final String DEFAULT_PORT_VALUE = "38475";


    protected Settings() {

    }

    public static String getHost() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppInstance.getContext());
        return sp.getString(KEY_HOST, "");
    }

    public static int getPort() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppInstance.getContext());
        try {
            String value = sp.getString(KEY_PORT, DEFAULT_PORT_VALUE);
            if (value.equals("")) {
                return Integer.parseInt(DEFAULT_PORT_VALUE);
            } else {
                return Integer.parseInt(value);
            }
        } catch (NumberFormatException ex) {
            return Integer.parseInt(DEFAULT_PORT_VALUE);
        }
    }

    public static int getSyncFrequency() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(AppInstance.getContext());
        String t = sp.getString(KEY_SYNC_PROFILE, "2");
        if (t.equals("1")) {
            return SYNC_FREQUENCY_RARELY;
        } else if (t.equals("3")) {
            return SYNC_FREQUENCY_OFTEN;
        } else {
            return SYNC_FREQUENCY_OPTIMALLY;
        }
    }

    public static SyncParams buildSyncParams() {
        int freq = getSyncFrequency();

        final int playlists;
        final int playstate;
        final int common;

        switch (freq) {
            case SYNC_FREQUENCY_RARELY:
                playlists = 1000*60*30;
                playstate = 1000*10;
                common = 1000*60*10;
                break;
            case SYNC_FREQUENCY_OFTEN:
                playlists = 1000*60*5;
                //playlists = 1000*30;
                playstate = 1000*5;
                common = 1000*60;
                break;
            default:
                playlists = 1000*60*15;
                playstate = 1000*5;
                common = 1000*60*5;
                break;
        }

        return new SyncParams() {
            @Override
            public long getPlaylistsUpdatePeriod() {
                return playlists;
            }

            @Override
            public long getPlaystateUpdatePeriod() {
                return playstate;
            }

            @Override
            public long getOthersUpdatePeriod() {
                return common;
            }
        };
    }

}