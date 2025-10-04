package fit.iuh.student.communicationservice.services;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import fit.iuh.student.communicationservice.dtos.requests.DeleteGroupRequest;
import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;

import java.util.List;

public interface GroupService {
    GroupResponse createGroup(CreateGroupRequest request);
    GroupResponse findById(String groupId);
    List<GroupResponse> getGroupsByUserId(String userId);
    List<GroupResponse> getAllGroups();
    boolean deleteGroup(DeleteGroupRequest request);
}
