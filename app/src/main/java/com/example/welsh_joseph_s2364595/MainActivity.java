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

import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.view.SituationDetailFragment;
import com.example.welsh_joseph_s2364595.view.SituationListFragment;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel.LoadStatus;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Date;

public class MainActivity extends AppCompatActivity
        implements SituationListFragment.SituationListListener {

    private TextView summaryText;
    private SituationViewModel viewModel;
    private ProgressBar loadingBar;

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

        summaryText = findViewById(R.id.summaryText);
        loadingBar = findViewById(R.id.loadingBar);

        viewModel = new ViewModelProvider(this).get(SituationViewModel.class);

        viewModel.getFiltered().observe(this, list -> updateSummary());
        viewModel.getLastUpdated().observe(this, time -> updateSummary());

        viewModel.getLoading().observe(this, isLoading ->
                loadingBar.setVisibility(Boolean.TRUE.equals(isLoading) ? View.VISIBLE : View.GONE));

        viewModel.getStatus().observe(this, this::showStatus);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.listHolder, new SituationListFragment())
                    .commit();
        }

        getSupportFragmentManager().addOnBackStackChangedListener(this::updateUpArrow);
        updateUpArrow();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);

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
                return true;
            }
        });
        return true;
    }

    private boolean isDetailShowing() {
        return getSupportFragmentManager().getBackStackEntryCount() > 0;
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

    private void updateSummary() {
        List<Situation> filteredList = viewModel.getFiltered().getValue();
        List<Situation> all = viewModel.getSituations().getValue();
        if (filteredList == null || all == null) {
            return;   // nothing downloaded yet
        }

        String q = viewModel.getQuery().getValue();
        q = (q == null) ? "" : q.trim();

        String text;
        if (q.isEmpty()) {
            int incidents = 0;
            for (Situation s : all) {
                if (s.getCategory().equals("Incident")) {
                    incidents++;
                }
            }
            int roadworks = all.size() - incidents;
            text = getString(R.string.summary_all, incidents, roadworks, all.size());
        } else {
            text = getString(R.string.summary_filtered, filteredList.size(), all.size(), q);
        }

        Long time = viewModel.getLastUpdated().getValue();
        if (time != null) {
            text = text + " · " + getString(R.string.summary_updated, formatTime(time));
        }

        summaryText.setText(text);
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
                summaryText.setText(R.string.summary_no_data);
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

    @Override
    public void onSituationSelected() {
        Situation s = viewModel.getSelected().getValue();
        if (s != null) {
            Log.d("MainActivity", "Selected: " + s.getId() + " · " + s.getRoadName());
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.listHolder, new SituationDetailFragment())
                .addToBackStack(null)
                .commit();
    }
}