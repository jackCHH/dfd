package com.dfd.dfd;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class voterPage extends AppCompatActivity {
    String TAG= "voterPage";

    BandInfo[] pairedBands;
    BandClient bandClient;
    TextView mVoterScore;
    BandHeartRateEventListener heartRateListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        mVoterScore= (TextView) findViewById(R.id.voterScore);

        pairedBands = BandClientManager.getInstance().getPairedBands();
        // changed getActivity() to this
        bandClient = BandClientManager.getInstance().create(this, pairedBands[0]);

        Log.i(TAG, "hello");
        // check current user heart rate consent
        if(bandClient.getSensorManager().getCurrentHeartRateConsent() !=
                UserConsent.GRANTED) {
            Log.i(TAG, "User did not consent");
            // user hasn’t consented, request consent
            // the calling class is an Activity and implements
            // HeartRateConsentListener
            bandClient.getSensorManager().requestHeartRateConsent(this,
                    mHeartRateConsentListener);
        }
        new Thread(new Runnable() {
            public void run() {
                try {
                    Log.i(TAG, "starting band thing");
                    startBandThing();
                } catch (Error e) {
                    Log.i(TAG, ""+e);
                }
            }
        }).start();

    }

    public HeartRateConsentListener mHeartRateConsentListener= new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {

        }
    };

    public void startBandThing() {
        BandPendingResult<ConnectionState> pendingResult =
                bandClient.connect();
        try {
            ConnectionState state = pendingResult.await();
            if(state == ConnectionState.CONNECTED) {
                try {
                    BandPendingResult<String> pendingVersion = bandClient.getFirmwareVersion();
                    final String fwVersion = pendingVersion.await();
                    pendingVersion = bandClient.getHardwareVersion();
                    final String hwVersion = pendingVersion.await();
                } catch (InterruptedException | BandException ex) {
                    // catch
                }

                heartRateListener = new BandHeartRateEventListener() {
                    @Override
                    public void onBandHeartRateChanged(final BandHeartRateEvent event) {
                        // do work on heart rate changed (i.e., update UI)
                        //send to socket
                        Log.i(TAG, "HR: "+event.getHeartRate());
                        Log.i(TAG, "TV: " + mVoterScore.getText());

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mVoterScore.setText("" + event.getHeartRate());
                            }
                        });
                    }
                };

                try {
                    // register the listener
                    bandClient.getSensorManager().registerHeartRateEventListener(
                            heartRateListener);
                } catch(BandException ex) {
                    // handle BandException
                }
            } else {
                // do work on failure
            }
        } catch(InterruptedException ex) {
            // handle InterruptedException
        } catch(BandException ex) {
            // handle BandException
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            // register the listener
            bandClient.getSensorManager().unregisterHeartRateEventListener(
                    heartRateListener);
        } catch(BandException ex) {
            // handle BandException
        }
    }

}
