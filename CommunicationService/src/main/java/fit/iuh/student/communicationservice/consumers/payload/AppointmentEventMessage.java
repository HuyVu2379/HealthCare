package fit.iuh.student.communicationservice.consumers.payload;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class AppointmentEventMessage {

    @JsonProperty("type")
    private String eventType;
    private AppointmentData data;
    public AppointmentEventMessage(AppointmentData data, String eventType) {
        this.data = data;
        this.eventType = eventType;
    }

    public AppointmentData getPage() {
        return data;
    }
}
