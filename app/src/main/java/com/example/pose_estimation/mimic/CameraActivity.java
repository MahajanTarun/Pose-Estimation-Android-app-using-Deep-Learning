package com.example.pose_estimation.mimic;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.view.WindowManager;

import com.example.pose_estimation.mimic.R;

import java.util.Calendar;

/**
 * Main {@code Activity} class for the Camera app.
 */
public class CameraActivity extends Activity implements ActivityCompat.OnRequestPermissionsResultCallback {
    Calendar calendar;

    int time1,time2;


    private boolean checkedPermissions=false;
    private int PERMISSIONS_REQUEST_CODE=1;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_camera);
        checkPermission();
    }

    private void checkPermission(){
        if (!checkedPermissions && !allPermissionsGranted()) {
            ActivityCompat.requestPermissions(this, getRequiredPermissions(), PERMISSIONS_REQUEST_CODE);
            return;
        } else {
            checkedPermissions = true;
        }
        getFragmentManager().beginTransaction().replace(R.id.container,Camera2BasicFragment.newInstance()).commit();
    }

    private String[] getRequiredPermissions() {
        return new String[]{"android.permission.WRITE_EXTERNAL_STORAGE","android.permission.CAMERA"};
    }

    private boolean allPermissionsGranted() {
        for (String permission : getRequiredPermissions()) {
            if (ContextCompat.checkSelfPermission(this, permission)
                    != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        getFragmentManager().beginTransaction().replace(R.id.container,Camera2BasicFragment.newInstance()).commit();
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


}
