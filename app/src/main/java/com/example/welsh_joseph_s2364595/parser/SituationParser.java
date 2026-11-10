package com.example.welsh_joseph_s2364595.parser;

import android.util.Log;

import com.example.welsh_joseph_s2364595.model.LocationPoint;
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
        boolean insideComment = false;
        boolean insideFrom = false;
        boolean insideTo = false;
        String lastValue = "";

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

                        String id = xpp.getAttributeValue(null, "id");
                        if (id == null) {
                            id = "";
                        }
                        current.setId(id);

                        String recordType = xpp.getAttributeValue(XSI_NS, "type");
                        if (recordType == null) {
                            recordType = "";
                        }
                        current.setRecordType(recordType);
                    }
                    else if (current != null && tag.equals("overallStartTime")) {
                        current.setStartTime(xpp.nextText());
                    }
                    else if (current != null && tag.equals("overallEndTime")) {
                        current.setEndTime(xpp.nextText());
                    }
                    else if (current != null && tag.equals("tpegDirection")) {
                        current.setDirection(xpp.nextText());
                    }
                    else if (current != null && tag.equals("urgentRoadworks")) {
                        current.setUrgent(Boolean.parseBoolean(xpp.nextText()));
                    }
                    else if (current != null && ( tag.equals("abnormalTrafficType")  ||  tag.equals("accidentType")  ||  tag.equals("roadOrCarriagewayOrLaneManagementType")  ||  tag.equals("roadMaintenanceType"))) {
                        current.setTypeDetail(xpp.nextText());
                    }
                    else if (current != null && ( tag.equals("generalPublicComment"))) {
                        insideComment = true;
                    }
                    else if (current != null && insideComment && tag.equals("value")) {
                        current.setComment(xpp.nextText());
                    }
                    else if (current != null && tag.equals("from")) {
                        insideFrom = true;
                    }
                    else if (current != null && tag.equals("to")) {
                        insideTo = true;
                    }
                    else if (current != null && tag.equals("latitude")) {
                        double lat = toDouble(xpp.nextText());

                        if (insideFrom) {
                            current.getFrom().setLatitude(lat);
                        }
                        else if (insideTo) {
                            current.getTo().setLatitude(lat);
                        }
                    }
                    else if (current != null && tag.equals("longitude")) {
                        double lon = toDouble(xpp.nextText());

                        if (insideFrom) {
                            current.getFrom().setLongitude(lon);
                        }
                        else if (insideTo) {
                            current.getTo().setLongitude(lon);
                        }
                    }
                    else if (current != null && (insideFrom || insideTo) && tag.equals("value")) {
                        lastValue = xpp.nextText();
                    }
                    else if (current != null && (insideFrom || insideTo)
                            && (tag.equals("tpegIlcPointDescriptorType")
                            || tag.equals("tpegJunctionPointDescriptorType")
                            || tag.equals("tpegOtherPointDescriptorType"))) {

                        String label = xpp.nextText();

                        LocationPoint point;
                        if (insideFrom) {
                            point = current.getFrom();
                        }
                        else {
                            point = current.getTo();
                        }

                        if (label.equals("tpegIlcName1") || label.equals("linkName")) {
                            point.setRoadName(lastValue);
                        }
                        else if (label.equals("tpegIlcName2")) {
                            point.setSecondRoadName(lastValue);
                        }
                        else if (label.equals("junctionName")) {
                            point.setJunctionName(lastValue);
                        }
                        else if (label.equals("intersectionName") || label.equals("nonLinkedPointName")) {
                            point.setPlaceName(lastValue);
                        }

                        lastValue = "";
                    }
                    else if (current != null && tag.equals("delaysType")) {
                        current.getImpact().setDelayType(xpp.nextText());
                    }
                    else if (current != null && tag.equals("numberOfLanesRestricted")) {
                        current.getImpact().setLanesRestricted(toInt(xpp.nextText()));
                    }
                    else if (current != null && tag.equals("numberOfOperationalLanes")) {
                        current.getImpact().setOperationalLanes(toInt(xpp.nextText()));
                    }

                } else if (eventType == XmlPullParser.END_TAG) {
                    String tag = xpp.getName();

                    if (tag.equals("situationRecord") && current != null) {
                        situations.add(current);
                        Log.d("SituationParser", "Added: " + current.toString());
                        current = null;
                    }
                    if ( tag.equals("generalPublicComment")) {
                        insideComment = false;
                    }
                    if ( tag.equals("from")) {
                        insideFrom = false;
                    }
                    if ( tag.equals("to")) {
                        insideTo = false;
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

    // Returns 0.0 if the text isn't a number (hasCoordinates() treats 0.0 as "no location")
    private double toDouble(String text) {
        try {
            return Double.parseDouble(text.trim());
        } catch (NumberFormatException e) {
            Log.w("SituationParser", "Bad number '" + text + "', using 0.0");
            return 0.0;
        }
    }

    // Returns -1 if the text isn't a whole number (-1 means "not given")
    private int toInt(String text) {
        try {
            return Integer.parseInt(text.trim());
        } catch (NumberFormatException e) {
            Log.w("SituationParser", "Bad number '" + text + "', using -1");
            return -1;
        }
    }
}