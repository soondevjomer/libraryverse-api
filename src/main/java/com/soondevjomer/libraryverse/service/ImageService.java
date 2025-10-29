package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.UploadDto;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    UploadDto uploadBookCover(MultipartFile file, String bookTitle, Long libraryId);

    UploadDto uploadLibraryCover(MultipartFile file, String libraryName, Long libraryId);

    UploadDto uploadProfileImage(MultipartFile file, String username, Long userId);

    String saveTempBookCover(MultipartFile file, Long libraryId);

    void deleteImageFile(String imageUrl);
}
