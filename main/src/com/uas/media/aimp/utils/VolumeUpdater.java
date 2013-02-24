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

import com.uas.media.aimp.player.AimpPlayer;

/**
 * User: uas.sorokin@gmail.com
 */
public class VolumeUpdater {

	public static final int DIRECTION_UP = 1;
	public static final int DIRECTION_DOWN = -1;

	private static final float VOLUME_KOEFF = 1.1f;
	private static final float VOLUME_INC = 2.6f;
	private static final long DELAY = 250;

	private float mVolumeKoeff = VOLUME_KOEFF; // addition to volume
	private boolean mIsActive = false;

    private AimpPlayer mAimpPlayer;
	private Updater tUpdater;


	public VolumeUpdater() {
		mIsActive = false;
	}


	public void start(AimpPlayer aimpPlayer, int direction) {
        if (mIsActive) {
            return;
        }

		mAimpPlayer = aimpPlayer;
		mIsActive = true;
		tUpdater = new Updater(direction);
		tUpdater.start();
	}

	public void stop(AimpPlayer aimpPlayer) {
        if (!mIsActive) {
            return;
        }

		mAimpPlayer = aimpPlayer;
		mIsActive = false;
		tUpdater = null;
	}


	class Updater extends Thread {
		private final int mDirection;

		public Updater(int direction) {
			mDirection = direction;
		}
		
		@Override
		public void run() {
			while (mIsActive) {
				try {
					int volume = mAimpPlayer.getVolume() + mDirection*(int)mVolumeKoeff;
					if (volume > 100) {
                        volume = 100;
                    }
					if (volume < 0) {
                        volume = 0;
                    }
					mAimpPlayer.setVolume(volume);
					mVolumeKoeff += VOLUME_INC;
					Thread.sleep(DELAY);
				} catch (Exception e) {
                    Logger.e(e.getMessage(), e);
                }
			}
			mVolumeKoeff = VOLUME_KOEFF;
		}
	}

}