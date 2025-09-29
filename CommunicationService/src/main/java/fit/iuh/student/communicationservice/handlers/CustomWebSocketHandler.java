package fit.iuh.student.communicationservice.handlers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import fit.iuh.student.communicationservice.dtos.requests.SendMessageRequest;
import fit.iuh.student.communicationservice.dtos.requests.GetMessagesRequest;
import fit.iuh.student.communicationservice.dtos.requests.GetGroupsRequest;
import fit.iuh.student.communicationservice.dtos.responses.GroupResponse;
import fit.iuh.student.communicationservice.dtos.responses.MessageResponse;
import fit.iuh.student.communicationservice.repositories.GroupRepository;
import fit.iuh.student.communicationservice.services.GroupService;
import fit.iuh.student.communicationservice.services.MessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
@Slf4j
public class CustomWebSocketHandler implements WebSocketHandler {

    private final GroupService groupService;
    private final MessageService messageService;
    private final ObjectMapper objectMapper;
    private final GroupRepository groupRepository;
    // Lưu trữ tất cả các WebSocket sessions
    private final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    // Lưu trữ các sessions theo group
    private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> groupSessions = new ConcurrentHashMap<>();

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
                case "create_group":
                    handleCreateGroup(session, data);
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

        // Xóa session khỏi danh sách chung
        sessions.remove(session.getId());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    private void handleCreateGroup(WebSocketSession session, JsonNode data) throws Exception {
        CreateGroupRequest request = objectMapper.treeToValue(data, CreateGroupRequest.class);
        if(groupRepository.existsGroupByGroupName(request.getGroupName())){
            sendError(session, "Group name already exists: " + request.getGroupName());
            return;
        }
        GroupResponse response = groupService.createGroup(request);
        // Broadcast group created event to all connected clients
        broadcastToAll("group_created", response);
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

    private void broadcastToAll(String action, Object data) {
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
