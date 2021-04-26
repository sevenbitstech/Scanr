package com.pancard.android.model;

/**
 * Created by seven-bits-pc11 on 24/5/17.
 */
public class SecurityStatus {

    public boolean status;
    public String pin;

    public SecurityStatus() {

    }

    public SecurityStatus(boolean status, String pin) {
        this.status = status;
        this.pin = pin;

    }

    public boolean isStatus() {
        return status;
    }

    public void setStatus(boolean status) {
        this.status = status;
    }

    public String getPin() {
        return pin;
    }

    public void setPin(String pin) {
        this.pin = pin;
    }
}
