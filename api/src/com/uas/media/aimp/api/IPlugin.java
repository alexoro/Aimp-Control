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

package com.uas.media.aimp.api;

import com.uas.media.aimp.api.models.CurrentSongInfo;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;

import java.io.IOException;
import java.util.List;

/**
 * User: uas.sorokin@gmail.com
 */
public interface IPlugin {

	int PLAY_STATE_STOPPED = 0;
	int PLAY_STATE_PAUSED = 1;
	int PLAY_STATE_PLAYING = 2;
	
	
	String getRemotePluginName();

	String getHttpClientName();
	void setHttpClientName(String name);

    int getDefaultRemotePort();

	String getRemoteHost();
    void setRemoteHost(String host);

	int getRemotePort();
    void setRemotePort(int port);


	void setConnectionTimeout(int timeout);
	int getConnectionTimeout();

	int getTrafficIn();
	int getTrafficOut();


	/**
	 * Check if AIMP is accessible
	 * @return true if AIMP returned a correct response
	 */
	boolean ping() throws InterruptedException;

    boolean play() throws ApiException, IOException, InterruptedException;
    boolean play(int playlistId, int songPosition) throws ApiException, IOException, InterruptedException;
    boolean play(int playlistId, int songPosition, int playPosition) throws ApiException, IOException, InterruptedException;
    boolean stop() throws ApiException, IOException, InterruptedException;
    boolean pause() throws ApiException, IOException, InterruptedException;
    boolean next() throws ApiException, IOException, InterruptedException;
    boolean previous() throws ApiException, IOException, InterruptedException;


    /**
     * One of const PLAY_STATE_*
     * @return 0 - stopped, 1 - paused, 2 - playing
     */
    int getPlayState() throws ApiException, IOException, InterruptedException;

    void setSongPlayPosition(int second) throws ApiException, IOException, InterruptedException;
    int getSongPlayPosition() throws ApiException, IOException, InterruptedException;

    void setRepeatSong(boolean state) throws ApiException, IOException, InterruptedException;
    boolean isRepeatSong() throws ApiException, IOException, InterruptedException;

    void setVolume(int volume) throws ApiException, IOException, InterruptedException;
    int getVolume() throws ApiException, IOException, InterruptedException;

    void setMute(boolean state) throws ApiException, IOException, InterruptedException;
    boolean isMute() throws ApiException, IOException, InterruptedException;

    void setShuffle(boolean state) throws ApiException, IOException, InterruptedException;
    boolean isShuffle() throws ApiException, IOException, InterruptedException;


    /**
     * Get info about current playing (stopped) song
     * @return Object that describes current playing song's info.
     * @throws ApiException
     */
    CurrentSongInfo getCurrentSongInfo() throws ApiException, IOException, InterruptedException;

	List<Playlist> getPlaylists() throws ApiException, IOException, InterruptedException;
    String getPlaylistHash(int playlistId)throws ApiException, IOException, InterruptedException;

	List<Song> getPlaylistSongs(int playlistId) throws ApiException, IOException, InterruptedException;
	void removeSong(int playlistId, int songPosition) throws ApiException, IOException, InterruptedException;

}