package com.obuspartners.modules.user_and_role_management.domain.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.obuspartners.modules.user_and_role_management.domain.enums.RoleType;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * Role entity for user permissions and access control
 * 
 * @author OBUS Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    @NotNull(message = "Role type is required")
    private RoleType roleType;

    @Column(name = "display_name", nullable = false)
    @NotBlank(message = "Display name is required")
    @Size(max = 100, message = "Display name must not exceed 100 characters")
    private String displayName;

    @Column(length = 500)
    @Size(max = 500, message = "Description must not exceed 500 characters")
    private String description;

    @Column(nullable = false)
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @ManyToMany(mappedBy = "roles")
    private Set<User> users = new HashSet<>();

    // Custom constructors
    public Role(RoleType roleType, String displayName, String description) {
        this.roleType = roleType;
        this.displayName = displayName;
        this.description = description;
        this.active = true;
        this.createdAt = LocalDateTime.now();
        this.users = new HashSet<>();
    }

    public Role(RoleType roleType, String displayName, String description, Boolean active) {
        this(roleType, displayName, description);
        this.active = active;
    }


    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Role{" +
                "id=" + id +
                ", roleType=" + roleType +
                ", displayName='" + displayName + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
