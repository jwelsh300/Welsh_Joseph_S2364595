package com.example.welsh_joseph_s2364595.view;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;


public class SituationListFragment extends Fragment {

    public interface SituationListListener {
        void onSituationSelected();
    }

    private SituationAdapter adapter;
    private SituationViewModel viewModel;
    private SituationListListener listener;
    private RecyclerView recycler;
    private TextView emptyText;
    private String lastFilter = null;

    public SituationListFragment() {}

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof SituationListListener) {
            listener = (SituationListListener) context;
        } else {
            throw new RuntimeException(context + " must implement SituationListListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        listener = null;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_situation_list, container, false);

        recycler = v.findViewById(R.id.situationRecycler);
        emptyText = v.findViewById(R.id.emptyText);

        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(llm);

        adapter = new SituationAdapter(s -> {
            viewModel.select(s);
            listener.onSituationSelected();
        });

        recycler.setAdapter(adapter);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SituationViewModel.class);

        viewModel.getFiltered().observe(getViewLifecycleOwner(), list -> {
            if (list == null) {
                return;
            }
            List<Situation> sorted = new ArrayList<>(list);
            Comparator<Situation> bySeverity = Comparator.comparingInt(SituationStyle::rankFor);
            sorted.sort(bySeverity.thenComparing(Situation::getRoadName));

            adapter.setSituations(sorted);

            String filter = currentFilter();
            showEmptyState(sorted.isEmpty(), filter);
            
            if (lastFilter != null && !lastFilter.equals(filter)) {
                recycler.scrollToPosition(0);
            }
            lastFilter = filter;
        });
    }

    private void showEmptyState(boolean empty, String filter) {
        if (!empty) {
            emptyText.setVisibility(View.GONE);
        } else if (!filter.isEmpty()) {
            emptyText.setText(getString(R.string.empty_no_results, filter));
            emptyText.setVisibility(View.VISIBLE);
        } else {
            emptyText.setText(R.string.empty_no_data);
            emptyText.setVisibility(View.VISIBLE);
        }
    }

    private String currentFilter() {
        return FilterText.describe(requireContext(),
                viewModel.getQuery().getValue(), viewModel.getNearby().getValue());
    }
}