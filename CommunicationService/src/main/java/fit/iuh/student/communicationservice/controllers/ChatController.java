package fit.iuh.student.communicationservice.controllers;

import fit.iuh.student.communicationservice.dtos.responses.GetSummaryResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats")
public class ChatController {
    @GetMapping("/get-summary/{id}")
    public GetSummaryResponse getSummaryResponse(
            @PathVariable String id
    ){
        return null;
    }
}
