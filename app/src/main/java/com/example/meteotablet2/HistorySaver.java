package com.example.meteotablet2;

public class HistorySaver {

    private String requestText;
    private String requestDate;

    public HistorySaver(String requestText, String requestDate) {
        this.requestText = requestText;
        this.requestDate = requestDate;
    }

    public String getRequestText() {
        return requestText;
    }

    public void setRequestText(String requestText) {
        this.requestText = requestText;
    }

    public String getRequestDate() {
        return requestDate;
    }

    public void setRequestDate(String requestDate) {
        this.requestDate = requestDate;
    }
}
