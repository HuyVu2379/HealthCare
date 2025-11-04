package fit.iuh.student.communicationservice.controllers;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import fit.iuh.student.communicationservice.dtos.requests.SendMessageRequest;
import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;
import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.entities.Group;
import fit.iuh.student.communicationservice.repositories.GroupRepository;
import fit.iuh.student.communicationservice.services.GroupService;
import fit.iuh.student.communicationservice.handlers.CustomWebSocketHandler;
import lombok.extern.slf4j.Slf4j;
import fit.iuh.student.communicationservice.services.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

/**
 * REST Controller cho Communication Service
 * WebSocket functionality đã được chuyển sang CustomWebSocketHandler
 * Controller này cung cấp REST APIs cho các client không sử dụng WebSocket
 */
@RestController
@RequestMapping("/api/communication")
@RequiredArgsConstructor
@Slf4j
public class SocketController {

    private final GroupService groupService;
    private final MessageService messageService;
    private final CustomWebSocketHandler customWebSocketHandler;
    private final GroupRepository groupRepository;

    /**
     * REST API để tạo Group
     */
    @PostMapping("/groups")
    public ResponseEntity<GroupResponse> createGroup(@Valid @RequestBody CreateGroupRequest request) {
        GroupResponse response = groupService.createGroup(request);
        try {
            // Extract memberIds từ response
            List<String> memberIds = response.getMembers().stream()
                    .map(member -> member.getUserId())
                    .collect(java.util.stream.Collectors.toList());

            // Notify members và auto-join sessions vào group (giống WebSocket flow)
            customWebSocketHandler.notifyGroupCreated(
                    response.getGroupId(),
                    memberIds,
                    response
            );
            log.info("Notified and auto-joined members for group: {}", response.getGroupId());
        } catch (Exception e) {
            log.error("Failed to notify members for group: {}", response.getGroupId(), e);
        }
        return ResponseEntity.ok(response);
    }

    /**
     * REST API để gửi message
     */
    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> sendMessage(@Valid @RequestBody SendMessageRequest request) {
        MessageResponse response = messageService.sendMessage(request);
        return ResponseEntity.ok(response);
    }

    /**
     * REST API để lấy messages của group
     */
    @GetMapping("/groups/{groupId}/messages")
    public ResponseEntity<List<MessageResponse>> getMessages(
            @PathVariable String groupId,
            @RequestParam(defaultValue = "0") Integer page,
            @RequestParam(defaultValue = "20") Integer size) {
        List<MessageResponse> messages = messageService.getMessagesByGroupIdWithPagination(groupId, page, size);
        return ResponseEntity.ok(messages);
    }

    /**
     * REST API để lấy danh sách groups của user
     */
    @GetMapping("/users/{userId}/groups")
    public ResponseEntity<List<GroupResponse>> getUserGroups(@PathVariable String userId) {
        List<GroupResponse> groups = groupService.getGroupsByUserId(userId);
        return ResponseEntity.ok(groups);
    }

    /**
     * REST API để lấy tất cả groups
     */
    @GetMapping("/groups")
    public ResponseEntity<List<GroupResponse>> getAllGroups() {
        List<GroupResponse> groups = groupService.getAllGroups();
        return ResponseEntity.ok(groups);
    }

    /**
     * REST API để lấy thông tin một group
     */
    @GetMapping("/groups/{groupId}")
    public ResponseEntity<GroupResponse> getGroup(@PathVariable String groupId) {
        GroupResponse group = groupService.findById(groupId);
        return ResponseEntity.ok(group);
    }

    /**
     * REST API để tìm group theo danh sách member IDs
     * Dùng cho web frontend khi cần load existing group sau khi nhận error "Group already exists"
     *
     * @param memberIds Danh sách userId của các members
     * @return GroupResponse nếu tìm thấy group, 404 nếu không tìm thấy
     */
    @PostMapping("/groups/find-by-members")
    public ResponseEntity<GroupResponse> findGroupByMembers(@RequestBody List<String> memberIds) {
        log.info("Finding group by member IDs: {}", memberIds);

        // Validate input
        if (memberIds == null || memberIds.isEmpty()) {
            log.warn("Invalid request: memberIds is null or empty");
            return ResponseEntity.badRequest().build();
        }

        // Tìm group với members
        Optional<Group> existingGroup = groupRepository.findGroupByMemberIds(memberIds);

        if (existingGroup.isPresent()) {
            log.info("Found existing group: {}", existingGroup.get().getGroupId());
            // Trả về full GroupResponse với lastMessage
            GroupResponse response = groupService.findById(existingGroup.get().getGroupId());
            return ResponseEntity.ok(response);
        }

        // Không tìm thấy
        log.info("No group found with member IDs: {}", memberIds);
        return ResponseEntity.notFound().build();
    }
}
