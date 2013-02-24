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

package com.uas.media.aimp.mobile;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import com.uas.media.aimp.AimpPlayerInstance;
import com.uas.media.aimp.R;
import com.uas.media.aimp.player.AimpPlayer;
import com.uas.media.aimp.player.StateObserver;
import com.uas.media.aimp.player.StateObserverViaHandler;
import com.uas.media.aimp.utils.InitErrorHandler;
import com.uas.media.aimp.utils.Logger;

/**
 * User: uas.sorokin@gmail.com
 */
public class CommonControllerFragment extends Fragment {

    class ViewHolder {
        public ViewGroup wrapper;
        public ImageView mute;
        public ImageView repeat;
        public ImageView shuffle;
        public SeekBar volume;
    }

    private ViewHolder mViewHolder;

    private AimpPlayer mAimpPlayer;
    private boolean mVolumeInTouch;
    private StateObserver mStateObserver;
    private SeekBar.OnSeekBarChangeListener mVolumeListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup wrapper = (ViewGroup) inflater.inflate(R.layout.mobile_common_controller, container, false);

        mAimpPlayer = AimpPlayerInstance.get();
        mVolumeListener = new VolumeChangeListenerImpl();
        mStateObserver = new StateObserverImpl();
        mVolumeInTouch = false;

        try {
            initViewHolder(wrapper);
            initUi();
            initUiEvents();
        } catch (Exception ex) {
            InitErrorHandler.handle(ex);
        }

        mAimpPlayer.registerStateObserver(mStateObserver);

        return wrapper;
    }

    @Override
    public void onDestroyView() {
        mAimpPlayer.unregisterStateObserver(mStateObserver);
        super.onDestroyView();
    }


    protected void initViewHolder(ViewGroup w) {
        mViewHolder = new ViewHolder();
        mViewHolder.wrapper = w;
        mViewHolder.mute    = (ImageView) w.findViewById(R.id.mute);
        mViewHolder.repeat  = (ImageView) w.findViewById(R.id.repeat);
        mViewHolder.shuffle = (ImageView) w.findViewById(R.id.shuffle);
        mViewHolder.volume  = (SeekBar) w.findViewById(R.id.volume);
    }

    protected void initUi() {
        int resId;

        resId = mAimpPlayer.isShuffle() ? R.drawable.btn_shuffle_active : R.drawable.btn_shuffle_default;
        mViewHolder.shuffle.setImageResource(resId);

        resId = mAimpPlayer.isRepeatSong() ? R.drawable.btn_repeat_active : R.drawable.btn_repeat_default;
        mViewHolder.repeat.setImageResource(resId);

        resId = mAimpPlayer.isMute() ? R.drawable.btn_mute_on : R.drawable.btn_mute_off;
        mViewHolder.mute.setImageResource(resId);

        mViewHolder.volume.setProgress(mAimpPlayer.getVolume());
    }

    protected void initUiEvents() {
        mViewHolder.volume.setOnSeekBarChangeListener(mVolumeListener);
        mViewHolder.mute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.setMute(!mAimpPlayer.isMute());
            }
        });
        mViewHolder.repeat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.setRepeatSong(!mAimpPlayer.isRepeatSong());
            }
        });
        mViewHolder.shuffle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.setShuffle(!mAimpPlayer.isShuffle());
            }
        });
    }


    class StateObserverImpl extends StateObserverViaHandler {
        @Override
        public void onVolumeChanged(int volume) {
            if (mVolumeInTouch) {
                return;
            }
            initUi();
        }

        @Override
        public void onMuteStateChanged(boolean state) {
            initUi();
        }

        @Override
        public void onShuffleStateChanged(boolean state) {
            initUi();
        }

        @Override
        public void onRepeatSongStateChanged(boolean state) {
            initUi();
        }
    }


    class VolumeChangeListenerImpl implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mVolumeInTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mVolumeInTouch = false;
            if (mAimpPlayer.isConnected()) {
                mAimpPlayer.setVolume(seekBar.getProgress());
            }
        }
    }

}