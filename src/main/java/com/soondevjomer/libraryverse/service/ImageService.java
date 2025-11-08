package com.soondevjomer.libraryverse.service;

import com.soondevjomer.libraryverse.dto.UploadDto;
import org.springframework.web.multipart.MultipartFile;

public interface ImageService {

    UploadDto uploadBookCover(MultipartFile file, String bookTitle, Long libraryId);

    UploadDto uploadLibraryCover(MultipartFile file, String libraryName, Long libraryId);

    UploadDto uploadProfileImage(MultipartFile file, String username, Long userId);

    void deleteImageFolder(String category, Long ownerId);

    UploadDto copyImageFromExisting(
            String originalCoverUrl,
            String originalThumbUrl,
            String category,
            Long newOwnerId,
            String title
    );

    void deleteImageFile(String imageUrl);

}
