package com.exam.fraudmonitorapp.model.enums;

public enum CategoryEnum {

    PURCHASE("PURCHASE"),
    TRANSFER("TRANSFER"),
    ATM("ATM"),
    ONLINE("ONLINE"),
    INTERNATIONAL("INTERNATIONAL"),
    BILL_PAYMENT("BILL_PAYMENT");
    private String categoryValue;
    CategoryEnum(String categoryValue){ // Constructor takes an argument
        this.categoryValue = categoryValue;
    }
    // Getter method for the field
    public String getCategoryValue() {
        return categoryValue;
    }
}
