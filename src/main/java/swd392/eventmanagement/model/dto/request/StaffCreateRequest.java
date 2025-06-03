package swd392.eventmanagement.model.dto.request;

import java.util.Set;

import lombok.Data;

@Data
public class StaffCreateRequest {
    private String email;
    private Set<String> roleName;
}
