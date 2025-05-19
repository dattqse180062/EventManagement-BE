package swd392.eventmanagement.service;

import swd392.eventmanagement.model.dto.response.UserDTO;
import swd392.eventmanagement.model.entity.User;

public interface UserService {
    UserDTO getCurrentUser();
    User getUserByEmail(String email);
} 