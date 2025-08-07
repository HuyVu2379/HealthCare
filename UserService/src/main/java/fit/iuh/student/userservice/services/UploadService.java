package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.responses.UploadFile;
import org.springframework.http.codec.multipart.FilePart;

import java.util.List;
import java.util.Map;

public interface UploadService {
    Map<String, Object> uploadFile(FilePart filePart, String folder);
    UploadFile uploadMultipleFiles(FilePart fileParts, String folder);
    Map<String, Object> deleteFile(String publicId);
    Map<String, Object> deleteMultipleFiles(List<String> publicIds);
    String extractPublicIdFromUrl(String cloudinaryUrl);
    List<String> extractPublicIdsFromUrls(List<String> urls);
}
