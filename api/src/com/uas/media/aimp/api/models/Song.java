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
public class Song {

	private String mName;
	private int mDuration;

    public Song() {

    }

    public Song(String name, int duration) {
		setName(name);
		setDuration(duration);
	}


	public void setName(String name) {
        if (name == null) {
            throw new IllegalArgumentException("Name is null");
        }
		mName = name;
	}

	public String getName() {
		return mName;
	}

	public void setDuration(int length) {
        if (length  < 0) {
            throw new IllegalArgumentException("Song duration must be positive");
        }
		mDuration = length;
	}

	public int getDuration() {
		return mDuration;
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

    @Override
    public int hashCode() {
        return mName.hashCode() ^ mDuration;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (!(obj instanceof Song)) return false;

        Song s = (Song) obj;
        return ( mName.equals(s.getName()) && mDuration == s.getDuration() );
    }

    @Override
    public String toString() {
        return String.format(
                "[%02d:%02d] %s",
                getMinutesDuration(),
                getSecondsDuration(),
                getName()
        );
    }

}