package swd392.eventmanagement.model.dto.request;

import lombok.Data;

@Data
public class DepartmentRequest {
    private String name;
    private String code;
    private String avatarUrl;
    private String bannerUrl;
}
