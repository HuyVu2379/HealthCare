package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.MessageResponse;
import fit.iuh.student.userservice.dtos.responses.SuccessEntityResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorCertificationResponse;
import fit.iuh.student.userservice.dtos.responses.UpdateDoctorResponse;
import fit.iuh.student.userservice.services.DoctorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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
}
