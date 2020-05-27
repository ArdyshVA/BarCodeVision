/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package ru.ard.warehousscanner;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.samples.vision.barcodereader.R;

import static ru.ard.warehousscanner.MainActivity.APP_PREFERENCES;
import static ru.ard.warehousscanner.MainActivity.PARAM_PORT;
import static ru.ard.warehousscanner.MainActivity.PARAM_USER_NAME;
import static ru.ard.warehousscanner.MainActivity.PARAM_IP;
import static ru.ard.warehousscanner.MainActivity.SERVER_DEFAULT_IP;
import static ru.ard.warehousscanner.MainActivity.SERVER_DEFAULT_PORT;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class SettingsBarCodeActivity extends Activity {
    //VIEWS
    private TextView ip;
    private TextView port;
    private TextView name;
    private TextView author_email;
    private Button back;

    //SETTINGS
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

    //ПОЛЯ ДЛЯ РАБОТЫ КЛАССА
    ClipboardManager clipboardManager;
    ClipData clipData;

    @SuppressLint("SourceLockedOrientationActivity")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        ip = (TextView) findViewById(R.id.ip_tw);
        port = (TextView) findViewById(R.id.port_tw);
        name = (TextView) findViewById(R.id.name_tw);
        author_email = (TextView) findViewById(R.id.author_email_label);
        back = (Button) findViewById(R.id.back_btn);

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        settingsEditor = settings.edit();
        clipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);

        ip.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                settingsEditor.putString(PARAM_IP, ip.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        port.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                settingsEditor.putString(PARAM_PORT, port.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        name.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                settingsEditor.putString(PARAM_USER_NAME, name.getText().toString()).apply();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        author_email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clipData = ClipData.newPlainText("e-mail", "v.a.ardyshev@gmail.com");
                clipboardManager.setPrimaryClip(clipData);
                Toast.makeText(getApplicationContext(), "Электронная почта скопирована в буфер обмена", Toast.LENGTH_SHORT).show();
            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ip.setText(settings.getString(PARAM_IP, SERVER_DEFAULT_IP));
        port.setText(settings.getString(PARAM_PORT, SERVER_DEFAULT_PORT));
        name.setText(settings.getString(PARAM_USER_NAME, ""));
    }
}
