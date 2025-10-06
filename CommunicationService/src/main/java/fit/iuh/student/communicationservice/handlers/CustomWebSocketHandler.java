package fit.iuh.student.communicationservice.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.student.communicationservice.consumers.ScheduleSocketConsumer;
import fit.iuh.student.communicationservice.consumers.payload.AppointmentData;
import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import fit.iuh.student.communicationservice.dtos.requests.DeleteGroupRequest;
import fit.iuh.student.communicationservice.dtos.requests.SendMessageRequest;
import fit.iuh.student.communicationservice.dtos.requests.GetMessagesRequest;
import fit.iuh.student.communicationservice.dtos.requests.GetGroupsRequest;
import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;
import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.entities.Group;
import fit.iuh.student.communicationservice.publishers.ScheduleSocketPublisher;
import fit.iuh.student.communicationservice.publishers.payload.ScheduleEventMessage;
import fit.iuh.student.communicationservice.repositories.GroupRepository;
import fit.iuh.student.communicationservice.services.GroupService;
import fit.iuh.student.communicationservice.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomWebSocketHandler implements WebSocketHandler {

    private final GroupService groupService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    private final GroupRepository groupRepository;
    private final ScheduleSocketPublisher publisher;
    // Lưu trữ tất cả các WebSocket sessions
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Lưu trữ các sessions theo group
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> groupSessions = new ConcurrentHashMap<>();

    // Lưu trữ các sessions theo userId
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> userSessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);

        // Delay 200ms để đảm bảo client socket sẵn sàng trước khi gửi welcome message
        CompletableFuture.delayedExecutor(500, TimeUnit.MILLISECONDS)
                .execute(() -> {
                    try {
                        if (session.isOpen()) {
                            sendWelcomeMessage(session);
                        } else {
                            log.warn("Session {} is closed before sending welcome message", session.getId());
                        }
                    } catch (Exception e) {
                        log.error("Failed to send welcome message after delay", e);
                    }
                });
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        try {
            String payload = (String) message.getPayload();
            log.info("Received message from {}: {}", session.getId(), payload);

            JsonNode jsonMessage = objectMapper.readTree(payload);
            String action = jsonMessage.get("action").asText();
            JsonNode data = jsonMessage.get("data");

            switch (action) {
                case "authenticate":
                    handleAuthenticate(session, data);
                    break;
                case "create_group":
                    handleCreateGroup(session, data);
                    break;
                case "delete_group":
                    handleDeleteGroup(session, data);
                    break;
                case "send_message":
                    handleSendMessage(session, data);
                    break;
                case "get_messages":
                    handleGetMessages(session, data);
                    break;
                case "get_groups":
                    handleGetGroups(session, data);
                    break;
                case "join_group":
                    handleJoinGroup(session, data);
                    break;
                case "leave_group":
                    handleLeaveGroup(session, data);
                    break;
                case "schedule_appointment":
                    handleScheduleAppointment(data);
                    break;
                default:
                    sendError(session, "Unknown action: " + action);
            }
        } catch (Exception e) {
            log.error("Error handling message: ", e);
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Transport error for session {}: ", session.getId(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), closeStatus);

        // Xóa session khỏi tất cả các group
        groupSessions.values().forEach(sessionList -> sessionList.remove(session.getId()));

        // Xóa session khỏi userSessions
        userSessions.values().forEach(sessionList -> sessionList.remove(session.getId()));

        // Xóa session khỏi danh sách chung
        sessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleAuthenticate(WebSocketSession session, JsonNode data) throws Exception {
        String userId = data.get("userId").asText();

        // Thêm session vào userSessions map (với kiểm tra trùng)
        CopyOnWriteArrayList<String> userSessionList = userSessions.computeIfAbsent(userId, k -> new CopyOnWriteArrayList<>());
        if (!userSessionList.contains(session.getId())) {
            userSessionList.add(session.getId());
            log.info("Session {} authenticated for user {}", session.getId(), userId);
        }

        sendMessage(session, createResponse("authenticate", "success", "User authenticated: " + userId));
    }

    private void handleCreateGroup(WebSocketSession session, JsonNode data) throws Exception {
        CreateGroupRequest request = objectMapper.treeToValue(data, CreateGroupRequest.class);

        // Kiểm tra xem có member nào có userId là "AI" không
        boolean hasAI = request.getMembers().stream()
                .anyMatch(member -> "AI".equals(member.getUserId()));

        GroupResponse response;
        if(hasAI){
            // Nếu có AI thì tạo group trực tiếp (không kiểm tra duplicate)
            response = groupService.createGroup(request);
        } else {
            // Chuyển đổi danh sách members thành List userId
            List<String> memberIds = request.getMembers().stream()
                    .map(m -> m.getUserId())
                    .collect(Collectors.toList());

            // Kiểm tra xem đã tồn tại group với cùng members chưa
            Optional<Group> existingGroup = groupRepository.findGroupByMemberIds(memberIds);
            if(existingGroup.isPresent()){
                sendError(session, "Group with these members already exists");
                return;
            }

            response = groupService.createGroup(request);
        }

        String groupId = response.getGroupId();

        // Auto-join session của người tạo vào group (với kiểm tra trùng)
        CopyOnWriteArrayList<String> groupSessionList = groupSessions.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>());
        if (!groupSessionList.contains(session.getId())) {
            groupSessionList.add(session.getId());
            log.info("Creator session {} joined group {}", session.getId(), groupId);
        }

        // Lấy danh sách userId của tất cả members
        List<String> memberIds = response.getMembers().stream()
                .map(member -> member.getUserId())
                .collect(Collectors.toList());

        // Gửi event group_created cho tất cả members đang online và auto-join họ vào group
        notifyMembersAndJoinGroup(memberIds, groupId, "group_created", response);
    }

    private void handleSendMessage(WebSocketSession session, JsonNode data) throws Exception {
        SendMessageRequest request = objectMapper.treeToValue(data, SendMessageRequest.class);
        MessageResponse response = messageService.sendMessage(request);

        // Broadcast message to group members
        broadcastToGroup(request.getGroupId(), "message_received", response);
    }

    private void handleGetMessages(WebSocketSession session, JsonNode data) throws Exception {
        GetMessagesRequest request = objectMapper.treeToValue(data, GetMessagesRequest.class);
        List<MessageResponse> messages = messageService.getMessagesByGroupIdWithPagination(request.getGroupId(),
                                                                   request.getPage(),
                                                                   request.getSize());
        sendMessage(session, createResponse("messages", "success", messages));
    }

    private void handleGetGroups(WebSocketSession session, JsonNode data) throws Exception {
        GetGroupsRequest request = objectMapper.treeToValue(data, GetGroupsRequest.class);
        List<GroupResponse> groups = groupService.getGroupsByUserId(request.getUserId());
        sendMessage(session, createResponse("groups", "success", groups));
    }

    private void handleJoinGroup(WebSocketSession session, JsonNode data) throws Exception {
        String groupId = data.get("groupId").asText();

        groupSessions.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>())
                     .add(session.getId());

        sendMessage(session, createResponse("join_group", "success", "Joined group: " + groupId));
        log.info("Session {} joined group {}", session.getId(), groupId);
    }

    private void handleLeaveGroup(WebSocketSession session, JsonNode data) throws Exception {
        String groupId = data.get("groupId").asText();

        CopyOnWriteArrayList<String> sessionList = groupSessions.get(groupId);
        if (sessionList != null) {
            sessionList.remove(session.getId());
        }

        sendMessage(session, createResponse("leave_group", "success", "Left group: " + groupId));
        log.info("Session {} left group {}", session.getId(), groupId);
    }

    private void handleDeleteGroup(WebSocketSession session, JsonNode data) throws Exception {
        DeleteGroupRequest request = objectMapper.treeToValue(data, DeleteGroupRequest.class);

        // Lấy thông tin group trước khi xóa để thông báo cho members
        GroupResponse groupInfo = null;
        try {
            groupInfo = groupService.findById(request.getGroupId());
        } catch (Exception e) {
            sendError(session, "Group not found");
            return;
        }

        // Thực hiện xóa group
        boolean isDeleted = groupService.deleteGroup(request);

        if (isDeleted) {
            // Xóa group khỏi groupSessions
            groupSessions.remove(request.getGroupId());

            // Lấy danh sách userId của tất cả members để thông báo
            List<String> memberIds = groupInfo.getMembers().stream()
                    .map(member -> member.getUserId())
                    .collect(Collectors.toList());

            // Gửi thông báo group_deleted cho tất cả members
            notifyMembers(memberIds, "group_deleted",
                Map.of("groupId", request.getGroupId(),
                       "groupName", groupInfo.getGroupName(),
                       "deletedBy", request.getUserId()));

            // Gửi response thành công cho người xóa
            sendMessage(session, createResponse("group_deleted", "success",
                Map.of("groupId", request.getGroupId(), "message", "Group deleted successfully")));

            log.info("Group {} deleted by user {}", request.getGroupId(), request.getUserId());
        } else {
            sendError(session, "Failed to delete group. You may not have permission or group not found.");
        }
    }

    private void handleScheduleAppointment(JsonNode data) throws Exception {
         ScheduleEventMessage request = objectMapper.treeToValue(data, ScheduleEventMessage.class);

        // Chuyển tiếp dữ liệu sự kiện lịch hẹn đến RabbitMQ
        publisher.publishScheduleEventSocket(request);
        // Gửi thông báo WebSocket đến các session của bệnh nhân và bác sĩ
    }

    public void handlePublishScheduleToClient(AppointmentData data) throws Exception {
        List<String> targetUserIds = new ArrayList<>();

        if (data.getDoctorId() != null) {
            targetUserIds.add(data.getDoctorId());
        }

        if (data.getPatientId() != null) {
            targetUserIds.add(data.getPatientId());
        }

        if (!targetUserIds.isEmpty()) {
            // Gửi thông báo đến các session của bệnh nhân và bác sĩ
            notifyMembers(targetUserIds, "schedule_appointment_response", targetUserIds);
            log.info("Sent schedule appointment notification to users: {}", targetUserIds);
        }
    }
    private void broadcastToGroup(String groupId, String action, Object data) {
        CopyOnWriteArrayList<String> sessionList = groupSessions.get(groupId);
        if (sessionList != null) {
            String message = createResponse(action, "broadcast", data);
            sessionList.forEach(sessionId -> {
                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    try {
                        sendMessage(session, message);
                    } catch (Exception e) {
                        log.error("Error broadcasting to session {}: ", sessionId, e);
                    }
                }
            });
        }
    }

    public void broadcastToAll(String action, Object data) {
        String message = createResponse(action, "broadcast", data);
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                try {
                    sendMessage(session, message);
                } catch (Exception e) {
                    log.error("Error broadcasting to all sessions: ", e);
                }
            }
        });
    }

    /**
     * Helper method to notify group members and auto-join their sessions to the group
     * @param memberIds List of userId to notify
     * @param groupId Group ID to join sessions to
     * @param action WebSocket action type
     * @param data Data to send
     */
    private void notifyMembersAndJoinGroup(List<String> memberIds, String groupId, String action, Object data) {
        String message = createResponse(action, "broadcast", data);
        CopyOnWriteArrayList<String> groupSessionList = groupSessions.computeIfAbsent(groupId, k -> new CopyOnWriteArrayList<>());

        memberIds.forEach(userId -> {
            CopyOnWriteArrayList<String> userSessionList = userSessions.get(userId);
            if (userSessionList != null) {
                userSessionList.forEach(sessionId -> {
                    WebSocketSession session = sessions.get(sessionId);
                    if (session != null && session.isOpen()) {
                        try {
                            // Gửi thông báo cho session
                            sendMessage(session, message);

                            // Auto-join session vào group (với kiểm tra trùng)
                            if (!groupSessionList.contains(sessionId)) {
                                groupSessionList.add(sessionId);
                                log.info("Auto-joined session {} (user {}) to group {}", sessionId, userId, groupId);
                            }
                        } catch (Exception e) {
                            log.error("Error notifying session {} for user {}: ", sessionId, userId, e);
                        }
                    }
                });
            }
        });
    }

    /**
     * Helper method to notify group members (without auto-join)
     * @param memberIds List of userId to notify
     * @param action WebSocket action type
     * @param data Data to send
     */
    private void notifyMembers(List<String> memberIds, String action, Object data) {
        String message = createResponse(action, "broadcast", data);

        memberIds.forEach(userId -> {
            CopyOnWriteArrayList<String> userSessionList = userSessions.get(userId);
            if (userSessionList != null) {
                userSessionList.forEach(sessionId -> {
                    WebSocketSession session = sessions.get(sessionId);
                    if (session != null && session.isOpen()) {
                        try {
                            sendMessage(session, message);
                        } catch (Exception e) {
                            log.error("Error notifying session {} for user {}: ", sessionId, userId, e);
                        }
                    }
                });
            }
        });
    }

    private void sendMessage(WebSocketSession session, String message) throws IOException {
        if (session.isOpen()) {
            session.sendMessage(new TextMessage(message));
        }
    }

    private void sendWelcomeMessage(WebSocketSession session) throws IOException {
        sendMessage(session, createResponse("connection", "established", "WebSocket connected successfully"));
    }

    private void sendError(WebSocketSession session, String error) {
        try {
            String errorMessage = createResponse("error", "error", error);
            sendMessage(session, errorMessage);
        } catch (IOException e) {
            log.error("Error sending error message: ", e);
        }
    }

    private String createResponse(String action, String status, Object data) {
        try {
            return objectMapper.writeValueAsString(new WebSocketResponse(action, status, data));
        } catch (Exception e) {
            log.error("Error creating response: ", e);
            return "{\"action\":\"error\",\"status\":\"error\",\"data\":\"Error creating response\"}";
        }
    }

    // Inner class for response structure
    public static class WebSocketResponse {
        public String action;
        public String status;
        public Object data;

        public WebSocketResponse(String action, String status, Object data) {
            this.action = action;
            this.status = status;
            this.data = data;
        }
    }
}
