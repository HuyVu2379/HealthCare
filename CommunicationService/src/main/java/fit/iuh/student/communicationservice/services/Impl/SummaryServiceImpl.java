package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.dtos.requests.UpdateSummaryRequest;
import fit.iuh.student.communicationservice.dtos.responses.GetSummaryResponse;
import fit.iuh.student.communicationservice.entities.Message;
import fit.iuh.student.communicationservice.entities.Summary;
import fit.iuh.student.communicationservice.repositories.MessageRepository;
import fit.iuh.student.communicationservice.repositories.SummaryRepository;
import fit.iuh.student.communicationservice.services.SummaryService;
import jakarta.ws.rs.core.MediaType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SummaryServiceImpl implements SummaryService {
    private final SummaryRepository summaryRepository;
    private final RestTemplate restTemplate;
    private final MessageRepository messageService;
    @Value("${gemini.api-key}")
    private String gemini_api_key;
//    public Summary createSummary(CreateSummaryRequest summary) {
//        try{
//            Summary sum = Summary.builder()
//                    .summaryId(UUID.randomUUID().toString())
//                    .groupId(summary.getGroupId())
//                    .contentSummary(summary.getContentSummary())
//                    .build();
//            return summaryRepository.save(sum);
//        }catch (Exception e){
//            throw e;
//        }
//    }

    @Override
    public Summary updateSummary(UpdateSummaryRequest summary) {
        try {
            Summary existSum = summaryRepository.findByGroupId(summary.getGroupId());
            if (existSum != null) {
                String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + gemini_api_key;
                // Prompt
                String prompt = "Bạn là AI tóm tắt hội thoại.\n"
                        + "Summary cũ:\n" + existSum.getContentSummary() + "\n"
                        + "Tin nhắn mới:\n" + summary.getContentSummary() + "\n"
                        + "Hãy trả về summary mới, ngắn gọn và đủ ý.";
                Map<String, Object> body = Map.of(
                        "contents", List.of(
                                Map.of(
                                        "role", "user",
                                        "parts", List.of(Map.of("text", prompt))
                                )
                        )
                );
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(org.springframework.http.MediaType.valueOf(MediaType.APPLICATION_JSON));

                HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

                // Call API
                ResponseEntity<Map> response = restTemplate.exchange(
                        GEMINI_URL,
                        HttpMethod.POST,
                        entity,
                        Map.class
                );

                // Process AI response
                if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                    Map<String, Object> responseBody = response.getBody();
                    List<Map<String, Object>> candidates = (List<Map<String, Object>>) responseBody.get("candidates");
                    if (candidates != null && !candidates.isEmpty()) {
                        Map<String, Object> firstCandidate = candidates.get(0);
                        Map<String, Object> content = (Map<String, Object>) firstCandidate.get("content");
                        if (content != null) {
                            List<Map<String, Object>> parts = (List<Map<String, Object>>) content.get("parts");
                            if (parts != null && !parts.isEmpty()) {
                                String newSummary = (String) parts.get(0).get("text");
                                if (newSummary != null && !newSummary.trim().isEmpty()) {
                                    existSum.setContentSummary(newSummary.trim());
                                }
                            }
                        }
                    }
                }
                return summaryRepository.save(existSum);

            }
            Summary sum = Summary.builder()
                    .summaryId(UUID.randomUUID().toString())
                    .groupId(summary.getGroupId())
                    .contentSummary(summary.getContentSummary())
                    .build();
            return summaryRepository.save(sum);
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public GetSummaryResponse getSummaryByGroupId(String groupId) {
        try {
            Summary existSum = summaryRepository.findByGroupId(groupId);
            Pageable pageable = PageRequest.of(0, 10);
            List<String> messages = messageService.findByGroup_idOrderByCreatedAtDesc(groupId, pageable).stream()
                    .map(Message::getContent)
                    .toList();
            return GetSummaryResponse.builder()
                    .summary(existSum != null ? existSum.getContentSummary() : "")
                    .messages(messages)
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }
}
