package ru.ard.warehousscanner.connect;

import com.android.volley.Response;
import com.android.volley.toolbox.StringRequest;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerRequest extends StringRequest {
    private Map<String, String> params;

    public ServerRequest(String url, String action, List<String> keys, List<String> values, Response.Listener<String> listener, Response.ErrorListener errorListener) {
        super(Method.POST, url, listener, errorListener);
        params = new HashMap<>();
        params.put("action",action);
        if (keys !=null && values!= null)
            for (int i = 0; i <keys.size() ; i++) {
                params.put(keys.get(i),values.get(i));
            }
    }
    @Override
    public Map<String, String> getParams() {
        return params;
    }
}