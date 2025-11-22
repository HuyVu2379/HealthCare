package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.CertificationDto;
import fit.iuh.student.userservice.dtos.requests.AddCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.*;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.services.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/doctors")
@RequiredArgsConstructor
public class DoctorController {
    private final DoctorService doctorService;
    @PutMapping("/update")
    public ResponseEntity<MessageResponse<UpdateDoctorResponse>> updateDoctor(
            @RequestBody UpdateDoctorRequest updateDoctorRequest
            ){
        UpdateDoctorResponse response = doctorService.updateDoctor(updateDoctorRequest);
        if(updateDoctorRequest == null) {
            MessageResponse<UpdateDoctorResponse> re = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to reset password",
                    false,
                    response
            );
            return new ResponseEntity<>(re, HttpStatus.BAD_REQUEST);
        }
        return SuccessEntityResponse.ok("update doctor success",response);
    }

    @PutMapping("/updateCertification/{doctorId}")
    public ResponseEntity<MessageResponse<UpdateDoctorCertificationResponse>> updateDoctorCertification(
            @RequestBody UpdateDoctorCertificationRequest request,
            @PathVariable String doctorId
    ){
        UpdateDoctorCertificationResponse response = doctorService.updateDoctorCertification(request,doctorId);
        if(response == null) {
            MessageResponse<UpdateDoctorCertificationResponse> re = new MessageResponse<>(
                    HttpStatus.BAD_REQUEST.value(),
                    "Failed to reset password",
                    false,
                    response
            );
            return new ResponseEntity<>(re, HttpStatus.BAD_REQUEST);
        }
        return SuccessEntityResponse.ok("update doctor certification success",response);
    }
    @PutMapping("/updateRating/{doctorId}")
    public ResponseEntity<Boolean> updateDoctorRating(
            @PathVariable String doctorId,
            @RequestParam double rating
    ){
        int response = doctorService.updateDoctorRating(doctorId, rating);
        if(response == 0) {
            return new ResponseEntity<>(false, HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(true, HttpStatus.OK);
    }
    @GetMapping("/getDoctorByIds")
    public ResponseEntity<MessageResponse<List<DoctorResponse>>> getDoctorByIds(
            @RequestParam List<String> doctorIds
    ){
        List<DoctorResponse> response = doctorService.getDoctorByIds(doctorIds);
        if(response == null || response.isEmpty()) {
            throw new UserNotFoundException("Doctors not found for the provided ids");
        }
        return SuccessEntityResponse.ok("Get doctors by ids success",response);
    }
    @GetMapping("/getDoctorForClient/{doctorId}")
    public DoctorClientResponse getDoctorForClient(
            @PathVariable String doctorId
    )
    {
        return doctorService.getDoctorIdByIdForClient(doctorId);
    }
    
    @GetMapping("/getDoctorById/{doctorId}")
    public ResponseEntity<MessageResponse<DoctorResponse>> getDoctorById(
            @PathVariable String doctorId
    ){
        DoctorResponse response = doctorService.getDoctorById(doctorId);
        if(response == null) {
            throw new UserNotFoundException("Doctor not found with id: " + doctorId);
        }
        return SuccessEntityResponse.ok("Get doctor by id success", response);
    }
    
    // API thêm certification mới
    @PostMapping("/addCertification/{userId}")
    public ResponseEntity<MessageResponse<CertificationDto>> addCertification(
            @RequestBody AddCertificationRequest request,
            @PathVariable String userId
    ){
        CertificationDto response = doctorService.addCertification(request, userId);
        return SuccessEntityResponse.ok("Add certification success", response);
    }

    // API cập nhật certification theo ID
    @PutMapping("/updateCertification/{userId}/{certificationId}")
    public ResponseEntity<MessageResponse<CertificationDto>> updateCertification(
            @RequestBody UpdateCertificationRequest request,
            @PathVariable String userId,
            @PathVariable String certificationId
    ){
        CertificationDto response = doctorService.updateCertification(request, userId, certificationId);
        return SuccessEntityResponse.ok("Update certification success", response);
    }

    // API xóa certification theo ID
    @DeleteMapping("/deleteCertification/{userId}/{certificationId}")
    public ResponseEntity<MessageResponse<String>> deleteCertification(
            @PathVariable String userId,
            @PathVariable String certificationId
    ){
        doctorService.deleteCertification(userId, certificationId);
        return SuccessEntityResponse.ok("Delete certification success", "Certification deleted successfully");
    }
    
    // API lấy danh sách certifications của user
    @GetMapping("/getCertifications/{userId}")
    public ResponseEntity<MessageResponse<List<CertificationDto>>> getCertifications(
            @PathVariable String userId
    ){
        List<CertificationDto> response = doctorService.getCertificationsByUserId(userId);
        return SuccessEntityResponse.ok("Get certifications success", response);
    }

    // API lấy danh sách bác sĩ nổi bật
    @GetMapping("/outstanding")
    public ResponseEntity<MessageResponse<List<DoctorResponse>>> getOutstandingDoctors(){
        List<DoctorResponse> response = doctorService.getOutstandingDoctors();
        if(response == null || response.isEmpty()) {
            return SuccessEntityResponse.ok("No outstanding doctors found", response);
        }
        return SuccessEntityResponse.ok("Get outstanding doctors success", response);
    }
}
