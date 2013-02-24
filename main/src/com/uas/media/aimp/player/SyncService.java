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

package com.uas.media.aimp.player;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * User: uas.sorokin@gmail.com
 */
public class SyncService extends Service {

    public interface ErrorsCallback {
        void onException(Exception ex);
    }

    private IBinder mBinder;
    private Map<AimpPlayer, Timer> mTimers;
    private Map<AimpPlayer, ErrorsCallback> mErrorCallbacks;


    @Override
    public void onCreate() {
        super.onCreate();
        mBinder = new LocalBinder();
        mTimers = new HashMap<AimpPlayer, Timer>();
        mErrorCallbacks = new HashMap<AimpPlayer, ErrorsCallback>();
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {
        startForeground(0, null);
        return START_STICKY_COMPATIBILITY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        for (Map.Entry<AimpPlayer, Timer> e: mTimers.entrySet()) {
            cancelSync(e.getKey());
        }

        stopForeground(true);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }


    public void submitSync(AimpPlayer player, SyncParams syncParams, ErrorsCallback callback) {
        Timer t = new Timer();

        mTimers.put(player, t);
        mErrorCallbacks.put(player, callback);

        Updaters updaters = new Updaters(player);

        t.scheduleAtFixedRate(
                updaters.getPlaylistsUpdater(),
                0, syncParams.getPlaylistsUpdatePeriod());
        t.scheduleAtFixedRate(
                updaters.getPlayStateUpdater(),
                0, syncParams.getPlaystateUpdatePeriod());
        t.scheduleAtFixedRate(
                updaters.getCommonsUpdater(),
                0, syncParams.getOthersUpdatePeriod());
    }

    public void cancelSync(AimpPlayer player) {
        Timer t = mTimers.remove(player);
        t.cancel();
        t.purge();

        mErrorCallbacks.remove(player);
    }


    class Updaters {

        private AimpPlayer mPlayer;
        private AimpPlayerPackageLoaders mLoaders;

        Updaters(AimpPlayer player) {
            mPlayer = player;
            mLoaders = new AimpPlayerPackageLoaders(mPlayer);
        }

        protected void notifyException(Exception ex) {
            ErrorsCallback callback = mErrorCallbacks.get(mPlayer);
            if (callback != null) {
                callback.onException(ex);
            }
        }

        public TimerTask getPlayStateUpdater() {
             return new TimerTask() {
                 @Override
                 public void run() {
                     try {
                         if (mPlayer.hasPlaylists()) {
                             mLoaders.loadSongPlayPosition();
                             mLoaders.loadCommons();
                             mLoaders.loadCurrentSong();
                         }
                     } catch (Exception ex) {
                         notifyException(ex);
                     }
                 }
             };
        }

        public TimerTask getPlaylistsUpdater() {
            return new TimerTask() {
                @Override
                public void run() {
                    try {
                        mLoaders.loadPlaylists(AimpPlayerPackageLoaders.CHECK_HASH_YES);
                        mLoaders.loadCurrentSong();
                        mLoaders.loadSongPlayPosition();
                    } catch (Exception ex) {
                        notifyException(ex);
                    }
                }
            };
        }

        public TimerTask getCommonsUpdater() {
            return new TimerTask() {
                @Override
                public void run() {
                    try {
                        mLoaders.loadCommons();
                        mLoaders.loadVolume();
                    } catch (Exception ex) {
                        notifyException(ex);
                    }
                }
            };
        }

    }


    class LocalBinder extends Binder {
        public SyncService getService() {
            return SyncService.this;
        }
    }

}