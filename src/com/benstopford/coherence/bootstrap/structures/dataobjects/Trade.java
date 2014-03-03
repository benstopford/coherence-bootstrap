package com.benstopford.coherence.bootstrap.structures.dataobjects;

import java.io.Serializable;

public class Trade implements Serializable {
    long id;
    String system;
    String type;
    String book;
    String ccy;
    double amount;
    double rate;

    public Trade(long id, String system, String type, String book, String ccy, double amount, double rate) {
        this.id = id;
        this.system = system;
        this.type = type;
        this.book = book;
        this.ccy = ccy;
        this.amount = amount;
        this.rate = rate;
    }

    public Trade(long id, String system, String book) {
        this.id = id;
        this.system = system;
        this.book = book;
    }

    public long getId() {
        return id;
    }

    public String getSystem() {
        return system;
    }

    public String getType() {
        return type;
    }

    public String getBook() {
        return book;
    }

    public String getCcy() {
        return ccy;
    }

    public double getAmount() {
        return amount;
    }

    public double getRate() {
        return rate;
    }
}
