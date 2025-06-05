package lii.buildmaster.projecttracker.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lii.buildmaster.projecttracker.model.enums.ProjectStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "projects")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Project extends AuditableEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Project name is required")
    @Size(max = 100, message = "Project name must not exceed 100 characters")
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Size(max = 2000, message = "Description must not exceed 500 characters")
    @Column(name = "description", length = 2000)
    private String description;

    @NotNull(message = "Deadline is required")
    @Column(name = "deadline", nullable = false)
    private LocalDateTime deadline;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private ProjectStatus status;

    @OneToMany(mappedBy = "project", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @ToString.Exclude
    private List<Task> tasks = new ArrayList<>();

    public Project(String name, String description, LocalDateTime deadline, ProjectStatus status) {
        this.name = name;
        this.description = description;
        this.deadline = deadline;
        this.status = status;
    }

    public void addTask(Task task) {
        tasks.add(task);
        task.setProject(this);
    }

    public void removeTask(Task task) {
        tasks.remove(task);
        task.setProject(null);
    }
}
