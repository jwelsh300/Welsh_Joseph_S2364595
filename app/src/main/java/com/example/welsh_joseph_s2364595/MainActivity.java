/*  Starter project for Mobile Platform Development - 1st diet 26/27
    You should use this project as the starting point for your assignment.
*/

//
// Name                 Joseph Welsh
// Student ID           S2364595
// Programme of Study   Computing Science
//

package com.example.welsh_joseph_s2364595;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.view.Menu;
import android.view.MenuItem;
import android.text.InputType;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.ProgressBar;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.view.SituationDetailFragment;
import com.example.welsh_joseph_s2364595.view.SituationListFragment;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel.LoadStatus;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel.NearbySetting;
import com.example.welsh_joseph_s2364595.view.FilterText;
import com.example.welsh_joseph_s2364595.view.SituationMapFragment;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements SituationListFragment.SituationListListener {

    private TextView summaryText;
    private SituationViewModel viewModel;
    private ProgressBar loadingBar;
    private MenuItem toggleItem;

    private static final String PREFS_NAME = "settings";
    private static final String KEY_REFRESH_MINUTES = "refresh_minutes";
    private static final int DEFAULT_REFRESH_MINUTES = 60;
    private static final int DEFAULT_RADIUS_MILES = 15;

    private View detailPlaceholder;   // landscape only

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        setTitle(R.string.title_main);


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        summaryText = findViewById(R.id.summaryText);             // portrait only (null in landscape)
        loadingBar = findViewById(R.id.loadingBar);
        detailPlaceholder = findViewById(R.id.detailPlaceholder); // landscape only (null in portrait)

        // Landscape has no summary TextView: put the starting text in the subtitle instead
        showSummary(getString(R.string.summary_loading));

        viewModel = new ViewModelProvider(this).get(SituationViewModel.class);

        viewModel.setAutoRefreshMinutes(loadRefreshMinutes());

        viewModel.getFiltered().observe(this, list -> updateSummary());
        viewModel.getLastUpdated().observe(this, time -> updateSummary());

        viewModel.getLoading().observe(this, isLoading ->
                loadingBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getStatus().observe(this, this::showStatus);

        viewModel.getSelected().observe(this, s -> {
            if (detailPlaceholder != null) {
                detailPlaceholder.setVisibility(s != null ? View.GONE : View.VISIBLE);
            }
        });

        if (savedInstanceState == null) {
            showListOrMap();
        } else {
            arrangeFragmentsAfterRotation();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this::onBackStackChanged);
        updateUpArrow();
        updateSummaryVisibility();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

        toggleItem = menu.findItem(R.id.action_toggle_view);
        updateToggleItem();

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setQueryHint(getString(R.string.search_hint));
        searchView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS);

        String saved = viewModel.getQuery().getValue();
        saved = (saved == null) ? "" : saved.trim();
        if (!saved.isEmpty()) {
            searchItem.expandActionView();
            searchView.setQuery(saved, false);
            searchView.clearFocus();
        }

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String text) {
                viewModel.setQuery(text);
                searchView.clearFocus();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String text) {
                if (!text.isEmpty() && isDetailShowing()) {
                    getSupportFragmentManager().popBackStack();
                }
                viewModel.setQuery(text);
                return true;
            }
        });

        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                if (isDetailShowing()) {
                    getSupportFragmentManager().popBackStack();
                }
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                viewModel.setQuery("");
                invalidateOptionsMenu(); // fix for toolbar buttons vanishing after collapsing search
                return true;
            }
        });
        return true;
    }

    private boolean isDetailShowing() {
        return getSupportFragmentManager().getBackStackEntryCount() > 0;
    }

    // two pane for landscape
    private boolean isTwoPane() {
        return findViewById(R.id.detailHolder) != null;
    }

    private void arrangeFragmentsAfterRotation() {
        FragmentManager fm = getSupportFragmentManager();
        boolean somethingSelected = viewModel.getSelected().getValue() != null;

        if (isTwoPane()) {
            if (fm.getBackStackEntryCount() > 0) {
                fm.popBackStackImmediate();
            }
            if (somethingSelected && fm.findFragmentById(R.id.detailHolder) == null) {
                fm.beginTransaction()
                        .replace(R.id.detailHolder, new SituationDetailFragment())
                        .commit();
            }
        } else {
            Fragment orphan = fm.findFragmentById(R.id.detailHolder);
            if (orphan != null) {
                fm.beginTransaction().remove(orphan).commitNow();

                if (somethingSelected) {
                    fm.beginTransaction()
                            .replace(R.id.listHolder, new SituationDetailFragment())
                            .addToBackStack(null)
                            .commit();
                }
            }
        }
    }

    // to prevent bug with rotating twice turning on list into on detail
    private void onBackStackChanged() {
        updateUpArrow();
        updateSummaryVisibility();

        if (!isTwoPane() && getSupportFragmentManager().getBackStackEntryCount() == 0) {
            viewModel.select(null);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_refresh) {
            viewModel.refresh();
            return true;
        }
        if (item.getItemId() == R.id.action_key) {
            new AlertDialog.Builder(this)
                    .setTitle(R.string.key_title)
                    .setView(R.layout.dialog_key)
                    .setPositiveButton(android.R.string.ok, null)
                    .show();
            return true;
        }
        if (item.getItemId() == R.id.action_interval) {
            showIntervalDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_nearby) {
            showNearbyDialog();
            return true;
        }
        if (item.getItemId() == R.id.action_toggle_view) {
            toggleListOrMap();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        getSupportFragmentManager().popBackStack();
        return true;
    }

    private void updateUpArrow() {
        ActionBar bar = getSupportActionBar();
        if (bar != null) {
            bar.setDisplayHomeAsUpEnabled(
                    getSupportFragmentManager().getBackStackEntryCount() > 0);
        }
    }

    // to hide portrait list summary when detail screen is open
    private void updateSummaryVisibility() {
        if (summaryText != null) {
            summaryText.setVisibility(
                    getSupportFragmentManager().getBackStackEntryCount() > 0 ? View.GONE : View.VISIBLE);
        }
    }

    private void updateSummary() {
        List<Situation> filteredList = viewModel.getFiltered().getValue();
        List<Situation> all = viewModel.getSituations().getValue();
        if (filteredList == null || all == null) {
            return;
        }

        String filter = FilterText.describe(this,
                viewModel.getQuery().getValue(), viewModel.getNearby().getValue());

        String text;
        if (filter.isEmpty()) {
            int incidents = 0;
            for (Situation s : all) {
                if (s.getCategory().equals("Incident")) {
                    incidents++;
                }
            }
            int roadworks = all.size() - incidents;
            text = getString(R.string.summary_all, incidents, roadworks, all.size());
        } else {
            text = getString(R.string.summary_filtered, filteredList.size(), all.size(), filter);
        }

        Long time = viewModel.getLastUpdated().getValue();
        if (time != null) {
            text = text + " · " + getString(R.string.summary_updated, formatTime(time))
                    + " · " + getString(R.string.summary_interval, viewModel.getAutoRefreshMinutes());
        }

        showSummary(text);
    }

    private void showSummary(CharSequence text) {
        if (summaryText != null) {
            summaryText.setText(text);
        } else {
            ActionBar bar = getSupportActionBar();
            if (bar != null) {
                bar.setSubtitle(text);
            }
        }
    }

    private void showStatus(LoadStatus status) {
        if (status == null || status == LoadStatus.OK) {
            return;
        }

        String text;
        int duration;
        switch (status) {
            case FAILED_NO_DATA:
                text = getString(R.string.msg_no_data);
                duration = Snackbar.LENGTH_INDEFINITE;
                showSummary(getString(R.string.summary_no_data));
                break;
            case FAILED_OLD_DATA:
                Long time = viewModel.getLastUpdated().getValue();
                text = getString(R.string.msg_old_data, time == null ? "?" : formatTime(time));
                duration = Snackbar.LENGTH_LONG;
                break;
            case PARTIAL_INCIDENTS_FAILED:
                text = getString(R.string.msg_incidents_failed);
                duration = Snackbar.LENGTH_LONG;
                break;
            case PARTIAL_ROADWORKS_FAILED:
                text = getString(R.string.msg_roadworks_failed);
                duration = Snackbar.LENGTH_LONG;
                break;
            default:
                return;
        }

        Snackbar.make(findViewById(R.id.main), text, duration)
                .setAction(R.string.action_retry, v -> viewModel.refresh())
                .show();

        if (status != LoadStatus.FAILED_NO_DATA) {
            viewModel.statusShown();
        }
    }

    private String formatTime(long millis) {
        return DateFormat.getTimeFormat(this).format(new Date(millis));
    }

    private void showIntervalDialog() {
        String[] labels = getResources().getStringArray(R.array.interval_labels);
        int[] minutes = getResources().getIntArray(R.array.interval_minutes);

        // Tick the option matching the current setting (-1 = none, if it somehow isn't listed)
        int saved = loadRefreshMinutes();
        int checked = -1;
        for (int i = 0; i < minutes.length; i++) {
            if (minutes[i] == saved) {
                checked = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(R.string.interval_title)
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    int chosen = minutes[which];
                    saveRefreshMinutes(chosen);
                    viewModel.setAutoRefreshMinutes(chosen);
                    updateSummary();            // show the new interval straight away
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void showNearbyDialog() {
        String[] names = getResources().getStringArray(R.array.place_names);

        // Tick the current place, if a filter is on (-1 = nothing ticked)
        NearbySetting current = viewModel.getNearby().getValue();
        int checked = -1;
        if (current != null) {
            for (int i = 0; i < names.length; i++) {
                if (names[i].equals(current.placeName)) {
                    checked = i;
                    break;
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(R.string.nearby_place_title)
                .setSingleChoiceItems(names, checked, (dialog, which) -> {
                    dialog.dismiss();
                    showRadiusDialog(which);
                })
                .setNegativeButton(android.R.string.cancel, null);

        if (current != null) {
            builder.setNeutralButton(R.string.nearby_turn_off,
                    (dialog, which) -> viewModel.clearNearby());
        }

        builder.show();
    }

    private void showRadiusDialog(int placeIndex) {
        String[] names = getResources().getStringArray(R.array.place_names);
        String[] lats = getResources().getStringArray(R.array.place_lats);
        String[] lons = getResources().getStringArray(R.array.place_lons);
        String[] labels = getResources().getStringArray(R.array.radius_labels);
        int[] radii = getResources().getIntArray(R.array.radius_miles);

        String name = names[placeIndex];
        double lat = Double.parseDouble(lats[placeIndex]);
        double lon = Double.parseDouble(lons[placeIndex]);

        NearbySetting current = viewModel.getNearby().getValue();
        int wanted = (current != null) ? current.radiusMiles : DEFAULT_RADIUS_MILES;
        int checked = -1;
        for (int i = 0; i < radii.length; i++) {
            if (radii[i] == wanted) {
                checked = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.nearby_radius_title, name))
                .setSingleChoiceItems(labels, checked, (dialog, which) -> {
                    if (isDetailShowing()) {
                        getSupportFragmentManager().popBackStack();
                    }
                    viewModel.setNearby(name, lat, lon, radii[which]);
                    dialog.dismiss();
                })
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private int loadRefreshMinutes() {
        return getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .getInt(KEY_REFRESH_MINUTES, DEFAULT_REFRESH_MINUTES);
    }

    private void saveRefreshMinutes(int minutes) {
        getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                .edit()
                .putInt(KEY_REFRESH_MINUTES, minutes)
                .apply();   // saves in the background; commit() would block the main thread
    }

    @Override
    public void onSituationSelected() {
        Situation s = viewModel.getSelected().getValue();
        if (s != null) {
            Log.d("MainActivity", "Selected: " + s.getId() + " · " + s.getRoadName());
        }

        if (isTwoPane()) {
            if (getSupportFragmentManager().findFragmentById(R.id.detailHolder) == null) {
                getSupportFragmentManager()
                        .beginTransaction()
                        .replace(R.id.detailHolder, new SituationDetailFragment())
                        .commit();
            }
        } else {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.listHolder, new SituationDetailFragment())
                    .addToBackStack(null)
                    .commit();
        }
    }

    private void toggleListOrMap() {
        if (isDetailShowing()) {
            getSupportFragmentManager().popBackStackImmediate();
        }
        viewModel.setMapShown(!viewModel.isMapShown());
        showListOrMap();
        updateToggleItem();
    }

    private void showListOrMap() {
        Fragment screen = viewModel.isMapShown()
                ? new SituationMapFragment()
                : new SituationListFragment();
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.listHolder, screen)
                .commit();
    }

    private void updateToggleItem() {
        if (toggleItem == null) {
            return;
        }
        boolean map = viewModel.isMapShown();
        toggleItem.setIcon(map ? R.drawable.ic_list : R.drawable.ic_map);
        toggleItem.setTitle(map ? R.string.action_show_list : R.string.action_show_map);
    }
}