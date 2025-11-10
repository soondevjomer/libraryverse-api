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
        log.info("Starting uploadToCloudinary for folder: {}", folder);

        if (file == null) {
            log.error("File is null, cannot upload to Cloudinary");
            return UploadDto.builder().build();
        }

        log.info("File details - name: {}, size: {}, contentType: {}",
                file.getOriginalFilename(), file.getSize(), file.getContentType());

        try {
            if (file.getSize() == 0) {
                log.error("File size is 0, upload aborted.");
                return UploadDto.builder().build();
            }

            log.info("Uploading image bytes to Cloudinary folder: {}", folder);

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "format", "jpg",
                    "overwrite", true,
                    "unique_filename", true
            );

            log.info("Upload options: {}", uploadOptions);

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            if (uploadResult == null) {
                log.error("Upload result is null, Cloudinary did not return a response");
                return UploadDto.builder().build();
            }

            log.info("Upload completed, raw Cloudinary response: {}", uploadResult);

            String fileUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            log.info("Extracted URLs - secure_url: {}, public_id: {}", fileUrl, publicId);

            if (fileUrl == null || publicId == null) {
                log.error("Upload failed: missing secure_url or public_id in response");
                return UploadDto.builder().build();
            }

            String thumbnailUrl = cloudinary.url()
                    .transformation(new Transformation().width(300).height(450).crop("fill"))
                    .secure(true)
                    .generate(publicId + ".jpg");

            log.info("Generated thumbnail URL: {}", thumbnailUrl);

            log.info("Successfully uploaded to Cloudinary: {}", fileUrl);

            return UploadDto.builder()
                    .fileName(publicId)
                    .fileUrl(fileUrl)
                    .thumbnailFileUrl(thumbnailUrl)
                    .folderPath(folder)
                    .build();

        } catch (Exception e) {
            log.error("Exception during Cloudinary upload to folder {}: {}", folder, e.getMessage(), e);
            try {
                deleteImageFolder(folder, null);
            } catch (Exception cleanupError) {
                log.error("Cleanup after failed upload failed: {}", cleanupError.getMessage(), cleanupError);
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
