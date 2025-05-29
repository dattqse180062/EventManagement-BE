package swd392.eventmanagement.model.dto.request;

import lombok.Data;

@Data
public class RoleCapacityCreateRequest {
    private String roleName;
    private Integer maxCapacity;
}
