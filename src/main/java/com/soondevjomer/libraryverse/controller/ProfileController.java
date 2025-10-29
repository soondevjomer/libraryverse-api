package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.dto.CheckRequestDto;
import com.soondevjomer.libraryverse.dto.ProfileDto;
import com.soondevjomer.libraryverse.service.ProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("profile")
@RequiredArgsConstructor
public class ProfileController {
    private final ProfileService profileService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> updateProfile(
            @RequestPart(value = "profile")ProfileDto profileDto,
            @RequestPart(value = "file", required = false)MultipartFile file
    ) {
        log.info("Update profile controller...");
        return ResponseEntity.ok(profileService.updateProfile(profileDto, file));
    }

    @PostMapping("/check-email")
    public ResponseEntity<?> emailExist(@RequestBody CheckRequestDto checkRequestDto) {
        log.info("email provided: {}", checkRequestDto.getRequest());
        log.info("current email provided: {}", checkRequestDto.getCurrent());
        return ResponseEntity.ok(Map.of("exist", profileService.emailExist(checkRequestDto)));
    }

    @PostMapping("/check-username")
    public ResponseEntity<?> usernameExist(@RequestBody CheckRequestDto checkRequestDto) {
        log.info("username provided: {}", checkRequestDto.getRequest());
        log.info("current username provided: {}", checkRequestDto.getCurrent());
        return ResponseEntity.ok(Map.of("exist", profileService.usernameExist(checkRequestDto)));
    }

    @GetMapping
    public ResponseEntity<?> getProfile() {
        log.info("Getting profile from controller");
        return ResponseEntity.ok(profileService.getProfile());
    }

}
