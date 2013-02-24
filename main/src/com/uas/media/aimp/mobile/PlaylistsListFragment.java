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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import com.uas.media.aimp.AimpPlayerInstance;
import com.uas.media.aimp.R;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import com.uas.media.aimp.player.AimpPlayer;
import com.uas.media.aimp.player.StateObserver;
import com.uas.media.aimp.player.StateObserverViaHandler;
import com.uas.media.aimp.utils.InitErrorHandler;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: uas.sorokin@gmail.com
 */
public class PlaylistsListFragment extends Fragment {

    public interface OnInitCompletedListener {
        void onInitCompleted();
    }

    private ViewPager mPlsWrapper;
    private AimpPlayer mAimpPlayer;
    private PlaylistsAdapter mPlaylistsAdapter;
    private StateObserver mStateObserver;
    private ViewTreeObserver.OnGlobalLayoutListener mAdapterInitWaiter;
    private OnInitCompletedListener mInitCompleteListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup wrapper = (ViewGroup) inflater.inflate(R.layout.mobile_pls_list, null, false);
        //NOTICE Error appears when view's id is pls_wrapper
        mPlsWrapper = (ViewPager) wrapper.findViewById(R.id.view_pager);

        mAimpPlayer = AimpPlayerInstance.get();
        mStateObserver = new StateObserverImpl();
        mAimpPlayer.registerStateObserver(mStateObserver);

        try {
            initUi();
        } catch (Exception ex) {
            InitErrorHandler.handle(ex);
        }

        return wrapper;
    }

    @Override
    public void onDestroyView() {
        mAimpPlayer.unregisterStateObserver(mStateObserver);
        mAimpPlayer = null;
        super.onDestroyView();
    }

    public void setOnInitCompleteListener(OnInitCompletedListener listener) {
        mInitCompleteListener = listener;
    }

    public boolean scrollTo(Playlist playlist, Song song) {
        int playlistPosition = mAimpPlayer.getPlaylistPosition(playlist.getId());
        int songPosition = playlist.findSongPosition(song);
        mPlsWrapper.setCurrentItem(playlistPosition);

        PlaylistFragment pf = mPlaylistsAdapter.getFragment(playlistPosition);
        pf.scrollTo(songPosition);

        return true;
    }

    protected void initUi() {
        mAdapterInitWaiter = new AdapterInitWaiter();
        mPlsWrapper.getViewTreeObserver().addOnGlobalLayoutListener(mAdapterInitWaiter);
        updateAdapter();
    }

    protected void updateAdapter() {
        // init params
        Map<Integer, Integer> scrollPositions = new HashMap<Integer, Integer>();
        int playlistPosition = mPlsWrapper.getCurrentItem();
        if (mPlaylistsAdapter == null) {
            Playlist currentPlaylist = mAimpPlayer.getCurrentPlaylist();
            Song currentSong = mAimpPlayer.getCurrentSong();
            if (currentSong != null) {
                int songPosition = currentPlaylist.findSongPosition(currentSong);
                scrollPositions.put(currentPlaylist.getId(), songPosition);
            }
        } else {
            scrollPositions.putAll(mPlaylistsAdapter.getScrollPositions());
        }
        if (mPlaylistsAdapter == null || playlistPosition >= mAimpPlayer.getPlaylists().size()) {
            playlistPosition = mAimpPlayer.getPlaylistPosition(mAimpPlayer.getCurrentPlaylist().getId());
        }

        // set up adapter
        mPlaylistsAdapter = new PlaylistsAdapter(getChildFragmentManager(), mAimpPlayer.getPlaylists(), scrollPositions);
        mPlsWrapper.setAdapter(mPlaylistsAdapter);
        mPlsWrapper.setCurrentItem(playlistPosition);
    }


    class StateObserverImpl extends StateObserverViaHandler {
        @Override
        public void onPlaylistsInfoUpdated(List<Playlist> pls, Playlist currentPlaylist) {
            updateAdapter();
        }
    }

    class AdapterInitWaiter implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            mPlsWrapper.getViewTreeObserver().removeGlobalOnLayoutListener(mAdapterInitWaiter);
            if (mInitCompleteListener != null) {
                mInitCompleteListener.onInitCompleted();
            }
        }
    }

    class PlaylistsAdapter extends FragmentStatePagerAdapter {

        private List<Playlist> mPlaylists;
        private Map<Integer, PlaylistFragment> mFragments;
        private Map<Integer, Integer> mPositions;

        public PlaylistsAdapter(FragmentManager fm, List<Playlist> pls, Map<Integer, Integer> scrollPositions) {
            super(fm);
            mPlaylists = pls;
            mFragments = new HashMap<Integer, PlaylistFragment>();
            mPositions = new HashMap<Integer, Integer>();
            mPositions.putAll(scrollPositions);
        }

        @Override
        public Fragment getItem(int index) {
            int playlistId = mPlaylists.get(index).getId();
            Integer scrollPosition = mPositions.get(playlistId);
            if (scrollPosition == null) {
                scrollPosition = 0;
            }

            PlaylistFragment pf = PlaylistFragment.newInstance(playlistId, scrollPosition);
            mFragments.put(index, pf);
            return pf;
        }

        @Override
        public int getCount() {
            return mPlaylists.size();
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            PlaylistFragment pf = mFragments.get(position);
            mPositions.put(pf.getPlaylistId(), pf.getScrollPosition());
            mFragments.remove(position);
            super.destroyItem(container, position, object);
        }

        public PlaylistFragment getFragment(int index) {
            return mFragments.get(index);
        }

        public Map<Integer, Integer> getScrollPositions() {
            for (Map.Entry<Integer, PlaylistFragment> e: mFragments.entrySet()) {
                mPositions.put(e.getValue().getPlaylistId(), e.getValue().getScrollPosition());
            }
            return mPositions;
        }

        public void clear() {
            mFragments.clear();
            mPositions.clear();
        }

    }

}