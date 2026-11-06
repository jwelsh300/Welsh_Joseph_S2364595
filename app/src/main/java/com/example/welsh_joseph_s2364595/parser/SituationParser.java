package com.example.welsh_joseph_s2364595.parser;

import android.util.Log;

import com.example.welsh_joseph_s2364595.model.Situation;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;

public class SituationParser {

    private static final String XSI_NS = "http://www.w3.org/2001/XMLSchema-instance";

    public ArrayList<Situation> parse(String xml, String category) {
        ArrayList<Situation> situations = new ArrayList<>();
        Situation current = null;

        try {
            XmlPullParserFactory factory = XmlPullParserFactory.newInstance();
            factory.setNamespaceAware(true);
            XmlPullParser xpp = factory.newPullParser();
            xpp.setInput(new StringReader(xml));

            int eventType = xpp.getEventType();
            while (eventType != XmlPullParser.END_DOCUMENT) {

                if (eventType == XmlPullParser.START_TAG) {
                    String tag = xpp.getName();

                    if (tag.equals("situationRecord")) {
                        current = new Situation();
                        current.setCategory(category);
                        current.setId(xpp.getAttributeValue(null, "id"));
                        current.setRecordType(xpp.getAttributeValue(XSI_NS, "type"));
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    String tag = xpp.getName();

                    if (tag.equals("situationRecord") && current != null) {
                        situations.add(current);
                        Log.d("SituationParser", "Added: " + current.getId());
                        current = null;
                    }
                }

                eventType = xpp.next();
            }
        } catch (XmlPullParserException e) {
            Log.e("SituationParser", "Parsing error: " + e);
        } catch (IOException e) {
            Log.e("SituationParser", "IO error: " + e);
        }

        return situations;
    }
}