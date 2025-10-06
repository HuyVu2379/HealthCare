package fit.iuh.student.communicationservice.controllers;

import fit.iuh.student.communicationservice.dtos.responses.GetSummaryResponse;
import fit.iuh.student.communicationservice.services.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chats")
@RequiredArgsConstructor
public class ChatController {
    private final SummaryService summaryService;
    @GetMapping("/get-summary/{id}")
    public GetSummaryResponse getSummaryResponse(
            @PathVariable String id
    ){
        return summaryService.getSummaryByGroupId(id);
    }
}
