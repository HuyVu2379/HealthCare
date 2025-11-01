package fit.iuh.student.healthrecordservice.dtos.responses;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordFullTimelineResponse {
    private Long totalVisits;
    private Integer totalEpisodes;
    private List<EpisodeGroup> episodes;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EpisodeGroup {
        private String episodeId;              // Root record ID
        private Boolean isCurrentEpisode;      // True if contains current visit
        private Date firstVisitDate;           // Date of initial visit in this episode
        private Integer totalVisitsInEpisode;  // Number of visits in this episode
        private String serviceName;            // Service/diagnosis of root record
        private String rootDiagnosis;          // Diagnosis of initial visit
        private List<VisitDetail> visits;      // All visits in this episode (sorted DESC)
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class VisitDetail {
        private String recordId;
        private String appointmentId;
        private Date appointmentDate;          // From Appointment
        private String episodeType;            // INITIAL or FOLLOW_UP
        private String parentRecordId;         // For follow-ups
        private Boolean isCurrentVisit;        // Highlight current
        private String diagnosis;
        private String symptoms;
        private String treatment;
        private String doctorNote;
        private String serviceName;
        private Integer visitNumberInEpisode;  // 1, 2, 3... (for display)
        private List<PrescriptionResponse> prescriptions;
        private Date createdAt;
    }
}
