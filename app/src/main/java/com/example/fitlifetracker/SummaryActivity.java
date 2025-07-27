package com.example.fitlifetracker;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import com.google.android.material.appbar.MaterialToolbar;
import java.util.ArrayList;

public class SummaryActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_summary);

        MaterialToolbar toolbar = findViewById(R.id.toolbar_summary);
        toolbar.setNavigationOnClickListener(v -> finish());

        BarChart barChart = findViewById(R.id.barChart);
        setupBarChart(barChart);
    }

    private void setupBarChart(BarChart barChart) {
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(0, 4500));
        entries.add(new BarEntry(1, 8000));
        entries.add(new BarEntry(2, 6200));
        entries.add(new BarEntry(3, 5100));
        entries.add(new BarEntry(4, 7300));
        entries.add(new BarEntry(5, 6800));
        entries.add(new BarEntry(6, 9200));

        BarDataSet dataSet = new BarDataSet(entries, "Steps per Day");
        dataSet.setColor(getResources().getColor(R.color.navy_blue, getTheme()));
        dataSet.setValueTextColor(getResources().getColor(R.color.navy_blue_light, getTheme()));
        dataSet.setValueTextSize(12f);

        BarData barData = new BarData(dataSet);
        barChart.setData(barData);

        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.setDrawValueAboveBar(true);
        barChart.setFitBars(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(getResources().getColor(R.color.navy_blue_light, getTheme()));
        final String[] days = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        xAxis.setValueFormatter(new IndexAxisValueFormatter(days));

        barChart.getAxisLeft().setAxisMinimum(0);
        barChart.getAxisLeft().setTextColor(getResources().getColor(R.color.navy_blue_light, getTheme()));
        barChart.getAxisRight().setEnabled(false);

        barChart.invalidate();
    }
}