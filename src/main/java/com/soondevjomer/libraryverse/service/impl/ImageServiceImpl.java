package com.soondevjomer.libraryverse.service.impl;

import com.soondevjomer.libraryverse.dto.UploadDto;
import com.soondevjomer.libraryverse.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.stream.FileImageOutputStream;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.*;
import java.util.Comparator;
import java.util.Iterator;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageServiceImpl implements ImageService {

    private static final String BOOK_COVER_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/book-covers/";
    private static final String LIBRARY_COVER_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/library-covers/";
    private static final String PROFILE_IMAGE_UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/profile-images/";

    @Override
    public UploadDto uploadBookCover(MultipartFile file, String bookTitle, Long bookId) {
        log.info("Uploading book cover");
        return uploadImage(
                file,
                bookTitle,
                bookId,
                BOOK_COVER_UPLOAD_DIR + bookId + "/",
                "/files/book-covers/" + bookId + "/",
                300, 450
        );
    }

    @Override
    public UploadDto uploadLibraryCover(MultipartFile file, String libraryName, Long libraryId) {
        log.info("Uploading library cover");
        return uploadImage(
                file,
                libraryName,
                libraryId,
                LIBRARY_COVER_UPLOAD_DIR + libraryId + "/",
                "/files/library-covers/" + libraryId + "/",
                800, 400
        );
    }

    @Override
    public UploadDto uploadProfileImage(MultipartFile file, String username, Long userId) {
        log.info("Uploading profile image");
        return uploadImage(
                file,
                username,
                userId,
                PROFILE_IMAGE_UPLOAD_DIR + userId + "/",
                "/files/profile-images/" + userId + "/",
                200, 200
        );
    }

    private UploadDto uploadImage(MultipartFile file, String name, Long ownerId,
                                  String baseDir, String publicPath,
                                  int thumbWidth, int thumbHeight) {
        if (file == null || file.isEmpty())
            throw new IllegalArgumentException("File cannot be empty");

        try {
            String safeName = name.trim().replaceAll("[^a-zA-Z0-9-_]", "_").toLowerCase();
            Path folderPath = Paths.get(baseDir);
            Files.createDirectories(folderPath);

            // Clean old images
            try (var files = Files.list(folderPath)) {
                files.forEach(existing -> {
                    try { Files.deleteIfExists(existing); } catch (IOException ignored) {}
                });
            }

            // Use consistent format (JPG)
            String baseFileName = safeName + "_" + System.currentTimeMillis();
            String fullFileName = baseFileName + ".jpg";
            String thumbFileName = baseFileName + "_thumb.jpg";

            Path fullPath = folderPath.resolve(fullFileName);
            Path thumbPath = folderPath.resolve(thumbFileName);

            // Read image (any supported format)
            BufferedImage image = ImageIO.read(file.getInputStream());
            if (image == null) throw new IOException("Unsupported or invalid image format");

            // Save main as JPEG
            writeAsJpeg(image, fullPath);

            // Create and save thumbnail
            createThumbnail(image, thumbPath, thumbWidth, thumbHeight);

            String fullUrl = publicPath + fullFileName;
            String thumbUrl = publicPath + thumbFileName;
            log.info("full url: {} and thumb url: {}", fullUrl, thumbUrl);

            log.info("Saved normalized JPG image: {}", fullPath);
            log.info("Saved JPG thumbnail: {}", thumbPath);

            return UploadDto.builder()
                    .fileName(fullFileName)
                    .fileUrl(fullUrl)
                    .thumbnailFileUrl(thumbUrl)
                    .folderPath(folderPath.toString())
                    .build();

        } catch (IOException e) {
            log.error("Failed to save image: {}", e.getMessage(), e);
            throw new RuntimeException("Failed to save image", e);
        }
    }

    private void createThumbnail(BufferedImage originalImage, Path targetPath, int width, int height) throws IOException {
        int origWidth = originalImage.getWidth();
        int origHeight = originalImage.getHeight();
        double ratio = Math.min((double) width / origWidth, (double) height / origHeight);
        int newWidth = (int) (origWidth * ratio);
        int newHeight = (int) (origHeight * ratio);

        Image scaled = originalImage.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
        BufferedImage thumbnail = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

        Graphics2D g2d = thumbnail.createGraphics();
        g2d.setComposite(AlphaComposite.Src);
        g2d.setColor(Color.WHITE); // white background for JPG
        g2d.fillRect(0, 0, width, height);
        g2d.drawImage(scaled, (width - newWidth) / 2, (height - newHeight) / 2, null);
        g2d.dispose();

        writeAsJpeg(thumbnail, targetPath);
    }

    private void writeAsJpeg(BufferedImage image, Path outputPath) throws IOException {
        // Convert ARGB (with alpha) → RGB (no alpha)
        BufferedImage rgbImage = new BufferedImage(
                image.getWidth(),
                image.getHeight(),
                BufferedImage.TYPE_INT_RGB
        );

        Graphics2D g2d = rgbImage.createGraphics();
        g2d.setColor(Color.WHITE); // optional: white background instead of transparent
        g2d.fillRect(0, 0, image.getWidth(), image.getHeight());
        g2d.drawImage(image, 0, 0, null);
        g2d.dispose();

        try (FileImageOutputStream output = new FileImageOutputStream(outputPath.toFile())) {
            Iterator<ImageWriter> writers = ImageIO.getImageWritersByFormatName("jpg");
            if (!writers.hasNext()) throw new IOException("No JPEG writer available");

            ImageWriter writer = writers.next();
            writer.setOutput(output);

            ImageWriteParam param = writer.getDefaultWriteParam();
            if (param.canWriteCompressed()) {
                param.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
                param.setCompressionQuality(0.9f);
            }

            writer.write(null, new IIOImage(rgbImage, null, null), param);
            writer.dispose();
        }
    }


    @Override
    public void deleteImageFile(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) return;

        try {
            String rel = fileUrl.replaceFirst("^/files", "/uploads");
            Path path = Paths.get(System.getProperty("user.dir"), rel).normalize();
            if (Files.exists(path)) {
                Files.delete(path);
                log.info("Deleted image file: {}", path);
            }
            Path parent = path.getParent();
            if (parent != null && Files.isDirectory(parent)) {
                try (var s = Files.list(parent)) {
                    if (s.findAny().isEmpty()) {
                        Files.delete(parent);
                        log.info("Deleted empty folder: {}", parent);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to delete image: {}", e.getMessage(), e);
        }
    }

    @Override
    public void deleteImageFolder(String category, Long ownerId) {
        if (category == null || ownerId == null) return;

        Path folderPath = Paths.get(System.getProperty("user.dir"), "uploads", category, String.valueOf(ownerId));
        try {
            if (Files.exists(folderPath)) {
                Files.walk(folderPath)
                        .sorted(Comparator.reverseOrder()) // delete files before dirs
                        .forEach(path -> {
                            try { Files.delete(path); } catch (IOException ignored) {}
                        });
                log.info("Deleted image folder [{}] for ID {}: {}", category, ownerId, folderPath);
            } else {
                log.debug("No folder found for [{}] ID {}", category, ownerId);
            }
        } catch (IOException e) {
            log.error("Failed to delete image folder for [{}] ID {}: {}", category, ownerId, e.getMessage());
        }
    }

}
