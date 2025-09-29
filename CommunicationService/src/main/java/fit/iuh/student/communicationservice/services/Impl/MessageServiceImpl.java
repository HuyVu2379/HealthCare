package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.dtos.requests.SendMessageRequest;
import fit.iuh.student.communicationservice.dtos.requests.UpdateSummaryRequest;
import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.entities.Message;
import fit.iuh.student.communicationservice.mappers.MessageMapper;
import fit.iuh.student.communicationservice.repositories.MessageRepository;
import fit.iuh.student.communicationservice.services.MessageService;
import fit.iuh.student.communicationservice.services.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    private final SummaryService summaryService;

    public MessageResponse sendMessage(SendMessageRequest request) {
        Message message = new Message();
        message.setMessage_id(UUID.randomUUID().toString());
        message.setGroup_id(request.getGroupId());
        message.setSender_id(request.getSenderId());
        message.setContent(request.getContent());
        message.setSendAt(LocalDateTime.now());
        Message savedMessage = messageRepository.save(message);
        if (request.getGroupId().contains("AI")) {
            summaryService.updateSummary(UpdateSummaryRequest.builder()
                    .groupId(request.getGroupId())
                    .contentSummary(request.getContent()).build()
            );
        }
        MessageResponse response = messageMapper.toMessageResponse(savedMessage);
        if (request.getTempMessageId() != null && !request.getTempMessageId().isEmpty()) {
            response.setTempMessageId(request.getTempMessageId());
        }
        return response;
    }

    public List<MessageResponse> getMessagesByGroupId(String groupId) {
        List<Message> messages = messageRepository.findByGroup_idOrderByCreatedAtAsc(groupId);
        return messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    public List<MessageResponse> getMessagesByGroupIdWithPagination(String groupId, Integer page, Integer size) {
        Pageable pageable = PageRequest.of(page, size);
        List<Message> messages = messageRepository.findByGroup_idOrderByCreatedAtDesc(groupId, pageable);
        return messages.stream()
                .map(messageMapper::toMessageResponse)
                .collect(Collectors.toList());
    }

    @Override
    public MessageResponse getLastMessageByGroupId(String groupId) {
        return messageRepository
                .findLatestMessageByGroupId(groupId, PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(messageMapper::toMessageResponse)
                .orElse(null);
    }

}
