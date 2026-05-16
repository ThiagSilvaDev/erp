package com.thiagsilvadev.erpapi.user.internal.service;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.user.api.UserFacade;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Service
public class UserService implements UserFacade {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserService(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSnapshot> findById(UUID id) {
        return userRepository.findById(id).map(userMapper::toSnapshot);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<UserSnapshot> findByEmail(String email) {
        Contact contact = Contact.create(email, null);
        return userRepository.findByContactEmailValue(contact.getEmail()).map(userMapper::toSnapshot);
    }
}
