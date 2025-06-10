package ru.edu.retro.apiservice.models.db;

import jakarta.persistence.Column;
import jakarta.persistence.ConstraintMode;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "_user")
@Data
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "nickname", nullable = false, length = 255)
    private String nickname;

    @Column(name = "login", nullable = false, unique = true, length = 255)
    private String login;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "refresh_token_id", referencedColumnName = "id", foreignKey = @ForeignKey(ConstraintMode.CONSTRAINT))
    private RefreshToken token;
}
