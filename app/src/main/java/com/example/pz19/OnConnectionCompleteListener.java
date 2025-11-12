package com.example.pz19;

import org.json.JSONObject;

public interface OnConnectionCompleteListener {
    void onSuccess(JSONObject response);
    void onFail(String message);
}