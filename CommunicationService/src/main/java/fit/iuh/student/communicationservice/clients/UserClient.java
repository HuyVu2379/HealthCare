package fit.iuh.student.communicationservice.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "UserService")
public interface UserClient {
    @PutMapping("api/v1/doctors/updateRating/{doctorId}")
    ResponseEntity<Boolean> updateDoctorRating(
        @PathVariable  String doctorId,
        @RequestParam  double rating
    );
}
