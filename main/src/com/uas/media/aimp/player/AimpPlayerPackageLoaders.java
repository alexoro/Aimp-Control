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

import com.uas.media.aimp.api.ApiException;
import com.uas.media.aimp.api.IPlugin;
import com.uas.media.aimp.api.models.CurrentSongInfo;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;

import java.io.IOException;
import java.util.List;

/**
 * User: uas.sorokin@gmail.com
 */
class AimpPlayerPackageLoaders {

    public static final boolean CHECK_HASH_YES = true;
    public static final boolean CHECK_HASH_NO = false;

    private AimpPlayer mPlayer;

    AimpPlayerPackageLoaders(AimpPlayer player) {
        mPlayer = player;
    }


    void loadCommons() throws InterruptedException, IOException, ApiException {
        boolean isShuffle = mPlayer.getPlugin().isShuffle();
        boolean isRepeat  = mPlayer.getPlugin().isRepeatSong();
        boolean isMute    = mPlayer.getPlugin().isMute();
        AimpPlayer.PlayState playState =
                mPlayer.getPlugin().getPlayState() == IPlugin.PLAY_STATE_PLAYING
                ? AimpPlayer.PlayState.PLAYING
                : AimpPlayer.PlayState.STOPPED
        ;

        synchronized (mPlayer) {
            mPlayer.setStateIsShuffle(isShuffle);
            mPlayer.setStateIsRepeatSong(isRepeat);
            mPlayer.setStateIsMute(isMute);
            mPlayer.setStatePlayState(playState);
        }
    }

    void loadSongPlayPosition() throws InterruptedException, IOException, ApiException {
        int playPosition = mPlayer.getPlugin().getSongPlayPosition();

        synchronized (mPlayer) {
            int duration = mPlayer.getCurrentSong().getDuration();
            mPlayer.setStateSongPlayPosition(Math.min(playPosition, duration));
        }
    }

    void loadCurrentSong() throws InterruptedException, IOException, ApiException {
        CurrentSongInfo currentSong = mPlayer.getPlugin().getCurrentSongInfo();
        int songPlayPosition = mPlayer.getPlugin().getSongPlayPosition();
        int checkedSongPlayPosition = currentSong.getSongPosition() < 0 ? 0 : songPlayPosition;

        synchronized (mPlayer) {
            mPlayer.setStateCurrentSong(
                    currentSong.getPlaylistId(),
                    currentSong.getSongPosition(),
                    checkedSongPlayPosition
            );
        }
    }

    void loadVolume() throws InterruptedException, IOException, ApiException {
        int volume = mPlayer.getPlugin().getVolume();

        synchronized (mPlayer) {
            mPlayer.setStateVolumeValue(volume);
        }
    }

    void loadPlaylists(boolean checkHash) throws InterruptedException, IOException, ApiException {
        List<Playlist> plsListRemote = mPlayer.getPlugin().getPlaylists();

        // we should download songs only if flag is set
        if (!checkHash) {
            for (Playlist pl: plsListRemote) {
                loadPlaylistSongs(pl);
            }
        } else {
            for (Playlist pRemote: plsListRemote) {
                Playlist pLocal = mPlayer.getPlaylistById(pRemote.getId());
                if (pLocal == null || !pLocal.getHash().equals(pRemote.getHash())) {
                    loadPlaylistSongs(pRemote);
                } else {
                    pRemote.setSongs(pLocal.getSongs());
                }
            }
        }

        // load current playlist id
        int currentPlaylistId = mPlayer.getPlugin().getCurrentSongInfo().getPlaylistId();

        synchronized (mPlayer) {
            mPlayer.setStatePlaylists(plsListRemote, currentPlaylistId);
        }
    }


    /**
     * Load songs to specified playlist
     * @param pl - Playlist to load songs
     */
    void loadPlaylistSongs(Playlist pl) throws InterruptedException, IOException, ApiException {
        List<Song> s = mPlayer.getPlugin().getPlaylistSongs(pl.getId());
        pl.setSongs(s);
    }

}