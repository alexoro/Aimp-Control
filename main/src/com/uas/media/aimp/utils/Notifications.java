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

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.widget.RemoteViews;
import com.uas.media.aimp.AimpPlayerInstance;
import com.uas.media.aimp.AppInstance;
import com.uas.media.aimp.R;
import com.uas.media.aimp.api.IPlugin;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import com.uas.media.aimp.mobile.MainActivity;
import com.uas.media.aimp.player.AimpPlayer;
import com.uas.media.aimp.player.ConnectionListener;
import com.uas.media.aimp.player.StateObserver;

/**
 * User: uas.sorokin@gmail.com
 */
public class Notifications {

    private static final int CURRENT_SONG_ID = 1;

    private static final String ACTION_PLAY_PAUSE = "Notifications.ACTION_PLAY_PAUSE";
    private static final String ACTION_PREVIOUS = "Notifications.ACTION_PREVIOUS";
    private static final String ACTION_NEXT = "Notifications.ACTION_NEXT";

    private Notifications() {

    }

    public static void init() {
        AimpPlayerInstance.get().registerStateObserver(new StateObserver() {
            @Override
            public void onPlay(Song song) {
                raiseNotification();
            }

            @Override
            public void onPause(Song song) {
                raiseNotification();
            }

            @Override
            public void onStop(Song song) {
                raiseNotification();
            }

            @Override
            public void onSongChanged(Playlist playlist, Song song, int position, double percentage) {
                raiseNotification();
            }
        });

        AimpPlayerInstance.get().registerConnectionListener(new ConnectionListener() {
            @Override
            public void onConnectionStatusChanged(IPlugin plugin, AimpPlayer.ConnectionStatus status) {
                if (status == AimpPlayer.ConnectionStatus.DISCONNECTED) {
                    closeNotifications();
                }
            }
        });


        IntentFilter infi = new IntentFilter();
        infi.addAction(ACTION_PLAY_PAUSE);
        infi.addAction(ACTION_PREVIOUS);
        infi.addAction(ACTION_NEXT);
        AppInstance.getContext().registerReceiver(sBroadcastReceiver, infi);

        closeNotifications();
    }


    protected static void raiseNotification() {
        NotificationManager nm = (NotificationManager)AppInstance.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.notify(CURRENT_SONG_ID, buildCurrentSongNotification());
    }

    protected static void closeNotifications() {
        NotificationManager nm = (NotificationManager)AppInstance.getContext().getSystemService(Context.NOTIFICATION_SERVICE);
        nm.cancel(CURRENT_SONG_ID);
    }


    protected static Notification buildCurrentSongNotification() {
        Context ctx = AppInstance.getContext();
        AimpPlayer player = AimpPlayerInstance.get();
        Song s = player.getCurrentSong();
        String title = (s == null) ? "" : s.getName();

        Notification n = new Notification(
                R.drawable.ic_notify_cs_small,
                title,
                System.currentTimeMillis()
        );
        n.flags = Notification.FLAG_ONGOING_EVENT;

        // pendingIntents
        Intent notificationIntent = new Intent(ctx, MainActivity.class);
        n.contentIntent = PendingIntent.getActivity(
                ctx, 0, notificationIntent, 0
        );

        // fill views
        RemoteViews contentView = new RemoteViews(ctx.getPackageName(), R.layout.notifcation_current_song);
        contentView.setTextViewText(R.id.title, title);
        if (player.isPlaying()) {
            contentView.setImageViewResource(R.id.play_pause, R.drawable.btn_pause);
        } else {
            contentView.setImageViewResource(R.id.play_pause, R.drawable.btn_play);
        }
        n.contentView = contentView;

        contentView.setOnClickPendingIntent(
                R.id.play_pause_clickable,
                PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_PLAY_PAUSE), PendingIntent.FLAG_UPDATE_CURRENT)
        );
        contentView.setOnClickPendingIntent(
                R.id.previous_clickable,
                PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_PREVIOUS), PendingIntent.FLAG_UPDATE_CURRENT)
        );
        contentView.setOnClickPendingIntent(
                R.id.next_clickable,
                PendingIntent.getBroadcast(ctx, 0, new Intent(ACTION_NEXT), PendingIntent.FLAG_UPDATE_CURRENT)
        );

        return n;
    }

    protected static BroadcastReceiver sBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            AimpPlayer player = AimpPlayerInstance.get();

            if (ACTION_PLAY_PAUSE.equals(intent.getAction())) {
                if( player.isPlaying() ) {
                    player.pause();
                } else {
                    player.play();
                }
            } else if (ACTION_PREVIOUS.equals(intent.getAction())) {
                player.previous();
            } else if (ACTION_NEXT.equals(intent.getAction())) {
                player.next();
            }
        }
    };

}