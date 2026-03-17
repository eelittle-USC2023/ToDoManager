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
@Table(name = "roles")
public class Role {
    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36, nullable = false)
    private UUID id;

    @Column(nullable = false, length = 200)
    private String title;

    @ManyToOne(optional = false)
    @JoinColumn(name = "organization_id")
    private Organization organization;

    @ManyToMany
    @JoinTable(
        name = "role_users",
        joinColumns = @JoinColumn(
            name = "role_id",
            referencedColumnName = "id"            // column name in roles table
        ),
        inverseJoinColumns = @JoinColumn(
            name = "user_id",
            referencedColumnName = "id"            // column name in users table
        )
    )
    private Set<User> users = new HashSet<>();

    // work hours (string or structured) - store as a String for flexibility
    @Column(name = "work_hours", length = 100)
    private String workHours;

    @Column(name = "work_days", length = 15)
    private String workDays; // as string representation

    @Column(name = "hours_per_week")
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
    public void setUsers(Set<User> users) { this.users = users; }

    public String getWorkHours() { return workHours; }
    public void setWorkHours(String workHours) { this.workHours = workHours; }

    public String getWorkDays() { return workDays; }
    public void setWorkDays(String workDays) { this.workDays = workDays; }

    public Double getHoursPerWeek() { return hoursPerWeek; }
    public void setHoursPerWeek(Double hoursPerWeek) { this.hoursPerWeek = hoursPerWeek; }

    public Role getSupervisorRole() { return supervisorRole; }
    public void setSupervisorRole(Role supervisorRole) { this.supervisorRole = supervisorRole; }
}