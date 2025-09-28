package fit.iuh.student.healthrecordservice.services;

import fit.iuh.student.healthrecordservice.dtos.responses.MedicalResultsResponse;

public interface MedicalResultsService {
    MedicalResultsResponse getResultsByAppointmentId(String appointmentId, String currentUserId, String currentUserRole);
}


