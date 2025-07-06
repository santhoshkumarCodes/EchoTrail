package com.echotrail.capsulems.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class Capsule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long userId;
    private String title;

    @Column(columnDefinition = "TEXT")
    private String contentMarkdown;

    @Column(columnDefinition = "TEXT")
    private String contentHtml;

    private boolean isPublic = false;
    private boolean isUnlocked = false;

    private boolean isChained;

    private LocalDateTime unlockAt;
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

}