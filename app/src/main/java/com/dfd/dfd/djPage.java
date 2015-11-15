package com.dfd.dfd;

import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import android.media.MediaPlayer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class djPage extends AppCompatActivity {
    final String TAG= "djPage";

    private ScheduledFuture<?> mMoverFuture;
    private static final int REFRESH_RATE = 10000;
    private Random rand= new Random();
    private int songNum= 0;

    private String[] streamURLs= {"http://listen.radionomy.com/smoothjazz247",
    "http://listen.radionomy.com/acoustic-fm"};
    private MediaPlayer mPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dj_page);

        mPlayer= new MediaPlayer();
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Creates a WorkerThread
        ScheduledExecutorService executor = Executors
                .newScheduledThreadPool(1);

        // Execute the run() in Worker Thread every REFRESH_RATE
        // milliseconds
        // Save reference to this job in mMoverFuture
        mMoverFuture = executor.scheduleWithFixedDelay(new Runnable() {
            @Override
            public void run() {
//					Log.i(TAG, "run called");
                // TODO - implement movement logic.
                // Each time this method is run the BubbleView should
                // move one step. If the BubbleView exits the display,
                // stop the BubbleView's Worker Thread.
                // Otherwise, request that the BubbleView be redrawn.

                try {
                    songNum= 1-songNum;
                    Log.i(TAG, "changing song to " + streamURLs[songNum]);
                    if (mPlayer.isPlaying()) mPlayer.reset();
                    mPlayer.setDataSource(streamURLs[songNum]);
                    mPlayer.prepare();
                    mPlayer.start();

//                    mPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
//                        @Override
//                        public void onPrepared(MediaPlayer mp) {
//                            mp.start();
//                        }
//                    });

                } catch(IOException e) {
                    Log.i(TAG, "ERROR: " + e);
                }

            }
        }, 0, REFRESH_RATE, TimeUnit.MILLISECONDS);
    }

    @Override
    protected void onPause() {
        super.onPause();
        
        mMoverFuture.cancel(true);
        mPlayer.stop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dj_page, menu);
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

//public class loadSong extends AsyncTask<String, void, void> {
//    @Override
//    protected void doInBackground(String... params) {
//        try {
//            Log.i(TAG, "Going to sleep");
//            Thread.sleep(10000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
//}
