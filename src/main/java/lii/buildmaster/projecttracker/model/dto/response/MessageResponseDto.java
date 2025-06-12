package lii.buildmaster.projecttracker.model.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponseDto {

    private String message;
    private boolean success;

    public static MessageResponseDto success(String message) {
        return MessageResponseDto.builder()
                .message(message)
                .success(true)
                .build();
    }

    public static MessageResponseDto error(String message) {
        return MessageResponseDto.builder()
                .message(message)
                .success(false)
                .build();
    }
}
