package com.example.myapplication;

import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class DataVisualizationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_visualization);

        TextView headerTitle = findViewById(R.id.headerTitle);
        headerTitle.setText(getText(R.string.data_viz_header));

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        Accordion accordion = new Accordion(this, TestAccordion());
        recyclerView.setAdapter(accordion);
    }

    private List<BudgetCategory> TestAccordion() {
        return new ArrayList<BudgetCategory>() {{
            add(new BudgetCategory("Food", 200, new ArrayList<Expense>() {{
                add(new Expense("McDonalds", 12.57));
                add(new Expense("Tres Gatos", 120.29));
            }}));
            add(new BudgetCategory("Travel", 1000, new ArrayList<Expense>() {{
                add(new Expense("United Airlines", 842.76));
                add(new Expense("Marriott", 488.19));
            }}));
        }};
    }
}
