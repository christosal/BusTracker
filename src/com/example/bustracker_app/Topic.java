package com.example.bustracker_app;

import java.io.Serializable;

public class Topic implements Serializable {
    private String busLine;
    public static final long serialVersionUID = 22149313046710534L;

    public String getBusLine() {
        return busLine;
    }

    public Topic(String busLine) {
        this.busLine = busLine;
    }
}
