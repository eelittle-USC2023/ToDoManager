package com.example.todomanager.model;

import jakarta.persistence.*;
import java.util.*;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "users")
public class User {


    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false)
    private UUID id;
    

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password;

    // bi-directional relationships
    @OneToMany(mappedBy = "owner", cascade = CascadeType.ALL)
    private Set<Organization> ownedOrganizations = new HashSet<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private Set<Task> tasks = new HashSet<>();

    @ManyToMany
    @JoinTable(name = "role_users",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id", columnDefinition = "binary(16)"))
    private Set<Role> roles = new HashSet<>();

    // constructors
    public User() {}
    public User(UUID id, String username, String password) {
        this.id = id;
        this.username = username;
        this.password = password;
    }
    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public Set<Organization> getOwnedOrganizations() { return ownedOrganizations; }
    public Set<Task> getTasks() { return tasks; }
    public Set<Role> getRoles() { return roles; }
}