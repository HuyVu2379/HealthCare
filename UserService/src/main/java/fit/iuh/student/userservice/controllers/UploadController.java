package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.responses.UploadFile;
import fit.iuh.student.userservice.services.UploadService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class UploadController {
    private final UploadService uploadService;
    
    @PostMapping(value = "/file", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> uploadFile(@RequestPart("file") FilePart filePart,
                                                         @RequestParam(value = "folder", defaultValue = "HealthCare") String folder) {
        Map<String, Object> result = uploadService.uploadFile(filePart, folder);
        return ResponseEntity.ok(result);
    }
    
    @PostMapping(value = "/files", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadFile> uploadMultipleFiles(@RequestPart("file") FilePart filePart,
                                                         @RequestParam(value = "folder", defaultValue = "HealthCare") String folder) {
        UploadFile result = uploadService.uploadMultipleFiles(filePart, folder);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/file")
    public ResponseEntity<Map<String, Object>> deleteFile(@RequestParam("url") String url) {
        String publicId = uploadService.extractPublicIdFromUrl(url);
        Map<String, Object> result = uploadService.deleteFile(publicId);
        return ResponseEntity.ok(result);
    }
    
    @DeleteMapping("/files")
    public ResponseEntity<Map<String, Object>> deleteMultipleFiles(@RequestBody List<String> urls) {
        List<String> publicIds = uploadService.extractPublicIdsFromUrls(urls);
        Map<String, Object> result = uploadService.deleteMultipleFiles(publicIds);
        return ResponseEntity.ok(result);
    }
}
