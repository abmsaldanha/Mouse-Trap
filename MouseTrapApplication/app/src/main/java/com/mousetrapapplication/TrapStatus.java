package com.mousetrapapplication;

import java.time.LocalDate;

public class TrapStatus {
    int image;
    LocalDate date;
    String status;

    public TrapStatus() {}

    public TrapStatus(int image, LocalDate date, String status) {
        this.image = image;
        this.date = date;
        this.status = status;
    }

    public void setImage(int image) {
        this.image = image;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDate getDate() {
        return date;
    }

    public String getStatus(){
        return status;
    }

    public int getImage() { return image; }
}
