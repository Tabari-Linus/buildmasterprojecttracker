package lii.buildmaster.projecttracker.model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;


import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "developers")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Developer extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Developer name is required")
    @Size(max = 200, message = "Name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 200)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 200, message = "Email must not exceed 150 characters")
    @Column(name = "email", nullable = false, unique = true, length = 200)
    private String email;

    @Size(max = 500, message = "Skills must not exceed 500 characters")
    @Column(name = "skills", length = 500)
    private String skills;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", unique = true)
    @ToString.Exclude
    @JsonBackReference
    private User user;


    @OneToMany(mappedBy = "developer", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties("developer")
    @Builder.Default
    @ToString.Exclude
    private List<Task> assignedTasks = new ArrayList<>();


    public Developer(String name, String email, String skills) {
        this.name = name;
        this.email = email;
        this.skills = skills;
    }

    public String getUsername() {
        return user != null ? user.getUsername() : null;
    }

    public boolean isOwnedBy(String username) {
        return user != null && user.getUsername().equals(username);
    }

    public Long getUserId() {
        return user != null ? user.getId() : null;
    }

    public void assignTask(Task task) {
        assignedTasks.add(task);
        task.setDeveloper(this);
    }

    public void unassignTask(Task task) {
        assignedTasks.remove(task);
        task.setDeveloper(null);
    }
}
