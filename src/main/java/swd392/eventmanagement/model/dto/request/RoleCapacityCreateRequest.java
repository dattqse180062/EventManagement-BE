package swd392.eventmanagement.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RoleCapacityCreateRequest {
    @NotBlank(message = "Role name cannot be empty when role capacity is provided")
    private String roleName;

    @NotNull(message = "Max capacity cannot be null when role capacity is provided")
    @Positive(message = "Max capacity must be greater than 0")
    private Integer maxCapacity;
}
