package com.dfd.dfd;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

//Import for Microsoft Band
import com.microsoft.band.BandClient;
import com.microsoft.band.BandClientManager;
import com.microsoft.band.BandException;
import com.microsoft.band.BandInfo;
import com.microsoft.band.BandIOException;
import com.microsoft.band.ConnectionState;
import com.microsoft.band.BandPendingResult;
import com.microsoft.band.UserConsent;
import com.microsoft.band.sensors.BandHeartRateEvent;
import com.microsoft.band.sensors.BandHeartRateEventListener;
import com.microsoft.band.sensors.HeartRateConsentListener;

public class MainActivity extends AppCompatActivity{

    BandInfo[] pairedBands;
    BandClient bandClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        pairedBands = BandClientManager.getInstance().getPairedBands();
        // changed getActivity() to this
        bandClient = BandClientManager.getInstance().create(this, pairedBands[0]);

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
            } else {
                // do work on failure
            }
        } catch(InterruptedException ex) {
                // handle InterruptedException
        } catch(BandException ex) {
                // handle BandException
        }

        // check current user heart rate consent
        if(bandClient.getSensorManager().getCurrentHeartRateConsent() !=
                UserConsent.GRANTED) {
            // user hasn’t consented, request consent
            // the calling class is an Activity and implements
            // HeartRateConsentListener
            bandClient.getSensorManager().requestHeartRateConsent(this,
                    mHeartRateConsentListener);
        }

    }

    public HeartRateConsentListener mHeartRateConsentListener= new HeartRateConsentListener() {
        @Override
        public void userAccepted(boolean b) {
            BandHeartRateEventListener heartRateListener = new BandHeartRateEventListener() {
                @Override
                public void onBandHeartRateChanged(BandHeartRateEvent event) {
                    // do work on heart rate changed (i.e., update UI)
                    //send to socket
                }
            };

            try {
                // register the listener
                bandClient.getSensorManager().registerHeartRateEventListener(
                        heartRateListener);
            } catch(BandException ex) {
                // handle BandException
            }
        }
    };

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
