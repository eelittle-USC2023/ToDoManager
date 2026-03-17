package com.example.todomanager.model;

import jakarta.persistence.*;
import java.util.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;

@JsonIdentityInfo(
    generator = ObjectIdGenerators.PropertyGenerator.class,
    property = "id"
)
@Entity
@Table(name = "organizations")
public class Organization {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "uuid", length = 36, nullable = false)
    private UUID id;

    @Column(nullable = false, unique = true, length = 200)
    private String name;

    // owner: allow null because DB FK is ON DELETE SET NULL
    @ManyToOne(optional = true)
    @JoinColumn(name = "owner_id")
    private User owner;

    @OneToMany(mappedBy = "organization", cascade = CascadeType.ALL, orphanRemoval = true)
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