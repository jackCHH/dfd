package com.dfd.dfd;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class djPage extends AppCompatActivity {
    final String TAG= "djPage";

    private ScheduledFuture<?> mMoverFuture;
    private static final int REFRESH_RATE = 10000;
    private Random rand= new Random();
    private int songNum= 0;
    private String mGenre;

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
//                    songNum= 1-songNum;
                    mGenre= getTop();
                    Log.i(TAG, "changing song to " + mGenre);
                    if (mPlayer.isPlaying()) mPlayer.reset();
                    mPlayer.setDataSource(mGenre);
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

    public String getTop() {
        HttpURLConnection urlConnection= null;
        try {
            Log.i(TAG, "URL: "+"https://moloso.herokuapp" +
                    ".com/stats/");
            URL url = new URL("https://moloso.herokuapp" +
                    ".com/stats.json");
            urlConnection = (HttpURLConnection) url.openConnection();
            Log.i(TAG, "opened connection");
            urlConnection.setRequestMethod("GET");
            urlConnection.setRequestProperty("Content-type", "application/JSON");
            if (urlConnection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                // OK
                Log.i(TAG, "success");
                BufferedReader rd= new BufferedReader(new InputStreamReader(urlConnection
                        .getInputStream()));
                StringBuilder sb= new StringBuilder();
                String line= null;
                while((line= rd.readLine()) != null) {
                    sb.append(line+'\n');
                }
                Log.i(TAG, sb.toString());
                return highestHeartGenre(new JSONArray(sb.toString()));
            } else {
                // Server returned HTTP error code.
                Log.i(TAG, ""+urlConnection.getResponseCode());
            }
        } catch (Exception e) {
            Log.i(TAG, ""+e);
        } finally{
            if (urlConnection!=null) urlConnection.disconnect();
        }
        return "";
    }

    public String highestHeartGenre(JSONArray json) {
//        Log.i(TAG, "JSON: "+json.get(0).getClass());
        int max=0;
        String maxGenre= "";
        for (int i=0; i<json.length(); i++) {
            try {
                JSONObject thisHeartRate= (JSONObject) json.get(i);
                if ((int) thisHeartRate.get("heartrate") > max) {
                    max= (int) thisHeartRate.get("heartrate");
                    maxGenre= (String) thisHeartRate.get("genre");
                }
            } catch (JSONException e) {
                Log.i(TAG, "ERROR: "+e);
            }
        }
        return maxGenre;
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
