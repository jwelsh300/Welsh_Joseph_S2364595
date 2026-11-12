package com.example.welsh_joseph_s2364595.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.model.Impact;
import com.example.welsh_joseph_s2364595.model.LocationPoint;
import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;

public class SituationDetailFragment extends Fragment {

    private SituationViewModel viewModel;

    private TextView detailCategory, detailType;
    private TextView detailRoad, detailDirection, detailFrom, detailTo;
    private TextView detailStart, detailEnd;
    private TextView detailLanes, detailDelay, detailUrgent;
    private TextView detailComment, detailId;

    private View rowRoad, rowDirection, rowFrom, rowTo;
    private View rowStart, rowEnd;
    private View rowLanes, rowDelay, rowUrgent;

    public SituationDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_situation_detail, container, false);

        detailCategory  = v.findViewById(R.id.detailCategory);
        detailType      = v.findViewById(R.id.detailType);
        detailRoad      = v.findViewById(R.id.detailRoad);
        detailDirection = v.findViewById(R.id.detailDirection);
        detailFrom      = v.findViewById(R.id.detailFrom);
        detailTo        = v.findViewById(R.id.detailTo);
        detailStart     = v.findViewById(R.id.detailStart);
        detailEnd       = v.findViewById(R.id.detailEnd);
        detailLanes     = v.findViewById(R.id.detailLanes);
        detailDelay     = v.findViewById(R.id.detailDelay);
        detailUrgent    = v.findViewById(R.id.detailUrgent);
        detailComment   = v.findViewById(R.id.detailComment);
        detailId        = v.findViewById(R.id.detailId);

        rowRoad      = v.findViewById(R.id.rowRoad);
        rowDirection = v.findViewById(R.id.rowDirection);
        rowFrom      = v.findViewById(R.id.rowFrom);
        rowTo        = v.findViewById(R.id.rowTo);
        rowStart     = v.findViewById(R.id.rowStart);
        rowEnd       = v.findViewById(R.id.rowEnd);
        rowLanes     = v.findViewById(R.id.rowLanes);
        rowDelay     = v.findViewById(R.id.rowDelay);
        rowUrgent    = v.findViewById(R.id.rowUrgent);

        return v;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(requireActivity()).get(SituationViewModel.class);

        viewModel.getSelected().observe(getViewLifecycleOwner(), s -> {
            if (s == null) {
                return;
            }
            showSituation(s);
        });
    }

    private void showSituation(Situation s) {
        detailCategory.setText(s.getCategory());
        String type = s.getTypeDetail().isEmpty() ? s.getRecordType() : s.getTypeDetail();
        detailType.setText(readable(type));

        setRow(rowRoad, detailRoad, s.getRoadName());
        setRow(rowDirection, detailDirection, readable(s.getDirection()));
        setRow(rowFrom, detailFrom, describe(s.getFrom()));
        setRow(rowTo, detailTo, describe(s.getTo()));

        setRow(rowStart, detailStart, s.getStartTime());
        setRow(rowEnd, detailEnd, s.getEndTime());

        Impact impact = s.getImpact();
        setRow(rowLanes, detailLanes, describeLanes(impact));
        setRow(rowDelay, detailDelay, readable(impact.getDelayType()));

        String urgent = "";
        if (s.getCategory().equals("Roadwork")) {
            urgent = s.isUrgent() ? "Yes" : "No";
        }
        setRow(rowUrgent, detailUrgent, urgent);

        String comment = s.getComment().replaceAll("(?i)<br\\s*/?>", "\n").trim();
        detailComment.setText(comment);

        detailId.setText("Ref: " + s.getId());
    }

    private void setRow(View row, TextView value, String text) {
        if (text.isEmpty()) {
            row.setVisibility(View.GONE);
        } else {
            row.setVisibility(View.VISIBLE);
            value.setText(text);
        }
    }

    private String describe(LocationPoint p) {
        StringBuilder sb = new StringBuilder();

        String place = p.getPlaceName().isEmpty() ? p.getRoadName() : p.getPlaceName();
        addLine(sb, place);

        if (!p.getJunctionName().isEmpty()) {
            addLine(sb, "Junction " + p.getJunctionName());
        }
        if (!p.getSecondRoadName().isEmpty()) {
            addLine(sb, "Crosses " + p.getSecondRoadName());
        }
        return sb.toString();
    }

    private void addLine(StringBuilder sb, String line) {
        if (line.isEmpty()) {
            return;
        }
        if (sb.length() > 0) {
            sb.append("\n");
        }
        sb.append(line);
    }

    private String describeLanes(Impact impact) {
        if (impact.isRoadClosed()) {
            return "Road closed";
        }
        int restricted = impact.getLanesRestricted();
        if (restricted > 0) {
            return restricted + (restricted == 1 ? " lane restricted" : " lanes restricted");
        }
        int open = impact.getOperationalLanes();
        if (open > 0) {
            return open + (open == 1 ? " lane open" : " lanes open");
        }
        return "";
    }

    private String readable(String code) {
        if (code.isEmpty()) {
            return "";
        }
        String spaced = code.replaceAll("([a-z])([A-Z])", "$1 $2").toLowerCase();
        return Character.toUpperCase(spaced.charAt(0)) + spaced.substring(1);
    }
}
