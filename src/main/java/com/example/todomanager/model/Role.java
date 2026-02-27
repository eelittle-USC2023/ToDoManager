package com.example.todomanager.model;

import jakarta.persistence.*;
import java.util.*;

@Entity
@Table(name = "roles")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id", columnDefinition = "char(36)")
    private Organization organization;

    @ManyToMany
    @JoinTable(name = "role_users",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "user_id", columnDefinition = "char(36)"))
    private Set<User> users = new HashSet<>();

    // work hours (string or structured) - store as a String for flexibility
    private String workHours;
    private String workDays; // as string representation
    private Double hoursPerWeek;

    @ManyToOne
    @JoinColumn(name = "supervisor_role_id")
    private Role supervisorRole;

    public Role() {}
    // getters/setters
    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public Organization getOrganization() { return organization; }
    public void setOrganization(Organization organization) { this.organization = organization; }
    public Set<User> getUsers() { return users; }
    public void setUsers(Set<User> users) { this.users = users;}
    public String getWorkHours() { return workHours; }
    public void setWorkHours(String workHours) { this.workHours = workHours; }
    public String getWorkDays() { return workDays; }
    public void setWorkDays(String workDays) { this.workDays = workDays; }
    public Double getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Double hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }
    public Role getSupervisorRole() { return supervisorRole; }
    public void setSupervisorRole(Role supervisorRole) { this.supervisorRole = supervisorRole; }
}