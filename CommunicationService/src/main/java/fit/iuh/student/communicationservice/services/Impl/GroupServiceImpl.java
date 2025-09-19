package fit.iuh.student.communicationservice.services.Impl;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;
import fit.iuh.student.communicationservice.entities.Group;
import fit.iuh.student.communicationservice.mappers.GroupMapper;
import fit.iuh.student.communicationservice.repositories.GroupRepository;
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
    public GroupResponse createGroup(CreateGroupRequest request) {
        Group group = new Group();
        if(request.getMemberIds().contains("AI")){
            group.setGroupId(UUID.randomUUID() + "-AI");
        }
        group.setGroupId(UUID.randomUUID().toString());
        group.setGroupName(request.getGroupName());
        group.setAppointment_id(request.getAppointmentId());
        group.setMemberIds(request.getMemberIds());
        group.setCreatedAt(LocalDateTime.now());
        group.setUpdatedAt(LocalDateTime.now());

        Group savedGroup = groupRepository.save(group);

        return groupMapper.toGroupResponse(savedGroup);
    }

    public GroupResponse findById(String groupId) {
        Group group = groupRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found with id: " + groupId));
        return groupMapper.toGroupResponse(group);
    }

    public List<GroupResponse> getGroupsByUserId(String userId) {
        List<Group> groups = groupRepository.findByMemberIdsContaining(userId);
        return groups.stream()
                .map(groupMapper::toGroupResponse)
                .collect(Collectors.toList());
    }

    public List<GroupResponse> getAllGroups() {
        List<Group> groups = groupRepository.findAll();
        return groups.stream()
                .map(groupMapper::toGroupResponse)
                .collect(Collectors.toList());
    }

}
