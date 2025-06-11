package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.response.UserDTO;
import swd392.eventmanagement.model.entity.User;

import java.util.List;

public interface UserService {
    UserDTO getCurrentUser();
    User getUserByEmail(String email);

    List<UserDTO> getUsersNotInDepartment();
} 