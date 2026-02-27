package com.example.todomanager.model;

import jakarta.persistence.*;
import java.util.UUID;

import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

@Entity
@Table(name = "task_folders",
       uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "title"}))
public class TaskFolder {

    @Id
    @JdbcTypeCode(SqlTypes.CHAR)
    @Column(name = "id", length = 36)
    private UUID id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "user_id", columnDefinition = "char(36)")
    private User user;

    @Column(nullable = false)
    private String title;

    @Column(length = 1000)
    private String note;

    public TaskFolder() {}

    public TaskFolder(UUID id, User user, String title, String note) {
        this.id = id;
        this.user = user;
        this.title = title;
        this.note = note;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}