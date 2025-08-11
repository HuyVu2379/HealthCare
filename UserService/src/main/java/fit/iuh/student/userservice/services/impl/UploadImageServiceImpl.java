package fit.iuh.student.userservice.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import fit.iuh.student.userservice.dtos.responses.UploadFile;
import fit.iuh.student.userservice.exceptions.errors.FileDeleteException;
import fit.iuh.student.userservice.exceptions.errors.FileUploadException;
import fit.iuh.student.userservice.services.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadImageServiceImpl implements UploadService {
    private final Cloudinary cloudinary;

    // File size constants
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final String[] ALLOWED_IMAGE_TYPES = {"image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"};
    private static final String[] ALLOWED_VIDEO_TYPES = {"video/mp4", "video/avi", "video/mov", "video/wmv"};
    private static final String[] ALLOWED_DOCUMENT_TYPES = {"application/pdf", "application/msword",
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"};

    @Override
    public UploadFile uploadFile(MultipartFile file, String folder) {
        try {
            validateFile(file);

            Map<String, Object> params = ObjectUtils.asMap(
                    "folder", folder,
                    "resource_type", determineResourceType(file.getContentType()),
                    "public_id", generatePublicId(file.getOriginalFilename()),
                    "overwrite", true,
                    "use_filename", true,
                    "unique_filename", false
            );
            Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
            String fileUrl = (String) result.get("secure_url");
            String publicId = (String) result.get("public_id");
            return UploadFile.builder()
                    .imageUrls(List.of(fileUrl))
                    .publicIds(List.of(publicId))
                    .build();

        } catch (IOException e) {
            throw new FileUploadException("Failed to upload file: " + e.getMessage());
        } catch (Exception e) {
            throw new FileUploadException("Unexpected error occurred while uploading file");
        }
    }

    @Override
    public UploadFile uploadFiles(List<MultipartFile> files, String folder) {
        if (files == null || files.isEmpty()) {
            throw new FileUploadException("No files provided for upload");
        }

        List<String> fileUrls = new ArrayList<>();
        List<String> publicIds = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();

        for (MultipartFile file : files) {
            try {
                validateFile(file);
                Map<String, Object> params = ObjectUtils.asMap(
                        "folder", folder,
                        "resource_type", determineResourceType(file.getContentType()),
                        "public_id", generatePublicId(file.getOriginalFilename()),
                        "overwrite", true,
                        "use_filename", true,
                        "unique_filename", false
                );
                Map<String, Object> result = cloudinary.uploader().upload(file.getBytes(), params);
                fileUrls.add((String) result.get("secure_url"));
                publicIds.add((String) result.get("public_id"));
            } catch (Exception e) {
                log.error("Error uploading file {}: {}", file.getOriginalFilename(), e.getMessage());
                failedFiles.add(file.getOriginalFilename());
            }
        }

        if (fileUrls.isEmpty()) {
            throw new FileUploadException("All files failed to upload. Failed files: " + String.join(", ", failedFiles));
        }

        if (!failedFiles.isEmpty()) {
            log.warn("Some files failed to upload: {}", String.join(", ", failedFiles));
        }
        return UploadFile.builder()
                .imageUrls(fileUrls)
                .publicIds(publicIds)
                .build();
    }

    @Override
    public void deleteFile(String publicId) {
        if (publicId == null || publicId.trim().isEmpty()) {
            throw new FileDeleteException("Public ID cannot be null or empty");
        }
        try {
            Map<String, Object> result = cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
            String resultStatus = (String) result.get("result");
            if ("ok".equals(resultStatus)) {
                log.info("File deleted successfully: {}", publicId);
            } else if ("not found".equals(resultStatus)) {
                log.warn("File not found for deletion: {}", publicId);
                throw new FileDeleteException("File not found: " + publicId);
            } else {
                log.error("Failed to delete file: {}. Result: {}", publicId, result);
                throw new FileDeleteException("Failed to delete file: " + publicId);
            }

        } catch (IOException e) {
            log.error("Error deleting file {}: {}", publicId, e.getMessage(), e);
            throw new FileDeleteException("Failed to delete file: " + e.getMessage());
        }
    }

    /**
     * Validates the uploaded file for size, type, and content
     */
    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("File is empty or null");
        }

        // Check file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileUploadException(
                String.format("File size exceeds maximum limit of %d MB. Current size: %d bytes",
                    MAX_FILE_SIZE / (1024 * 1024), file.getSize())
            );
        }

        // Check file type
        String contentType = file.getContentType();
        if (contentType == null || !isAllowedContentType(contentType)) {
            throw new FileUploadException("File type not allowed: " + contentType +
                ". Allowed types: images, videos, and documents (PDF, DOC, DOCX)");
        }

        // Validate filename
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.trim().isEmpty()) {
            throw new FileUploadException("File must have a valid filename");
        }

        // Check for potential security issues in filename
        if (containsUnsafeCharacters(originalFilename)) {
            throw new FileUploadException("Filename contains unsafe characters");
        }
    }

    /**
     * Checks if the content type is allowed
     */
    private boolean isAllowedContentType(String contentType) {
        if (contentType == null) {
            return false;
        }

        // Check image types
        for (String type : ALLOWED_IMAGE_TYPES) {
            if (contentType.equals(type)) {
                return true;
            }
        }

        // Check video types
        for (String type : ALLOWED_VIDEO_TYPES) {
            if (contentType.equals(type)) {
                return true;
            }
        }

        // Check document types
        for (String type : ALLOWED_DOCUMENT_TYPES) {
            if (contentType.equals(type)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Determines the resource type for Cloudinary based on content type
     */
    private String determineResourceType(String contentType) {
        if (contentType == null) {
            return "auto";
        }

        if (contentType.startsWith("image/")) {
            return "image";
        } else if (contentType.startsWith("video/")) {
            return "video";
        } else {
            return "raw"; // For documents and other files
        }
    }

    /**
     * Generates a unique public ID for the file
     */
    private String generatePublicId(String originalFilename) {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String uuid = UUID.randomUUID().toString().substring(0, 8);

        String filename = "file";
        if (originalFilename != null && !originalFilename.trim().isEmpty()) {
            // Remove file extension and clean filename
            int lastDotIndex = originalFilename.lastIndexOf('.');
            if (lastDotIndex > 0) {
                filename = originalFilename.substring(0, lastDotIndex);
            } else {
                filename = originalFilename;
            }

            // Clean filename - keep only alphanumeric characters, hyphens, and underscores
            filename = filename.replaceAll("[^a-zA-Z0-9._-]", "_");

            // Limit filename length
            if (filename.length() > 50) {
                filename = filename.substring(0, 50);
            }
        }

        return String.format("%s_%s_%s", timestamp, uuid, filename);
    }

    /**
     * Checks for unsafe characters in filename
     */
    private boolean containsUnsafeCharacters(String filename) {
        // Check for potentially dangerous patterns
        String[] unsafePatterns = {"../", "..\\", "<script", "javascript:", "vbscript:", "onload=", "onerror="};

        String lowerFilename = filename.toLowerCase();
        for (String pattern : unsafePatterns) {
            if (lowerFilename.contains(pattern)) {
                return true;
            }
        }

        return false;
    }
}
