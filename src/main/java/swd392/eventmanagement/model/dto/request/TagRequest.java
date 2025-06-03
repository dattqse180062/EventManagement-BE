package swd392.eventmanagement.model.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class TagRequest {

    @NotBlank(message = "Tên tag không được để trống")
    @Size(max = 100, message = "Tên tag tối đa 100 ký tự")
    private String name;

    private String description;
}
