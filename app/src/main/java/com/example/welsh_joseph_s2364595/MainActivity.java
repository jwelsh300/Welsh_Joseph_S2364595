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
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.view.SituationListFragment;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;

public class MainActivity extends AppCompatActivity implements OnClickListener {
    private TextView summaryText;
    private Button startButton;
    private SituationViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        // Set up the raw links to the graphical components
        summaryText = (TextView)findViewById(R.id.summaryText);
        startButton = (Button)findViewById(R.id.startButton);
        startButton.setOnClickListener(this);

        viewModel = new ViewModelProvider(this).get(SituationViewModel.class);

        // Draw whatever list arrives (always runs on the main thread)
        viewModel.getSituations().observe(this, list -> {
            if (list == null) {
                return;
            }
            int incidents = 0;
            for (Situation s : list) {
                if (s.getCategory().equals("Incident")) {
                    incidents++;
                }
            }
            int roadworks = list.size() - incidents;
            summaryText.setText("Incidents: " + incidents
                    + ", Roadworks: " + roadworks
                    + ", Total: " + list.size());
        });
        // Put the list Fragment in the placeholder, only on first launch
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.listHolder, new SituationListFragment())
                    .commit();
        }
    }

    public void onClick(View aview)
    {
        viewModel.refresh();
    }
}