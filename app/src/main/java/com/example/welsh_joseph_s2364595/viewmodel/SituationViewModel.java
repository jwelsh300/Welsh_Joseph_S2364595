package com.example.welsh_joseph_s2364595.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.MediatorLiveData;

import com.example.welsh_joseph_s2364595.data.SituationRepository;
import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.model.LocationPoint;

import java.util.List;
import java.util.concurrent.Executors;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class SituationViewModel extends ViewModel {

    private static final long AUTO_REFRESH_MINUTES = 60;

    private final SituationRepository repository = new SituationRepository();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> autoRefresh;

    private final MutableLiveData<List<Situation>> situations = new MutableLiveData<>();
    private final MutableLiveData<Situation> selected = new MutableLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<Situation>> filtered = new MediatorLiveData<>();

    private final AtomicBoolean busy = new AtomicBoolean(false);

    public SituationViewModel() {
        filtered.addSource(situations, list -> applyFilter());
        filtered.addSource(query, text -> applyFilter());
        refresh();
        startAutoRefresh(AUTO_REFRESH_MINUTES);
    }

    public LiveData<String> getQuery() {
        return query;
    }

    public LiveData<List<Situation>> getSituations() {
        return situations;
    }

    public LiveData<Situation> getSelected() {
        return selected;
    }

    public LiveData<List<Situation>> getFiltered() {
        return filtered;
    }

    public void select(Situation s) {
        selected.setValue(s);
    }

    public void setQuery(String text) {
        query.setValue(text == null ? "" : text);
    }

    public void refresh() {
        if (!busy.compareAndSet(false, true)) {
            return;
        }

        executor.execute(() -> {
            try {
                List<Situation> result = repository.fetchAll();
                if (result != null) {
                    situations.postValue(result);
                }
            } finally {
                busy.set(false);
            }
        });
    }

    private void startAutoRefresh(long minutes) {
        if (autoRefresh != null) {
            autoRefresh.cancel(false);
        }
        autoRefresh = executor.scheduleWithFixedDelay(
                this::refresh, minutes, minutes, TimeUnit.MINUTES);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }

    private void applyFilter() {

        List<Situation> list = situations.getValue();
        if (list == null) {
            return;
        }

        String raw = query.getValue();
        String q = (raw == null ? "" : raw).trim().toUpperCase(Locale.ROOT);

        List<Situation> result = new ArrayList<>();
        for (Situation s : list) {
            if (q.isEmpty() || matchesSituation(s, q)) {
                result.add(s);
            }
        }
        filtered.setValue(result);
    }

    private static boolean matchesSituation(Situation s, String q) {
        return matchesPoint(s.getFrom(), q) || matchesPoint(s.getTo(), q);
    }

    private static boolean matchesPoint(LocationPoint p, String q) {
        return matchesRoad(p.getRoadName(), q) || matchesRoad(p.getSecondRoadName(), q);
    }

    private static boolean matchesRoad(String road, String q) {
        String r = road.trim().toUpperCase(Locale.ROOT);
        if (!r.startsWith(q)) {
            return false;
        }
        if (r.length() == q.length() || !Character.isDigit(q.charAt(q.length() - 1))) {
            return true;
        }
        return !Character.isDigit(r.charAt(q.length()));
    }

}