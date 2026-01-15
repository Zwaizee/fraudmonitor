package com.exam.fraudmonitorapp.model.enums;

public enum ChannelEnum {

    WEB("WEB"),
    ATM("ATM");
    private String channelValue;
    ChannelEnum(String channelValue){ // Constructor takes an argument
        this.channelValue = channelValue;
    }
    // Getter method for the field
    public String getChannelValue() {
        return channelValue;
    }
}



