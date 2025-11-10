package com.soondevjomer.libraryverse.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.Transformation;
import com.cloudinary.utils.ObjectUtils;
import com.soondevjomer.libraryverse.dto.UploadDto;
import com.soondevjomer.libraryverse.service.CloudinaryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryImageServiceImpl implements CloudinaryService {

    private final Cloudinary cloudinary;

    /**
     * Core method to upload a file to Cloudinary.
     * Handles thumbnails and cleans up on failure.
     */
    private UploadDto uploadToCloudinary(MultipartFile file, String folder) {
        log.info("Starting uploadToCloudinary for folder: {}", folder);

        if (file == null) {
            log.error("File is null, cannot upload to Cloudinary");
            return UploadDto.builder().build();
        }

        try {
            if (file.getSize() == 0) {
                log.error("File size is 0, upload aborted.");
                return UploadDto.builder().build();
            }

            Map<String, Object> uploadOptions = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", "image",
                    "format", "jpg",
                    "overwrite", true,
                    "unique_filename", true
            );

            var uploadResult = cloudinary.uploader().upload(file.getBytes(), uploadOptions);

            if (uploadResult == null) {
                log.error("Upload result is null, Cloudinary did not return a response");
                return UploadDto.builder().build();
            }

            String fileUrl = (String) uploadResult.get("secure_url");
            String publicId = (String) uploadResult.get("public_id");

            if (fileUrl == null || publicId == null) {
                log.error("Upload failed: missing secure_url or public_id in response");
                return UploadDto.builder().build();
            }

            // Generate a consistent thumbnail version
            String thumbnailUrl = cloudinary.url()
                    .transformation(new Transformation().width(300).height(450).crop("fill"))
                    .secure(true)
                    .generate(publicId + ".jpg");

            log.info("Successfully uploaded image to Cloudinary folder: {}", folder);

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
            return UploadDto.builder().build();
        }
    }

    /**
     * Uploads book cover into "book-covers/{bookId}" folder.
     */
    @Override
    public UploadDto uploadBookCover(MultipartFile file, String bookTitle, Long bookId) {
        return uploadToCloudinary(file, "book-covers/" + bookId);
    }

    /**
     * Uploads library cover into "library-covers/{libraryId}" folder.
     */
    @Override
    public UploadDto uploadLibraryCover(MultipartFile file, String libraryName, Long libraryId) {
        return uploadToCloudinary(file, "library-covers/" + libraryId);
    }

    /**
     * Uploads profile image into "profile-images/{userId}" folder.
     */
    @Override
    public UploadDto uploadProfileImage(MultipartFile file, String username, Long userId) {
        return uploadToCloudinary(file, "profile-images/" + userId);
    }

    /**
     * Deletes all images in a Cloudinary folder (e.g., for book/library/profile cleanup).
     */
    @Override
    public void deleteImageFolder(String category, Long ownerId) {
        try {
            String folderPath = (ownerId != null)
                    ? category + "/" + ownerId
                    : category;

            log.info("Deleting all images from Cloudinary folder: {}", folderPath);

            Map<String, Object> deleteOptions = ObjectUtils.asMap("folder", folderPath, "invalidate", true);
            cloudinary.api().deleteResourcesByPrefix(folderPath, deleteOptions);

            log.info("Successfully deleted Cloudinary folder: {}", folderPath);
        } catch (Exception e) {
            log.error("Error deleting Cloudinary folder {}: {}", category, e.getMessage(), e);
        }
    }

    /**
     * Copies existing Cloudinary image URLs (book cover + thumbnail)
     * to a new folder for a new book.
     * This is used when duplicating an existing book across libraries.
     */
    @Override
    public UploadDto copyImageFromExisting(
            String originalCoverUrl,
            String originalThumbUrl,
            String category,
            Long newOwnerId,
            String title
    ) {
        log.info("Copying existing image into new folder: {}/{}", category, newOwnerId);

        try {
            // Open stream from original Cloudinary URL
            URL sourceUrl = new URL(originalCoverUrl);
            try (InputStream inputStream = sourceUrl.openStream()) {

                byte[] fileBytes = inputStream.readAllBytes();

                Map<String, Object> uploadOptions = ObjectUtils.asMap(
                        "folder", category + "/" + newOwnerId,
                        "resource_type", "image",
                        "format", "jpg",
                        "overwrite", true,
                        "unique_filename", true
                );

                log.info("Uploading copied image to Cloudinary folder: {}/{}", category, newOwnerId);

                var uploadResult = cloudinary.uploader().upload(fileBytes, uploadOptions);

                if (uploadResult == null) {
                    log.error("Cloudinary did not return a response when copying image");
                    return UploadDto.builder().build();
                }

                String fileUrl = (String) uploadResult.get("secure_url");
                String publicId = (String) uploadResult.get("public_id");

                if (fileUrl == null || publicId == null) {
                    log.error("Copy failed: missing secure_url or public_id");
                    return UploadDto.builder().build();
                }

                String thumbnailUrl = cloudinary.url()
                        .transformation(new Transformation().width(300).height(450).crop("fill"))
                        .secure(true)
                        .generate(publicId + ".jpg");

                log.info("Successfully copied image to new folder: {}", fileUrl);

                return UploadDto.builder()
                        .fileName(publicId)
                        .fileUrl(fileUrl)
                        .thumbnailFileUrl(thumbnailUrl)
                        .folderPath(category + "/" + newOwnerId)
                        .build();
            }
        } catch (Exception e) {
            log.error("Failed to copy image from existing URL: {}", e.getMessage(), e);
            return UploadDto.builder().build();
        }
    }

    /**
     * Deletes a single image file from Cloudinary using its full secure_url.
     */
    @Override
    public void deleteImageFile(String imageUrl) {
        try {
            if (imageUrl == null || imageUrl.isEmpty()) {
                log.warn("Image URL is empty, skipping deletion.");
                return;
            }

            // Extract public ID from Cloudinary URL
            // Example: https://res.cloudinary.com/demo/image/upload/v1234567890/book-covers/45/sample.jpg
            String publicId = imageUrl
                    .replaceAll("^.+?/upload/(v[0-9]+/)?", "")  // remove base + version
                    .replace(".jpg", "")
                    .replace(".png", "")
                    .replace(".jpeg", "");

            log.info("Deleting single Cloudinary image by public_id: {}", publicId);

            cloudinary.uploader().destroy(publicId, ObjectUtils.asMap("invalidate", true));

            log.info("Successfully deleted Cloudinary image: {}", imageUrl);
        } catch (Exception e) {
            log.error("Error deleting image {}: {}", imageUrl, e.getMessage(), e);
        }
    }
}
