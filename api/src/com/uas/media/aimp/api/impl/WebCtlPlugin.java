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

package com.uas.media.aimp.api.impl;

import com.uas.media.aimp.api.ApiException;
import com.uas.media.aimp.api.ApiRequestException;
import com.uas.media.aimp.api.IPlugin;
import com.uas.media.aimp.api.Logger;
import com.uas.media.aimp.api.models.CurrentSongInfo;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import org.apache.http.HttpEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * User: uas.sorokin@gmail.com
 */
public class WebCtlPlugin implements IPlugin {

    public static final int DEFAULT_REQUEST_TIMEOUT = 5000;
    public static final int DEFAULT_PORT = 38475;

	interface Statuses {
		int VOLUME = 1;
		int BALANCE = 2;
		int SPEED = 3;
		int PLAY = 4;
		int MUTE = 5;
		int REVERBATION = 6;
		int ECHO = 7;
		int CHORUS = 8;
		int FLANGER = 9;
		int EQUALIZER = 10;

		int EQUALIZER1  = 11;
		int EQUALIZER2  = 12;
		int EQUALIZER3  = 13;
		int EQUALIZER4  = 14;
		int EQUALIZER5  = 15;
		int EQUALIZER6  = 16;
		int EQUALIZER7  = 17;
		int EQUALIZER8  = 18;
		int EQUALIZER9  = 19;
		int EQUALIZER10 = 20;
		int EQUALIZER11 = 21;
		int EQUALIZER12 = 22;
		int EQUALIZER13 = 23;
		int EQUALIZER14 = 24;
		int EQUALIZER15 = 25;
		int EQUALIZER16 = 26;
		int EQUALIZER17 = 27;
		int EQUALIZER18 = 28;

		int REPEAT_SONG = 29;
		int STOP = 30;
		int POSITION = 31;
		int LENGTH = 32;
		int REPEAT_PLAYLIST = 33;
		int REPEAT_PLAYLIST_1 = 34;
		int KBPS = 35;
		int KHz = 36;
		int MODE = 37;
		int RADIO = 38;
		int STREAM_TYPE = 39;
		int TIMER = 40;
		int SHUFFLE = 41;
	}


    private String mRemoteHost;
    private int mRemotePort;
    private int mConnectionTimeout;
    private String mHttpClientName;


    public WebCtlPlugin() {

    }

    public WebCtlPlugin(String host) {
        this(host, DEFAULT_PORT);
    }

    public WebCtlPlugin(String host, int port) {
        mRemoteHost = host;
        mRemotePort = port;
        mHttpClientName = "";
        mConnectionTimeout = DEFAULT_REQUEST_TIMEOUT;
    }
	
	@Override
	public int getDefaultRemotePort() {
		return DEFAULT_PORT;
	}

	@Override
	public String getRemotePluginName() {
		return "AIMP Web Control Plugin";
	}

    @Override
    public String getRemoteHost() {
        return mRemoteHost;
    }

    @Override
    public void setRemoteHost(String host) {
        if (host == null) {
            throw new IllegalArgumentException("Host has null value");
        }
        mRemoteHost = host;
    }

    @Override
    public int getRemotePort() {
        return mRemotePort;
    }

    @Override
    public void setRemotePort(int port) {
        if (port < 1) {
            throw new IllegalArgumentException("Port value is below 0. Given value is " + port);
        }
        mRemotePort = port;
    }

    @Override
    public void setConnectionTimeout(int timeout) {
        if (timeout < 1) {
            throw new IllegalArgumentException("Connection timeout must be positive number. Given value is " + timeout);
        }
        mConnectionTimeout = timeout;
    }

    @Override
    public int getConnectionTimeout() {
        return mConnectionTimeout;
    }

    @Override
    public int getTrafficIn() {
        return 0;
    }

    @Override
    public int getTrafficOut() {
        return 0;
    }

    @Override
    public String getHttpClientName() {
        return mHttpClientName;
    }

    @Override
    public void setHttpClientName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("HttpClientName is null");
        }
        mHttpClientName = name;
    }

    // ================================================================================
    // ================================================================================


    @Override
	public boolean ping() throws InterruptedException {
		try {
			getPlaylists();
			return true;
        } catch(InterruptedException ex) {
            throw ex;
		} catch(Exception e) {
			Logger.e("Ping operation failed. Reason: " + e.getMessage(), e);
			return false;
		}
	}

    @Override
    public boolean play() throws ApiException, IOException, InterruptedException {
        sendRequest("/?action=player_play");
        return true;
    }

    @Override
    public boolean play(int playlistId, int songPosition) throws ApiException, IOException, InterruptedException {
        play(playlistId, songPosition, 0);
        return true;
    }

    @Override
    public boolean play(int playlistId, int songPosition, int playPosition) throws ApiException, IOException, InterruptedException {
        if (songPosition < 0) {
            throw new IllegalArgumentException("Invalid song's position value: " + songPosition);
        }
        if (playPosition < 0) {
            throw new IllegalArgumentException("Invalid song's play position value: " + playPosition);
        }

        sendRequest(
                "/?action=set_song_play" +
                        "&playlist=" + playlistId +
                        "&song=" + songPosition
        );

        setSongPlayPosition(playPosition);
        return true;
    }

    @Override
    public boolean stop() throws ApiException, IOException, InterruptedException {
        sendRequest("/?action=player_stop");
        return true;
    }

    @Override
    public boolean pause() throws ApiException, IOException, InterruptedException {
        sendRequest("/?action=player_pause");
        return true;
    }

    @Override
    public boolean next() throws ApiException, IOException, InterruptedException {
        sendRequest("/?action=player_next");
        return true;
    }

    @Override
    public boolean previous() throws ApiException, IOException, InterruptedException {
        sendRequest("/?action=player_prevous");
        return true;
    }

    @Override
    public int getPlayState() throws ApiException, IOException, InterruptedException {
        return getCustomStatus(Statuses.PLAY).equals("1")
                ? PLAY_STATE_PLAYING
                : PLAY_STATE_STOPPED
        ;
    }

    @Override
    public int getSongPlayPosition() throws ApiException, IOException, InterruptedException {
        String r = getCustomStatus(Statuses.POSITION);
        try {
            int position = Integer.parseInt(r);
            if (position < 0) {
                throw new NumberFormatException();
            }
            return position;
        } catch(NumberFormatException ex) {
            throw new ApiRequestException("Song play position expected to be a not negative integer. Response is: " + r);
        }
    }

    @Override
    public void setSongPlayPosition(int second) throws ApiException, IOException, InterruptedException {
        if (second < 0) {
            throw new IllegalArgumentException("Seconds must be not negative. Given value: " + second);
        }
        setCustomStatus(Statuses.POSITION, second);
    }

    @Override
    public void setRepeatSong(boolean state) throws ApiException, IOException, InterruptedException {
        setCustomStatus(Statuses.REPEAT_SONG, state);
    }

    @Override
    public boolean isRepeatSong() throws ApiException, IOException, InterruptedException {
        String r = getCustomStatus(Statuses.REPEAT_SONG);
        if (!r.equals("1") && !r.equals("0")) {
            throw new ApiRequestException("Repeat value is not 0 or 1. Response: " + r);
        } else {
            return r.equals("1");
        }
    }

    @Override
    public void setVolume(int volume) throws ApiException, IOException, InterruptedException {
        if (volume < 0 || volume > 100) {
            throw new IllegalArgumentException("Volume value must be in range of [0, 100]. Given value is " + volume);
        }
        setCustomStatus(Statuses.VOLUME, volume);
    }

    @Override
    public int getVolume() throws ApiException, IOException, InterruptedException {
        String r = getCustomStatus(Statuses.VOLUME);
        try {
            int volume = Integer.parseInt(r);
            if (volume < 0 || volume > 100) {
                throw new NumberFormatException();
            }
            return volume;
        } catch(NumberFormatException ex) {
            throw new ApiRequestException("Volume value must be a positive integer in range [0, 100]. Response: " + r);
        }
    }

    @Override
    public void setMute(boolean state) throws ApiException, IOException, InterruptedException {
        setCustomStatus(Statuses.MUTE, state);
    }

    @Override
    public boolean isMute() throws ApiException, IOException, InterruptedException {
        String r = getCustomStatus(Statuses.MUTE);
        if (!r.equals("1") && !r.equals("0")) {
            throw new ApiRequestException("Mute value is not 0 or 1. Response: " + r);
        } else {
            return r.equals("1");
        }
    }

    @Override
    public void setShuffle(boolean state) throws ApiException, IOException, InterruptedException {
        setCustomStatus(Statuses.SHUFFLE, state);
    }

    @Override
    public boolean isShuffle() throws ApiException, IOException, InterruptedException {
        String r = getCustomStatus(Statuses.SHUFFLE);
        if (!r.equals("1") && !r.equals("0")) {
            throw new ApiRequestException("Shuffle value is not 0 or 1. Response: " + r);
        } else {
            return r.equals("1");
        }
    }

    @Override
    public CurrentSongInfo getCurrentSongInfo() throws ApiException, IOException, InterruptedException {
        String response = asString(sendRequest("/?action=get_song_current"));

        try {
            JSONObject json = new JSONObject(response);

            if (!json.getString("status").equals("OK")) {
                throw new ApiRequestException("Unable to retrieve info about current playing song");
            }

            CurrentSongInfo si = new CurrentSongInfo();
            si.setPlaylistId(json.getInt("PlayingList"));
            si.setSongPosition(json.getInt("PlayingFile"));
            if (si.getSongPosition() < 0) {
                si.setInfo(null);
            } else {
                si.setInfo(new Song(json.getString("PlayingFileName"), json.getInt("length")));
            }

            return si;
        } catch (JSONException e) {
            throw new ApiRequestException("Unable to retrieve info about current playing song. Error parsing response: " + response);
        }
    }


	@Override
	public List<Playlist> getPlaylists() throws ApiException, IOException, InterruptedException {
		ArrayList<Playlist> result = new ArrayList<Playlist>(0);

		String response = asString(sendRequest("/?action=get_playlist_list"));

		JSONArray json;
		try {
			json = new JSONArray(response);
			result.ensureCapacity(json.length());

			for (int i = 0; i < json.length(); ++i) {
				JSONObject entry = json.getJSONObject(i);

				Playlist pl = new Playlist();
				pl.setId(entry.getInt("id"));
				pl.setName(entry.getString("name"));
				pl.setSizeInBytes(entry.getLong("size"));
				pl.setDuration(entry.getInt("duration"));
        		pl.setHash( getPlaylistHash(pl.getId()) );

				result.add(pl);
			}
		} catch (JSONException e) {
			throw new ApiRequestException("Error parsing response: " + response);
		}

		return result;
	}


    @Override
    public String getPlaylistHash(int playlistId) throws ApiException, IOException, InterruptedException {
        return asString(sendRequest("/?action=get_playlist_crc&id=" + playlistId));
    }

	@Override
	public List<Song> getPlaylistSongs(int playlistId) throws ApiException, IOException, InterruptedException {
		ArrayList<Song> result = new ArrayList<Song>(0);

        //TODO Jackson crashes when meets \ in song's name
        String response = asString(sendRequest("/?action=get_playlist_songs&id=" + playlistId));

		JSONObject json;
		try {
			json = new JSONObject(response);

			if (!json.getString("status").equals("OK")) {
				throw new ApiRequestException("Retrieve playlist's songs operation failed. Playlist with ID " + playlistId + " not found");
			}

			JSONArray songs = (JSONArray)json.get("songs");
			result.ensureCapacity(songs.length());
			for (int i = 0; i < songs.length(); ++i) {
				JSONObject entry = (JSONObject)songs.get(i);
				result.add(new Song(entry.getString("name"), entry.getInt("length")/1000));
			}
		} catch (JSONException e) {
			throw new ApiRequestException("Error parsing response: " + response);
		}

		return result;
	}

	@Override
	public void removeSong(int playlistId, int songPosition) throws ApiException, IOException, InterruptedException {
		sendRequest(
				"/?action=playlist_del_file" +
				"&playlist=" + playlistId +
				"&file=" + songPosition
		);
	}


    protected HttpEntity sendRequest(String request) throws IOException {
        final String uri = String.format(
                "http://%s:%d%s",
                mRemoteHost, mRemotePort, request
        );

        HttpParams httpParameters = new BasicHttpParams();
        HttpConnectionParams.setConnectionTimeout(httpParameters, mConnectionTimeout);
        HttpConnectionParams.setSoTimeout(httpParameters, mConnectionTimeout);

        DefaultHttpClient httpClient = new DefaultHttpClient(httpParameters);
        HttpGet httpGet = new HttpGet(uri);
        httpGet.setHeader("User-Agent", mHttpClientName);

        return httpClient.execute(httpGet).getEntity();
    }

    protected String asString(HttpEntity entity) throws IOException {
        return EntityUtils.toString(entity, HTTP.UTF_8);
    }

    protected InputStream asStream(HttpEntity entity) throws IOException {
        return entity.getContent();
    }


    protected void setCustomStatus(String status, String value) throws ApiException, IOException, InterruptedException {
        sendRequest(String.format(
                "/?action=set_custom_status&status=%s&value=%s",
                status,
                value
        ));
    }

    protected void setCustomStatus(int status, boolean value) throws ApiException, IOException, InterruptedException {
        setCustomStatus(String.valueOf(status), value ? "1" : "0");
    }

    protected void setCustomStatus(int status, int value) throws ApiException, IOException, InterruptedException {
        setCustomStatus(String.valueOf(status), String.valueOf(value));
    }

    protected String getCustomStatus(String status) throws ApiException, IOException, InterruptedException {
        return asString(sendRequest(String.format(
                "/?action=get_custom_status&status=%s",
                status
        )));
    }

    protected String getCustomStatus(int status) throws ApiException, IOException, InterruptedException {
        return getCustomStatus(String.valueOf(status));
    }

}