package com.codeplatform.service;

import com.codeplatform.common.Mapper;
import com.codeplatform.dto.UserResponse;
import com.codeplatform.entity.User;
import com.codeplatform.exception.ResourceNotFoundException;
import com.codeplatform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final Mapper mapper;

    public UserResponse getById(Long id) {
        return mapper.toUserResponse(userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id)));
    }

    public UserResponse getByUsername(String username) {
        return mapper.toUserResponse(userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username)));
    }

    @Transactional
    public UserResponse updateProfile(Long id, String bio, String avatarUrl) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + id));
        user.setBio(bio);
        user.setAvatarUrl(avatarUrl);
        return mapper.toUserResponse(userRepository.save(user));
    }
}
