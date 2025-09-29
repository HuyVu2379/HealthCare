package fit.iuh.student.communicationservice.entities;

import fit.iuh.student.communicationservice.dtos.requests.CreateGroupRequest;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Document
@Data
@Builder
@EqualsAndHashCode(of = {"groupId"})
public class Group {
    @Id
    private String groupId;
    private String groupName;
    private String appointment_id;
    private Boolean hasMessage = false;
    private List<CreateGroupRequest.MemberDTO> members;
    @CreatedDate
    private LocalDateTime createdAt;
    @LastModifiedDate
    private LocalDateTime updatedAt;
}
