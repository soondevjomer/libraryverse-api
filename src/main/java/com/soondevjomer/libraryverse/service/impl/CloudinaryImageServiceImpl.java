package com.soondevjomer.libraryverse.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.soondevjomer.libraryverse.dto.UploadDto;
import com.soondevjomer.libraryverse.service.CloudinaryService;
import com.soondevjomer.libraryverse.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImageServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    private UploadDto uploadToCloudinary(MultipartFile file, String folder) {
        try {
            log.info("Uploading image to Cloudinary folder: {}", folder);

            // Upload original
            var uploadResult = cloudinary.uploader().upload(file.getBytes(), ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "format", "jpg",
                    "overwrite", true,
                    "unique_filename", true
            ));

            String fileUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            String thumbnailUrl = cloudinary.url()
                    .transformation(new Transformation().width(300).height(450).crop("fill"))
                    .secure(true)
                    .generate(publicId + ".jpg");

            log.info("Uploaded image to Cloudinary: {}", fileUrl);
            log.info("Generated thumbnail: {}", thumbnailUrl);

            return UploadDto.builder()
                    .fileName(publicId)
                    .fileUrl(fileUrl)
                    .thumbnailFileUrl(thumbnailUrl)
                    .folderPath(folder)
                    .build();

        } catch (Exception e) {
            log.error("Failed to upload image to Cloudinary folder {}: {}", folder, e.getMessage(), e);

            // Clean up folder if something went wrong
            try {
                deleteImageFolder(folder, null);
            } catch (Exception cleanupError) {
                log.error("Cleanup after failed Cloudinary upload failed: {}", cleanupError.getMessage());
            }
            return UploadDto.builder()
                    .fileName(null)
                    .folderPath(null)
                    .fileUrl(null)
                    .thumbnailFileUrl(null)
                    .build();
        }
    }

    @Override
    public UploadDto uploadBookCover(MultipartFile file, String bookTitle, Long bookId) {
        return uploadToCloudinary(file, "book-covers/" + bookId);
    }

    @Override
    public UploadDto uploadLibraryCover(MultipartFile file, String libraryName, Long libraryId) {
        return uploadToCloudinary(file, "library-covers/" + libraryId);
    }

    @Override
    public UploadDto uploadProfileImage(MultipartFile file, String username, Long userId) {
        return uploadToCloudinary(file, "profile-images/" + userId);
    }

    @Override
    public void deleteImageFolder(String category, Long ownerId) {

    }

    @Override
    public UploadDto copyImageFromExisting(String originalCoverUrl, String originalThumbUrl, String category, Long newOwnerId, String title) {
        return null;
    }

    @Override
    public void deleteImageFile(String imageUrl) {

    }
}
