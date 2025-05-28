package swd392.eventmanagement.model.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DepartmentResponse {
    private Long id;
    private String name;
    private String code;
    private String avatarUrl;
    private String bannerUrl;
    private String createdAt;
    private String updatedAt;
}
