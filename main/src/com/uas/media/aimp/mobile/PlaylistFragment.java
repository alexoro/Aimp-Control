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

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.uas.media.aimp.AimpPlayerInstance;
import com.uas.media.aimp.R;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import com.uas.media.aimp.player.AimpPlayer;
import com.uas.media.aimp.player.StateObserver;
import com.uas.media.aimp.player.StateObserverViaHandler;
import com.uas.media.aimp.utils.InitErrorHandler;
import com.uas.media.aimp.utils.Logger;

import java.util.List;


/**
 * User: uas.sorokin@gmail.com
 */
public class PlaylistFragment extends Fragment {

    public static final String PLAYLIST_ID = "playlistId";
    public static final String SCROLL_POSITION = "scrollPosition";

    static class ViewHolder {
        public ViewGroup wrapper;
        public TextView title;
        public TextView duration;
        public ListView list;
        public EditText searcher;
        public ImageView searchCancel;
    }

    private ViewHolder mViewHolder;

    private AimpPlayer mAimpPlayer;
    private SongsAdapter mSongsAdapter;
    private int mPlaylistId;
    private boolean mIsCurrentPlaylist;
    private Song mCurrentSong;
    private int mScrollPosition;

    private TextWatcher mFilterTextWatcher;
    private View.OnClickListener mSearcherCancelListener;
    private AbsListView.OnScrollListener mScrollListener;
    private StateObserver mAimpStateObserver;


    public static PlaylistFragment newInstance(int playlistId, int scrollPosition) {
        PlaylistFragment pageFragment = new PlaylistFragment();
        Bundle bundle = new Bundle();
        bundle.putInt(PLAYLIST_ID, playlistId);
        bundle.putInt(SCROLL_POSITION, scrollPosition);
        pageFragment.setArguments(bundle);
        return pageFragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        ViewGroup wrapper = (ViewGroup) inflater.inflate(R.layout.mobile_pls, container, false);

        mPlaylistId = getArguments().getInt(PLAYLIST_ID);
        mScrollPosition = getArguments().getInt(SCROLL_POSITION);
        mAimpPlayer = AimpPlayerInstance.get();
        mCurrentSong = mAimpPlayer.getCurrentSong();

        Playlist currentPlaylist = mAimpPlayer.getCurrentPlaylist();
        mIsCurrentPlaylist = (currentPlaylist != null && mPlaylistId == currentPlaylist.getId());

        try {
            initViewHolder(wrapper);
            initUi();
            initUiEvents();
        } catch (Exception ex) {
            InitErrorHandler.handle(ex);
        }

        mAimpStateObserver = new StateObserverImpl();
        mAimpPlayer.registerStateObserver(mAimpStateObserver);

        return wrapper;
    }

    @Override
    public void onDestroyView() {
        clearUiEvents();
        mAimpPlayer.unregisterStateObserver(mAimpStateObserver);
        mAimpPlayer = null;
        mSongsAdapter = null;
        super.onDestroyView();
    }


    // ============================================================
    // =========== INIT UI
    // ============================================================

    protected void initViewHolder(ViewGroup w) {
        mViewHolder = new ViewHolder();
        mViewHolder.wrapper = w;
        mViewHolder.title           = (TextView) w.findViewById(R.id.title);
        mViewHolder.duration        = (TextView) w.findViewById(R.id.duration);
        mViewHolder.list            = (ListView) w.findViewById(R.id.list);
        mViewHolder.searcher        = (EditText) w.findViewById(R.id.search);
        mViewHolder.searchCancel    = (ImageView) w.findViewById(R.id.cancel);
    }

    protected void initUi() {
        final Playlist pl = mAimpPlayer.getPlaylistById(mPlaylistId);

        mViewHolder.title.setText(formatPlaylistHeader(pl));
        mViewHolder.duration.setText(formatPlaylistDuration(pl));

        mViewHolder.list.setTextFilterEnabled(true);
        updateAdapter();
        scrollTo(mScrollPosition);
    }

    protected void initUiEvents() {
        mFilterTextWatcher = new SearchTextWatcherImpl();
        mSearcherCancelListener = new SearchCancelClickListener();
        mScrollListener = new SrollListener();

        mViewHolder.searcher.addTextChangedListener(mFilterTextWatcher);
        mViewHolder.searchCancel.setOnClickListener(mSearcherCancelListener);

        mViewHolder.list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Playlist current = mAimpPlayer.getPlaylistById(mPlaylistId);
                Song s = mSongsAdapter.getItem(position);
                onSongClick(current, s);
            }
        });
        mViewHolder.list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Playlist current = mAimpPlayer.getPlaylistById(mPlaylistId);
                Song s = mSongsAdapter.getItem(position);
                onSongLongClick(current, s);
                return true;
            }
        });
        mViewHolder.list.setOnScrollListener(mScrollListener);
    }

    protected void clearUiEvents() {
        mViewHolder.searcher.removeTextChangedListener(mFilterTextWatcher);
    }


    // ============================================================
    // =========== UI UTILS
    // ============================================================

    public int getScrollPosition() {
        return mScrollPosition;
    }

    public int getPlaylistId() {
        return mPlaylistId;
    }

    public void scrollTo(final int position) {
        mViewHolder.list.requestFocusFromTouch();
        mViewHolder.list.setSelection(position);
        //mViewHolder.list.setSelectionFromTop(position, 0);
        mViewHolder.list.requestFocus();
    }

    protected void updateAdapter() {
        Playlist pl = mAimpPlayer.getPlaylistById(mPlaylistId);
        mSongsAdapter = new SongsAdapter(getActivity(), pl.getSongs());
        mViewHolder.list.setAdapter(mSongsAdapter);
    }

    protected String formatPlaylistHeader(Playlist playlist) {
        return getActivity().getString(R.string.playlist_header_format)
                .replace("%PLAYLIST_POSITION%", String.valueOf(mAimpPlayer.getPlaylistPosition(playlist.getId()) + 1))
                .replace("%PLAYLIST_COUNT%", String.valueOf(mAimpPlayer.getPlaylists().size()))
                .replace("%PLAYLIST_NAME%", playlist.getName())
        ;
    }

    protected String formatPlaylistDuration(Playlist playlist) {
        int duration = playlist.getDuration()/1000;
        int hours = duration / 3600;
        int seconds = duration%60;
        int minutes = duration / 60 - hours * 60;
        return getString(R.string.playlist_length_format, hours, minutes, seconds);
    }

    protected void onSongClick(Playlist playlist, Song s) {
        mAimpPlayer.changeSong(playlist.getId(), playlist.findSongPosition(s));
    }

    protected void onSongLongClick(final Playlist playlist, final Song s) {
        AlertDialog d = new AlertDialog.Builder(getActivity())
                .setTitle(R.string.song_delete_title)
                .setMessage(getString(R.string.song_delete_message, s.getName()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.song_delete_submit, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Playlist pl = mAimpPlayer.getPlaylistById(mPlaylistId);
                        int songPosition = pl.findSongPosition(s);
                        mAimpPlayer.removeSong(mPlaylistId, songPosition);
                    }
                })
                .setCancelable(true)
                .create()
        ;
        d.show();
    }


    class SearchTextWatcherImpl implements TextWatcher {
        @Override
        public void afterTextChanged(Editable s) {

        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            mViewHolder.searchCancel.setVisibility(
                    s.length() == 0 ? View.GONE : View.VISIBLE
            );
            mSongsAdapter.getFilter().filter(s);
        }
    }

    class SearchCancelClickListener implements View.OnClickListener {
        @Override
        public void onClick(View view) {
            mViewHolder.searcher.setText("");
        }
    }

    class StateObserverImpl extends StateObserverViaHandler {
        @Override
        public void onSongChanged(Playlist playlist, Song song, int position, double percentage) {
            if (playlist != null) { // YES, it should not be happen, but it happens :(
                mIsCurrentPlaylist = playlist.getId() == mPlaylistId;
                mCurrentSong = song;
                mSongsAdapter.notifyDataSetChanged();
            }
        }

        @Override
        public void onPlaylistUpdated(Playlist playlist) {
            int scrollPosition = getScrollPosition();
            updateAdapter();
            scrollTo(scrollPosition);
        }
    }

    class SrollListener implements AbsListView.OnScrollListener {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {

        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            mScrollPosition = firstVisibleItem;
        }
    }


    // ============================================================
    // =========== SONG's ADAPTER
    // ============================================================

    static class SongViewHolder {
        public TextView position;
        public TextView title;
        public TextView duration;
    }

    class SongsAdapter extends ArrayAdapter<Song> {

        private String mSongLengthFormat;

        public SongsAdapter(Context context, List<Song> songs) {
            super(context, -1, songs);
            mSongLengthFormat = getString(R.string.song_length_format);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            if (v == null) {
                v = getActivity().getLayoutInflater().inflate(R.layout.mobile_pls_song, parent, false);
                SongViewHolder holder = new SongViewHolder();
                holder.position = (TextView) v.findViewById(R.id.position);
                holder.title    = (TextView) v.findViewById(R.id.title);
                holder.duration = (TextView) v.findViewById(R.id.duration);
                v.setTag(holder);
            }

            final Song s = getItem(position);
            final SongViewHolder holder = (SongViewHolder) v.getTag();

            holder.position.setText(String.valueOf(position + 1));
            holder.title.setText(s.getName());
            holder.duration.setText(String.format(
                    mSongLengthFormat,
                    s.getMinutesDuration(),
                    s.getSecondsDuration())
            );

            if (mIsCurrentPlaylist && mCurrentSong != null && mCurrentSong.equals(s)) {
                v.setBackgroundResource(R.drawable.bg_pls_song_default);
            } else {
                v.setBackgroundResource(R.drawable.bg_pls_song_current);
            }

            return v;
        }
    }

}