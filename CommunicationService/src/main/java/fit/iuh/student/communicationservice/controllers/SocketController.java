package fit.iuh.student.communicationservice.controllers;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import fit.iuh.student.communicationservice.dtos.requests.SendMessageRequest;
import fit.iuh.student.communicationservice.dtos.requests.GetMessagesRequest;
import fit.iuh.student.communicationservice.dtos.requests.GetGroupsRequest;
import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;
import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.services.GroupService;
import fit.iuh.student.communicationservice.services.MessageService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;

import java.util.List;


@Controller
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class SocketController {

    private final GroupService groupService;
    private final MessageService messageService;
    private final SimpMessagingTemplate simpMessagingTemplate;
    /**
     * API Socket để tạo Group
     * Client gửi message đến "/app/group/create"
     * Kết quả sẽ được broadcast đến tất cả subscribers của "/topic/group/created"
     */
    @MessageMapping("/group/create")
    @SendTo("/topic/group/created")
    public GroupResponse createGroup(@Valid CreateGroupRequest request) {
        try {
            GroupResponse groupResponse = groupService.createGroup(request);
            // Thông báo đến các members về việc được thêm vào group
            for (String memberId : request.getMemberIds()) {
                simpMessagingTemplate.convertAndSend(
                    "/queue/user/" + memberId + "/group/joined",
                    groupResponse
                );
            }

            return groupResponse;
        } catch (Exception e) {
            // Gửi thông báo lỗi
            simpMessagingTemplate.convertAndSend("/topic/group/error",
                "Error creating group: " + e.getMessage());
            throw e;
        }
    }

    /**
     * API Socket để gửi Message
     * Client gửi message đến "/app/message/send"
     * Message sẽ được gửi đến group channel tương ứng
     */
    @MessageMapping("/message/send")
    public void sendMessage(@Valid SendMessageRequest request) {
        try {
            MessageResponse messageResponse = messageService.sendMessage(request);

            // Gửi message đến group channel
            simpMessagingTemplate.convertAndSend(
                "/topic/group/" + request.getGroupId() + "/messages",
                messageResponse
            );

//            // Nếu có receiver cụ thể, gửi thêm thông báo riêng
//            if (request.getReceiverId() != null && !request.getReceiverId().isEmpty()) {
//                simpMessagingTemplate.convertAndSend(
//                    "/queue/user/" + request.getReceiverId() + "/messages",
//                    messageResponse
//                );
//            }

        } catch (Exception e) {
            // Gửi thông báo lỗi đến sender
            simpMessagingTemplate.convertAndSend(
                "/queue/user/" + request.getSenderId() + "/message/error",
                "Error sending message: " + e.getMessage()
            );
        }
    }

    /**
     * API Socket để nhận messages từ một group
     * Client gửi request đến "/app/message/get"
     * Kết quả sẽ được gửi trực tiếp đến user channel
     */
    @MessageMapping("/message/get")
    public void getMessages(@Valid GetMessagesRequest request) {
        try {
            List<MessageResponse> messages;

            if (request.getPage() != null && request.getSize() != null) {
                // Lấy messages với phân trang
                messages = messageService.getMessagesByGroupIdWithPagination(
                    request.getGroupId(), request.getPage(), request.getSize()
                );
            } else {
                // Lấy tất cả messages
                messages = messageService.getMessagesByGroupId(request.getGroupId());
            }

            // Gửi danh sách messages đến topic của group
            simpMessagingTemplate.convertAndSend(
                "/topic/group/" + request.getGroupId() + "/messages/list",
                messages
            );

        } catch (Exception e) {
            simpMessagingTemplate.convertAndSend("/topic/group/error",
                "Error getting messages: " + e.getMessage());
        }
    }

    /**
     * API Socket để load danh sách groups của user
     * Client gửi request đến "/app/group/list"
     * Kết quả sẽ được gửi đến user channel riêng
     */
    @MessageMapping("/group/list")
    public void getGroups(@Valid GetGroupsRequest request) {
        try {
            List<GroupResponse> groups = groupService.getGroupsByUserId(request.getUserId());

            // Gửi danh sách groups đến user channel riêng
            simpMessagingTemplate.convertAndSend(
                "/queue/user/" + request.getUserId() + "/groups",
                groups
            );

        } catch (Exception e) {
            simpMessagingTemplate.convertAndSend(
                "/queue/user/" + request.getUserId() + "/groups/error",
                "Error getting groups: " + e.getMessage()
            );
        }
    }

    /**
     * API Socket để load tất cả groups (cho admin hoặc public groups)
     * Client gửi request đến "/app/group/all"
     */
    @MessageMapping("/group/all")
    @SendTo("/topic/groups/all")
    public List<GroupResponse> getAllGroups() {
        try {
            return groupService.getAllGroups();
        } catch (Exception e) {
            simpMessagingTemplate.convertAndSend("/topic/group/error",
                "Error getting all groups: " + e.getMessage());
            return List.of(); // Return empty list on error
        }
    }

    /**
     * API Socket để join vào một group chat
     * Client gửi message đến "/app/group/join"
     */
    @MessageMapping("/group/join")
    public void joinGroup(String groupId) {
        try {
            GroupResponse group = groupService.findById(groupId);
            // Có thể thêm logic để xác thực user có quyền join group không

            // Gửi thông tin group đến user
            simpMessagingTemplate.convertAndSend("/topic/group/" + groupId + "/joined", group);

        } catch (Exception e) {
            simpMessagingTemplate.convertAndSend("/topic/group/error",
                "Error joining group: " + e.getMessage());
        }
    }
}
