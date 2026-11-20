package com.example.welsh_joseph_s2364595.view;

import android.util.Log;
import android.content.Context;

import androidx.annotation.ColorRes;
import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.model.Situation;

public final class SituationStyle {

    private static final String TAG = "SituationStyle";

    private static final int[] ICONS = {
            R.drawable.ic_road_closed,
            R.drawable.ic_accident,
            R.drawable.ic_traffic,
            R.drawable.ic_obstruction,
            R.drawable.ic_lane_closure,
            R.drawable.ic_roadworks,
            R.drawable.ic_info
    };

    private static final int[] LABELS = {
            R.string.type_road_closed,
            R.string.type_accident,
            R.string.type_abnormal_traffic,
            R.string.type_obstruction,
            R.string.type_lanes_restricted,
            R.string.type_roadworks,
            R.string.type_other
    };

    private SituationStyle() {}

    @DrawableRes
    public static int iconFor(Situation s) {
        return ICONS[typeIndex(s)];
    }

    @StringRes
    public static int labelFor(Situation s) {
        return LABELS[typeIndex(s)];
    }

    // "Roadworks · A84", "Road closed · M8": the same title in list, detail and map
    public static String titleFor(Context context, Situation s) {
        String title = context.getString(labelFor(s));
        if (!s.getRoadName().isEmpty()) {
            title = title + " · " + s.getRoadName();
        }
        return title;
    }

    @ColorRes
    public static int delayColourFor(Situation s) {
        if (!"Roadwork".equals(s.getCategory())) {
            return android.R.color.transparent;
        }

        String delay = s.getImpact().getDelayType();

        if ("veryLongDelays".equals(delay)) {
            return R.color.delay_vlong;
        } else if ("longDelays".equals(delay)) {
            return R.color.delay_long;
        } else if ("delays".equals(delay)) {
            return R.color.delay_short;
        } else {
            return R.color.delay_na;
        }
    }

    public static int rankFor(Situation s) {
        if (isClosed(s)) {
            return 0;
        }

        if (s.getCategory().equals("Incident")) {
            return 1;
        }

        String delay = s.getImpact().getDelayType();

        if ("veryLongDelays".equals(delay)) {
            return 2;
        } else if ("longDelays".equals(delay)) {
            return 3;
        } else if ("delays".equals(delay)) {
            return 4;
        } else {
            return 5;
        }
    }

    private static int typeIndex(Situation s) {

        if (isClosed(s)) {
            return 0;
        }

        String recordType = s.getRecordType();

        if ("Accident".equals(recordType)) {
            return 1;
        }

        if ("AbnormalTraffic".equals(recordType)) {
            return 2;
        }

        if ("GeneralObstruction".equals(recordType)) {
            return 3;
        }

        if ("RoadOrCarriagewayOrLaneManagement".equals(recordType)
                || s.getImpact().getLanesRestricted() > 0) {
            return 4;
        }

        if ("MaintenanceWorks".equals(recordType)) {
            return 5;
        }

        Log.w(TAG, "Unrecognised type: " + s.getCategory() + " | " + recordType
                + " | " + s.getTypeDetail() + " (" + s.getId() + ")");
        return 6;
    }

    private static boolean isClosed(Situation s) {
        return s.getImpact().isRoadClosed() || "roadClosed".equals(s.getTypeDetail());
    }
}