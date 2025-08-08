package fit.iuh.student.userservice.services.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import fit.iuh.student.userservice.dtos.responses.UploadFile;
import fit.iuh.student.userservice.services.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class UploadImageServiceImpl implements UploadService {
    private final Cloudinary cloudinary;
    
    @Override
    public UploadFile uploadFile(FilePart filePart, String folder) {
        try {
            Path tempFile = Files.createTempFile("upload-", filePart.filename());
            DataBufferUtils.write(filePart.content(), tempFile, StandardOpenOption.WRITE)
                    .block();
            // Upload the file to Cloudinary
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("resource_type", "auto");
            
            Map<String, Object> result = cloudinary.uploader().upload(tempFile.toFile(), params);
            
            // Delete the temporary file
            Files.delete(tempFile);
            
            // Extract the URL from the result
            String imageUrl = (String) result.get("url");
            
            return UploadFile.builder()
                    .imageUrls(List.of(imageUrl))
                    .build();
        } catch (IOException e) {
            log.error("Error uploading file: {}", e.getMessage(), e);
            return UploadFile.builder().imageUrls(List.of()).build();
        }
    }

    @Override
    public UploadFile uploadMultipleFiles(FilePart filePart, String folder) {
        try {
            // Create a temporary file
            Path tempFile = Files.createTempFile("upload-", filePart.filename());
            
            // Write the file content to the temporary file
            DataBufferUtils.write(filePart.content(), tempFile, StandardOpenOption.WRITE)
                    .block();
            
            // Upload the file to Cloudinary
            Map<String, Object> params = new HashMap<>();
            params.put("folder", folder);
            params.put("resource_type", "auto");
            
            Map<String, Object> result = cloudinary.uploader().upload(tempFile.toFile(), params);
            
            // Delete the temporary file
            Files.delete(tempFile);
            
            // Extract the URL from the result
            String imageUrl = (String) result.get("url");
            
            return UploadFile.builder()
                    .imageUrls(List.of(imageUrl))
                    .build();
        } catch (IOException e) {
            log.error("Error uploading multiple files: {}", e.getMessage(), e);
            return UploadFile.builder().imageUrls(List.of()).build();
        }
    }

    @Override
    public Map<String, Object> deleteFile(String publicId) {
        try {
            return cloudinary.uploader().destroy(publicId, ObjectUtils.emptyMap());
        } catch (IOException e) {
            log.error("Error deleting file: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public Map<String, Object> deleteMultipleFiles(List<String> publicIds) {
        try {
            return cloudinary.api().deleteResources(publicIds, ObjectUtils.emptyMap());
        } catch (Exception e) {
            log.error("Error deleting multiple files: {}", e.getMessage(), e);
            return Map.of("error", e.getMessage());
        }
    }

    @Override
    public String extractPublicIdFromUrl(String cloudinaryUrl) {
        // Pattern to extract public ID from Cloudinary URL
        // Example URL: https://res.cloudinary.com/dbd0act4g/image/upload/v1629123456/HealthCare/abcdef123456.jpg
        Pattern pattern = Pattern.compile("v\\d+/([^/]+/[^/\\.]+)");
        Matcher matcher = pattern.matcher(cloudinaryUrl);
        
        if (matcher.find()) {
            return matcher.group(1);
        }
        
        return "";
    }

    @Override
    public List<String> extractPublicIdsFromUrls(List<String> urls) {
        return urls.stream()
                .map(this::extractPublicIdFromUrl)
                .filter(id -> !id.isEmpty())
                .collect(Collectors.toList());
    }
}
