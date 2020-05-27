package ru.ard.warehousscanner.listeners;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;

import com.android.volley.Response;

import org.json.JSONException;
import org.json.JSONObject;

import static ru.ard.warehousscanner.connect.DbService.PARAM_ID;
import static ru.ard.warehousscanner.connect.DbService.SUCCESS;

//удаляет из настроек запись о том, что нужно совершить отказ, если принял положительный ответ сервера
public class RefusalWorkResponseListener implements Response.Listener<String> {

    private SharedPreferences settings;
    private SharedPreferences.Editor settingsEditor;

    @SuppressLint("CommitPrefEdits")
    public RefusalWorkResponseListener(SharedPreferences settings) {
        this.settings = settings;
        settingsEditor = settings.edit();
    }

    @Override
    public void onResponse(String response) {
        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getBoolean(SUCCESS);

            if (success) {
                settingsEditor.remove(PARAM_ID);
                settingsEditor.apply();
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
