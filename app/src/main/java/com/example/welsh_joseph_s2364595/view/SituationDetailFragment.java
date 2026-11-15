package com.example.welsh_joseph_s2364595.view;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.content.Context;
import android.widget.ImageView;
import android.text.format.DateFormat;

import androidx.core.content.ContextCompat;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.welsh_joseph_s2364595.R;
import com.example.welsh_joseph_s2364595.model.Impact;
import com.example.welsh_joseph_s2364595.model.LocationPoint;
import com.example.welsh_joseph_s2364595.model.Situation;
import com.example.welsh_joseph_s2364595.viewmodel.SituationViewModel;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Locale;

public class SituationDetailFragment extends Fragment {

    private SituationViewModel viewModel;

    private TextView detailCategory, detailType;
    private TextView detailDirection, detailFrom, detailTo;
    private TextView detailStart, detailEnd;
    private TextView detailLanes, detailDelay, detailUrgent;
    private TextView detailComment, detailId;
    private TextView labelFrom;

    private View rowDirection, rowFrom, rowTo;
    private View rowStart, rowEnd;
    private View rowLanes, rowDelay, rowUrgent;

    private View detailSeverity;
    private ImageView detailIcon;

    public SituationDetailFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_situation_detail, container, false);

        detailCategory  = v.findViewById(R.id.detailCategory);
        detailType      = v.findViewById(R.id.detailType);
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
        detailSeverity = v.findViewById(R.id.detailSeverity);
        detailIcon     = v.findViewById(R.id.detailIcon);

        rowDirection = v.findViewById(R.id.rowDirection);
        rowFrom      = v.findViewById(R.id.rowFrom);
        rowTo        = v.findViewById(R.id.rowTo);
        rowStart     = v.findViewById(R.id.rowStart);
        rowEnd       = v.findViewById(R.id.rowEnd);
        rowLanes     = v.findViewById(R.id.rowLanes);
        rowDelay     = v.findViewById(R.id.rowDelay);
        rowUrgent    = v.findViewById(R.id.rowUrgent);

        labelFrom = v.findViewById(R.id.labelFrom);

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
        Context context = requireContext();
        detailCategory.setText(s.getCategory());

        String title = getString(SituationStyle.labelFor(s));
        if (!s.getRoadName().isEmpty()) {
            title = title + " · " + s.getRoadName();
        }
        detailType.setText(title);
        detailIcon.setImageResource(SituationStyle.iconFor(s));
        detailIcon.setContentDescription(getString(SituationStyle.labelFor(s)));

        detailSeverity.setBackgroundColor(
                ContextCompat.getColor(context, SituationStyle.delayColourFor(s)));

        String comment = s.getComment().replaceAll("(?i)<br\\s*/?>", "\n").trim();
        detailComment.setText(comment);

        setRow(rowDirection, detailDirection, readable(s.getDirection()));

        String from = describe(s.getFrom());
        String to = describe(s.getTo());
        if (from.equals(to)) {
            labelFrom.setText(R.string.detail_label_location);
            setRow(rowFrom, detailFrom, from);
            setRow(rowTo, detailTo, "");
        } else {
            labelFrom.setText(R.string.detail_label_from);
            setRow(rowFrom, detailFrom, from);
            setRow(rowTo, detailTo, to);
        }

        setRow(rowStart, detailStart, formatDateTime(s.getStartTime()));
        setRow(rowEnd, detailEnd, formatDateTime(s.getEndTime()));

        Impact impact = s.getImpact();
        setRow(rowLanes, detailLanes, describeLanes(impact));

        boolean isRoadwork = "Roadwork".equals(s.getCategory());
        String delay = readable(impact.getDelayType());
        if (delay.isEmpty() && isRoadwork) {
            delay = getString(R.string.key_colour_none);
        }
        setRow(rowDelay, detailDelay, delay);

        String urgent = (isRoadwork && s.isUrgent()) ? getString(R.string.detail_urgent_yes) : "";
        setRow(rowUrgent, detailUrgent, urgent);

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

    private String formatDateTime(String iso) {
        if (iso.isEmpty()) {
            return "";
        }
        try {
            OffsetDateTime time = OffsetDateTime.parse(iso);
            String timePattern = DateFormat.is24HourFormat(requireContext()) ? "HH:mm" : "h:mm a";
            DateTimeFormatter formatter =
                    DateTimeFormatter.ofPattern("EEE d MMM yyyy, " + timePattern, Locale.getDefault());
            return time.atZoneSameInstant(ZoneId.systemDefault()).format(formatter);
        } catch (DateTimeParseException e) {
            return iso;
        }
    }
}
