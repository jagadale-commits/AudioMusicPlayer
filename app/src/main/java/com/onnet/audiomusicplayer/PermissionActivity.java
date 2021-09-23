package com.onnet.audiomusicplayer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class PermissionActivity extends AppCompatActivity {
    private static final int READ_STORAGE_PERMISSION_REQUEST_CODE = 41;
    TextView tvError;
    Button btnPermission;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!checkPermissionForReadExtertalStorage(this)) {
            requestPermissionForReadExtertalStorage(this);

        }else
        {
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_permission);
        tvError = findViewById(R.id.textView);
        btnPermission = findViewById(R.id.buttonP);
        btnPermission.setOnClickListener(view -> new AlertDialog.Builder(PermissionActivity.this)
                .setTitle("필요한 권한")
                .setMessage("권한을 활성화하려면 아래 단계를 따르십시오. \n" +
                        "앱 사용 권한 클릭 \n" +
                        "거부된 섹션의 파일 및 미디어를 클릭합니다.\n" +
                        "미디어 전용 옵션에 대한 액세스 허용 설정")
                .setPositiveButton("그래", (dialog, which) -> {
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", PermissionActivity.this.getPackageName(), null);
                    intent.setData(uri);
                    PermissionActivity.this.startActivity(intent);
                    finish();
                })
                .setNegativeButton("취소", (dialog, which) -> dialog.dismiss()).create().show());
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == READ_STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                finish();
                Intent intent = new Intent(this, MainActivity.class);
                startActivity(intent);
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }
    public boolean checkPermissionForReadExtertalStorage(Context context) {
        int result = context.checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    public void requestPermissionForReadExtertalStorage(Context context) {
            ActivityCompat.requestPermissions((Activity) context, new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE},
                    READ_STORAGE_PERMISSION_REQUEST_CODE);
    }
}