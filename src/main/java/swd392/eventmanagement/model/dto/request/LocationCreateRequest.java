package swd392.eventmanagement.model.dto.request;

import lombok.Data;

@Data
public class LocationCreateRequest {
    private String address;
    private String ward;
    private String district;
    private String city;
}
