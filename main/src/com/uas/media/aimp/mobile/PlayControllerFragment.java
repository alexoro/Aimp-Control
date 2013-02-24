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
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import com.uas.media.aimp.player.AimpPlayer;
import com.uas.media.aimp.player.StateObserver;
import com.uas.media.aimp.player.StateObserverViaHandler;
import com.uas.media.aimp.utils.InitErrorHandler;
import com.uas.media.aimp.utils.Logger;

/**
 * User: uas.sorokin@gmail.com
 */
public class PlayControllerFragment extends Fragment {

    static class ViewHolder {
        public ViewGroup wrapper;
        public ImageView play;
        public ImageView pause;
        public ImageView previous;
        public ImageView next;
        public ImageView stop;
        public SeekBar playPosition;
    }

    private ViewHolder mViewHolder;

    private AimpPlayer mAimpPlayer;
    private boolean mTrackPositionInTouch;

    private SeekBar.OnSeekBarChangeListener mPlayPositionListener;
    private StateObserver mStateObserver;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup wrapper = (ViewGroup) inflater.inflate(R.layout.mobile_play_controller, container, false);

        mAimpPlayer = AimpPlayerInstance.get();
        mPlayPositionListener = new PlayPositionListenerImpl();
        mStateObserver = new StateObserverImpl();
        mTrackPositionInTouch = false;

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
        mViewHolder.play         = (ImageView) w.findViewById(R.id.play);
        mViewHolder.pause        = (ImageView) w.findViewById(R.id.pause);
        mViewHolder.previous     = (ImageView) w.findViewById(R.id.previous);
        mViewHolder.next         = (ImageView) w.findViewById(R.id.next);
        mViewHolder.stop         = (ImageView) w.findViewById(R.id.stop);
        mViewHolder.playPosition = (SeekBar) w.findViewById(R.id.play_position);
    }

    protected void initUi() {
        Song s = mAimpPlayer.getCurrentSong();
        if (s != null) {
            mViewHolder.playPosition.setMax(s.getDuration());
            mViewHolder.playPosition.setProgress(mAimpPlayer.getSongPlayPosition());
        } else {
            mViewHolder.playPosition.setMax(1);
            mViewHolder.playPosition.setProgress(0);
        }
    }

    protected void initUiEvents() {
        mViewHolder.playPosition.setOnSeekBarChangeListener(mPlayPositionListener);

        mViewHolder.play.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.play();
            }
        });
        mViewHolder.pause.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.pause();
            }
        });
        mViewHolder.previous.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.previous();
            }
        });
        mViewHolder.next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.next();
            }
        });
        mViewHolder.stop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAimpPlayer.stop();
            }
        });
    }


    class PlayPositionListenerImpl implements SeekBar.OnSeekBarChangeListener {
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            mTrackPositionInTouch = true;
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            mTrackPositionInTouch = false;
            if (mAimpPlayer.isConnected()) {
                mAimpPlayer.changeSongPlayPosition(seekBar.getProgress());
            }
        }
    };

    class StateObserverImpl extends StateObserverViaHandler {

        @Override
        public void onPlay(Song song) {
            mViewHolder.playPosition.setEnabled(true);
        }

        @Override
        public void onPause(Song song) {
            mViewHolder.playPosition.setEnabled(false);
        }

        @Override
        public void onStop(Song song) {
            mViewHolder.playPosition.setEnabled(false);
            mViewHolder.playPosition.setMax(1);
            mViewHolder.playPosition.setProgress(0);
        }

        @Override
        public void onSongPlayPositionChanged(Playlist playlist, Song song, int position, double percentage) {
            if (mTrackPositionInTouch) {
                return;
            }
            mViewHolder.playPosition.setMax(song.getDuration());
            mViewHolder.playPosition.setProgress(position);
        }

    }

}