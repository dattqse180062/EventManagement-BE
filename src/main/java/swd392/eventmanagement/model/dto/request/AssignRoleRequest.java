package swd392.eventmanagement.model.dto.request;

import lombok.Data;

@Data
public class AssignRoleRequest {
    private Long userId;
    private Long departmentId;
    private Long departmentRoleId;

}
