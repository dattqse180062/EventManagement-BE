package swd392.eventmanagement.model.dto.response;

import lombok.Data;

@Data
public class AssignedUserResponseDTO {
    private Long userId;
    private String userName;
    private String roleName;
    private String email;
}