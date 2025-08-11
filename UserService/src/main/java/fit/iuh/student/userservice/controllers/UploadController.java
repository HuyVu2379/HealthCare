package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.responses.UploadFile;
import fit.iuh.student.userservice.services.UploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
@Slf4j
public class UploadController {
    
    private final UploadService uploadService;

    /**
     * Upload single file
     */
    @PostMapping(value = "/single", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFile> uploadSingleFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "folder", defaultValue = "HealthCare") String folder) {
        
        log.info("Uploading single file: {} to folder: {}", file.getOriginalFilename(), folder);
        
        UploadFile result = uploadService.uploadFile(file, folder);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Upload multiple files
     */
    @PostMapping(value = "/multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFile> uploadMultipleFiles(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "folder", defaultValue = "HealthCare") String folder) {
        
        log.info("Uploading {} files to folder: {}", files.size(), folder);
        
        UploadFile result = uploadService.uploadFiles(files, folder);
        
        return ResponseEntity.ok(result);
    }

    /**
     * Delete file by public ID
     */
    @DeleteMapping("/{publicId}")
    public ResponseEntity<String> deleteFile(@PathVariable String publicId) {
        log.info("Deleting file with public ID: {}", publicId);
        
        uploadService.deleteFile(publicId);
        
        return ResponseEntity.ok("File deleted successfully");
    }

    /**
     * Upload avatar specifically
     */
    @PostMapping(value = "/avatar", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFile> uploadAvatar(
            @RequestParam("file") MultipartFile file) {
        
        log.info("Uploading avatar: {}", file.getOriginalFilename());
        
        UploadFile result = uploadService.uploadFile(file, "avatars");
        
        return ResponseEntity.ok(result);
    }

    /**
     * Upload document specifically
     */
    @PostMapping(value = "/document", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFile> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "category", defaultValue = "general") String category) {
        
        log.info("Uploading document: {} to category: {}", file.getOriginalFilename(), category);
        
        String folder = "documents/" + category;
        UploadFile result = uploadService.uploadFile(file, folder);
        
        return ResponseEntity.ok(result);
    }
}
