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

import android.os.Handler;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;

import java.util.List;

/**
 * User: uas.sorokin@gmail.com
 */
public class StateObserverViaHandler extends StateObserver {

    private Handler mHandler;

    public StateObserverViaHandler() {
        mHandler = new Handler();
    }

    @Override
    protected void notifyPlay(final Song song) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPlay(song);
            }
        });
    }

    @Override
    protected void notifyStop(final Song song) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onStop(song);
            }
        });
    }

    @Override
    protected void notifyPause(final Song song) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPause(song);
            }
        });
    }

    @Override
    protected void notifyPlaylistsInfoUpdated(final List<Playlist> pls, final Playlist playlist) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPlaylistsInfoUpdated(pls, playlist);
            }
        });
    }

    @Override
    protected void notifyPlaylistUpdated(final Playlist playlist) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onPlaylistUpdated(playlist);
            }
        });
    }

    @Override
    protected void notifySongChanged(final Playlist playlist, final Song song, final int position, final double percentage) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onSongChanged(playlist, song, position, percentage);
            }
        });
    }

    @Override
    protected void notifySongPlayPositionChanged(final Playlist playlist, final Song song, final int position, final double percentage) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onSongPlayPositionChanged(playlist, song, position, percentage);
            }
        });
    }

    @Override
    protected void notifyVolumeChanged(final int volume) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onVolumeChanged(volume);
            }
        });
    }

    @Override
    protected void notifyShuffleStateChanged(final boolean state) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onShuffleStateChanged(state);
            }
        });
    }

    @Override
    protected void notifyRepeatSongStateChanged(final boolean state) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onRepeatSongStateChanged(state);
            }
        });
    }

    @Override
    protected void notifyMuteStateChanged(final boolean state) {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onMuteStateChanged(state);
            }
        });
    }

    @Override
    protected void notifyDataIsTooLarge() {
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                onDataIsTooLarge();
            }
        });
    }

}