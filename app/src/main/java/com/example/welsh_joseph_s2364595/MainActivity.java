/*  Starter project for Mobile Platform Development - 1st diet 26/27
    You should use this project as the starting point for your assignment.
    This project simply reads the data from the required source and displays the
    raw data in a TextField
*/

//
// Name                 Joseph Welsh
// Student ID           S2364595
// Programme of Study   Computing Science
//

package com.example.welsh_joseph_s2364595;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.parser.SituationParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;


public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView rawDataDisplay;
    private Button startButton;

    //DOWNLOADING FROM TRAFFIC SCOTLAND
    final String clientId = BuildConfig.TS_CLIENT_ID;
    final String clientKey = BuildConfig.TS_CLIENT_KEY;
    final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    final String roadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Set up the raw links to the graphical components
        rawDataDisplay = (TextView)findViewById(R.id.rawDataDisplay);
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        // More Code goes here

    }

    public void onClick(View aview)
    {
        startProgress();
    }

    public void startProgress()
    {
        new Thread(new Task()).start();
    } //

    // Need separate thread to access the internet resource over network
    // Other neater solutions should be adopted in later iterations. - hopefully done that here
    private class Task implements Runnable
    {
        @Override
        public void run() {
            Log.d("MyTask", "in run");

            String unplannedXml = downloadXml(unplannedEventsURL);
            String roadworksXml = downloadXml(roadworksURL);

            SituationParser parser = new SituationParser();

            ArrayList<Situation> incidents = new ArrayList<>();
            if (!unplannedXml.isEmpty()) {
                incidents = parser.parse(unplannedXml, "Incident");
            } else {
                Log.e("MyTask", "Unplanned events download failed");
            }

            ArrayList<Situation> roadworks = new ArrayList<>();
            if (!roadworksXml.isEmpty()) {
                roadworks = parser.parse(roadworksXml, "Roadwork");
            } else {
                Log.e("MyTask", "Roadworks download failed");
            }

            ArrayList<Situation> allSituations = new ArrayList<>();
            allSituations.addAll(incidents);
            allSituations.addAll(roadworks);

            Log.d("MyTask", "Incidents: " + incidents.size()
                    + ", Roadworks: " + roadworks.size()
                    + ", Total: " + allSituations.size());

            final String summary = "Incidents: " + incidents.size()
                    + ", Roadworks: " + roadworks.size();

            MainActivity.this.runOnUiThread(new Runnable()
            {
                public void run() {
                    Log.d("UI thread", "I am the UI thread");
                    rawDataDisplay.setText(summary);
                }
            });
        }

        // Downloads one feed and returns its XML, or "" if nothing usable came back
        private String downloadXml(String address) {
            StringBuilder result = new StringBuilder();
            BufferedReader in;
            String inputLine;

            try {
                URL url = new URL(address);
                HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                //ADD AUTHORISATION (string with clientId:clientKey)
                urlConnection.setRequestProperty("Authorization",
                        Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));
                try {
                    in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                    while ((inputLine = in.readLine()) != null) {
                        result.append(inputLine).append('\n');
                    }
                    in.close();
                } catch (IOException e) {
                    Log.e("urlConnection", "ioexception reading " + address);
                }
                finally {
                    urlConnection.disconnect();
                }
            } catch (IOException e) {
                Log.e("URL", "ioexception opening " + address);
            }

            // Clean up any leading garbage characters, safely
            String text = result.toString();
            int i = text.indexOf("<?");
            if (i < 0) {
                return "";
            }
            return text.substring(i);
        }
    }
}