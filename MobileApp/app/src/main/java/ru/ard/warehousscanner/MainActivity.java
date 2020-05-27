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

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.app.Activity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.google.android.gms.common.api.CommonStatusCodes;
import com.google.android.gms.samples.vision.barcodereader.R;
import com.google.android.gms.vision.barcode.Barcode;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.regex.Pattern;

import ru.ard.scanner.BarcodeCaptureActivity;
import ru.ard.warehousscanner.connect.DbService;
import ru.ard.warehousscanner.listeners.RefusalWorkResponseListener;

import static android.view.View.VISIBLE;
import static ru.ard.warehousscanner.connect.DbService.DATA;
import static ru.ard.warehousscanner.connect.DbService.PARAM_ID;
import static ru.ard.warehousscanner.connect.DbService.REASON;
import static ru.ard.warehousscanner.connect.DbService.RECORDS_COUNT;
import static ru.ard.warehousscanner.connect.DbService.SUCCESS;

/**
 * Main activity demonstrating how to pass extra parameters to an activity that
 * reads barcodes.
 */
public class MainActivity extends Activity implements View.OnClickListener {

    //HARD CODE PARAMS
    public static final String APP_VERSION = "ver.09";
    public static final String SERVER_DEFAULT_IP = "192.168.35.159";
    public static final String SERVER_DEFAULT_PORT = "8080";
    public static final String VNC_DEFAULT_PORT = "5900";
    public static final String VNC_DEFAULT_PASSWORD = "admin";

    public static String getServerApiUrl(String ip, String port) {
        return "http://" + ip + ":" + port + "/server_api.php";
    }

    //STATE
    public static final String STATE_FIRST_START = "beforeStart";
    public static final String STATE_START = "start";
    public static final String STATE_AFTER_SCAN = "afterScan";
    public static final String STATE_SlEEP = "sleep";
    public static final String STATE_SCAN = "scan";

    //ТЕГИ для логирования
    private static final String TAG = "BarcodeMain";

    //Произвольные строковые значения
    public static final String REASON_ALL_BARCODES_SCANNED = "all_barcodes_scanned";
    public static final String BARCODE_IS_ALREADY_EXISTS = "barcode_is_already_exists";

    //SETTINGS PARAMS
    public static final String APP_PREFERENCES = "BarCodeVisionSettings";
    public static final String PARAM_IP = "ip";
    public static final String PARAM_PORT = "port";
    public static final String PARAM_NEED_RELOAD = "needReload";
    public static final String PARAM_USER_NAME = "userName";
    public static final String PARAM_VNC_IP = "vncIp";
    public static final String PARAM_VNC_PORT = "vncPort";
    public static final String PARAM_VNC_PASSWORD = "vncPassword";
        //задает с какой страницей работем
    public static final String PARAM_START_FROM = "startFrom";
    public static final String START_FROM_MAIN = "startMain";

    //VIEWS
    private CompoundButton useFlash;
    private CompoundButton autoSend;
    private TextView id;
    private TextView webArticle;
    private TextView catalog;
    private TextView barcodeValue;
    private TextView barcodeType;
    private TextView name;
    private TextView dialogTitle;
    private TextView count;
    private TextView version;
    private Button send;
    private Button scan;
    private Button rescan;
    private Button reload;
    private Button change_mode;
    private ImageButton settingsButton;
    private Button skip;
    private HorizontalScrollView scrollView;
    private ProgressBar progressBar;
    private EditText dialogInput;
    private View viewDialog;

    //SETTINGS
    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

    //КОДЫ ДЛЯ ACTIVITY RESULT и подобные
    public static final int RC_BARCODE_CAPTURE = 9001;
    public static final int RC_SETTINGS = 9002;
    private static final int READ_PHONE_STATE_PERM = 3;

    //ПОЛЯ ДЛЯ РАБОТЫ КЛАССА
    private String state = STATE_FIRST_START; //текущее состояние класса
    private DbService dbService;
    private AlertDialog.Builder alertDialogBuilder;
    //задает признак того, что перезагрузка привела к успеху
    private boolean flagReload = false;
    private RefusalWorkResponseListener refusalWorkResponseListener;
    //идентификация пользователя
//    private String deviceIMEI;
//    private String phoneNumber;
    //выставялется в true, когда нужно уведомить о количестве карточек, например при потере соединения с сервером
    private boolean flagGetCount = true;
    //дает знать setState о том, что после применения ip идет перезагрузка, которая не гасит прогрессбар, поэтому его включать не надо
    private boolean onIpAcceptFlag = false;

    final Response.Listener<String> responseListenerGetRecord = new Response.Listener<String>() {
        private boolean svernut;

        @Override
        public void onResponse(String response) {
            if (!flagReload) {
                progressBar.setVisibility(View.INVISIBLE);
            } else {
                //иначе его отключает таймер
                flagReload = false;
            }
            //на случай, если делали перезагрузку
            showAll();
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean(SUCCESS);

                if (success) {
                    JSONArray data = jsonResponse.getJSONArray(DATA);
                    id.setText(data.getString(0));
                    webArticle.setText(data.getString(1));
                    name.setText(data.getString(2));
                    catalog.setText(data.getString(3));
                    count.setText(data.getString(4));
                    scrollView.postDelayed(new Runnable() {
                        public void run() {
                            scrollView.fullScroll(HorizontalScrollView.FOCUS_RIGHT);
                        }
                    }, 300L);
                } else {
                    String reason = jsonResponse.getString(REASON);
                    if (reason.equals(REASON_ALL_BARCODES_SCANNED)) {
                        //закончились карточки на сервере
                        hideAll();
                        name.setText("В базе не осталось наименований для сканирования");
                    } else {
                        serverErrorParams(jsonResponse.getString(REASON));
                    }
                }
            } catch (JSONException e) {
                serverErrAnswer(e);
            }
        }
    };
    final Response.Listener<String> responseListenerPutRecord = new Response.Listener<String>() {
        private boolean svernut;

        @Override
        public void onResponse(String response) {
            progressBar.setVisibility(View.INVISIBLE);
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean(SUCCESS);
                if (success) {
                    setState(STATE_START);
                } else if (BARCODE_IS_ALREADY_EXISTS.equals(jsonResponse.getString(REASON))) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Такой штрих-код уже сканировали!")
                            .setPositiveButton("Ок", null)
                            .create()
                            .show();
                } else {
                    serverErrorParams(jsonResponse.getString(REASON));
                }
            } catch (JSONException e) {
                serverErrAnswer(e);
            }
        }
    };
    final Response.Listener<String> responseListenerGetCardsCount = new Response.Listener<String>() {
        private boolean svernut;

        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean(SUCCESS);

                if (success) {
                    flagGetCount = false;
                    int count = jsonResponse.getInt(RECORDS_COUNT);
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setMessage("Доступно " + count + " позиций для работы")
                            .setPositiveButton("Ок", null)
                            .create()
                            .show();
                } else {
                    serverErrorParams(jsonResponse.getString(REASON));
                }
            } catch (JSONException e) {
                serverErrAnswer(e);
            }
        }
    };
    final Response.Listener<String> responseListenerSkipCard = new Response.Listener<String>() {
        private boolean svernut;

        @Override
        public void onResponse(String response) {
            try {
                JSONObject jsonResponse = new JSONObject(response);
                boolean success = jsonResponse.getBoolean(SUCCESS);

                if (success) {
                    state = STATE_SlEEP;
                    setState(STATE_START);
                } else {
                    serverErrorParams(jsonResponse.getString(REASON));
                }

            } catch (JSONException e) {
                serverErrAnswer(e);
            }
        }
    };
    final Response.ErrorListener errorListener = new Response.ErrorListener() {
        private boolean svernut;

        @Override
        public void onErrorResponse(VolleyError error) {
            progressBar.setVisibility(View.INVISIBLE);
            flagReload = false;
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Соединение с сервером \n[" + settings.getString(PARAM_IP, "") + "] не установлено. \nПроверьте " +
                    "запущен ли сервер на ПК и настройки брандмауэра.")
                    .setNeutralButton("Ок, обращусь к разработчику", null)
                    .create()
                    .show();
        }
    };
    final Response.ErrorListener errorListenerGetRecord = new Response.ErrorListener() {
        private boolean svernut;

        @Override
        public void onErrorResponse(VolleyError error) {
            //если появлилась проблема с соединением, то ставлю флаг, что нужно при первой возможности показать доступное количество для работы
            flagGetCount = true;

            if (!flagReload) {
                progressBar.setVisibility(View.INVISIBLE);
                send.setVisibility(View.INVISIBLE);
            } else {
                //иначе его отключает таймер
                flagReload = false;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Соединение с сервером \n[" + settings.getString(PARAM_IP, "") + "] не установлено. \nПроверьте " +
                    "запущен ли сервер на ПК и настройки брандмауэра.")
                    .setNeutralButton("Ок, обращусь к разработчику", null)
                    .create()
                    .show();
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @SuppressLint({"CommitPrefEdits", "SourceLockedOrientationActivity"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        settings = getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
        settingsEditor = settings.edit();

        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        dbService = new DbService(this);

        id = (TextView) findViewById(R.id.id);
        webArticle = (TextView) findViewById(R.id.wabarticle);
        catalog = (TextView) findViewById(R.id.catalog);
        barcodeValue = (TextView) findViewById(R.id.barcode_value);
        barcodeType = (TextView) findViewById(R.id.barcode_type);
        name = (TextView) findViewById(R.id.name);
        count = (TextView) findViewById(R.id.count);
        version = (TextView) findViewById(R.id.version);
        version.setText(APP_VERSION);

        useFlash = (CompoundButton) findViewById(R.id.use_flash);
        autoSend = (CompoundButton) findViewById(R.id.autosend);

        send = (Button) findViewById(R.id.send);
        scan = (Button) findViewById(R.id.read_barcode);
        rescan = (Button) findViewById(R.id.clear);
        reload = (Button) findViewById(R.id.reload);
        settingsButton = (ImageButton) findViewById(R.id.settings_btn);
        skip = (Button) findViewById(R.id.skip);
        change_mode = (Button) findViewById(R.id.change_mode);
        scrollView = (HorizontalScrollView) findViewById(R.id.horiz_lay);
        scrollView.setHorizontalScrollBarEnabled(false);
        reload.setVisibility(View.INVISIBLE);
        send.setOnClickListener(this);
        scan.setOnClickListener(this);
        rescan.setOnClickListener(this);
        reload.setOnClickListener(this);
        settingsButton.setOnClickListener(this);
        skip.setOnClickListener(this);
        change_mode.setOnClickListener(this);

        progressBar = (ProgressBar) findViewById(R.id.progress);

        refusalWorkResponseListener = new RefusalWorkResponseListener(settings);

        //DIALOG_IP
        alertDialogBuilder = new AlertDialog.Builder(this);
        viewDialog = LayoutInflater.from(this).inflate(R.layout.dialog, null);
        //set dialog.xml to alertdialog builder
        alertDialogBuilder.setView(viewDialog);
        dialogInput = (EditText) viewDialog.findViewById(R.id.editTextDialogUserInput);
        dialogTitle = (TextView) viewDialog.findViewById(R.id.dialog_title);

        //получаем имя пользователя если требуется
        askUserName();
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onResume() {
        super.onResume();

        switch (state) {
            case STATE_SlEEP:
                setState(STATE_START);
                break;

            case STATE_SCAN:
                setState(STATE_AFTER_SCAN);
                break;

            case STATE_FIRST_START:
                hideAll();
                reload.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.INVISIBLE);
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (state.equals(STATE_START)) {
            setState(STATE_SlEEP);
        }
    }

    public void setState(String state) {
        switch (state) {
            case STATE_START:
                if (this.state.equals(STATE_FIRST_START)) {
                    showAll();
                }

                if (flagGetCount) {
                    dbService.getCardsCount(responseListenerGetCardsCount, null);
                }

                if (!onIpAcceptFlag) {
                    progressBar.setVisibility(View.VISIBLE);
                } else {
                    onIpAcceptFlag = false;
                }

                //если это попытка обновления когда кончились карточки, от не отображать
                if (!flagReload && reload.getVisibility() != VISIBLE) {
                    scan.setVisibility(View.VISIBLE);
                } else {
                    //делаю задержку при обновлении,чтобы прогресс бар прокрутился и дал понять, что оно произошло
                    new ProgressBarTask().execute();
                }

                send.setVisibility(View.INVISIBLE);
                rescan.setVisibility(View.INVISIBLE);

                String refusalId = settings.getString(PARAM_ID, null);
                if (refusalId == null) {
                    //отправка отказа от обработки карточки при сворачивании была успешной
                    clearFields();
                    dbService.getRecord(responseListenerGetRecord, errorListenerGetRecord);
                } else {
                    //нужно сперва отправлять отказ
                    dbService.refuseWork(refusalId, refusalWorkResponseListener, null);
                }
                break;

            case STATE_AFTER_SCAN:
                //если автообовление, сразу прячем ненужные кнопки
                if (autoSend.isChecked()) {
                    send.setVisibility(View.INVISIBLE);
                    rescan.setVisibility(View.INVISIBLE);
                    scan.setVisibility(View.VISIBLE);
                } else {
                    send.setVisibility(View.VISIBLE);
                    rescan.setVisibility(View.VISIBLE);
                    scan.setVisibility(View.INVISIBLE);
                }
                break;

            case STATE_SlEEP:
                if (!id.getText().toString().equals("")) {
                    settingsEditor.putString(PARAM_ID, id.getText().toString()).apply();
                    dbService.refuseWork(id.getText().toString(), refusalWorkResponseListener, null);
                }
                break;
        }
        this.state = state;
    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    @Override
    public void onClick(View v) {
        Intent intent;

        switch (v.getId()) {
            case R.id.read_barcode:
                // launch barcode activity.
                intent = new Intent(this, BarcodeCaptureActivity.class);
                intent.putExtra(BarcodeCaptureActivity.UseFlash, useFlash.isChecked());
                intent.putExtra(PARAM_ID, id.getText().toString());
                setState(STATE_SCAN);
                startActivityForResult(intent, RC_BARCODE_CAPTURE);
                break;

            case R.id.settings_btn:
                // launch activity.
                intent = new Intent(this, SettingsBarCodeActivity.class);
                setState(STATE_SlEEP);
                startActivity(intent);
                break;

            case R.id.clear:
                barcodeValue.setText("");
                barcodeType.setText("");
                scan.callOnClick();
                break;

            case R.id.send:
                send();
                break;

            case R.id.reload:
                flagReload = true;
                setState(STATE_START);
                break;

            case R.id.skip:
                if (!id.getText().toString().equals("")) {
                    dbService.skip(id.getText().toString(), settings.getString(PARAM_USER_NAME, ""),
                            responseListenerSkipCard, errorListener);
                }
                break;
        }
    }

    private void clearFields() {
        barcodeValue.setText("");
        barcodeType.setText("");
        id.setText("");
        webArticle.setText("");
        catalog.setText("");
        name.setText("");
        count.setText("");
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
                    }
                    Log.d(TAG, "Barcode read: " + barcode.displayValue);
                } else {
                    if (settings.contains(PARAM_NEED_RELOAD)) {
                        //вышли из режима сканирования сворачиванием, на сервер была попытка отправить отказ от работы, переходим в состояние Start
                        settingsEditor.remove(PARAM_NEED_RELOAD);
                        settingsEditor.apply();
                        setState(STATE_START);
                    } else {
                        //не стали сканировать, вернулись через кнопку back
                        //как будто не ходили сканировать
                        state = STATE_START;
                        Log.d(TAG, "No barcode captured, intent data is null");
                    }
                }
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Ошибка сканирования: " + CommonStatusCodes.getStatusCodeString(resultCode))
                        .setNeutralButton("Ок, обращусь к разработчику", null)
                        .create()
                        .show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    /**
     * Отправляет данные на сервер
     */
    private void send() {
        send(barcodeValue.getText().toString(), barcodeType.getText().toString());
    }

    private void send(String barcode, String codeType) {
        if (!barcode.equals("")) {
            progressBar.setVisibility(VISIBLE);
            scan.setVisibility(View.INVISIBLE);
            dbService.putRecord(
                    id.getText().toString(),
                    barcode,
                    codeType,
//                    deviceIMEI,
//                    phoneNumber == null ? "" : phoneNumber,
                    settings.getString(PARAM_USER_NAME, ""),
                    responseListenerPutRecord,
                    errorListener);
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Невеные данные для отправки")
                    .setNeutralButton("Ок, отсканирую повторно", null)
                    .create()
                    .show();
            setState(STATE_START);
        }
    }

    /**
     * Делает невидимыми все объекты, кроме кнопки обновить
     */
    private void hideAll() {
        autoSend.setVisibility(View.INVISIBLE);
        useFlash.setVisibility(View.INVISIBLE);
        scan.setVisibility(View.INVISIBLE);
        send.setVisibility(View.INVISIBLE);
        rescan.setVisibility(View.INVISIBLE);
        id.setVisibility(View.INVISIBLE);
        barcodeValue.setVisibility(View.INVISIBLE);
        barcodeType.setVisibility(View.INVISIBLE);
        skip.setVisibility(View.INVISIBLE);
        reload.setVisibility(View.VISIBLE);
    }

    /**
     * Прячет кнопку Обновить и показвыает остальные объекты, кроме кнопок отправить и повторить
     */
    private void showAll() {
        autoSend.setVisibility(View.VISIBLE);
        useFlash.setVisibility(View.VISIBLE);
        scan.setVisibility(View.VISIBLE);
        id.setVisibility(View.VISIBLE);
        barcodeValue.setVisibility(View.VISIBLE);
        barcodeType.setVisibility(View.VISIBLE);
        skip.setVisibility(View.VISIBLE);
        reload.setVisibility(View.INVISIBLE);
    }

    /**
     * получает имя пользователя и запоминает его в настройках
     */
    private void askUserName() {
        if (!settings.contains(PARAM_USER_NAME)) {
            //эта проверка нужна, чтобы можно было вызывать диалог из диалога (не успевает снять вьюшку из использования, а использовать одновременно дважды нельзя)
            if (viewDialog.getParent() != null) {
                ((ViewGroup) viewDialog.getParent()).removeView(viewDialog);
            }
            dialogTitle.setText(R.string.dialog_name);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            String name = dialogInput.getText().toString();
                            if (name.replaceAll(" ", "").length() == 0) {
                                Toast toast = Toast.makeText(getApplicationContext(),
                                        "Такое имя не подходит", Toast.LENGTH_LONG);
                                toast.show();
                                askUserName();
                            } else {
                                settingsEditor.putString(PARAM_USER_NAME, name)
                                        .apply();
                                dialog.cancel();
                                askServerIp();
                            }
                        }
                    })
                    .create().show();
        } else {
            state = STATE_SlEEP;
        }
    }

    private void askServerIp() {
        if (!settings.contains(PARAM_IP)) {
            //эта проверка нужна, чтобы можно было вызывать диалог из диалога (не успевает снять вьюшку из использования, а использовать одновременно дважды нельзя)
            if (viewDialog.getParent() != null) {
                ((ViewGroup) viewDialog.getParent()).removeView(viewDialog);
            }
            dialogTitle.setText(R.string.dialog_ip);
            dialogInput.setText(SERVER_DEFAULT_IP);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settingsEditor.putString(PARAM_IP, dialogInput.getText().toString())
                                    .apply();
                            settingsEditor.putString(PARAM_VNC_IP, dialogInput.getText().toString())
                                    .apply();
                            settingsEditor.putString(PARAM_VNC_PORT, VNC_DEFAULT_PORT)
                                    .apply();
                            settingsEditor.putString(PARAM_VNC_PASSWORD, VNC_DEFAULT_PASSWORD)
                                    .apply();
                            dialog.cancel();
                            askServerPort();
                        }
                    })
                    .create().show();
        }
    }

    private void askServerPort() {
        if (!settings.contains(PARAM_PORT)) {
            //эта проверка нужна, чтобы можно было вызывать диалог из диалога (не успевает снять вьюшку из использования, а использовать одновременно дважды нельзя)
            if (viewDialog.getParent() != null) {
                ((ViewGroup) viewDialog.getParent()).removeView(viewDialog);
            }
            dialogTitle.setText(R.string.dialog_port);
            dialogInput.setText(SERVER_DEFAULT_PORT);
            alertDialogBuilder
                    .setCancelable(false)
                    .setPositiveButton("Подтвердить", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            settingsEditor.putString(PARAM_PORT, dialogInput.getText().toString())
                                    .apply();
                            setState(STATE_START);
                        }
                    })
                    .create().show();
        }
    }

    /**
     * Проверяет ip адрес на маску
     */
    public static boolean ipCheck(String ip) {
        Pattern IP_PATTERN = Pattern.compile("^([1-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])(\\.([0-9]|[1-9][0-9]|1[0-9][0-9]|2[0-4][0-9]|25[0-5])){3}$");
        try {
            if (ip.equals("") || !IP_PATTERN.matcher(ip).matches()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    //получает номер телефона пользователя и IMEI для его идентификации в системе
//    @SuppressLint("HardwareIds")
    private void getPhoneInfo() {
//        TelephonyManager telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
//        //если прав нет, запрашиваем их
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
//            final String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE};
//            ActivityCompat.requestPermissions(this, permissions, READ_PHONE_STATE_PERM);
//            return;
//        } else {
//            deviceIMEI = telephonyManager.getDeviceId();
//            phoneNumber = telephonyManager.getLine1Number();
//        }
    }

    //отреагирует, когда будут предоставлены/не предоставлены какие-то права после их запроса
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode != READ_PHONE_STATE_PERM) {
            Log.d(TAG, "Got unexpected permission result: " + requestCode);
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);
            return;
        }

        if (grantResults.length != 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "Read phone state permission granted");
            getPhoneInfo();
            return;
        } else {
            AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
            builder.setMessage("Данный доступ нужен приложению, чтобы идентифицировать Вас, как автора отсканированных штрих-кодов")
                    .setCancelable(false)
                    .setPositiveButton("Ок, разрешу досутп", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String[] permissions = new String[]{Manifest.permission.READ_PHONE_STATE};
                            ActivityCompat.requestPermissions(MainActivity.this, permissions, READ_PHONE_STATE_PERM);
                        }
                    })
                    .create()
                    .show();
        }
    }

    //обрабатывает ошибку в json. Такая ошибка может возникать во время разработки, но проверку делать обязательно
    private void serverErrAnswer(JSONException e) {
        if (e != null) {
            e.printStackTrace();
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Соединение с сервером [" + settings.getString(PARAM_IP, "") + "] установлено. Сервер вернул что-то не то." +
                "1) Проверьте, работает ли сервер MySQL. 2) Проверьте корректность структуры БД.")
                .setNeutralButton("Ок, обращусь к разработчику", null)
                .create()
                .show();
    }

    //реакция на ситуацию, когда серверу скормили не те/ недостаточно параметров
    private void serverErrorParams(String error) {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage("Сервер не смог обработать команду, ответ сервера:" + error)
                .setNeutralButton("Ок, обращусь к разработчику", null)
                .create()
                .show();
    }

    //определяет по числовому эквиваленту типа штрихкода его строковое значение
    public static String decodeFormat(int format) {
        switch (format) {
            case Barcode.CODE_128:
                return "CODE_128";
            case Barcode.CODE_39:
                return "CODE_39";
            case Barcode.CODE_93:
                return "CODE_93";
            case Barcode.CODABAR:
                return "CODABAR";
            case Barcode.DATA_MATRIX:
                return "DATA_MATRIX";
            case Barcode.EAN_13:
                return "EAN_13";
            case Barcode.EAN_8:
                return "EAN_8";
            case Barcode.ITF:
                return "ITF";
            case Barcode.QR_CODE:
                return "QR_CODE";
            case Barcode.UPC_A:
                return "UPC_A";
            case Barcode.UPC_E:
                return "UPC_E";
            case Barcode.PDF417:
                return "PDF417";
            case Barcode.AZTEC:
                return "AZTEC";
            default:
                return "";
        }
    }

    //отключает прогресс бар через секунду после запуска, чтобы пользователь успел увидеть. что действие произошло
    class ProgressBarTask extends AsyncTask<Void, Void, Void> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressBar.setVisibility(VISIBLE);
        }

        @Override
        protected Void doInBackground(Void... voids) {
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            progressBar.setVisibility(View.INVISIBLE);
        }
    }
}
