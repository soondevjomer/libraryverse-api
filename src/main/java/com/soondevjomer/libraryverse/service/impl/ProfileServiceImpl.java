package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.constant.Role;
import com.soondevjomer.libraryverse.dto.CheckRequestDto;
import com.soondevjomer.libraryverse.dto.ProfileDto;
import com.soondevjomer.libraryverse.dto.UploadDto;
import com.soondevjomer.libraryverse.model.Customer;
import com.soondevjomer.libraryverse.model.User;
import com.soondevjomer.libraryverse.repository.CustomerRepository;
import com.soondevjomer.libraryverse.repository.UserRepository;
import com.soondevjomer.libraryverse.service.ImageService;
import com.soondevjomer.libraryverse.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.NoSuchElementException;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {
    private final UserRepository userRepository;
    private final CustomerRepository customerRepository;
    private final ImageService imageService;

    @Transactional
    @Override
    public ProfileDto updateProfile(ProfileDto profileDto, MultipartFile file) {
        log.info("updating profile");
        log.info("file: {}", file);

        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new NoSuchElementException("User not found"));

        ProfileDto newProfileDto = new ProfileDto();

        if (currentUser.getRole().equals(Role.READER)) {
            log.info("Updating profile for reader");
            // Try updating reader customer address / contactnumber
            Optional<Customer> optionalCustomer = customerRepository.findByUser(currentUser);
            if (optionalCustomer.isPresent()) {
                optionalCustomer.get().setContactNumber(profileDto.getContactNumber());
                optionalCustomer.get().setAddress(profileDto.getAddress());
                Customer customer = customerRepository.save(optionalCustomer.get());
                log.info("User profile has customer and its updated");
                newProfileDto.setAddress(customer.getAddress());
                newProfileDto.setContactNumber(customer.getContactNumber());
            }
        }

        String newUserName = profileDto.getUsername();

        log.info("Try uploading user image");
        if (file != null && !file.isEmpty()) {
            String oldUserImageUrl = currentUser.getImage();
            log.info("old image Url {}", oldUserImageUrl);
            if (oldUserImageUrl != null && !oldUserImageUrl.isEmpty()) {
                imageService.deleteImageFile(oldUserImageUrl);
            }
            UploadDto upload = imageService.uploadProfileImage(
                    file,
                    newUserName,
                    currentUser.getId()
            );
            currentUser.setImage(upload.getFileUrl());
        }

        currentUser.setUsername(profileDto.getUsername());
        currentUser.setName(profileDto.getName());
        currentUser.setEmail(profileDto.getEmail());
        User savedUser = userRepository.save(currentUser);

        newProfileDto.setUsername(savedUser.getUsername());
        newProfileDto.setName(savedUser.getName());
        newProfileDto.setEmail(savedUser.getEmail());
        newProfileDto.setImage(savedUser.getImage());

        return newProfileDto;
    }

    @Override
    public boolean usernameExist(CheckRequestDto checkRequestDto) {
        String username = checkRequestDto.getRequest().trim();
        String currentUsername= checkRequestDto.getCurrent() != null ? checkRequestDto.getCurrent().trim() : "";

        if (!currentUsername.isEmpty()) {
            Optional<User> optionalUser = userRepository.findByUsername(currentUsername);
            if (optionalUser.isPresent()) {
                if (username.equalsIgnoreCase(optionalUser.get().getUsername())) {
                    log.info("Username is same as current so it's valid");
                    return false;
                }
            }
        }

        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExist(CheckRequestDto checkRequestDto) {
        String email = checkRequestDto.getRequest().trim();
        String currentEmail = checkRequestDto.getCurrent() != null ? checkRequestDto.getCurrent().trim() : "";

        if (!currentEmail.isEmpty()) {
            Optional<User> optionalUser = userRepository.findByEmail(currentEmail);
            if (optionalUser.isPresent()) {
                if (email.equalsIgnoreCase(optionalUser.get().getEmail())) {
                    log.info("Email is same as current so its valid");
                    return false;
                }
            }
        }


        return userRepository.existsByEmail(email);
    }

    @Override
    public ProfileDto getProfile() {
        log.info("Get current user profile");
        String username = SecurityContextHolder.getContext().getAuthentication().getName();

        Optional<User> optionalUser = userRepository.findByUsername(username);

        if (optionalUser.isEmpty()) {
            return ProfileDto.builder().build();
        }

        ProfileDto profileDto = new ProfileDto();

        User currentUser = optionalUser.get();
        log.info("Check current user role");

        if (currentUser.getRole().equals(Role.READER)) {
            log.info("Current user/reader check if its customer");
            Optional<Customer> optionalCustomer = customerRepository.findByUser(currentUser);
            optionalCustomer.ifPresent(customer -> {
                profileDto.setAddress(customer.getAddress());
                profileDto.setContactNumber(customer.getContactNumber());
            });
        }

        profileDto.setName(currentUser.getName());
        profileDto.setUsername(currentUser.getUsername());
        profileDto.setEmail(currentUser.getEmail());
        profileDto.setImage(currentUser.getImage());

        return profileDto;
    }

}
