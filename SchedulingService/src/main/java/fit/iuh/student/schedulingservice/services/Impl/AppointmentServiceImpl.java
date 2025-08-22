package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateDoctorSchedule;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.repositories.AppointmentRepository;
import fit.iuh.student.schedulingservice.repositories.TimeSlotRepository;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import fit.iuh.student.schedulingservice.services.DoctorScheduleService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorScheduleService doctorScheduleService;

    @Override
    public Appointment bookingAppointment(CreateAppointmentRequest request) {
        try {
            Appointment apm = Appointment.builder()
                    .patientId(request.getPatientId())
                    .doctorId(request.getDoctorId())
                    .symptoms(request.getSymptoms())
                    .note(request.getNote())
                    .slotId(request.getSlotId())
                    .status(request.getStatus())
                    .timeSlot(timeSlotRepository.findById(request.getSlotId()).orElse(null))
                    .appointmentDate(request.getAppointmentDate())
                    .consultationType(request.getConsultationType())
                    .addressDetail(request.getAddressDetail())
                    .build();
            Appointment appointment = appointmentRepository.save(apm);
            doctorScheduleService.updateDoctorSchedule(UpdateDoctorSchedule.builder().
                    scheduleId(request.getScheduleId()).
                    timeSlotIds(List.of(request.getSlotId())).
                    build());
            return appointment;
        } catch (Exception e) {
            throw e;
        }
    }
}
