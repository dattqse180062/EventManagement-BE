package swd392.eventmanagement.model.dto.response;

import lombok.Data;

@Data
public class UnassignedUserResponseDTO {
    private Long userId;
    private String userName;
    private String email;
}
