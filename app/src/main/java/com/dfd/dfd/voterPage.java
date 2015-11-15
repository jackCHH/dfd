package com.dfd.dfd;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import java.io.IOException;

public class voterPage extends AppCompatActivity {
    String TAG= "voterPage";

    private String streamURL= "http://listen.radionomy.com/smoothjazz247";
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voter_page);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

//        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
//        fab.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
//                        .setAction("Action", null).show();
//            }
//        });

        mPlayer= new MediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        try {
            mPlayer.setDataSource(streamURL);
            mPlayer.prepareAsync();

            mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mp.start();
                }
            });

        } catch(IOException e) {
            Log.i(TAG, "ERROR: "+e);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        mPlayer.stop();
    }

}
