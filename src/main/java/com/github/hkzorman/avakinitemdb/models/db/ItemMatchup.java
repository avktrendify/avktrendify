package com.github.hkzorman.avakinitemdb.models.db;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.util.Set;

@Entity
public class ItemMatchup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JsonIgnore
    private ItemMatchupProposal proposal;

    @ManyToOne
    private AvakinItem item;

    @ManyToMany
    private Set<User> users;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public AvakinItem getItem() {
        return item;
    }

    public void setItem(AvakinItem item) {
        this.item = item;
    }

    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public ItemMatchupProposal getProposal() {
        return proposal;
    }

    public void setProposal(ItemMatchupProposal proposal) {
        this.proposal = proposal;
    }
}
