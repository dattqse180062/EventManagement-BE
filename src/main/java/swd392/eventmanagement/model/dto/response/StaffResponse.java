package swd392.eventmanagement.model.dto.response;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Data;

@Data
public class StaffResponse {
    private String eventName;
    private String staffName;
    private String email;
    private Set<String> roleName;
    private LocalDateTime assignedAt;
    private LocalDateTime updatedAt;
}
