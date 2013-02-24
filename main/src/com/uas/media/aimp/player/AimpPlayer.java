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

package com.uas.media.aimp.player;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import com.uas.media.aimp.api.ApiException;
import com.uas.media.aimp.api.IPlugin;
import com.uas.media.aimp.api.models.CurrentSongInfo;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import com.uas.media.aimp.utils.Logger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * User: uas.sorokin@gmail.com
 */
public class AimpPlayer {

    private static final int SONG_POSITION_UPDATER_DELAY = 0;
    private static final int SONG_POSITION_UPDATER_INTERVAL = 1000;

    private static final int ERRORS_COUNT_TO_CANCEL_CONNECT = 10;
    private static final int TIMEOUT_TO_RESET_ERRORS_COUNT = 1000*60;


    public enum ConnectionStatus {
        DISCONNECTED,
        CONNECTING,
        INIT,
        CONNECTED,
        DISCONNECTING
    }

    public enum PlayState {
        PLAYING, STOPPED, PAUSED
    }

    private enum Commands {
        SYNC,
        PLAY, STOP, PAUSE, NEXT, PREVIOUS,
        REPEAT, SHUFFLE, MUTE, VOLUME,
        CHANGE_SONG, CHANGE_SONG_PLAY_POSITION,
        REMOVE
    }

    private interface ApiTask {
        void execute() throws Exception;
    }


    private List<ConnectionListener> mConnectionListeners;
    private List<StateObserver> mStateObservers;

    private Context mContext;
    private IPlugin mPlugin;
    private SyncParams mSyncParams;

    private ConnectionStatus mConnStatus;
    private ServiceConnection mServiceConnection;
    private SyncService mSyncService;

    private Thread tFirstConnectionThread;
    private Thread tDisconnectThread;
    private ExecutorService mExecutorService;
    private int mErrorsCount;
    private long mErrorsLastRaiseTime;


    private volatile List<Playlist> mPlaylists;
    private volatile int mCurrentPlaylistId;
    private volatile int mCurrentSongPosition;
    private volatile PlayState mPlayState;
    private volatile int mSongPlayPosition;

    private volatile int mVolume;
    private volatile boolean mIsShuffle;
    private volatile boolean mIsRepeatSong;
    private volatile boolean mIsMute;
    private volatile int mVolumeBeforeMute;

    private Timer tSongPositionUpdater;


    public AimpPlayer() {
        init();
        stateInit();
    }

    protected void init() {
        mConnectionListeners = new CopyOnWriteArrayList<ConnectionListener>();
        mStateObservers = new CopyOnWriteArrayList<StateObserver>();

        mConnStatus = ConnectionStatus.DISCONNECTED;
        mErrorsCount = 0;
        mErrorsLastRaiseTime = -1;
    }


    // ======================================

    public int getErrorsCountToCancelConnect() {
        return ERRORS_COUNT_TO_CANCEL_CONNECT;
    }

    public long getTimeoutToResetErrorsCount() {
        return TIMEOUT_TO_RESET_ERRORS_COUNT;
    }

    public synchronized IPlugin getPlugin() {
        return mPlugin;
    }


    // ======================================
    // ======= REGISTER LISTENERS & OBSERVERS
    // ======================================

    public void registerConnectionListener(ConnectionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        mConnectionListeners.add(listener);
    }

    public void unregisterConnectionListener(ConnectionListener listener) {
        if (listener == null) {
            throw new IllegalArgumentException("Listener is null");
        }
        mConnectionListeners.remove(listener);
    }

    public void registerStateObserver(StateObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is null");
        }
        mStateObservers.add(observer);
    }

    public void unregisterStateObserver(StateObserver observer) {
        if (observer == null) {
            throw new IllegalArgumentException("Observer is null");
        }
        mStateObservers.remove(observer);
    }


    // ======================================
    // ======= CONNECTION & DISCONNECTION WRAPPERS
    // ======================================

    public synchronized boolean isConnected() {
        return mConnStatus == ConnectionStatus.CONNECTED;
    }

    public synchronized boolean isConnecting() {
        return !isConnected() && !isDisconnected();
    }

    public synchronized boolean isDisconnected() {
        return mConnStatus == ConnectionStatus.DISCONNECTED;
    }


    public synchronized void connect(Context context, IPlugin plugin, SyncParams syncParams) throws AimpException {
        if (context == null)
            throw new IllegalArgumentException("Context is null");
        if (plugin == null)
            throw new IllegalArgumentException("Plugin is null");
        if (syncParams == null)
            throw new IllegalArgumentException("Sync params is null");
        if (plugin.getRemoteHost() == null || plugin.getRemotePort() < 1 || plugin.getRemotePort() > 65535)
            throw new IllegalArgumentException("Host is not specified or port is not valid");

        if (isConnecting() || isConnected()) {
            throw new AimpException("Already connected or connecting");
        }

        mContext = context.getApplicationContext();
        mPlugin = plugin;
        mSyncParams = syncParams;

        mConnStatus = ConnectionStatus.CONNECTING;
        tFirstConnectionThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    doConnectAndInit();
                } catch (Exception ex) {
                    if (!(ex instanceof InterruptedException)) {
                        notifyUnresolvedError(ex);
                    }

                    try {
                        disconnect();
                    } catch (AimpException e) {
                        // already disconnecting. We skip this error
                    }
                }
            }
        });
        tFirstConnectionThread.setName("ConnectAndInitThread");
        tFirstConnectionThread.start();
    }

    public synchronized void disconnect() throws AimpException {
        if (tDisconnectThread != null) {
            throw new AimpException("Disconnecting or not connected");
        }

        tDisconnectThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    tFirstConnectionThread.interrupt();
                    tFirstConnectionThread.join();
                    destroyAndCleanConnection();
                } catch (InterruptedException e) {
                    // this thread will not be interrupted, so this message cannot be raised
                }
            }
        });
        tDisconnectThread.setName("DisconnectThread");
        tDisconnectThread.start();
    }


    // ======================================
    // ======= CONNECTION IMPLEMENTATION
    // ======================================

    /**
     * Do connect and init the base state
     * @throws ApiException If there was an error during the init
     * @throws IOException If there was an error during the init
     * @throws InterruptedException If connection operation was cancelled
     */
    protected void doConnectAndInit() throws ApiException, IOException, InterruptedException {
        // we update connection status earlier in connect()
        notifyConnectionStatusChanged();

        // trying to establish connection with remote host
        try {
            tryToResolveHost();
        } catch (UnknownHostException ex) {
            notifyHostNotFound();
            throw new InterruptedException();
        }

        // ping to check is AIMP installed
        if (!tryToPing()) {
            notifyAimpNotFound();
            throw new InterruptedException();
        }

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        // now is init time
        mConnStatus = ConnectionStatus.INIT;
        notifyConnectionStatusChanged();

        // AIMP is pinging, so let's init default values
        initWithDefaults();

        // AIMP is initialized, now create the sync service
        tryToCreateAndConnectToService();

        if (Thread.currentThread().isInterrupted()) {
            throw new InterruptedException();
        }

        // we connected. notify
        mConnStatus = ConnectionStatus.CONNECTED;
        notifyConnectionStatusChanged();

        // init executor
        mExecutorService = Executors.newSingleThreadExecutor();

        // all is OK, let's launch sync service
        mSyncService.submitSync(this, mSyncParams, new SyncService.ErrorsCallback() {
            @Override
            public void onException(Exception ex) {
                onExecutorException(Commands.SYNC, ex);
            }
        });

        // launch the song position updater
        tSongPositionUpdater = new Timer();
        tSongPositionUpdater.scheduleAtFixedRate(
                new SongPlayPositionUpdater(),
                SONG_POSITION_UPDATER_DELAY,
                SONG_POSITION_UPDATER_INTERVAL
        );
    }

    protected void tryToResolveHost() throws UnknownHostException {
        InetAddress.getByName(mPlugin.getRemoteHost());
    }

    protected boolean tryToPing() throws InterruptedException {
        return mPlugin.ping();
    }

    protected void tryToCreateAndConnectToService() throws InterruptedException {
        CountDownLatch cdl = new CountDownLatch(1);
        mServiceConnection = new ServiceConnectionImpl(cdl);
        mContext.bindService(
                new Intent(mContext, SyncService.class),
                mServiceConnection,
                Context.BIND_AUTO_CREATE
        );
        cdl.await();
    }

    protected void initWithDefaults() throws ApiException, IOException, InterruptedException {
        AimpPlayerPackageLoaders loaders = new AimpPlayerPackageLoaders(this);
        loaders.loadPlaylists(AimpPlayerPackageLoaders.CHECK_HASH_NO);
        loaders.loadCurrentSong();
        loaders.loadCommons();
        loaders.loadVolume();
        loaders.loadSongPlayPosition();
    }


    // ======================================
    // ======= DISCONNECTION IMPLEMENTATION
    // ======================================

    protected void destroyAndCleanConnection() {
        mConnStatus = ConnectionStatus.DISCONNECTING;
        notifyConnectionStatusChanged();

        // if there's an active update thread - cancel()
        if (mExecutorService != null) {
            mExecutorService.shutdownNow();
            mExecutorService = null;
        }

        // if sync service is active - cancel()
        if (mSyncService != null) {
            mContext.unbindService(mServiceConnection);
            mContext.stopService(new Intent(mContext, SyncService.class));
            mSyncService = null;
            mServiceConnection = null;
        }

        mContext = null;
        mPlugin = null;
        mSyncParams = null;

        mErrorsCount = 0;
        mErrorsLastRaiseTime = -1;

        stateClear();

        synchronized (this) {
            tFirstConnectionThread = null;
            tDisconnectThread = null;
        }

        mConnStatus = ConnectionStatus.DISCONNECTED;
        notifyConnectionStatusChanged();
    }


    // ======================================
    // ======= NOTIFYERS
    // ======================================

    protected void notifyConnectionStatusChanged() {
        for (ConnectionListener listener: mConnectionListeners) {
            listener.notifyConnectionStatusChanged(mPlugin, mConnStatus);
        }
    }

    protected void notifyHostNotFound() {
        for (ConnectionListener listener: mConnectionListeners) {
            listener.notifyHostNotFound(mPlugin);
        }
    }

    protected void notifyAimpNotFound() {
        for (ConnectionListener listener: mConnectionListeners) {
            listener.notifyAimpNotFound(mPlugin);
        }
    }

    protected void notifyUnresolvedError(Exception ex) {
        for (ConnectionListener listener: mConnectionListeners) {
            listener.notifyUnresolvedError(mPlugin, ex);
        }
    }


    // ============================================================
    // ============= STATE INIT
    // ============================================================

    protected void stateInit() {
        mPlaylists = new ArrayList<Playlist>();
        stateClear();
    }

    protected void stateClear() {
        mPlaylists.clear();
        mCurrentPlaylistId = -1;
        mCurrentSongPosition = -1;
        mPlayState = PlayState.STOPPED;
        mSongPlayPosition = 0;

        mVolume = 0;
        mIsShuffle = false;
        mIsRepeatSong = false;
        mIsMute = false;
        mVolumeBeforeMute = 0;

        if (tSongPositionUpdater != null) {
            tSongPositionUpdater.cancel();
            tSongPositionUpdater.purge();
            tSongPositionUpdater = null;
        }
    }


    // ============================================================
    // ============= GETTERS
    // ============================================================

    public synchronized boolean isPlaying() {
        return mPlayState == PlayState.PLAYING;
    }

    public synchronized boolean isPaused() {
        return mPlayState == PlayState.PAUSED;
    }

    public synchronized boolean isStopped() {
        return mPlayState == PlayState.STOPPED;
    }

    public synchronized PlayState getPlayState() {
        return mPlayState;
    }

    public synchronized int getVolume() {
        return mVolume;
    }

    public synchronized boolean isShuffle() {
        return mIsShuffle;
    }

    public synchronized boolean isRepeatSong() {
        return mIsRepeatSong;
    }

    public synchronized boolean isMute() {
        return mIsMute;
    }

    public synchronized boolean hasPlaylists() {
        return mPlaylists.size() != 0;
    }

    public synchronized List<Playlist> getPlaylists() {
        return Collections.unmodifiableList(mPlaylists);
    }

    public synchronized Playlist getPlaylistById(int id) {
        for (Playlist pl: mPlaylists) {
            if (pl.getId() == id) {
                return pl;
            }
        }
        return null;
    }

    public synchronized int getPlaylistPosition(int id) {
        for (int i = 0; i < mPlaylists.size(); i++) {
            if (mPlaylists.get(i).getId() == id) {
                return i;
            }
        }
        return -1;
    }

    public synchronized Playlist getCurrentPlaylist() {
        return getPlaylistById(mCurrentPlaylistId);
    }

    public synchronized Song getCurrentSong() {
        Playlist currentPlaylist = getCurrentPlaylist();
        if (mCurrentSongPosition == -1 || currentPlaylist == null) {
            return null;
        } else {
            return currentPlaylist.getSong(mCurrentSongPosition);
        }
    }

    public synchronized int getSongPlayPosition() {
        switch(mPlayState) {
            case PLAYING:
            case PAUSED:
                return mSongPlayPosition;
            case STOPPED:
            default:
                return 0;
        }
    }


    // =============================================
    // ============ SETTERS
    // =============================================

    protected synchronized void setStatePlaylists(List<Playlist> pls, int currentPlaylistId) {
        int hashcodesOld = 0;
        int hashcodesNew = 0;

        for (Playlist pl: mPlaylists) {
            hashcodesOld ^= pl.hashCode();
        }
        for (Playlist pl: pls) {
            hashcodesNew ^= pl.hashCode();
        }

        if (hashcodesOld != hashcodesNew) {
            mPlaylists.clear();
            mPlaylists.addAll(pls);
            mCurrentPlaylistId = currentPlaylistId;

            for (StateObserver so: mStateObservers) {
                so.notifyPlaylistsInfoUpdated(getPlaylists(), getCurrentPlaylist());
            }
        }
    }

    protected synchronized void setStateCurrentSong(int playlistId, int songPosition, int playPosition) {
        boolean isSongChanged = (mCurrentPlaylistId != playlistId) || (mCurrentSongPosition != songPosition);
        mCurrentPlaylistId = playlistId;
        mCurrentSongPosition = songPosition;

        if (songPosition < 0) {
            mSongPlayPosition = 0;

            for (StateObserver so: mStateObservers) {
                so.notifySongChanged(getCurrentPlaylist(), null, 0, 0.0d);
            }
        } else {
            mSongPlayPosition = playPosition;

            if (isSongChanged) {
                for (StateObserver so: mStateObservers) {
                    so.notifySongChanged(
                            getCurrentPlaylist(), getCurrentSong(),
                            mSongPlayPosition, (double) mSongPlayPosition / getCurrentSong().getDuration()
                    );
                }
            }
        }
    }

    protected synchronized void setStatePlayState(PlayState playState) {
        switch (playState) {
            case PLAYING:
                setStateIsPlaying();
                break;
            case STOPPED:
                setStateIsStopped();
                break;
            case PAUSED:
                setStateIsPaused();
                break;
        }
    }

    protected synchronized void setStateIsPlaying() {
        if (mPlayState == PlayState.PLAYING) {
            return;
        }

        mPlayState = PlayState.PLAYING;
        for (StateObserver so: mStateObservers) {
            so.notifyPlay(getCurrentSong());
        }
    }

    protected synchronized void setStateIsStopped() {
        if (mPlayState == PlayState.STOPPED) {
            return;
        }

        mPlayState = PlayState.STOPPED;
        for (StateObserver so: mStateObservers) {
            so.notifyStop(getCurrentSong());
        }
    }

    protected synchronized void setStateIsPaused() {
        if (mPlayState == PlayState.PAUSED) {
            return;
        }

        mPlayState = PlayState.PAUSED;
        for (StateObserver so: mStateObservers) {
            so.notifyPause(getCurrentSong());
        }
    }

    protected synchronized void setStateSongPlayPosition(int seconds) {
        boolean isPositionChanged = seconds != mSongPlayPosition;
        mSongPlayPosition = seconds;

        if (isPositionChanged) {
            for (StateObserver so: mStateObservers) {
                so.notifySongPlayPositionChanged(
                        getCurrentPlaylist(), getCurrentSong(),
                        mSongPlayPosition, (double) mSongPlayPosition / getCurrentSong().getDuration()
                );
            }
        }
    }

    protected synchronized void setStateVolumeValue(int volume) {
        boolean isVolumeChanged = volume == mVolume;

        mVolume = volume;
        if (mVolume > 0 && mIsMute) {
            setStateIsMute(false);
        }

        if (isVolumeChanged) {
            for (StateObserver so: mStateObservers) {
                so.notifyVolumeChanged(mVolume);
            }
        }
    }

    protected synchronized void setStateIsShuffle(boolean state) {
        if (mIsShuffle == state) {
            return;
        }

        mIsShuffle = state;
        for (StateObserver so: mStateObservers) {
            so.notifyShuffleStateChanged(mIsShuffle);
        }
    }

    protected synchronized void setStateIsRepeatSong(boolean state) {
        if (mIsRepeatSong == state) {
            return;
        }

        mIsRepeatSong = state;

        for (StateObserver so: mStateObservers) {
            so.notifyRepeatSongStateChanged(mIsRepeatSong);
        }
    }

    protected synchronized void setStateIsMute(boolean state) {
        if (mIsMute == state) {
            return;
        }

        mIsMute = state;
        for (StateObserver so: mStateObservers) {
            so.notifyMuteStateChanged(mIsMute);
        }
    }

    protected synchronized void removeSongFromPlaylist(int playlistId, int songPosition) {
        Playlist pl = getPlaylistById(playlistId);
        pl.removeSong(songPosition);

        for (StateObserver so: mStateObservers) {
            so.notifyPlaylistUpdated(pl);
        }
    }


    // ============================================================
    // ============= PUBLIC SETTERS
    // ============================================================

    public void play() {
        submit(Commands.PLAY, new ApiTask() {
            @Override
            public void execute() throws Exception {
                setStateIsPlaying();
                getPlugin().play();
            }
        });
    }

    public void stop() {
        submit(Commands.STOP, new ApiTask() {
            @Override
            public void execute() throws Exception {
                setStateIsStopped();
                getPlugin().stop();
            }
        });
    }

    public void pause() {
        submit(Commands.PAUSE, new ApiTask() {
            @Override
            public void execute() throws Exception {
                setStateIsPaused();
                getPlugin().pause();
            }
        });
    }

    public void next() {
        submit(Commands.NEXT, new ApiTask() {
            @Override
            public void execute() throws Exception {
                getPlugin().next();
                CurrentSongInfo csi = getPlugin().getCurrentSongInfo();
                setStateCurrentSong(csi.getPlaylistId(), csi.getSongPosition(), 0);
            }
        });
    }

    public void previous() {
        submit(Commands.PREVIOUS, new ApiTask() {
            @Override
            public void execute() throws Exception {
                getPlugin().previous();
                CurrentSongInfo csi = getPlugin().getCurrentSongInfo();
                setStateCurrentSong(csi.getPlaylistId(), csi.getSongPosition(), 0);
            }
        });
    }

    public void setRepeatSong(final boolean state) {
        submit(Commands.REPEAT, new ApiTask() {
            @Override
            public void execute() throws Exception {
                setStateIsRepeatSong(state);
                getPlugin().setRepeatSong(state);
            }
        });
    }

    public void setShuffle(final boolean state) {
        submit(Commands.SHUFFLE, new ApiTask() {
            @Override
            public void execute() throws Exception {
                setStateIsShuffle(state);
                getPlugin().setShuffle(state);
            }
        });
    }

    public void setMute(final boolean state) {
        submit(Commands.MUTE, new ApiTask() {
            @Override
            public void execute() throws Exception {
                int volume_new = state ? 0 : mVolumeBeforeMute;
                if (state) {
                    mVolumeBeforeMute = getVolume();
                }

                synchronized (AimpPlayer.this) {
                    setStateIsMute(state);
                    setStateVolumeValue(volume_new);
                }

                getPlugin().setMute(state);
            }
        });
    }

    public void setVolume(final int volume) {
        submit(Commands.VOLUME, new ApiTask() {
            @Override
            public void execute() throws Exception {
                setStateVolumeValue(volume);
                getPlugin().setVolume(volume);
            }
        });
    }


    public void changeSong(final int playlistId, final int songPosition) {
        submit(Commands.CHANGE_SONG, new ApiTask() {
            @Override
            public void execute() throws Exception {
                Playlist pl = getPlaylistById(playlistId);
                if (pl == null) {
                    throw new AimpException("Specified playlist is not found");
                }
                if (songPosition < 0 || songPosition >= pl.getSongs().size()) {
                    throw new AimpException("Specified song is not found");
                }

                synchronized (AimpPlayer.this) {
                    setStateCurrentSong(playlistId, songPosition, 0);
                    setStateIsPlaying();
                }

                getPlugin().play(playlistId, songPosition);
            }
        });
    }

    public void changeSongPlayPosition(final int position) {
        submit(Commands.CHANGE_SONG_PLAY_POSITION, new ApiTask() {
            @Override
            public void execute() throws Exception {
                if (isStopped()) {
                    return;
                }

                setStateSongPlayPosition(position);
                getPlugin().setSongPlayPosition(position);
            }
        });
    }

    public void removeSong(final int playlistId, final int songPosition) {
        submit(Commands.REMOVE, new ApiTask() {
            @Override
            public void execute() throws Exception {
                getPlugin().removeSong(playlistId, songPosition);
                removeSongFromPlaylist(playlistId, songPosition);
            }
        });
    }


    // =============================================
    // ============ Executor
    // =============================================

    protected void submit(final Commands command, final ApiTask apiTask) {
        Runnable r = new Runnable() {
            @Override
            public void run() {
                try {
                    apiTask.execute();
                } catch (Exception ex) {
                    onExecutorException(command, ex);
                }
            }
        };
        mExecutorService.submit(r);
    }


    protected void onExecutorException(Commands command, Exception ex) {
        Logger.e(ex.getMessage(), ex);

        if (ex instanceof InterruptedException) {
            return;
        }

        long errorLastRaiseTimeDiffToNow = System.currentTimeMillis() - mErrorsLastRaiseTime;
        if (errorLastRaiseTimeDiffToNow < getTimeoutToResetErrorsCount()) {
            mErrorsCount++;

            if (mErrorsCount >= getErrorsCountToCancelConnect()) {
                try {
                    disconnect();
                } catch (AimpException e) {
                    Logger.e(e.getMessage(), e);
                }
            } else {
                mErrorsLastRaiseTime = System.currentTimeMillis();
            }

        } else {
            mErrorsLastRaiseTime = System.currentTimeMillis();
            mErrorsCount = 1;
        }
    }


    class SongPlayPositionUpdater extends TimerTask {
        @Override
        public void run() {
            synchronized (AimpPlayer.this) {
                Song current = getCurrentSong();
                if (current != null && mPlayState == PlayState.PLAYING && mSongPlayPosition < current.getDuration()) {
                    setStateSongPlayPosition(getSongPlayPosition() + 1);
                }
            }
        }
    }

    class ServiceConnectionImpl implements ServiceConnection {
        private CountDownLatch mLatch;

        ServiceConnectionImpl(CountDownLatch latch) {
            mLatch = latch;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mSyncService = ((SyncService.LocalBinder)service).getService();
            mLatch.countDown();
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    }

    public static SyncParams defaultSyncParams() {
        return new SyncParams() {
            @Override
            public long getPlaylistsUpdatePeriod() {
                return 1000*60*5;
            }

            @Override
            public long getPlaystateUpdatePeriod() {
                return 1000*5;
            }

            @Override
            public long getOthersUpdatePeriod() {
                return 1000*10;
            }
        };
    }

}