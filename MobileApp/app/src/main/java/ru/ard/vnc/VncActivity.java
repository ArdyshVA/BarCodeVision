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

package ru.ard.vnc;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.R;
import com.google.android.gms.vision.barcode.Barcode;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Objects;

import ru.ard.scanner.BarcodeCaptureActivity;
import ru.ard.vnc.callback.VNCConnectErrorCallBack;
import ru.ard.vnc.connect.VNCManager;
import ru.ard.vnc.glavsoft.exceptions.AuthenticationFailedException;
import ru.ard.vnc.glavsoft.exceptions.FatalException;
import ru.ard.vnc.glavsoft.exceptions.TransportException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedProtocolVersionException;
import ru.ard.vnc.glavsoft.exceptions.UnsupportedSecurityTypeException;
import ru.ard.warehousscanner.connect.DbService;

import static android.view.View.INVISIBLE;
import static android.view.View.VISIBLE;
import static ru.ard.warehousscanner.MainActivity.APP_PREFERENCES;
import static ru.ard.warehousscanner.MainActivity.APP_VERSION;
import static ru.ard.warehousscanner.MainActivity.PARAM_START_FROM;
import static ru.ard.warehousscanner.MainActivity.PARAM_USER_NAME;
import static ru.ard.warehousscanner.MainActivity.PARAM_VNC_IP;
import static ru.ard.warehousscanner.MainActivity.PARAM_VNC_PASSWORD;
import static ru.ard.warehousscanner.MainActivity.PARAM_VNC_PORT;
import static ru.ard.warehousscanner.MainActivity.RC_BARCODE_CAPTURE;
import static ru.ard.warehousscanner.MainActivity.RC_SETTINGS;
import static ru.ard.warehousscanner.MainActivity.SERVER_DEFAULT_IP;
import static ru.ard.warehousscanner.MainActivity.START_FROM_MAIN;
import static ru.ard.warehousscanner.MainActivity.VNC_DEFAULT_PASSWORD;
import static ru.ard.warehousscanner.MainActivity.VNC_DEFAULT_PORT;
import static ru.ard.warehousscanner.MainActivity.decodeFormat;

/**
 * Активити, которое работает с VNC сервером
 */
public class VncActivity extends Activity implements View.OnClickListener, VNCConnectErrorCallBack {

    //HARD CODE PARAMS
    public static final String IP = "192.168.35.159";
    public static final String PORT = "5900";
    public static final String IP_PORT_SEPARATOR = ":";

    //ТЕГИ для логирования
    private static final String TAG = "BarcodeMain";

    //Произвольные строковые значения
    public static final String REASON_ALL_BARCODES_SCANNED = "all_barcodes_scanned";
    public static final String BARCODE_IS_ALREADY_EXISTS = "barcode_is_already_exists";

    //VIEWS
    private CompoundButton useFlash;
    private CompoundButton autoSend;
    private TextView barcodeType;
    private TextView barcodeValue;
    private TextView count;
    private TextView version;
    private Button send;
    private Button scan;
    private Button rescan;
    private Button change_mode;
    private ImageButton settingsButton;
    private ProgressBar progressBar;

    //SETTINGS
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

    //ПОЛЯ ДЛЯ РАБОТЫ КЛАССА
    VNCManager vncManager;
    DbService dbService;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"CommitPrefEdits", "SourceLockedOrientationActivity"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_vnc);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        barcodeType = (TextView) findViewById(R.id.vnc_barcode_type);
        barcodeValue = (TextView) findViewById(R.id.vnc_name);
        count = (TextView) findViewById(R.id.vnc_count);
        version = (TextView) findViewById(R.id.vnc_version);
        version.setText(APP_VERSION);

        useFlash = (CompoundButton) findViewById(R.id.vnc_use_flash);
        autoSend = (CompoundButton) findViewById(R.id.vnc_autosend);

        send = (Button) findViewById(R.id.vnc_send);
        scan = (Button) findViewById(R.id.vnc_read_barcode);
        rescan = (Button) findViewById(R.id.vnc_clear);
        settingsButton = (ImageButton) findViewById(R.id.vnc_settings_btn);
        change_mode = (Button) findViewById(R.id.vnc_change_mode);
        send.setOnClickListener(this);
        scan.setOnClickListener(this);
        rescan.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        change_mode.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.vnc_progress);

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        settingsEditor = settings.edit();

        send.setVisibility(INVISIBLE);
        progressBar.setVisibility(INVISIBLE);

        vncManager = new VNCManager();
        dbService = new DbService(this);

        //установить соединение с vnc
        //openConnection();
    }

    private void openConnection() {
        vncManager.reopenConnection(
                settings.getString(PARAM_VNC_PASSWORD, VNC_DEFAULT_PASSWORD),
                settings.getString(PARAM_VNC_IP, SERVER_DEFAULT_IP),
                Integer.parseInt(Objects.requireNonNull(settings.getString(PARAM_VNC_PORT, VNC_DEFAULT_PORT))),
                this
        );
    }

    @Override
    protected void onDestroy() {
        //закрыть соединение с vnc
        vncManager.closeConnection();
        super.onDestroy();
    }

    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.vnc_read_barcode:
                // launch barcode activity.
                intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
                startActivityForResult(intent, RC_BARCODE_CAPTURE
                );
                break;

            case R.id.vnc_settings_btn:
                // launch activity.
                intent = new Intent(this, SettingsVncActivity.class);
                startActivityForResult(intent, RC_SETTINGS);
                break;

            case R.id.vnc_clear:
                barcodeValue.setText("");
                barcodeType.setText("");
                scan.callOnClick();
                break;

            case R.id.vnc_send:
                send();
                break;

            case R.id.vnc_change_mode:
                settingsEditor.putString(PARAM_START_FROM, START_FROM_MAIN).apply();
                finish();
                break;
        }
    }

    private void clearFields() {
        barcodeValue.setText("");
        barcodeType.setText("");
        count.setText("");
    }

    /**
     * задаем новое поведение конпке назад
     */
    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == RC_BARCODE_CAPTURE) {
            if (resultCode == CommonStatusCodes.SUCCESS) {
                if (data != null) {
                    Barcode barcode = data.getParcelableExtra(BarcodeCaptureActivity.BarcodeObject);
                    if (autoSend.isChecked()) {
                        send(barcode.displayValue, decodeFormat(barcode.format));
                    } else {
                        barcodeValue.setText(barcode.displayValue);
                        barcodeType.setText(decodeFormat(barcode.format));
                        if (!barcodeValue.getText().toString().equals("")) {
                            setButtonsStateSend();
                        }
                    }
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    //не стали сканировать, вернулись через кнопку back
                    //как будто не ходили сканировать
                    Log.d(TAG, "No barcode captured, intent data is null");
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(VncActivity.this);
                builder.setMessage("Ошибка сканирования: " + CommonStatusCodes.getStatusCodeString(resultCode))
                        .setNeutralButton("Ок, обращусь к разработчику", null)
                        .create()
                        .show();
            }
        } else if (requestCode == RC_SETTINGS) {
            openConnection();
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Включает только кнопку сканировать
     */
    private void setButtonsStateScan() {
        send.setVisibility(INVISIBLE);
        rescan.setVisibility(INVISIBLE);
        scan.setVisibility(VISIBLE);
    }

    /**
     * Включает кнопки отпарвить и повторить
     */
    private void setButtonsStateSend() {
        send.setVisibility(VISIBLE);
        rescan.setVisibility(VISIBLE);
        scan.setVisibility(INVISIBLE);
    }

    /**
     * Отправляет данные на VNC сервер
     */
    private void send() {
        send(barcodeValue.getText().toString(), "");
    }

    /**
     * Отправляет данные на VNC сервер
     */
    private void send(String barcode, String codeType) {
        setButtonsStateScan();
        clearFields();
        try {
//            vncManager.sendString(barcode);
//            vncManager.sendCharENTER();
        } catch (Exception e) {
            e.printStackTrace();

        }
        progressBar.setVisibility(VISIBLE);
        dbService.sendToKeyboard(barcode, settings.getString(PARAM_USER_NAME, ""),
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        progressBar.setVisibility(INVISIBLE);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        progressBar.setVisibility(INVISIBLE);
                    }
                });
    }

    /**
     * Реализация этого метода - callback, вызывается, при ошибке открытия соединения
     *
     * @param e - Rfrfz
     */
    @Override
    public void onConnectError(final Exception e) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                String msg = "";
                if (e instanceof UnsupportedProtocolVersionException) {
                    msg = getResources().getString(R.string.vnc_error_unsopported_protocol) + "= " + e.getMessage();
                } else if (e instanceof UnsupportedSecurityTypeException) {
                    msg = getResources().getString(R.string.vnc_error_unsopported_security) + "= " + e.getMessage();
                } else if (e instanceof AuthenticationFailedException) {
                    msg = getResources().getString(R.string.vnc_error_password_ko) + "= " + e.getMessage();
                } else if (e instanceof TransportException) {
                    msg = getResources().getString(R.string.vnc_error_transport) + "= " + e.getMessage();
                } else if (e instanceof UnknownHostException) {
                    msg = getResources().getString(R.string.vnc_error_host_ko) + "= " + e.getMessage();
                } else if (e instanceof IOException) {
                    msg = getResources().getString(R.string.vnc_error_connection) + "= " + e.getMessage();
                } else if (e instanceof FatalException) {
                    msg = getResources().getString(R.string.vnc_error_connection) + "= " + e.getMessage();
                } else {
                    msg = getResources().getString(R.string.vnc_error_connection) + "= " + e.getMessage();
                }
                Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_LONG).show();
            }
        });
    }
}
