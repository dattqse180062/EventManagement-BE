package swd392.eventmanagement.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LocationCreateRequest {
    @NotBlank(message = "Address cannot be empty when location is provided")
    @Size(max = 255, message = "Address cannot exceed 255 characters")
    private String address;

    @NotBlank(message = "Ward cannot be empty when location is provided")
    @Size(max = 100, message = "Ward cannot exceed 100 characters")
    private String ward;

    @NotBlank(message = "District cannot be empty when location is provided")
    @Size(max = 100, message = "District cannot exceed 100 characters")
    private String district;

    @NotBlank(message = "City cannot be empty when location is provided")
    @Size(max = 100, message = "City cannot exceed 100 characters")
    private String city;
}
