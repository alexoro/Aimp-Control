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

import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;

import java.util.List;

/**
 * User: uas.sorokin@gmail.com
 */
public class StateObserver {

    protected void notifyPlay(final Song song) {
        onPlay(song);
    }

    protected void notifyStop(final Song song) {
        onStop(song);
    }

    protected void notifyPause(final Song song) {
        onPause(song);
    }

    protected void notifyPlaylistsInfoUpdated(final List<Playlist> pls, final Playlist playlist) {
        onPlaylistsInfoUpdated(pls, playlist);
    }

    protected void notifyPlaylistUpdated(final Playlist playlist) {
        onPlaylistUpdated(playlist);
    }

    protected void notifySongChanged(final Playlist playlist, final Song song, final int position, final double percentage) {
        onSongChanged(playlist, song, position, percentage);
    }

    protected void notifySongPlayPositionChanged(final Playlist playlist, final Song song, final int position, final double percentage) {
        onSongPlayPositionChanged(playlist, song, position, percentage);
    }

    protected void notifyVolumeChanged(final int volume) {
        onVolumeChanged(volume);
    }

    protected void notifyShuffleStateChanged(final boolean state) {
        onShuffleStateChanged(state);
    }

    protected void notifyRepeatSongStateChanged(final boolean state) {
        onRepeatSongStateChanged(state);
    }

    protected void notifyMuteStateChanged(final boolean state) {
        onMuteStateChanged(state);
    }

    //TODO implement it in code
    protected void notifyDataIsTooLarge() {
        onDataIsTooLarge();
    }


    /**
     * Is called when play state changes to PLAY
     * @param song Current song
     */
    public void onPlay(Song song) {}


    /**
     * Is called when play state changes to PAUSE
     * @param song Current song
     */
    public void onPause(Song song) {}


    /**
     * Is called when play state changes to STOP
     * @param song Current song
     */
    public void onStop(Song song) {}


    /**
     * Is called when data of playlists is updated.
     * @param pls List of playlists
     * @param currentPlaylist Current currentPlaylist
     */
    public void onPlaylistsInfoUpdated(List<Playlist> pls, Playlist currentPlaylist) {}

    /**
     * Is called when playlist's data is changed.
     * Event raises after onPlaylistsListUpdated is raised.
     * @param playlist Updated playlist
     */
    public void onPlaylistUpdated(Playlist playlist) {}


    /**
     * Is called when a new song is selected to be played
     * @param playlist A playlist contains selected song (current playlist)
     * @param song Selected song
     * @param position Play position of selected song in seconds
     * @param percentage Percentage of song play position (e.g. if position is in the middle, then value is 0.5f)
     */
    public void onSongChanged(Playlist playlist, Song song, int position, double percentage) {}


    /**
     * Is called when song play position is changed
     * @param playlist A playlist contains selected song (current playlist)
     * @param song Song
     * @param position Play position in seconds from song start
     * @param percentage Percentage of song play position (e.g. if position is in the middle, then value is 0.5f)
     */
    public void onSongPlayPositionChanged(Playlist playlist, Song song, int position, double percentage) {}


    public void onVolumeChanged(int volume) {}
    public void onShuffleStateChanged(boolean state) {}
    public void onRepeatSongStateChanged(boolean state) {}
    public void onMuteStateChanged(boolean state) {}

    protected void onDataIsTooLarge() {}

}