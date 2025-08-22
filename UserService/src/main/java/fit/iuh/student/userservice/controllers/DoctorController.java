package fit.iuh.student.userservice.controllers;

import fit.iuh.student.userservice.dtos.requests.UpdateDoctorCertificationRequest;
import fit.iuh.student.userservice.dtos.requests.UpdateDoctorRequest;
import fit.iuh.student.userservice.dtos.responses.*;
import fit.iuh.student.userservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.userservice.repositories.DoctorRepository;
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
    private final DoctorRepository doctorRepository;
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
        int response = doctorRepository.updateRatingForDoctorId(doctorId, rating);
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
}
