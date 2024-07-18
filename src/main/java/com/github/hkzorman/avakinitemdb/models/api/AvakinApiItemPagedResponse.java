package com.github.hkzorman.avakinitemdb.models.api;

public class AvakinApiItemPagedResponse {
    private int itemsCount;
    private int pagesCount;
    private AvakinApiItemSummary[] items;

    public int getItemsCount() {
        return itemsCount;
    }

    public void setItemsCount(int itemsCount) {
        this.itemsCount = itemsCount;
    }

    public int getPagesCount() {
        return pagesCount;
    }

    public void setPagesCount(int pagesCount) {
        this.pagesCount = pagesCount;
    }

    public AvakinApiItemSummary[] getItems() {
        return items;
    }

    public void setItems(AvakinApiItemSummary[] items) {
        this.items = items;
    }
}
