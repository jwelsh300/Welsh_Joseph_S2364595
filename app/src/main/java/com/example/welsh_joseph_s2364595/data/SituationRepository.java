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

    public static class FetchResult {
        public final List<Situation> incidents;
        public final List<Situation> roadworks;

        FetchResult(List<Situation> incidents, List<Situation> roadworks) {
            this.incidents = incidents;
            this.roadworks = roadworks;
        }
    }

    private static final String TAG = "SituationRepository";
    private static final int CONNECT_TIMEOUT_MS = 10000;
    private static final int READ_TIMEOUT_MS = 15000;

    private final String unplannedEventsURL = "https://datex2.trafficscotland.org/rest/2.3/publications/UnplannedEvents/Content.xml";
    private final String roadworksURL = "https://datex2.trafficscotland.org/rest/2.3/publications/CurrentRoadworks/Content.xml";

    private final String clientId = BuildConfig.TS_CLIENT_ID;
    private final String clientKey = BuildConfig.TS_CLIENT_KEY;

    public FetchResult fetchAll() {
        SituationParser parser = new SituationParser();

        List<Situation> incidents = null;
        String unplannedXml = downloadXml(unplannedEventsURL);
        if (!unplannedXml.isEmpty()) {
            incidents = parser.parse(unplannedXml, "Incident");
        } else {
            Log.e(TAG, "Unplanned events download failed");
        }

        List<Situation> roadworks = null;
        String roadworksXml = downloadXml(roadworksURL);
        if (!roadworksXml.isEmpty()) {
            roadworks = parser.parse(roadworksXml, "Roadwork");
        } else {
            Log.e(TAG, "Roadworks download failed");
        }

        Log.d(TAG, "Incidents: " + (incidents == null ? "FAILED" : incidents.size())
                + ", Roadworks: " + (roadworks == null ? "FAILED" : roadworks.size()));

        return new FetchResult(incidents, roadworks);
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
                Log.e(TAG, "ioexception reading " + address, e);
            }
            finally {
                urlConnection.disconnect();
            }
        } catch (IOException e) {
            Log.e(TAG, "ioexception opening " + address, e);
        }

        String text = result.toString();
        int i = text.indexOf("<?");
        if (i < 0) {
            return "";
        }
        return text.substring(i);
    }
}