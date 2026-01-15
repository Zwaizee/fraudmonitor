package com.exam.fraudmonitorapp.model.enums;

public enum CurrencyEnum {

    ZAR("ZAR"),
    USD("USD"),
    EUR("EUR"),
    GBP("GBP");
    private String currencyValue;
    CurrencyEnum(String currencyValue){this.currencyValue = currencyValue;}
    public String getCurrencyValue(){ return currencyValue;}

}
