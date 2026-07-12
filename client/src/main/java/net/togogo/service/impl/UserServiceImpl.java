package net.togogo.service.impl;

import lombok.RequiredArgsConstructor;
import net.togogo.common.BusinessException;
import net.togogo.common.ResultCode;
import net.togogo.dto.LoginRequest;
import net.togogo.dto.LoginResponse;
import net.togogo.dto.RegisterRequest;
import net.togogo.dto.UpdateUserRequest;
import net.togogo.dto.UserDTO;
import net.togogo.entity.User;
import net.togogo.repository.UserRepository;
import net.togogo.security.JwtUtil;
import net.togogo.service.UserService;
import net.togogo.util.PasswordUtil;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service("userServiceImpl")
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public UserDTO register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BusinessException(ResultCode.USERNAME_EXIST);
        }
        if (userRepository.existsByPhone(request.getPhone())) {
            throw new BusinessException(ResultCode.PHONE_EXIST);
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(PasswordUtil.encode(request.getPassword()))
                .phone(request.getPhone())
                .role(User.Role.USER)
                .build();

        User savedUser = userRepository.save(user);
        return convertToDTO(savedUser);
    }

    @Override
    @Transactional
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getAccount())
                .orElseThrow(() -> new BusinessException(ResultCode.USER_NOT_FOUND));

        if (!PasswordUtil.matches(request.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PASSWORD_ERROR);
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());
        return LoginResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phone(user.getPhone())
                .token(token)
                .role(user.getRole().name())
                .build();
    }

    @Override
    public Page<UserDTO> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable).map(this::convertToDTO);
    }

    @Override
    public UserDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    public UserDTO getUserByPhone(String phone) {
        User user = userRepository.findByPhone(phone)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));
        return convertToDTO(user);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new BusinessException(ResultCode.NOT_FOUND);
        }
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public UserDTO updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ResultCode.NOT_FOUND));

        if (request.getUsername() != null) {
            user.setUsername(request.getUsername());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getRole() != null) {
            user.setRole(User.Role.valueOf(request.getRole()));
        }

        User updatedUser = userRepository.save(user);
        return convertToDTO(updatedUser);
    }

    @Override
    public boolean isOwnUser(Long userId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) {
            return false;
        }
        
        Object principal = auth.getPrincipal();
        if (!(principal instanceof UserDetails)) {
            return false;
        }
        
        String username = ((UserDetails) principal).getUsername();
        return userRepository.findById(userId)
                .map(user -> user.getUsername().equals(username))
                .orElse(false);
    }

    private UserDTO convertToDTO(User user) {
        return UserDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .phoneNumber(user.getPhone())
                .createTime(user.getCreateTime())
                .role(user.getRole())
                .build();
    }
}