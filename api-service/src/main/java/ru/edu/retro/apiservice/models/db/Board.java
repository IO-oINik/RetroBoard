package ru.edu.retro.apiservice.models.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "Board")
@Data
public class Board {
    @Id
    @Column(name = "id", nullable = false, unique = true)
    private UUID id;

    @Column(name = "title", nullable = false, length = 255)
    private String title;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(name = "is_progress", nullable = false)
    private Boolean isProgress;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "ended_at")
    private LocalDateTime endedAt;

    @Column(name = "invite_edit_token", nullable = false)
    private UUID inviteEditToken;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "User_Board",
            joinColumns = @JoinColumn(name = "board_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> editors;
}
