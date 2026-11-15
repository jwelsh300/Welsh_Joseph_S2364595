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

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements SituationListFragment.SituationListListener {

    private TextView summaryText;
    private SituationViewModel viewModel;

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

        viewModel = new ViewModelProvider(this).get(SituationViewModel.class);

        viewModel.getFiltered().observe(this, filteredList -> {
            if (filteredList == null) {
                return;
            }
            List<Situation> all = viewModel.getSituations().getValue();
            if (all == null) {
                return;
            }

            String q = viewModel.getQuery().getValue();
            q = (q == null) ? "" : q.trim();

            if (q.isEmpty()) {
                int incidents = 0;
                for (Situation s : all) {
                    if (s.getCategory().equals("Incident")) {
                        incidents++;
                    }
                }
                int roadworks = all.size() - incidents;
                summaryText.setText(getString(R.string.summary_all, incidents, roadworks, all.size()));
            } else {
                summaryText.setText(getString(R.string.summary_filtered,
                        filteredList.size(), all.size(), q));
            }
        });

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
                Log.d("MainActivity", "Search collapsed → clearing query");
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