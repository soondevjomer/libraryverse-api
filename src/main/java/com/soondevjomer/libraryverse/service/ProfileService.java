package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.CheckRequestDto;
import com.soondevjomer.libraryverse.dto.ProfileDto;
import org.springframework.web.multipart.MultipartFile;

public interface ProfileService {

    ProfileDto updateProfile(ProfileDto profileDto, MultipartFile file);

    boolean usernameExist(CheckRequestDto checkRequestDto);

    boolean emailExist(CheckRequestDto checkRequestDto);

    ProfileDto getProfile();
}
