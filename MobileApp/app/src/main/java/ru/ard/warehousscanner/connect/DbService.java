package ru.ard.warehousscanner.connect;

import android.content.Context;
import android.content.SharedPreferences;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.toolbox.Volley;

import java.util.Arrays;
import java.util.Collections;

import ru.ard.warehousscanner.MainActivity;

import static ru.ard.warehousscanner.MainActivity.APP_PREFERENCES;
import static ru.ard.warehousscanner.MainActivity.PARAM_IP;
import static ru.ard.warehousscanner.MainActivity.PARAM_PORT;


public class DbService {
    public static final String PARAM_GET_RECORD = "get_record";
    public static final String PARAM_PUT_RECORD_DATA = "put_record_data";
    public static final String PARAM_REFUSAL_OF_WORK = "refusal_of_work";
    public static final String PARAM_GET_CARDS_COUNT = "get_cards_count";
    public static final String PARAM_SKIP_CARD = "skip_card";

    public static final String PARAM_ID = "id";
    public static final String PARAM_BARCODE = "barcode";
    public static final String PARAM_CODE_TYPE = "codetype";
    public static final String PARAM_PASSWORD = "pass";
//    public static final String PARAM_IMEI = "imei";
//    public static final String PARAM_PHONE_NUMBER = "phone_number";
    public static final String PARAM_AUTHOR = "author";

    public static final String SUCCESS = "success";
    public static final String RECORDS_COUNT = "records_count";
    public static final String REASON = "reason";
    public static final String DATA = "data";

    private RequestQueue requestQueue;
    private SharedPreferences settings;

    public DbService(Context context) {
        requestQueue = Volley.newRequestQueue(context);
        settings = context.getSharedPreferences(APP_PREFERENCES, Context.MODE_PRIVATE);
    }

    //запросить с сервера
    public void getRecord(
            Response.Listener<String> responseListener, Response.ErrorListener errorListener
    ) {
        ServerRequest request = new ServerRequest(
                MainActivity.getServerApiUrl(settings.getString(PARAM_IP, ""), settings.getString(PARAM_PORT, "")),
                PARAM_GET_RECORD, null, null, responseListener, errorListener
        );
        requestQueue.add(request);
    }

    //отослать на сервер
    public void putRecord(String id, String barcode, String codeType,
//                          String IMEI, String phoneNumber,
                          String author,
                          Response.Listener<String> responseListener,
                          Response.ErrorListener errorListener
    ) {
        ServerRequest request = new ServerRequest(
                MainActivity.getServerApiUrl(settings.getString(PARAM_IP, ""), settings.getString(PARAM_PORT, "")),
                PARAM_PUT_RECORD_DATA,
                Arrays.asList(
                        PARAM_ID, PARAM_BARCODE, PARAM_CODE_TYPE,
//                        PARAM_IMEI, PARAM_PHONE_NUMBER
                        PARAM_AUTHOR
                ),
                Arrays.asList(
                        id, barcode, codeType,
//                        IMEI, phoneNumber
                        author
                ),
                responseListener,
                errorListener
        );
        requestQueue.add(request);
    }

    //Снять карточку с работы
    public void refuseWork(
            String id,
            Response.Listener<String> responseListener,
            Response.ErrorListener errorListener
    ) {
        ServerRequest request = new ServerRequest(
                MainActivity.getServerApiUrl(settings.getString(PARAM_IP, ""), settings.getString(PARAM_PORT, "")),
                PARAM_REFUSAL_OF_WORK,
                Collections.singletonList(PARAM_ID),
                Collections.singletonList(id),
                responseListener,
                errorListener
        );
        requestQueue.add(request);
    }

    //Запросить сколько карточек осталось
    public void getCardsCount (
            Response.Listener<String> responseListener,
            Response.ErrorListener errorListener
    ) {
        ServerRequest request = new ServerRequest(
                MainActivity.getServerApiUrl(settings.getString(PARAM_IP, ""), settings.getString(PARAM_PORT, "")),
                PARAM_GET_CARDS_COUNT,
                Collections.<String>emptyList(),
                Collections.<String>emptyList(),
                responseListener,
                errorListener
        );
        requestQueue.add(request);
    }

    //Пропустить карточку
    public void skip (
            String id, String author,
            Response.Listener<String> responseListener,
            Response.ErrorListener errorListener
    ) {
        ServerRequest request = new ServerRequest(
                MainActivity.getServerApiUrl(settings.getString(PARAM_IP, ""), settings.getString(PARAM_PORT, "")),
                PARAM_SKIP_CARD,
                Arrays.asList(PARAM_ID, PARAM_AUTHOR),
                Arrays.asList(id, author),
                responseListener,
                errorListener
        );
        requestQueue.add(request);
    }

    //Отправляет в буфер клавиатуры компа
    public void sendToKeyboard (
            String barCode, String author,
            Response.Listener<String> responseListener,
            Response.ErrorListener errorListener
    ) {
        ServerRequest request = new ServerRequest(
                MainActivity.getServerApiUrl(settings.getString(PARAM_IP, ""), settings.getString(PARAM_PORT, "")),
                "send",
                Arrays.asList(PARAM_BARCODE, PARAM_AUTHOR, PARAM_PASSWORD),
                Arrays.asList(barCode, author, "asjgflsdafgkl"),
                responseListener,
                errorListener
        );
        requestQueue.add(request);
    }
}
