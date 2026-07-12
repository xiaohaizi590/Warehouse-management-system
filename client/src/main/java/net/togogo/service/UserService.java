package net.togogo.service;

import net.togogo.dto.LoginRequest;
import net.togogo.dto.LoginResponse;
import net.togogo.dto.RegisterRequest;
import net.togogo.dto.UpdateUserRequest;
import net.togogo.dto.UserDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    UserDTO register(RegisterRequest request);
    LoginResponse login(LoginRequest request);
    Page<UserDTO> getAllUsers(Pageable pageable);
    UserDTO getUserById(Long id);
    UserDTO getUserByUsername(String username);
    UserDTO getUserByPhone(String phone);
    void deleteUser(Long id);
    UserDTO updateUser(Long id, UpdateUserRequest request);
    boolean isOwnUser(Long userId);
}