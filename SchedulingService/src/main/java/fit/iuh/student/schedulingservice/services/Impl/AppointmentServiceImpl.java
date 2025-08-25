package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.clients.UserClient;
import fit.iuh.student.schedulingservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotDTO;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.entities.DoctorSchedule;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.exceptions.errors.NotFoundException;
import fit.iuh.student.schedulingservice.publisher.AppointmentEventPublisher;
import fit.iuh.student.schedulingservice.repositories.AppointmentRepository;
import fit.iuh.student.schedulingservice.repositories.DoctorScheduleRepository;
import fit.iuh.student.schedulingservice.repositories.TimeSlotRepository;
import fit.iuh.student.schedulingservice.services.AppointmentService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserClient userClient;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentEventPublisher appointmentEventPublisher;
    @Override
    public AppointmentResponse bookingAppointment(CreateAppointmentRequest request) {
        try {
            DoctorSchedule doctorSchedule = doctorScheduleRepository.findById(request.getScheduleId()).orElse(null);
            if(doctorSchedule.getTimeSlots().stream().noneMatch(ts -> ts.getSlotId() == request.getSlotId())){
                throw new NotFoundException("Time slot not found in the doctor's schedule");
            }
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
                    .doctorSchedule(doctorScheduleRepository.findById(request.getScheduleId()).orElse(null))
                    .build();
            Appointment appointment = appointmentRepository.save(apm);
            doctorSchedule.removeTimeSlot(timeSlotRepository.findById(request.getSlotId()).orElse(null));
            doctorScheduleRepository.save(doctorSchedule);
            DoctorClientResponse doctor = userClient.getDoctorForClient(request.getDoctorId());
            
            // Map TimeSlot to TimeSlotDTO
            TimeSlotDTO timeSlotDTO = null;
            if (apm.getTimeSlot() != null) {
                timeSlotDTO = TimeSlotDTO.builder()
                        .slotId(apm.getTimeSlot().getSlotId())
                        .startTime(apm.getTimeSlot().getStartTime())
                        .endTime(apm.getTimeSlot().getEndTime())
                        .build();
            }
            AppointmentResponse aprs = AppointmentResponse.builder()
                    .appointmentId(appointment.getAppointmentId())
                    .doctor(doctor)
                    .patient(userClient.getPatientForClient(apm.getPatientId()))
                    .symptoms(apm.getSymptoms())
                    .note(apm.getNote())
                    .status(apm.getStatus())
                    .timeSlot(timeSlotDTO)
                    .appointmentDate(apm.getAppointmentDate())
                    .consultationType(apm.getConsultationType())
                    .addressDetail(doctor.getClinicAddress())
                    .build();
            appointmentEventPublisher.publishBookingAppointmentEvent(aprs);
            return aprs;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Page<AppointmentResponse> getAppointmentByPatientIdWithPage(String patientId, int page, int size, String sortBy, String sortDir) {
        try {
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";
            }

            Sort.Direction direction = Sort.Direction.ASC; // default
            if (sortDir != null && sortDir.equalsIgnoreCase("DESC")) {
                direction = Sort.Direction.DESC;
            }

            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Appointment> app = appointmentRepository.findAppointmentByPatientIdWithPage(patientId, pageable);
            
            // Check if there are any appointments before accessing the first one
            if (app.getContent().isEmpty()) {
                return Page.empty(pageable);
            }
            
            DoctorClientResponse doctor = userClient.getDoctorForClient(app.getContent().get(0).getDoctorId());
            return app.map(appointment -> {
                // Map TimeSlot to TimeSlotDTO
                TimeSlotDTO timeSlotDTO = null;
                if (appointment.getTimeSlot() != null) {
                    timeSlotDTO = TimeSlotDTO.builder()
                            .slotId(appointment.getTimeSlot().getSlotId())
                            .startTime(appointment.getTimeSlot().getStartTime())
                            .endTime(appointment.getTimeSlot().getEndTime())
                            .build();
                }
                
                return AppointmentResponse.builder()
                        .appointmentId(appointment.getAppointmentId())
                        .doctor(doctor)
                        .patient(userClient.getPatientForClient(appointment.getPatientId()))
                        .symptoms(appointment.getSymptoms())
                        .note(appointment.getNote())
                        .status(appointment.getStatus())
                        .timeSlot(timeSlotDTO)
                        .appointmentDate(appointment.getAppointmentDate())
                        .consultationType(appointment.getConsultationType())
                        .addressDetail(doctor.getClinicAddress())
                        .build();
            });
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public boolean cancelAppointment(String appointmentId, String userId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) {
                return false;
            }
            appointment.setStatus(AppointmentStatus.CANCELED);
            appointmentRepository.save(appointment);
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Appointment rescheduleAppointment(UpdateAppointmentRequest request) {
        try{
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId()).orElse(null);
            if(appointment == null){
                throw new NotFoundException("Appointment not found");
            }
            DoctorSchedule ds = doctorScheduleRepository.findById(request.getNewScheduleId()).orElse(null);
            if(ds == null){
                throw new NotFoundException("Doctor schedule not found");
            }
            if(ds.getTimeSlots().stream().noneMatch(ts -> ts.getSlotId().equals(request.getNewSlotId()))) {
                throw new NotFoundException("Time slot not found in the doctor's schedule");
            }
            appointment.setAppointmentDate(request.getNewAppointmentDate());
            appointment.setSlotId(request.getNewSlotId());
            appointment.setTimeSlot(timeSlotRepository.findById(request.getNewSlotId()).orElse(null));
            return appointmentRepository.save(appointment);
        }catch (Exception e){
            throw e;
        }
    }
}
