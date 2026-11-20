package com.example.welsh_joseph_s2364595.view;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.annotation.ColorRes;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.ColorUtils;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.model.LocationPoint;
import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;

import java.util.List;
import java.util.HashMap;
import java.util.Map;

public class SituationMapFragment extends Fragment implements OnMapReadyCallback {

    private static final LatLng SCOTLAND = new LatLng(56.8, -4.2);
    private static final float SCOTLAND_ZOOM = 6f;
    private static final float SINGLE_RESULT_ZOOM = 12f;
    private static final int FIT_PADDING_PX = 100;
    private static final String KEY_CAMERA = "camera";
    private static final int MARKER_SIZE_DP = 36;
    private final Map<String, BitmapDescriptor> iconCache = new HashMap<>();

    private GoogleMap map;
    private SituationViewModel viewModel;
    private SituationListFragment.SituationListListener listener;
    private boolean firstStart;
    private String lastFilter = null;
    private CameraPosition savedCamera;

    public SituationMapFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SituationListFragment.SituationListListener) {
            listener = (SituationListFragment.SituationListListener) context;
        } else {
            throw new RuntimeException(context + " must implement SituationListListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            savedCamera = readCamera(savedInstanceState);
        }
    }

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

        if (savedCamera != null) {
            map.moveCamera(CameraUpdateFactory.newCameraPosition(savedCamera));
        } else if (firstStart) {
            map.moveCamera(CameraUpdateFactory.newLatLngZoom(SCOTLAND, SCOTLAND_ZOOM));
        }

        viewModel = new ViewModelProvider(requireActivity()).get(SituationViewModel.class);
        viewModel.getFiltered().observe(getViewLifecycleOwner(), this::drawSituations);

        map.setOnInfoWindowClickListener(marker -> {
            Object tag = marker.getTag();
            if (tag instanceof Situation && listener != null) {
                viewModel.select((Situation) tag);
                listener.onSituationSelected();
            }
        });
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        CameraPosition camera = (map != null) ? map.getCameraPosition() : savedCamera;
        if (camera != null) {
            outState.putParcelable(KEY_CAMERA, camera);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (map != null) {
            savedCamera = map.getCameraPosition();
        }
        map = null;
        iconCache.clear();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
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
            LatLng pinPosition = toLatLng(pin);

            String title = getString(SituationStyle.labelFor(s));
            if (!s.getRoadName().isEmpty()) {
                title = title + " · " + s.getRoadName();
            }

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(pinPosition)
                    .icon(markerIconFor(s))
                    .anchor(0.5f, 0.5f)
                    .title(title)
                    .snippet(getString(R.string.map_tap_for_details)));
            if (marker != null) {
                marker.setTag(s);
            }

            bounds.include(pinPosition);
            shown++;
            onlyPoint = pinPosition;

            if (from.hasCoordinates() && to.hasCoordinates()
                    && (from.getLatitude() != to.getLatitude()
                    || from.getLongitude() != to.getLongitude())) {
                map.addPolyline(new PolylineOptions()
                        .add(toLatLng(from), toLatLng(to))
                        .color(ContextCompat.getColor(requireContext(), fillColourFor(s)))
                        .width(8f));
                bounds.include(toLatLng(from));
                bounds.include(toLatLng(to));
            }
        }
        followResults(shown, onlyPoint, bounds);
    }

    private void followResults(int shown, LatLng onlyPoint, LatLngBounds.Builder bounds) {
        String filter = FilterText.describe(requireContext(),
                viewModel.getQuery().getValue(), viewModel.getNearby().getValue());

        boolean changed;
        if (lastFilter == null) {
            changed = firstStart;   // new screen: fit any active filter; after rotation: keep camera
        } else {
            changed = !lastFilter.equals(filter);
        }
        lastFilter = filter;

        if (!changed || filter.isEmpty() || shown == 0) {
            return;   // no change / no filter / nothing to show → leave the camera alone
        }

        if (shown == 1) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(onlyPoint, SINGLE_RESULT_ZOOM));
            return;
        }

        // newLatLngBounds needs the map's size: wait until the view has been laid out
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

    private static LatLng toLatLng(LocationPoint p) {
        return new LatLng(p.getLatitude(), p.getLongitude());
    }

    @ColorRes
    private static int fillColourFor(Situation s) {
        int colour = SituationStyle.delayColourFor(s);
        return (colour == android.R.color.transparent) ? R.color.marker_incident : colour;
    }

    // Same icon as the list, on a filled circle in the delay colour, cached per combination
    private BitmapDescriptor markerIconFor(Situation s) {
        int iconRes = SituationStyle.iconFor(s);
        int colourRes = fillColourFor(s);

        String key = iconRes + ":" + colourRes;
        BitmapDescriptor cached = iconCache.get(key);
        if (cached != null) {
            return cached;
        }

        Context context = requireContext();
        float density = getResources().getDisplayMetrics().density;
        int size = Math.round(MARKER_SIZE_DP * density);   // dp → pixels for this screen

        Bitmap bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        float centre = size / 2f;

        // White ring first, then the coloured circle slightly smaller: visible on any map background
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        canvas.drawCircle(centre, centre, centre, paint);

        int fill = ContextCompat.getColor(context, colourRes);
        paint.setColor(fill);
        canvas.drawCircle(centre, centre, centre - 2 * density, paint);

        Drawable icon = ContextCompat.getDrawable(context, iconRes);
        if (icon != null) {
            icon = icon.mutate();   // own copy: tinting must not affect the list's icons
            // Dark icon on light fills (e.g. yellow), white icon on dark fills
            boolean lightFill = ColorUtils.calculateLuminance(fill) > 0.5;
            icon.setTint(lightFill ? Color.BLACK : Color.WHITE);
            int inset = Math.round(size * 0.22f);
            icon.setBounds(inset, inset, size - inset, size - inset);
            icon.draw(canvas);
        }

        BitmapDescriptor descriptor = BitmapDescriptorFactory.fromBitmap(bitmap);
        iconCache.put(key, descriptor);
        return descriptor;
    }

    // getParcelable(key, Class) only exists from Android 13 and up so there's a guard for it
    @SuppressWarnings("deprecation")
    private static CameraPosition readCamera(Bundle state) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return state.getParcelable(KEY_CAMERA, CameraPosition.class);
        }
        return state.getParcelable(KEY_CAMERA);
    }
}