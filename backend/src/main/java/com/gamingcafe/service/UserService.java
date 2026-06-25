package com.gamingcafe.service;

import com.gamingcafe.entity.Role;
import com.gamingcafe.entity.User;
import com.gamingcafe.exception.ResourceNotFoundException;
import com.gamingcafe.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    public User getById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
    }

    public Page<User> listStaff(Pageable pageable) {
        return userRepository.findByRole(Role.STAFF, pageable);
    }

    public Page<User> listCustomers(Pageable pageable) {
        return userRepository.findByRole(Role.CUSTOMER, pageable);
    }

    @Transactional
    public User toggleEnabled(Long userId, boolean enabled) {
        User user = getById(userId);
        user.setEnabled(enabled);
        return userRepository.save(user);
    }
}
