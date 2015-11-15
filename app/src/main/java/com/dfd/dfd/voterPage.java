package com.dfd.dfd;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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

import org.w3c.dom.Text;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class voterPage extends AppCompatActivity {
    String TAG= "voterPage";

    BandInfo[] pairedBands;
    BandClient bandClient;
    TextView mVoterScore;
    String mGenre= "http://listen.radionomy.com/smoothjazz247";
    int mUsername=1;


    BandHeartRateEventListener heartRateListener;
    int mHeartRate;

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

//        EditText mUsernameBox= (EditText) findViewById(R.id.username);
//        mUsername= mUsernameBox.getText().toString();
//        mUsernameBox.setOnFocusChangeListener(new View.OnFocusChangeListener() {
//            @Override
//            public void onFocusChange(View v, boolean hasFocus) {
//                EditText userNameBox= (EditText) v;
//                mUsername= userNameBox.getText().toString();
//            }
//        });

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

    public void onRadioButtonClicked(View view) {
        // Is the button now checked?
        boolean checked = ((RadioButton) view).isChecked();

        // Check which radio button was clicked
        switch(view.getId()) {
            case R.id.smoothJazz:
                if (checked) {
                    mGenre= "http://listen.radionomy.com/smoothjazz247";
                }
                break;
            case R.id.acousticGuitar:
                if (checked) {
                    mGenre = "http://listen.radionomy.com/acoustic-fm";
                }
                break;
            case R.id.hitRadio:
                if (checked) {
                    mGenre = "http://listen.radionomy.com/100-hit-radio";
                }
                break;

            case R.id.user1:
                if (checked) {
                    mUsername= 1;
                }
                break;
            case R.id.user2:
                if (checked) {
                    mUsername= 2;
                }
                break;

        }
    }

    public HeartRateConsentListener mHeartRateConsentListener= new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {

        }
    };

    public void updateWebsite(int name, int heartRate, String genre) {
        HttpURLConnection urlConnection= null;
        try {
            Log.i(TAG, "URL: "+"https://moloso.herokuapp" +
                    ".com/stats/update?id="+name+"&heartrate="+heartRate+"&genre="+genre);
            URL url = new URL("https://moloso.herokuapp" +
                ".com/stats/update?id="+name+"&heartrate="+heartRate+"&genre="+genre);
            urlConnection = (HttpURLConnection) url.openConnection();
            Log.i(TAG, "opened connection");
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-type", "application/JSON");
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // OK
                Log.i(TAG, "success");
                // otherwise, if any other status code is returned, or no status
                // code is returned, do stuff in the else block
            } else {
                // Server returned HTTP error code.
                Log.i(TAG, ""+urlConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.i(TAG, ""+e);
        } finally{
            if (urlConnection!=null) urlConnection.disconnect();
        }
    }

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
                        mHeartRate= event.getHeartRate();
                        Log.i(TAG, "HR: "+mHeartRate);
                        Log.i(TAG, "TV: " + mVoterScore.getText());

                        updateWebsite(mUsername, mHeartRate,
                                mGenre);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                mVoterScore.setText("" + mHeartRate);
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
