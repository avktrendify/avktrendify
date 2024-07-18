package com.github.hkzorman.avakinitemdb.models.ui;

import com.github.hkzorman.avakinitemdb.models.db.AvakinItem;

import java.util.List;

public class PagedResponse<T> {

    private int page;
    private int totalPages;
    private List<T> items;

    public PagedResponse(int page, int totalPages, List<T> items) {
        this.items = items;
        this.page = page;
        this.totalPages = totalPages;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalPages() {
        return totalPages;
    }

    public void setTotalPages(int totalPages) {
        this.totalPages = totalPages;
    }

    public List<T> getItems() {
        return items;
    }

    public void setItems(List<T> items) {
        this.items = items;
    }
}
