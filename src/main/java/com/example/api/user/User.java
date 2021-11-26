package com.example.api.user;

import com.example.api.security.audit.Auditable;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.hibernate.Hibernate;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Enumerated;
import javax.persistence.Lob;
import javax.persistence.EnumType;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

/**
 * @author Oleg Turchin
 */

@Entity
@Getter @Setter @ToString
@RequiredArgsConstructor
@Table(name = "users",
        uniqueConstraints = {
                @UniqueConstraint(columnNames = "login"),
                @UniqueConstraint(columnNames = "email")
        })
public class User extends Auditable<String> {
    @Column(nullable = false)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequence-generator")
    @GenericGenerator(
            name = "sequence-generator",
            strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator",
            parameters = {
                    @org.hibernate.annotations.Parameter(name = "sequence_name", value = "users_sequence"),
                    @org.hibernate.annotations.Parameter(name = "initial_value", value = "1"),
                    @org.hibernate.annotations.Parameter(name = "increment_size", value = "1")
            }
    )
    private Long id;

    @NotBlank
    @Column(length = 125)
    private String login;

    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @NotBlank
    @Column(length = 255)
    private String password;

    @NotBlank
    private String fullName;

    @NotBlank
    @Email
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private UserLang lang;

    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private UserTheme theme;

    @NotNull
    private Boolean active = true;

    @NotNull()
    private long tokenExp = 86400000;

    @Lob
    private String url_avatar;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        User user = (User) o;
        return id != null && Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
