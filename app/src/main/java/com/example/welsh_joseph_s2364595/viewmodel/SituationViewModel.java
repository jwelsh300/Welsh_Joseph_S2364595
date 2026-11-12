package com.example.welsh_joseph_s2364595.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.welsh_joseph_s2364595.data.SituationRepository;
import com.example.welsh_joseph_s2364595.model.Situation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SituationViewModel extends ViewModel {

    private final SituationRepository repository = new SituationRepository();
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final MutableLiveData<List<Situation>> situations = new MutableLiveData<>();
    private final MutableLiveData<Situation> selected = new MutableLiveData<>();
    private volatile boolean busy = false;

    public SituationViewModel() {
        refresh();
    }

    public LiveData<List<Situation>> getSituations() {
        return situations;
    }

    public LiveData<Situation> getSelected() {
        return selected;
    }

    public void select(Situation s) {
        selected.setValue(s);
    }

    public void refresh() {
        if (busy) {
            return;
        }
        busy = true;

        executor.execute(() -> {
            try {
                List<Situation> result = repository.fetchAll();
                if (result != null) {
                    situations.postValue(result);
                }

            } finally {
                busy = false;
            }
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}