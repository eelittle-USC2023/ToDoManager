package com.example.todomanager.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @Column(name = "uuid", columnDefinition = "char(36)")
    private UUID id;

    @Column(nullable = false, unique = true)
    private String name;

    // owner
    @ManyToOne(optional = false)
    @JoinColumn(name = "owner_id", columnDefinition = "char(36)")
    private User owner;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL)
    private Set<Role> roles = new HashSet<>();

    public Organization() {}
    public Organization(UUID id, String name, User owner) {
        this.id = id;
        this.name = name;
        this.owner = owner;
    }
    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public User getOwner() { return owner; }
    public void setOwner(User owner) { this.owner = owner; }

    public Set<Role> getRoles() { return roles; }
}