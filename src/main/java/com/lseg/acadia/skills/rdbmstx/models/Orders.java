package com.lseg.acadia.skills.rdbmstx.models;

import java.math.BigDecimal;

public class Orders {
    public long id;
    public String product;
    public BigDecimal cost;
    public long userId;

    public Orders() {

    }

    public Orders(String product, BigDecimal cost, long userId) {
        this.product = product;
        this.cost = cost;
        this.userId = userId;
    }
}
