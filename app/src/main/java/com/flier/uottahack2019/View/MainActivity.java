package com.flier.uottahack2019.View;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.flier.uottahack2019.R;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void btnchange(View view) {
        Intent intent = new Intent(MainActivity.this, FileViewActivity.class);
        startActivity(intent);
    }
}
