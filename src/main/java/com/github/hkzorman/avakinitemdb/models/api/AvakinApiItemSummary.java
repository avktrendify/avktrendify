package com.github.hkzorman.avakinitemdb.models.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public class AvakinApiItemSummary {
    @JsonProperty("co")
    private double cost;
    @JsonProperty("p_u")
    private String imgUrl;
    @JsonProperty("ca")
    private String currency;
    @JsonProperty("it_id")
    private String itemId;
    @JsonProperty("s_da")
    private long timestamp;

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getImgUrl() {
        return imgUrl;
    }

    public void setImgUrl(String imgUrl) {
        this.imgUrl = imgUrl;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
