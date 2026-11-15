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

    public enum LoadStatus { // for partial load/failure output
        OK,
        FAILED_NO_DATA,
        FAILED_OLD_DATA,
        PARTIAL_INCIDENTS_FAILED,
        PARTIAL_ROADWORKS_FAILED
    }

    private static final long AUTO_REFRESH_MINUTES = 60;

    private final SituationRepository repository = new SituationRepository();

    private final ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();

    private ScheduledFuture<?> autoRefresh;

    private final MutableLiveData<List<Situation>> situations = new MutableLiveData<>();
    private final MutableLiveData<Situation> selected = new MutableLiveData<>();
    private final MutableLiveData<String> query = new MutableLiveData<>("");
    private final MediatorLiveData<List<Situation>> filtered = new MediatorLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);
    private final MutableLiveData<Long> lastUpdated = new MutableLiveData<>();
    private final MutableLiveData<LoadStatus> status = new MutableLiveData<>();

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

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public LiveData<Long> getLastUpdated() {
        return lastUpdated;
    }

    public LiveData<LoadStatus> getStatus() {
        return status;
    }

    public void statusShown() {
        status.setValue(null);   // main thread only
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
            loading.postValue(true);
            try {
                SituationRepository.FetchResult r = repository.fetchAll();
                List<Situation> previous = situations.getValue();

                if (r == null || r.incidents == null && r.roadworks == null) {
                    status.postValue(previous == null
                            ? LoadStatus.FAILED_NO_DATA
                            : LoadStatus.FAILED_OLD_DATA);
                    return;
                }

                // halves
                List<Situation> incidents = (r.incidents != null)
                        ? r.incidents
                        : oldOfCategory(previous, "Incident");
                List<Situation> roadworks = (r.roadworks != null)
                        ? r.roadworks
                        : oldOfCategory(previous, "Roadwork");

                List<Situation> merged = new ArrayList<>(incidents.size() + roadworks.size());
                merged.addAll(incidents);
                merged.addAll(roadworks);

                lastUpdated.postValue(System.currentTimeMillis());
                situations.postValue(merged);

                if (r.incidents == null) {
                    status.postValue(LoadStatus.PARTIAL_INCIDENTS_FAILED);
                } else if (r.roadworks == null) {
                    status.postValue(LoadStatus.PARTIAL_ROADWORKS_FAILED);
                } else {
                    status.postValue(LoadStatus.OK);
                }
            } finally {
                busy.set(false);
                loading.postValue(false);
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

    private static List<Situation> oldOfCategory(List<Situation> previous, String category) {
        List<Situation> result = new ArrayList<>();
        if (previous == null) {
            return result;
        }
        for (Situation s : previous) {
            if (category.equals(s.getCategory())) {
                result.add(s);
            }
        }
        return result;
    }


}