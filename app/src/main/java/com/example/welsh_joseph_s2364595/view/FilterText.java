package com.example.welsh_joseph_s2364595.view;

import android.content.Context;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel.NearbySetting;

public final class FilterText {
    private FilterText() {}

    public static String describe(Context context, String query, NearbySetting near) {
        String q = (query == null) ? "" : query.trim();

        String road = q.isEmpty()
                ? ""
                : context.getString(R.string.filter_road, q);

        String nearby = (near == null)
                ? ""
                : context.getString(R.string.filter_nearby, near.radiusMiles, near.placeName);

        if (road.isEmpty()) {
            return nearby;
        }
        if (nearby.isEmpty()) {
            return road;
        }
        return road + " " + nearby;
    }
}
