package com.example.welsh_joseph_s2364595.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;

public class SituationListFragment extends Fragment {

    private SituationAdapter adapter;
    private SituationViewModel viewModel;

    public SituationListFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_situation_list, container, false);

        RecyclerView recycler = v.findViewById(R.id.situationRecycler);

        LinearLayoutManager llm = new LinearLayoutManager(requireContext());
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        recycler.setLayoutManager(llm);

        adapter = new SituationAdapter();
        recycler.setAdapter(adapter);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SituationViewModel.class);

        viewModel.getSituations().observe(getViewLifecycleOwner(), list -> {
            if (list == null) {
                return;
            }
            adapter.setSituations(list);
        });
    }
}