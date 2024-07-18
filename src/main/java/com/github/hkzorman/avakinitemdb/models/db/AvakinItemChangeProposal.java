package com.github.hkzorman.avakinitemdb.models.db;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
public class AvakinItemChangeProposal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;
    @ManyToOne
    private AvakinItem item;
    private String newTitle;
    private boolean isActive;
    @Column(nullable = true)
    private Boolean isApproved;
    @ManyToOne
    private User createdBy;
    private LocalDateTime createdOn;
    @ManyToOne
    private User actionedBy;
    private LocalDateTime actionedOn;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public AvakinItem getItem() {
        return item;
    }

    public void setItem(AvakinItem item) {
        this.item = item;
    }

    public String getNewTitle() {
        return newTitle;
    }

    public void setNewTitle(String newTitle) {
        this.newTitle = newTitle;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public Boolean isApproved() {
        return isApproved;
    }

    public void setApproved(Boolean approved) {
        isApproved = approved;
    }

    public User getActionedBy() {
        return actionedBy;
    }

    public void setActionedBy(User actionedBy) {
        this.actionedBy = actionedBy;
    }

    public LocalDateTime getActionedOn() {
        return actionedOn;
    }

    public void setActionedOn(LocalDateTime actionedOn) {
        this.actionedOn = actionedOn;
    }
}
