package ru.dwdm.trademate;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import java.util.ArrayList;

public class SelectModelActivity extends AppCompatActivity {

    public static final String KEY_MODEL = "KEY_MODEL";

    private ArrayList<Model> models = new ArrayList<>();
    private RecyclerView selectRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_model);

        models.add(new Model("Ugandan Knuckles TheWaveVR", R.drawable.knuckles));
        models.add(new Model("Model poss", R.drawable.poss));
        models.add(new Model("Wheelchair", R.drawable.wheelchair));
        models.add(new Model("vending machine 10", R.drawable.voda));
        models.add(new Model("Vending Machine", R.drawable.vending_machine));

        selectRecycler = findViewById(R.id.select_recycler);
        selectRecycler.setHasFixedSize(true);
        LinearLayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        selectRecycler.setLayoutManager(layoutManager);

        selectRecycler.setAdapter(new SelectAdapter(models, model -> {
            getIntent().putExtra(KEY_MODEL, model.getIdRes());
            setResult(RESULT_OK, getIntent());
            finish();
        }));
    }
}
