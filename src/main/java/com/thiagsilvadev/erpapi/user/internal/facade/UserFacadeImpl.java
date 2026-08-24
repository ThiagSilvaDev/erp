package com.thiagsilvadev.erpapi.user.internal.facade;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.thiagsilvadev.erpapi.common.domain.exception.ResourceNotFoundException;
import com.thiagsilvadev.erpapi.common.domain.model.Contact;
import com.thiagsilvadev.erpapi.user.api.UserFacade;
import com.thiagsilvadev.erpapi.user.api.UserSnapshot;
import com.thiagsilvadev.erpapi.user.internal.mapper.UserMapper;
import com.thiagsilvadev.erpapi.user.internal.model.User;
import com.thiagsilvadev.erpapi.user.internal.repository.UserRepository;

@Service
public class UserFacadeImpl implements UserFacade {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserFacadeImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public UserSnapshot getUser(UUID userId) {
        return userRepository
                .findById(userId)
                .map(userMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, userId));
    }

    @Override
    @Transactional(readOnly = true)
    public UserSnapshot resolveUser(String email) {
        Contact contact = Contact.create(email, null);
        return userRepository
                .findByContactEmailValue(contact.getEmail())
                .map(userMapper::toSnapshot)
                .orElseThrow(() -> new ResourceNotFoundException(User.class, email));
    }

    @Override
    @Transactional(readOnly = true)
    public void requireUser(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException(User.class, userId);
        }
    }
}
