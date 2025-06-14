package lii.buildmaster.projecttracker.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeveloperResponseDto {

    private Long id;
    private String name;
    private String email;
    private String skills;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime updatedAt;

    private long totalTaskCount;
    private long activeTaskCount;
    private long completedTaskCount;

    public DeveloperResponseDto(Long id, @NotBlank(message = "Developer name is required") @Size(max = 200, message = "Name must not exceed 100 characters") String name, @NotBlank(message = "Email is required") @Email(message = "Email should be valid") @Size(max = 200, message = "Email must not exceed 150 characters") String email, @Size(max = 500, message = "Skills must not exceed 500 characters") String skills) {

    }
}
