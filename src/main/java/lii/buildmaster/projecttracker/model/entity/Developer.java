package lii.buildmaster.projecttracker.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.domain.Auditable;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "developers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Developer extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Developer name is required")
    @Size(max = 100, message = "Name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    @Size(max = 150, message = "Email must not exceed 150 characters")
    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Size(max = 500, message = "Skills must not exceed 500 characters")
    @Column(name = "skills", length = 500)
    private String skills;

    @OneToMany(mappedBy = "developer", cascade = CascadeType.DETACH, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Task> assignedTasks = new ArrayList<>();

    public Developer(String name, String email, String skills) {
        this.name = name;
        this.email = email;
        this.skills = skills;
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
