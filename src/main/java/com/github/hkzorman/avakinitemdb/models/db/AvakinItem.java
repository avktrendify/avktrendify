package com.github.hkzorman.avakinitemdb.models.db;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.*;

import java.util.Objects;

@Entity
@Table(name="AvakinItem", indexes = {
        @Index(name = "idx_title", columnList = "title"),
        @Index(name = "idx_type", columnList = "type"),
        @Index(name = "idx_currency", columnList = "currency"),
        @Index(name = "idx_subType", columnList = "type,subType")
})
public class AvakinItem {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @JsonProperty("p_u")
    private String url;
    @JsonProperty("ti")
    private String title;
    @JsonProperty("title_es")
    private String titleEs;
    @JsonProperty("co")
    private double cost;
    @JsonProperty("ty")
    private String type;
    @JsonProperty("s_ty")
    private String subType;
    @JsonProperty("ca")
    private String currency;
    @JsonProperty("m_id")
    private String mId;
    @JsonProperty("p_na")
    private String pNa;
    @JsonProperty("in")
    private boolean inFlag;
    @JsonProperty("undefined")
    private boolean undefined;
    @JsonProperty("is_bu")
    private boolean isBuyable;
    @JsonProperty("s_da")
    private long stamp;
    @JsonProperty("it_id")
    private String itemId;
    @JsonProperty("is_ne")
    private boolean isNe;

    public AvakinItem() {

    }

    public AvakinItem(String itemId) {
        this.itemId = itemId;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String imgUrl) {
        this.url = imgUrl;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    public String getmId() {
        return mId;
    }

    public void setmId(String mId) {
        this.mId = mId;
    }

    public String getpNa() {
        return pNa;
    }

    public void setpNa(String pNa) {
        this.pNa = pNa;
    }

    public boolean isInFlag() {
        return inFlag;
    }

    public void setInFlag(boolean in) {
        this.inFlag = in;
    }

    public boolean isUndefined() {
        return undefined;
    }

    public void setUndefined(boolean undefined) {
        this.undefined = undefined;
    }

    public boolean isBuyable() {
        return isBuyable;
    }

    public void setBuyable(boolean buyable) {
        isBuyable = buyable;
    }

    public long getStamp() {
        return stamp;
    }

    public void setStamp(long timestamp) {
        this.stamp = timestamp;
    }

    public String getItemId() {
        return itemId;
    }

    public void setItemId(String itemId) {
        this.itemId = itemId;
    }

    public boolean isNe() {
        return isNe;
    }

    public void setNe(boolean ne) {
        isNe = ne;
    }

    public String getTitleEs() {
        return titleEs;
    }

    public void setTitleEs(String titleEs) {
        this.titleEs = titleEs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AvakinItem that = (AvakinItem) o;
        return Objects.equals(itemId, that.itemId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId);
    }
}
