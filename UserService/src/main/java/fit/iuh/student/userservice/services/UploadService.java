package fit.iuh.student.userservice.services;

import fit.iuh.student.userservice.dtos.responses.UploadFile;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UploadService {
    UploadFile uploadFile(MultipartFile file, String folder);
    UploadFile uploadFiles(List<MultipartFile> files, String folder);
    void deleteFile(String publicId);
}
