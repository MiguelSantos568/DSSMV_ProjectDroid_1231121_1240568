package com.example.condosched.ui;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import com.example.condosched.R;

public class CondominiumActivity extends AppCompatActivity {

    private ListView lv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.condominiumlist);
    }



    public void onClickButton(View v) {
        Log.d("BUTTONS", "User tapped Add Condo");

    }
}
