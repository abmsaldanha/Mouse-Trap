package com.mousetrapapplication;

public class TrapStatus {
    private int statusImage;
    private String databaseReference;
    private int cageImage;
    private String name;
    private String date;
    private String status;


    public TrapStatus() {}

    public TrapStatus(int statusImage, String date, String status, String name) {
        this.statusImage = statusImage;
        this.date = date;
        this.status = status;
        this.name = name;
    }


    // SETTERS

    public void setStatusImage(int statusImage) {
        this.statusImage = statusImage;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDatabaseReference(String reference) { this.databaseReference = reference; }

    public void setCageImage(int cageImage) { this.cageImage = cageImage; }


    // GETTERS

    public String getDate() {
        return date;
    }

    public String getStatus(){
        return status;
    }

    public int getStatusImage() { return statusImage; }

    public String getName() {
        return name;
    }

    public String getDatabaseReference() { return databaseReference; }

    public int getCageImage() { return cageImage; }
}
