package com.ma7moud3ly.aione_terminal;

import java.util.HashMap;

public class ShellResponse implements ResponseHandler {

    @Override
    public void onSuccess(String result) {

    }

    @Override
    public void onError(String result) {

    }

    @Override
    public void onScript() {

    }

    @Override
    public void set(HashMap<String, String> set) {

    }
}

interface ResponseHandler {

    public void onSuccess(String result);

    public void onError(String result);

    public void onScript();

    public void set(HashMap<String, String> set);
}


