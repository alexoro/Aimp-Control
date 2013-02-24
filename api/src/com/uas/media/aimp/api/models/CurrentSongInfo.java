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

package com.uas.media.aimp.api.models;

/**
 * User: uas.sorokin@gmail.com
 */
public class CurrentSongInfo {

	private int mPlaylistId;
    private int mSongPosition;
    private Song mSong;

	
    /**
     * Id of playlist where song is playing
     */
    public int getPlaylistId() {
        return mPlaylistId;
    }

    public void setPlaylistId(int playlistId) {
        this.mPlaylistId = playlistId;
    }


    /**
     * Position of file in the playlist
     */
    public int getSongPosition() {
        return mSongPosition;
    }

    public void setSongPosition(int songPosition) {
        mSongPosition = songPosition;
    }


    public Song getInfo() {
        return mSong;
    }

    public void setInfo(Song s) {
        mSong = s;
    }

    @Override
    public int hashCode() {
        return mPlaylistId ^ mSongPosition ^ mSong.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Song)) return false;

        CurrentSongInfo info = (CurrentSongInfo)obj;
        return (
                mPlaylistId == info.getPlaylistId()
                && mSongPosition == info.getSongPosition()
                && mSong.equals(info.getInfo())
        );
    }

}