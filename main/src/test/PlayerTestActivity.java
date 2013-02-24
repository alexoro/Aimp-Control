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

package test;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import com.uas.media.aimp.AimpPlayerInstance;
import com.uas.media.aimp.R;
import com.uas.media.aimp.api.IPlugin;
import com.uas.media.aimp.api.impl.WebCtlPlugin;
import com.uas.media.aimp.api.models.Playlist;
import com.uas.media.aimp.api.models.Song;
import com.uas.media.aimp.player.*;

/**
 * User: uas.sorokin@gmail.com
 */
public class PlayerTestActivity extends Activity {

    private Button vExecute;
    private Button vCancel;
    private TextView vProgress;
    private TextView vResult;

    private AimpPlayer mAimpPlayer;

    private static final String TAG = "T1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.api_test);

        vExecute = (Button) findViewById(R.id.execute);
        vCancel = (Button) findViewById(R.id.cancel);
        vProgress = (TextView) findViewById(R.id.progress);
        vResult = (TextView) findViewById(R.id.result);

        vExecute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                execWrapper();
            }
        });
        vCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cancel();
            }
        });

        mAimpPlayer = AimpPlayerInstance.get();

        mAimpPlayer.registerConnectionListener(mConnectionListener);
        mAimpPlayer.registerStateObserver(new StateObserver() {
            @Override
            public void onSongPlayPositionChanged(Playlist playlist, Song song, int position, double percentage) {
                Log.d(TAG, String.valueOf(position));
            }
        });
    }


    private void setResult(String result) {
        vResult.setText(result);
    }

    private void setProgress(String progress) {
        vProgress.setText(progress);
    }


    private void cancel() {
        try {
            mAimpPlayer.disconnect();
        } catch (AimpException e) {
            Log.d(TAG, e.getMessage(), e);
        }
    }

    private void execWrapper() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    exec();
                } catch (final Exception e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            vResult.setText(e.toString());
                        }
                    });
                }
            }
        }).start();
    }

    private void exec() throws Exception {
        IPlugin ir = new WebCtlPlugin("192.168.0.200", 38475);
        mAimpPlayer.connect(this, ir, AimpPlayer.defaultSyncParams());

        int stop = 0; stop = 1;
    }


    private ConnectionListener mConnectionListener = new ConnectionListenerViaHandler() {
        @Override
        public void onConnectionStatusChanged(IPlugin plugin, AimpPlayer.ConnectionStatus status) {
            Log.d(TAG, status.toString());
            super.onConnectionStatusChanged(plugin, status);
        }

        @Override
        public void onHostNotFound(IPlugin plugin) {
            super.onHostNotFound(plugin);
        }

        @Override
        public void onAimpNotFound(IPlugin plugin) {
            super.onAimpNotFound(plugin);
        }

        @Override
        public void onUnresolvedError(IPlugin plugin, Exception ex) {
            super.onUnresolvedError(plugin, ex);
        }
    };

}