package swd392.eventmanagement.model.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class JwtResponse {
    private String token;
    private String refreshToken;
    private String type = "Bearer";
    private Long id;
    private String email;
    private String fullName;
    private Set<String> roles;
    private Set<UserDepartmentRoleDTO> userDepartmentRoles;

    public JwtResponse(String token, String refreshToken, Long id, String email, String fullName, Set<String> roles,
            Set<UserDepartmentRoleDTO> userDepartmentRoles) {
        this.token = token;
        this.refreshToken = refreshToken;
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.roles = roles;
        this.userDepartmentRoles = userDepartmentRoles;
    }
}