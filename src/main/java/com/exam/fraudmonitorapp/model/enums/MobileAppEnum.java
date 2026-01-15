package com.exam.fraudmonitorapp.model.enums;

public enum MobileAppEnum {
    CARD_PRESENT("CARD_PRESENT"),
    CARD_NOT_PRESENT("CARD_NOT_PRESENT");
    private String mobileAppValue;
    MobileAppEnum(String mobileAppValue){ // Constructor takes an argument
        this.mobileAppValue = mobileAppValue;
    }
    // Getter method for the field
    public String getMobileAppValue() {
        return mobileAppValue;
    }
}
