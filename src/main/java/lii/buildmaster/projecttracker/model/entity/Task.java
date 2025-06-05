package lii.buildmaster.projecttracker.model.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lii.buildmaster.projecttracker.model.enums.TaskStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tasks")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Task extends AuditableEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Task title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Size(max = 2000, message = "Description must not exceed 1000 characters")
    @Column(name = "description", length = 2000)
    private String description;

    @NotNull(message = "Status is required")
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TaskStatus status;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    @NotNull(message = "Project is required")
    private Project project;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "developer_id")
    private Developer developer;

    public Task(String title, String description, TaskStatus status, LocalDateTime dueDate, Project project, Developer developer) {
        this.title = title;
        this.description = description;
        this.status = status;
        this.dueDate = dueDate;
        this.project = project;
        this.developer = developer;
    }

    public Task(String title, String description, TaskStatus status, LocalDateTime dueDate, Project project) {
        this(title, description, status, dueDate, project, null);
    }
}