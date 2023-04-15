package com.cambotutorial.sovary.qrscanner.Objects;

public class Magazine {

    private String  id;
    private String QR;
    private String name;
    private String date;

    public Magazine( String id, String QR, String name, String date) {
        this.id = id;
        this.QR = QR;
        this.name = name;
        this.date = date;
    }

    public Magazine() {
    }

    public String getQR() {
        return QR;
    }

    public void setQR(String QR) {
        this.QR = QR;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
