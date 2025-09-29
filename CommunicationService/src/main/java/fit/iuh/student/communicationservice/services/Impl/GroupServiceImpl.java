package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
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
        Group group = new Group();
        boolean hasAI = request.getMembers() != null && request.getMembers().stream().anyMatch(mem-> "AI".equals(mem.getUserId()));
        group.setGroupId(hasAI ? UUID.randomUUID().toString(): UUID.randomUUID() + "-AI");
        group.setGroupName(request.getGroupName());
        group.setAppointment_id(request.getAppointmentId());
        group.setMembers(request.getMembers());
        Group savedGroup = groupRepository.save(group);

        return GroupResponse.builder()
                .groupId(savedGroup.getGroupId())
                .groupName(savedGroup.getGroupName())
                .appointmentId(savedGroup.getAppointment_id())
                .members(savedGroup.getMembers())
                .createdAt(savedGroup.getCreatedAt())
                .updatedAt(group.getUpdatedAt())
                .lastMessageContent("")
                .timeLastMessage(null)
                .build();
    }

    public GroupResponse findById(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        MessageResponse lastMessage = messageMapper.toMessageResponse(messageRepository.findLastMessageByGroupId(groupId));
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
                    MessageResponse lastMessage = messageMapper.toMessageResponse(messageRepository.findLastMessageByGroupId(group.getGroupId()));
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
                    MessageResponse lastMessage = messageMapper.toMessageResponse(messageRepository.findLastMessageByGroupId(group.getGroupId()));
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

}
