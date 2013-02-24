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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.bugsense.trace.BugSenseHandler;
import com.uas.media.aimp.AimpPlayerInstance;
import com.uas.media.aimp.R;
import com.uas.media.aimp.api.IPlugin;
import com.uas.media.aimp.api.impl.WebCtlPlugin;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import com.uas.media.aimp.player.*;
import com.uas.media.aimp.utils.*;
import net.robotmedia.billing.BillingController;
import net.robotmedia.billing.BillingRequest;
import net.robotmedia.billing.helper.AbstractBillingObserver;
import net.robotmedia.billing.model.Transaction;

/**
 * User: uas.sorokin@gmail.com
 */
public class MainActivity extends SherlockFragmentActivity {

    private static final long DOUBLE_CLICK_DELAY = 350;
    private static final int MIN_KEYBOARD_HEIGHT_IN_DP = 120;

    static class ViewHolder {
        public View activityRootView;
        public ViewGroup connectionWrapper;
        public ViewGroup plsWrapper;
        public ViewGroup controllerWrapper;
        public ViewGroup panel;
        public ViewGroup initPls;

        public TextView timer;
        public ImageView playControllerIcon;
        public ImageView commonControllerIcon;
        public View playControllerSwitcher;
        public View commonControllerSwitcher;

        public ActionBar actionBar;
        public ImageView connectionIcon;
    }


    private ViewHolder mViewHolder;
    private PlayControllerFragment fPlayControllerFragment;
    private CommonControllerFragment fCommonControllerFragment;
    protected PlaylistsListFragment fPlaylistsList;

    private ViewTreeObserver.OnGlobalLayoutListener mOnGlobalLayoutListener;
    private AbstractBillingObserver mBillingObserver;
    private PlayerMenu.OnChooseListener mMenuListener;

    private AimpPlayer mAimpPlayer;
    private VolumeUpdater tVolumeUpdater;
    private ConnectionListener mConnectionListener;
    private StateObserver mStateObserver;

    private long mTimerLastClick;
    private AlertDialog mMenuDialog;
    private Dialog mConnectionDialog;
    private boolean mIsBillingSupported;
    private boolean mIsKeyboardDisplayed;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mobile_player);

        mAimpPlayer = AimpPlayerInstance.get();
        mOnGlobalLayoutListener = new OnGlobalLayoutListenerImpl();
        mBillingObserver = new AbstractBillingObserverImpl(this);
        mMenuListener = new PlayerMenuChooseImpl();

        mTimerLastClick = System.currentTimeMillis();
        tVolumeUpdater = new VolumeUpdater();

        mIsKeyboardDisplayed = false;

        mConnectionListener = new ConnectionListenerImpl();
        mStateObserver = new StateObserverImpl();

        initViewHolder();
        initUi();
        initUiEvents();

        mAimpPlayer.registerConnectionListener(mConnectionListener);
        mAimpPlayer.registerStateObserver(mStateObserver);
    }

    @Override
    protected void onStart() {
        super.onStart();

        // init billing
        mIsBillingSupported = false;
        BillingController.registerObserver(mBillingObserver);
        BillingController.checkBillingSupported(this);
        BillingController.checkSubscriptionSupported(this);

        // init bugsense
        BugSenseHandler.initAndStartSession(getApplicationContext(), Bugsense.API_KEY);
    }

    @Override
    protected void onResume() {
        super.onResume();
        mViewHolder.activityRootView.getViewTreeObserver().addOnGlobalLayoutListener(mOnGlobalLayoutListener);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mViewHolder.activityRootView.getViewTreeObserver().removeGlobalOnLayoutListener(mOnGlobalLayoutListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        BillingController.unregisterObserver(mBillingObserver);
        BugSenseHandler.closeSession(getApplicationContext());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAimpPlayer.unregisterConnectionListener(mConnectionListener);
        mAimpPlayer.unregisterStateObserver(mStateObserver);
        mAimpPlayer = null;
    }

    // ============================================================
    // =========== INIT
    // ============================================================

    protected void initViewHolder() {
        mViewHolder = new ViewHolder();

        mViewHolder.activityRootView  = findViewById(R.id.player_wrapper);
        mViewHolder.connectionWrapper = (ViewGroup) findViewById(R.id.connection_wrapper);
        mViewHolder.plsWrapper        = (ViewGroup) findViewById(R.id.pls_wrapper);
        mViewHolder.controllerWrapper = (ViewGroup) findViewById(R.id.controller_wrapper);
        mViewHolder.panel             = (ViewGroup) findViewById(R.id.panel);
        mViewHolder.initPls           = (ViewGroup) findViewById(R.id.init_pls);

        mViewHolder.timer                    = (TextView) findViewById(R.id.timer);
        mViewHolder.playControllerIcon       = (ImageView) findViewById(R.id.play_controller_icon);
        mViewHolder.commonControllerIcon     = (ImageView) findViewById(R.id.common_controller_icon);
        mViewHolder.playControllerSwitcher   = findViewById(R.id.play_controller_switcher);
        mViewHolder.commonControllerSwitcher = findViewById(R.id.common_controller_switcher);

        mViewHolder.actionBar = getSupportActionBar();
        mViewHolder.connectionIcon = (ImageView) findViewById(R.id.connection);
    }

    protected void initUiEvents() {
        mViewHolder.connectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectionClicked();
            }
        });
        mViewHolder.playControllerSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onPlayControllerSwitcherClicked();
            }
        });
        mViewHolder.commonControllerSwitcher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCommonControllerSwitcherClicked();
            }
        });
        mViewHolder.timer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onTimerClicked();
            }
        });
    }


    // ============================================================
    // =========== UI EVENTS
    // ============================================================

    protected void onConnectionClicked() {
        doConnect();
    }

    protected void onPlayControllerSwitcherClicked() {
        if (fPlayControllerFragment == null) {
            if (fCommonControllerFragment != null) {
                deactivateCommonController();
            }
            activatePlayController();
        } else {
            deactivatePlayController();
        }
    }

    protected void onCommonControllerSwitcherClicked() {
        if (fCommonControllerFragment == null) {
            if (fPlayControllerFragment != null) {
                deactivatePlayController();
            }
            activateCommonController();
        } else {
            deactivateCommonController();
        }
    }

    protected void onTimerClicked() {
        if (System.currentTimeMillis() - mTimerLastClick < DOUBLE_CLICK_DELAY && mAimpPlayer.isConnected()) {
            scrollToCurrent();
        }
        mTimerLastClick = System.currentTimeMillis();
    }


    // ============================================================
    // =========== INIT UI
    // ============================================================

    protected void initUi() {
        if (mAimpPlayer.isConnected()) {
            initUiWhenConnected();
        } else {
            initUiWhenDisconnected();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getSupportMenuInflater();
        inflater.inflate(R.menu.action_bar, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.others:
                showPopupDialog();
                return true;
            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    protected void initUiWhenConnected() {
        mViewHolder.panel.setVisibility(View.VISIBLE);
        mViewHolder.connectionWrapper.setVisibility(View.GONE);
        mViewHolder.connectionIcon.setVisibility(View.GONE);
        activatePlaylistsList();
    }

    protected void initUiWhenDisconnected() {
        mViewHolder.panel.setVisibility(View.GONE);
        mViewHolder.connectionWrapper.setVisibility(View.VISIBLE);
        mViewHolder.connectionIcon.setVisibility(View.VISIBLE);
        if (fPlayControllerFragment != null) {
            deactivatePlayController();
        }
        if (fCommonControllerFragment != null) {
            deactivateCommonController();
        }
        if (fPlaylistsList != null) {
            deactivatePlaylistsList();
        }
    }


    // ============================================================
    // =========== CONTROLLER's SWITCH LOGIC
    // ============================================================

    protected void activatePlayController() {
        fPlayControllerFragment = new PlayControllerFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(mViewHolder.controllerWrapper.getId(), fPlayControllerFragment);
        ft.commitAllowingStateLoss();
        mViewHolder.playControllerIcon.setImageResource(R.drawable.btn_play_controller_active);
    }

    protected void deactivatePlayController() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fPlayControllerFragment);
        ft.commitAllowingStateLoss();
        fPlayControllerFragment = null;
        mViewHolder.playControllerIcon.setImageResource(R.drawable.btn_play_controller_default);
    }

    protected void activateCommonController() {
        fCommonControllerFragment = new CommonControllerFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(mViewHolder.controllerWrapper.getId(), fCommonControllerFragment);
        ft.commitAllowingStateLoss();
        mViewHolder.commonControllerIcon.setImageResource(R.drawable.btn_common_controller_active);
    }

    protected void deactivateCommonController() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fCommonControllerFragment);
        ft.commitAllowingStateLoss();
        fCommonControllerFragment = null;
        mViewHolder.commonControllerIcon.setImageResource(R.drawable.btn_common_controller_default);
    }

    protected void activatePlaylistsList() {
        mViewHolder.initPls.setVisibility(View.VISIBLE);

        fPlaylistsList = new PlaylistsListFragment();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(mViewHolder.plsWrapper.getId(), fPlaylistsList);
        ft.commitAllowingStateLoss();

        fPlaylistsList.setOnInitCompleteListener(new PlaylistsListFragment.OnInitCompletedListener() {
            @Override
            public void onInitCompleted() {
                fPlaylistsList.setOnInitCompleteListener(null);
                mViewHolder.initPls.setVisibility(View.GONE);
            }
        });
    }

    protected void deactivatePlaylistsList() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.remove(fPlaylistsList);
        ft.commitAllowingStateLoss();
        fPlaylistsList = null;
    }

    public boolean isControllerVisible() {
        return fPlayControllerFragment != null || fCommonControllerFragment != null;
    }


    // ============================================================
    // =========== CONNECTION PROCESSOR
    // ============================================================

    protected void doConnect() {
        try {
            mAimpPlayer.connect(
                    getApplicationContext(),
                    new WebCtlPlugin(Settings.getHost(), Settings.getPort()),
                    Settings.buildSyncParams()
            );
        } catch (AimpException ex) {
            // already connecting, ignore the exception
            //throw new RuntimeException(ex);
        }
    }

    protected void doDisconnect() {
        try {
            mAimpPlayer.disconnect();
        } catch (AimpException ex) {
            // already connecting, ignore the exception
            //throw new RuntimeException(ex);
        }
    }

    protected Dialog buildConnectionDialog() {
        ProgressDialog pd = new ProgressDialog(this);
        pd.setTitle(R.string.connecting);
        pd.setMessage(getString(R.string.connecting));
        pd.setProgressStyle(ProgressDialog.STYLE_SPINNER);
        pd.setIndeterminate(true);
        pd.setCancelable(true);
        pd.setOnCancelListener(new Dialog.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                doDisconnect();
            }
        });
        return pd;
    }


    // ============================================================
    // =========== UI UPDATES & COMMANDS
    // ============================================================

    protected void setTimerValue(Song song, int playPosition) {
        if (song == null) {
            mViewHolder.timer.setText("");
        } else {
            int songDuration = song.getDuration();
            String value = getString(
                    R.string.timer_format,
                    playPosition / 60, playPosition % 60,
                    songDuration / 60, songDuration % 60
            );
            mViewHolder.timer.setText(value);
        }
    }

    protected void setElementVisibilityForSearch(boolean isKeyboardVisible) {
        if (isKeyboardVisible) {
            if (fPlayControllerFragment != null) {
                deactivatePlayController();
            }
            if (fCommonControllerFragment != null) {
                deactivateCommonController();
            }
        }
        mViewHolder.panel.setVisibility(isKeyboardVisible ? View.GONE : View.VISIBLE);
    }

    protected void scrollToCurrent() {
        Playlist currPl = mAimpPlayer.getCurrentPlaylist();
        Song currSong = mAimpPlayer.getCurrentSong();
        if (currSong != null) {
            fPlaylistsList.scrollTo(currPl, currSong);
        }
    }


    // ============================================================
    // =========== STATE OBSERVER
    // ============================================================

    class StateObserverImpl extends StateObserverViaHandler {
        @Override
        public void onSongPlayPositionChanged(Playlist playlist, Song song, int position, double percentage) {
            setTimerValue(song, position);
        }
        @Override
        public void onSongChanged(Playlist playlist, Song song, int position, double percentage) {
            if (isControllerVisible()) {
                scrollToCurrent();
            }
        }
    }


    // ============================================================
    // =========== CONNECTION LISTENER
    // ============================================================

    class ConnectionListenerImpl extends ConnectionListenerViaHandler {
        @Override
        public void onConnectionStatusChanged(IPlugin plugin, AimpPlayer.ConnectionStatus status) {
            switch (status) {
                case CONNECTING:
                    mViewHolder.connectionIcon.setVisibility(View.GONE);
                    mConnectionDialog = buildConnectionDialog();
                    mConnectionDialog.show();
                    break;
                case CONNECTED:
                    closeConnectionDialog();
                    initUiWhenConnected();
                    break;
                case DISCONNECTED:
                    initUiWhenDisconnected();
                    break;
                default:
                    break;
            }
        }

        @Override
        public void onAimpNotFound(IPlugin plugin) {
            closeConnectionDialog();
            createConnectionErrorDialog(
                    R.string.error,
                    R.string.error_aimp_not_found
            ).show();
        }

        @Override
        public void onHostNotFound(IPlugin plugin) {
            closeConnectionDialog();
            createConnectionErrorDialog(
                    R.string.error,
                    R.string.error_host_not_found
            ).show();
        }

        @Override
        public void onUnresolvedError(IPlugin plugin, Exception ex) {
            closeConnectionDialog();
            UiUtils.sDisplayHelp(
                    MainActivity.this,
                    R.string.error,
                    R.string.error_unknown
            );
        }

        protected void closeConnectionDialog() {
            mViewHolder.connectionIcon.setVisibility(View.VISIBLE);
            mConnectionDialog.dismiss();
            mConnectionDialog = null;
        }

    }


    // ============================================================
    // =========== KEYBOARD APPEAR LISTENER
    // ============================================================

    class OnGlobalLayoutListenerImpl implements ViewTreeObserver.OnGlobalLayoutListener {
        @Override
        public void onGlobalLayout() {
            int heightDiff = mViewHolder.activityRootView.getRootView().getHeight() - mViewHolder.activityRootView.getHeight();
            if ((heightDiff > getMinKeyboardHeight()) != mIsKeyboardDisplayed) {
                mIsKeyboardDisplayed = heightDiff > getMinKeyboardHeight();
                setElementVisibilityForSearch(mIsKeyboardDisplayed);
            }
        }
    }

    protected int getMinKeyboardHeight() {
        return (int) TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                MIN_KEYBOARD_HEIGHT_IN_DP,
                getResources().getDisplayMetrics()
        );
    }


    // ============================================================
    // =========== BILLING
    // ============================================================

    class AbstractBillingObserverImpl extends AbstractBillingObserver {
        public AbstractBillingObserverImpl(Activity activity) {
            super(activity);
        }

        public void onBillingChecked(boolean supported) {
            mIsBillingSupported = supported;
            if (supported) {
                restoreTransactions();
            }
        }

        public void onPurchaseStateChanged(String itemId, Transaction.PurchaseState state) {

        }

        public void onRequestPurchaseResponse(String itemId, BillingRequest.ResponseCode response) {

        }

        public void onSubscriptionChecked(boolean supported) {

        }
    }

    /**
     * Restores previous transactions, if any. This happens if the application
     * has just been installed or the user wiped data. We do not want to do this
     * on every startup, rather, we want to do only when the database needs to
     * be initialized.
     */
    protected void restoreTransactions() {
        if (!mBillingObserver.isTransactionsRestored()) {
            BillingController.restoreTransactions(this);
        }
    }

    protected Dialog createBillingNotSupportedDialog(int titleId, int messageId) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(titleId)
                .setIcon(android.R.drawable.stat_sys_warning)
                .setMessage(messageId)
                .setCancelable(false)
                .setPositiveButton(android.R.string.ok, null)
        ;
        return builder.create();
    }


    // ===================================================
    // =========== KEY EVENT HANDLERS
    // ===================================================

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                if (mAimpPlayer.isConnected()) {
                    int direction = (event.getKeyCode() == KeyEvent.KEYCODE_VOLUME_UP)
                            ? VolumeUpdater.DIRECTION_UP
                            : VolumeUpdater.DIRECTION_DOWN;
                    if (event.getAction() == KeyEvent.ACTION_DOWN) {
                        tVolumeUpdater.start(mAimpPlayer, direction);
                    } else {
                        tVolumeUpdater.stop(mAimpPlayer);
                    }
                }
                return true;
            default:
                return super.dispatchKeyEvent(event);
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            onMenuKeyPressed();
            return true;
        } else {
            return super.onKeyDown(keyCode, event);
        }
    }


    // ===================================================
    // ===================== MENU
    // ===================================================

    protected void onMenuKeyPressed() {
        showPopupDialog();
    }

    protected void showPopupDialog() {
        if (mMenuDialog != null) {
            mMenuDialog.dismiss();
        } else {
            int[] items = new int[] {
                    mAimpPlayer.isConnected()
                            ? PlayerMenu.TYPE_DISCONNECT
                            : PlayerMenu.TYPE_CONNECT,
                    PlayerMenu.TYPE_INFO,
                    PlayerMenu.TYPE_PREFERENCES,
                    PlayerMenu.TYPE_DONATE,
                    PlayerMenu.TYPE_EXIT
            };
            mMenuDialog = PlayerMenu.createDialog(this, items, mMenuListener);
            mMenuDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    mMenuDialog = null;
                }
            });
            mMenuDialog.show();
        }
    }

    class PlayerMenuChooseImpl implements PlayerMenu.OnChooseListener {
        @Override
        public void onChoose(int type) {
            switch (type) {
                case PlayerMenu.TYPE_CONNECT:
                    doConnect();
                    break;
                case PlayerMenu.TYPE_DISCONNECT:
                    doDisconnect();
                    break;
                case PlayerMenu.TYPE_INFO:
                    displayInfoList();
                    break;
                case PlayerMenu.TYPE_DONATE:
                    if (!mIsBillingSupported) {
                        createBillingNotSupportedDialog(R.string.billing_not_supported_title, R.string.billing_not_supported_message).show();
                    } else {
                        BillingController.requestPurchase(MainActivity.this, Billing.DONATE_1_99, true /* confirm */, null);
                    }
                    break;
                case PlayerMenu.TYPE_PREFERENCES:
                    startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                    break;
                case PlayerMenu.TYPE_EXIT:
                    if (mAimpPlayer.isConnected()) {
                        doDisconnect();
                    }
                    finish();
                    break;
                default:
                    break;
            }
            mMenuDialog = null;
        }
    };

    protected void displayInfoList() {
        CharSequence[] items = new CharSequence[4];
        items[0] = getResources().getString(R.string.menu_info_reference);
        items[1] = getResources().getString(R.string.menu_info_old_versions);
        items[2] = getResources().getString(R.string.menu_info_github);
        items[3] = getResources().getString(R.string.menu_info_about);

        mMenuDialog = new AlertDialog.Builder(this)
                .setTitle(R.string.menu_info)
                .setItems(items, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int item) {
                        if (item == 0) {
                            UiUtils.sDisplayHelp(MainActivity.this, R.string.info_reference_title, R.string.info_reference_content);
                        }
                        if (item == 1) {
                            showOldVersionsDialog();
                        }
                        if (item == 2) {
                            showGitHubDialog();
                        }
                        if (item == 3) {
                            UiUtils.sDisplayHelp(MainActivity.this, R.string.info_about_title, R.string.info_about_content);
                        }
                    }
                })
                .setNeutralButton(R.string.close, null)
                .create()
        ;
        mMenuDialog.show();
    }

    protected void showOldVersionsDialog() {
        Dialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.info_old_versions_title)
                .setMessage(R.string.info_old_versions_message)
                .setPositiveButton(R.string.info_old_versions_goto_catalog_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.info_old_versions_goto_catalog_message))
                        );
                        startActivity(i);
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .create();
        d.show();
    }

    protected void showGitHubDialog() {
        Dialog d = new AlertDialog.Builder(this)
                .setTitle(R.string.menu_info_github_title)
                .setMessage(R.string.menu_info_github_message)
                .setPositiveButton(R.string.menu_info_github_goto_catalog_title, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent i = new Intent(
                                Intent.ACTION_VIEW,
                                Uri.parse(getString(R.string.menu_info_github_goto_catalog_message))
                        );
                        startActivity(i);
                    }
                })
                .setNeutralButton(R.string.cancel, null)
                .create();
        d.show();
    }


    protected Dialog createConnectionErrorDialog(int titleResId, int messageResId) {
        AlertDialog alertDialog = new AlertDialog.Builder(this)
                .setTitle(titleResId)
                .setMessage(messageResId)
                .setPositiveButton(
                        R.string.goto_settings,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                            }
                        }
                )
                .setNegativeButton(
                        R.string.close,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }
                )
                .create()
        ;
        return alertDialog;
    }

}