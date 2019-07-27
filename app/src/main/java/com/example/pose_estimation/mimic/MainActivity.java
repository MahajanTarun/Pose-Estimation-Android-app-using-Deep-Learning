package com.example.pose_estimation.mimic;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.support.v7.widget.Toolbar;

public class MainActivity extends AppCompatActivity {


    Button button;
    private ImageView img;
    Toolbar toolbarWidget;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

       /* Toolbar toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        toolbar.setLogo(R.drawable.logo);*/

        toolbarWidget = (Toolbar) findViewById(R.id.toolbar);

        toolbarWidget.setNavigationIcon(R.mipmap.logo);

        setSupportActionBar(toolbarWidget);



        img = (ImageView) findViewById(R.id.imageView1);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.logo_anim);
        img.setAnimation(animation);
        animation.start();


        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                //start new activity here
                Intent i = new Intent(getApplicationContext(),CameraActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP |Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(i);
                finish();
            }
        }, 4000);
    }

}








