package com.example.welsh_joseph_s2364595.data;

import android.util.Log;

import com.example.welsh_joseph_s2364595.BuildConfig;
import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.parser.SituationParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class SituationRepository {

    private static final String TAG = "SituationRepository";
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 15000;

    private final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    private final String roadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    private final String clientId = BuildConfig.TS_CLIENT_ID;
    private final String clientKey = BuildConfig.TS_CLIENT_KEY;

    public List<Situation> fetchAll() {
        String unplannedXml = downloadXml(unplannedEventsURL);
        String roadworksXml = downloadXml(roadworksURL);

        if (unplannedXml.isEmpty() && roadworksXml.isEmpty()) {
            Log.e(TAG, "Both downloads failed");
            return null;
        }

        SituationParser parser = new SituationParser();

        ArrayList<Situation> incidents = new ArrayList<>();
        if (!unplannedXml.isEmpty()) {
            incidents = parser.parse(unplannedXml, "Incident");
        } else {
            Log.e(TAG, "Unplanned events download failed");
        }

        ArrayList<Situation> roadworks = new ArrayList<>();
        if (!roadworksXml.isEmpty()) {
            roadworks = parser.parse(roadworksXml, "Roadwork");
        } else {
            Log.e(TAG, "Roadworks download failed");
        }

        List<Situation> allSituations = new ArrayList<>();
        allSituations.addAll(incidents);
        allSituations.addAll(roadworks);

        Log.d(TAG, "Incidents: " + incidents.size()
                + ", Roadworks: " + roadworks.size()
                + ", Total: " + allSituations.size());

        return allSituations;
    }

    private String downloadXml(String address) {
        StringBuilder result = new StringBuilder();
        BufferedReader in;
        String inputLine;

        try {
            URL url = new URL(address);
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestProperty("Authorization",
                    Base64.getEncoder().encodeToString((clientId + ":" + clientKey).getBytes()));
            urlConnection.setConnectTimeout(CONNECT_TIMEOUT_MS);
            urlConnection.setReadTimeout(READ_TIMEOUT_MS);
            try {
                in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
                while ((inputLine = in.readLine()) != null) {
                    result.append(inputLine).append('\n');
                }
                in.close();
            } catch (IOException e) {
                Log.e(TAG, "ioexception reading " + address);
            }
            finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e(TAG, "ioexception opening " + address);
        }

        String text = result.toString();
        int i = text.indexOf("<?");
        if (i < 0) {
            return "";
        }
        return text.substring(i);
    }
}