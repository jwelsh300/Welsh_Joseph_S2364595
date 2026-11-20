package com.example.welsh_joseph_s2364595.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.welsh_joseph_s2364595.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;

import com.example.welsh_joseph_s2364595.model.LocationPoint;
import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import java.util.List;

public class SituationMapFragment extends Fragment implements OnMapReadyCallback {
    private static final LatLng SCOTLAND = new LatLng(56.8, -4.2);
    private static final float SCOTLAND_ZOOM = 6f;
    private static final float SINGLE_RESULT_ZOOM = 12f;
    private static final int FIT_PADDING_PX = 100;
    private GoogleMap map;
    private SituationViewModel viewModel;
    private boolean firstStart;
    private String lastFilter = null;

    public SituationMapFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_situation_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        firstStart = (savedInstanceState == null);

        SupportMapFragment mapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.mapView);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);   // Google calls onMapReady when it's ready
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        if (getView() == null) {
            return;
        }
        map = googleMap;
        map.getUiSettings().setZoomControlsEnabled(true);
        map.getUiSettings().setMapToolbarEnabled(false);

        if (firstStart) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCOTLAND, SCOTLAND_ZOOM));
        }

        viewModel = new ViewModelProvider(requireActivity()).get(SituationViewModel.class);
        viewModel.getFiltered().observe(getViewLifecycleOwner(), this::drawSituations);
    }

    private void drawSituations(List<Situation> list) {
        if (map == null || list == null) {
            return;
        }
        map.clear();

        LatLngBounds.Builder bounds = new LatLngBounds.Builder();
        int shown = 0;
        LatLng onlyPoint = null;

        for (Situation s : list) {
            LocationPoint from = s.getFrom();
            LocationPoint to = s.getTo();

            LocationPoint pin;
            if (from.hasCoordinates()) {
                pin = from;
            } else if (to.hasCoordinates()) {
                pin = to;
            } else {
                continue;
            }

            String title = getString(SituationStyle.labelFor(s));
            if (!s.getRoadName().isEmpty()) {
                title = title + " · " + s.getRoadName();
            }

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(toLatLng(pin))
                    .title(title));
            if (marker != null) {
                marker.setTag(s);
            }

            LatLng pinPosition = toLatLng(pin);
            bounds.include(pinPosition);
            shown++;
            onlyPoint = pinPosition;

            if (from.hasCoordinates() && to.hasCoordinates()
                    && (from.getLatitude() != to.getLatitude()
                    || from.getLongitude() != to.getLongitude())) {
                map.addPolyline(new PolylineOptions().add(toLatLng(from), toLatLng(to)));
                bounds.include(toLatLng(from));
                bounds.include(toLatLng(to));
            }
        }
        followResults(shown, onlyPoint, bounds);
    }

    private static LatLng toLatLng(LocationPoint p) {
        return new LatLng(p.getLatitude(), p.getLongitude());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        map = null;
    }

    private void followResults(int shown, LatLng onlyPoint, LatLngBounds.Builder bounds) {
        String filter = FilterText.describe(requireContext(),
                viewModel.getQuery().getValue(), viewModel.getNearby().getValue());

        boolean changed;
        if (lastFilter == null) {
            changed = firstStart;
        } else {
            changed = !lastFilter.equals(filter);
        }
        lastFilter = filter;

        if (!changed || filter.isEmpty() || shown == 0) {
            return;
        }

        if (shown == 1) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(onlyPoint, SINGLE_RESULT_ZOOM));
            return;
        }

        LatLngBounds area = bounds.build();
        View v = getView();
        if (v == null) {
            return;
        }
        v.post(() -> {
            if (map == null || v.getWidth() == 0 || v.getHeight() == 0) {
                return;
            }
            map.animateCamera(CameraUpdateFactory.newLatLngBounds(
                    area, v.getWidth(), v.getHeight(), FIT_PADDING_PX));
        });
    }
}