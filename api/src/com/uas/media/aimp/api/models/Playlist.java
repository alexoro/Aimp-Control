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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: uas.sorokin@gmail.com
 */
public class Playlist {

	private int mId;
	private int mDuration;
	private long mSize;
	private String mName;
	private String mHash;

	private final ArrayList<Song> mSongs;

    private boolean mIsDataChanged;
    private int mHashCode;


	public Playlist() {
		mSongs = new ArrayList<Song>(256);
        mIsDataChanged = true;
        mHashCode = 0;
	}

	public int getId() {
		return mId;
	}

	public void setId(int id) {
		mId = id;
        mIsDataChanged = true;
	}


	public String getName() {
		return mName;
	}

	public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }
		mName = name;
        mIsDataChanged = true;
	}


	public long getSizeInBytes() {
		return mSize;
	}

	public void setSizeInBytes(long size) {
        if (size < 0) {
            throw new IllegalArgumentException("Size of playlist must be positive number");
        }
		mSize = size;
        mIsDataChanged = true;
	}


	public int getDuration() {
		return mDuration;
	}

	public void setDuration(int duration) {
        if (duration < 0) {
            throw new IllegalArgumentException("Playlist's duration must be positive number");
        }
		mDuration = duration;
        mIsDataChanged = true;
	}

    public int getHoursDuration() {
        return getMinutesDuration() / 60;
    }

    public int getMinutesDuration() {
        return mDuration / 60;
    }

    public int getSecondsDuration() {
        return mDuration % 60;
    }


    public String getHash() {
		return mHash;
	}

	public void setHash(String hash) {
        if (hash == null) {
            throw new IllegalArgumentException("Hash is null");
        }
		mHash = hash;
        mIsDataChanged = true;
	}

	public void setSongs(List<Song> songs) {
        if (songs == null) {
            throw new IllegalArgumentException("Songs list is null");
        }
        mSongs.clear();
        mSongs.ensureCapacity(songs.size());
        mSongs.addAll(songs);
        mIsDataChanged = true;
	}

	public List<Song> getSongs() {
		return Collections.unmodifiableList(mSongs);
	}

	public Song getSong(int position) {
        return mSongs.get(position);
	}


	public int findSongPosition(Song song) {
		return mSongs.indexOf(song);
	}

	public boolean containsSong(Song song) {
		return mSongs.contains(song);
	}

    public void removeSong(int position) {
        mSongs.remove(position);
    }

    @Override
    public int hashCode() {
        if (mIsDataChanged) {
            synchronized (this) {
                mHashCode = mId ^ mDuration ^ (int)mSize ^ mName.hashCode() ^ mHash.hashCode();
                mIsDataChanged = false;
            }
        }
        return mHashCode;
    }

    @Override
	public boolean equals(Object obj) {
		if( obj == null ) return false;
		if( !(obj instanceof Playlist) ) return false;

		return mHash.equals( ((Playlist)obj).getHash() );
	}

}
