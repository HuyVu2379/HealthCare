package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import fit.iuh.student.communicationservice.dtos.requests.DeleteGroupRequest;
import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;
import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.entities.Group;
import fit.iuh.student.communicationservice.mappers.GroupMapper;
import fit.iuh.student.communicationservice.mappers.MessageMapper;
import fit.iuh.student.communicationservice.repositories.GroupRepository;
import fit.iuh.student.communicationservice.repositories.MessageRepository;
import fit.iuh.student.communicationservice.services.GroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GroupServiceImpl implements GroupService {
    private final GroupRepository groupRepository;
    private final GroupMapper groupMapper;
    private final MessageRepository messageRepository;
    private final MessageMapper messageMapper;
    public GroupResponse createGroup(CreateGroupRequest request) {
        boolean hasAI = request.getMembers() != null && request.getMembers().stream()
                .anyMatch(mem -> {
                    String uid = mem.getUserId() != null ? mem.getUserId() : "";
                    String fn  = mem.getFullName() != null ? mem.getFullName() : "";
                    String s1 = uid.toLowerCase();
                    String s2 = fn.toLowerCase();
                    return s1.contains("AI") || s2.contains("AI");
                });

        // Chỉ check trùng cho chat 2 người KHÔNG có AI (bất kể appointment)
        if (!hasAI
                && request.getMembers() != null && request.getMembers().size() == 2) {

            String u1 = request.getMembers().get(0).getUserId();
            String u2 = request.getMembers().get(1).getUserId();

        //     var existingOpt = groupRepository.findOneToOneGroupByMembers(u1, u2);
        var existingOpt = groupRepository.findGroupByMemberIds(java.util.List.of(u1, u2));

            if (existingOpt.isPresent()) {
                Group existing = existingOpt.get();
                MessageResponse lastMessage = messageRepository
                        .findLatestMessageByGroupId(existing.getGroupId(), org.springframework.data.domain.PageRequest.of(0, 1))
                        .stream()
                        .findFirst()
                        .map(messageMapper::toMessageResponse)
                        .orElse(null);

                return GroupResponse.builder()
                        .groupId(existing.getGroupId())
                        .groupName(existing.getGroupName())
                        .appointmentId(existing.getAppointment_id())
                        .members(existing.getMembers())
                        .createdAt(existing.getCreatedAt())
                        .updatedAt(existing.getUpdatedAt())
                        .lastMessageContent(lastMessage != null ? lastMessage.getContent() : "")
                        .timeLastMessage(lastMessage != null ? lastMessage.getSendAt() : null)
                        .build();
            }
        }
        Group savedGroup = groupRepository.insert(Group.builder()
                .groupId(hasAI ? UUID.randomUUID() + "-AI" : UUID.randomUUID().toString())
                        .groupName(request.getGroupName() != null ? request.getGroupName() : "Chat with AI"
                        ).members(request.getMembers())
                        .appointment_id(request.getAppointmentId())
                        .createdAt(LocalDateTime.now())
                        .hasMessage(false)
                        .build()
                );

        return GroupResponse.builder()
                .groupId(savedGroup.getGroupId())
                .groupName(savedGroup.getGroupName())
                .appointmentId(savedGroup.getAppointment_id())
                .members(savedGroup.getMembers())
                .createdAt(savedGroup.getCreatedAt())
                .updatedAt(savedGroup.getUpdatedAt())
                .lastMessageContent("")
                .timeLastMessage(null)
                .build();
    }

    public GroupResponse findById(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        MessageResponse lastMessage = messageRepository
                .findLatestMessageByGroupId(groupId, org.springframework.data.domain.PageRequest.of(0, 1))
                .stream()
                .findFirst()
                .map(messageMapper::toMessageResponse)
                .orElse(null);
        return GroupResponse
                .builder()
                .groupId(group.getGroupId())
                .groupName(group.getGroupName())
                .appointmentId(group.getAppointment_id())
                .members(group.getMembers())
                .createdAt(group.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .lastMessageContent(lastMessage != null ? lastMessage.getContent() : "")
                .timeLastMessage(lastMessage != null ? lastMessage.getSendAt() : null)
                .build();
    }

    public List<GroupResponse> getGroupsByUserId(String userId) {
        List<Group> groups = groupRepository.findByMembersUserId(userId);
        return groups.stream()
                .map(group -> {
                    MessageResponse lastMessage = messageRepository
                            .findLatestMessageByGroupId(group.getGroupId(), org.springframework.data.domain.PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(messageMapper::toMessageResponse)
                            .orElse(null);
                    return GroupResponse
                            .builder()
                            .groupId(group.getGroupId())
                            .groupName(group.getGroupName())
                            .appointmentId(group.getAppointment_id())
                            .members(group.getMembers())
                            .createdAt(group.getCreatedAt())
                            .updatedAt(group.getUpdatedAt())
                            .lastMessageContent(lastMessage != null ? lastMessage.getContent() : "")
                            .timeLastMessage(lastMessage != null ? lastMessage.getSendAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        return groups.stream()
                .map(group -> {
                    MessageResponse lastMessage = messageRepository
                            .findLatestMessageByGroupId(group.getGroupId(), org.springframework.data.domain.PageRequest.of(0, 1))
                            .stream()
                            .findFirst()
                            .map(messageMapper::toMessageResponse)
                            .orElse(null);
                    return GroupResponse
                            .builder()
                            .groupId(group.getGroupId())
                            .groupName(group.getGroupName())
                            .appointmentId(group.getAppointment_id())
                            .members(group.getMembers())
                            .createdAt(group.getCreatedAt())
                            .updatedAt(group.getUpdatedAt())
                            .lastMessageContent(lastMessage != null ? lastMessage.getContent() : "")
                            .timeLastMessage(lastMessage != null ? lastMessage.getSendAt() : null)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean deleteGroup(DeleteGroupRequest request) {
        try {
            // Kiểm tra group có tồn tại không
            Group group = groupRepository.findByGroupId(request.getGroupId());
            if (group == null) {
                return false;
            }

            // Kiểm tra quyền xóa group (chỉ member của group mới có thể xóa)
            boolean isUserInGroup = group.getMembers().stream()
                    .anyMatch(member -> member.getUserId().equals(request.getUserId()));

            if (!isUserInGroup) {
                return false;
            }

            // Xóa tất cả messages trong group trước
            messageRepository.deleteAllByGroup_id(request.getGroupId());

            // Xóa group
            groupRepository.deleteByGroupId(request.getGroupId());

            return true;
        } catch (Exception e) {
            // Log error nếu cần
            return false;
        }
    }
}
