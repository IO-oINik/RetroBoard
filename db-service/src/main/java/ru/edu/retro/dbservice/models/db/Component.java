package ru.edu.retro.dbservice.models.db;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Data;

import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "Component")
@Data
public class Component {
    @Id
    private UUID id;

    @Column(nullable = false, columnDefinition = "text")
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ComponentType type;

    @Column(nullable = false)
    private float x;

    @Column(nullable = false)
    private float y;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "board_id")
    private Board board;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private User author;

    @Column(nullable = false)
    private Boolean isAnonymousAuthor;

    @Column(nullable = false)
    private Boolean isAnonymousVotes;

    @OneToMany(mappedBy = "component")
    private List<Vote> votes;
}
