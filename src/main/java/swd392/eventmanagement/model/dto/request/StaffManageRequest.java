package swd392.eventmanagement.model.dto.request;

import java.util.Set;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class StaffManageRequest {
    @NotBlank(message = "Email must not be blank")
    @Email(message = "Invalid email format")
    private String email;

    @NotEmpty(message = "At least one role must be provided")
    private Set<@NotBlank(message = "Role name must not be blank") String> roleName;
}
