package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.clients.UserClient;
import fit.iuh.student.schedulingservice.clients.dtos.DoctorClientResponse;
import fit.iuh.student.schedulingservice.dtos.requests.CreateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateAppointmentRequest;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.AppointmentWeekFilterResponse;
import fit.iuh.student.schedulingservice.dtos.responses.RescheduleAppointmentResponse;
import fit.iuh.student.schedulingservice.dtos.responses.TimeSlotDTO;
import fit.iuh.student.schedulingservice.entities.Appointment;
import fit.iuh.student.schedulingservice.entities.DoctorSchedule;
import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.enums.AppointmentStatus;
import fit.iuh.student.schedulingservice.exceptions.errors.NotFoundException;
import fit.iuh.student.schedulingservice.mappers.TimeSlotMapper;
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

import java.sql.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentServiceImpl implements AppointmentService {
    private final AppointmentRepository appointmentRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final UserClient userClient;
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final AppointmentEventPublisher appointmentEventPublisher;
    private final TimeSlotMapper timeSlotMapper;

    @Override
    public AppointmentResponse bookingAppointment(CreateAppointmentRequest request) {
        try {
            DoctorSchedule doctorSchedule = doctorScheduleRepository.findById(request.getScheduleId()).orElse(null);
            if (doctorSchedule.getTimeSlots().stream().noneMatch(ts -> ts.getSlotId() == request.getSlotId())) {
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
                    .appointmentDate(doctorSchedule.getWorkDate())
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
    public Page<AppointmentResponse> getAppointmentByPatientIdWithPage(String patientId, int page, int size, String sortBy, String startTime, String endTime, String sortDir) {
        try {
            if (sortBy == null || sortBy.isEmpty()) {
                sortBy = "createdAt";
            }

            Sort.Direction direction = Sort.Direction.ASC; // default
            if (sortDir != null && sortDir.equalsIgnoreCase("DESC")) {
                direction = Sort.Direction.DESC;
            }
            Date start = Date.valueOf(startTime);
            Date end = Date.valueOf(endTime);
            Sort sort = Sort.by(direction, sortBy);
            Pageable pageable = PageRequest.of(page, size, sort);
            Page<Appointment> app = appointmentRepository.findAppointmentByPatientIdWithPage(patientId, start, end, pageable);

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
            DoctorSchedule doctorSchedule = doctorScheduleRepository.findById(appointment.getDoctorId()).orElse(null);
            if (doctorSchedule == null) {
                return false;
            }
            // them lai time slot da huy vao lich lam viec cua bac si
            doctorSchedule.addTimeSlot(appointment.getTimeSlot());
            doctorScheduleRepository.save(doctorSchedule);
            appointment.setStatus(AppointmentStatus.CANCELED);
            appointmentRepository.save(appointment);
            appointmentEventPublisher.publishCancelledAppointmentEvent(AppointmentResponse.builder()
                    .appointmentId(appointmentId)
                    .doctor(userClient.getDoctorForClient(appointment.getDoctorId()))
                    .patient(userClient.getPatientForClient(appointment.getPatientId()))
                    .symptoms(appointment.getSymptoms())
                    .note(appointment.getNote())
                    .status(appointment.getStatus())
                    .timeSlot(TimeSlotDTO.builder()
                            .slotId(appointment.getTimeSlot().getSlotId())
                            .startTime(appointment.getTimeSlot().getStartTime())
                            .endTime(appointment.getTimeSlot().getEndTime())
                            .build())
                    .appointmentDate(appointment.getAppointmentDate())
                    .consultationType(appointment.getConsultationType())
                    .addressDetail(appointment.getAddressDetail())
                    .build());
            return true;
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public RescheduleAppointmentResponse rescheduleAppointment(UpdateAppointmentRequest request) {
        try {
            Appointment appointment = appointmentRepository.findById(request.getAppointmentId()).orElse(null);
            if (appointment == null) {
                throw new NotFoundException("Appointment not found");
            }
            DoctorSchedule ds = doctorScheduleRepository.findById(request.getNewScheduleId()).orElse(null);
            DoctorSchedule oldDs = doctorScheduleRepository.findById(request.getOldScheduleId()).orElse(null);
            if (ds == null) {
                throw new NotFoundException("Doctor schedule not found");
            }
            if (ds.getTimeSlots().stream().noneMatch(ts -> ts.getSlotId().equals(request.getNewSlotId()))) {
                throw new NotFoundException("Time slot not found in the doctor's schedule");
            }

            // Capture old appointment details before making changes
            Date oldAppointmentDate = appointment.getAppointmentDate();
            TimeSlot oldTimeSlot = appointment.getTimeSlot();

            // Update appointment with new details
            appointment.setAppointmentDate(ds.getWorkDate());
            appointment.setSlotId(request.getNewSlotId());
            TimeSlot newTimeSlot = timeSlotRepository.findById(request.getNewSlotId()).orElse(null);
            appointment.setTimeSlot(newTimeSlot);

            // remove time slot after reschedule
            ds.removeTimeSlot(newTimeSlot);
            // add old time slot back to old schedule
            oldDs.addTimeSlot(timeSlotRepository.findById(request.getOldSlotId()).orElse(null));
            // Save changes to repositories
            doctorScheduleRepository.save(oldDs);
            doctorScheduleRepository.save(ds);
            appointmentRepository.save(appointment);

            // Create DateAppointment objects for old and new appointment details
            RescheduleAppointmentResponse.DateAppointment oldAppointmentDetails = RescheduleAppointmentResponse.DateAppointment.builder()
                    .appointmentDate(oldAppointmentDate)
                    .timeSlot(timeSlotMapper.toTimeSlotDTO(oldTimeSlot))
                    .build();

            RescheduleAppointmentResponse.DateAppointment newAppointmentDetails = RescheduleAppointmentResponse.DateAppointment.builder()
                    .appointmentDate(appointment.getAppointmentDate())
                    .timeSlot(timeSlotMapper.toTimeSlotDTO(newTimeSlot))
                    .build();

            RescheduleAppointmentResponse ap = RescheduleAppointmentResponse.builder()
                    .appointmentId(appointment.getAppointmentId())
                    .doctor(userClient.getDoctorForClient(appointment.getDoctorId()))
                    .patient(userClient.getPatientForClient(appointment.getPatientId()))
                    .symptoms(appointment.getSymptoms())
                    .note(appointment.getNote())
                    .status(appointment.getStatus())
                    .timeSlot(TimeSlotDTO.builder()
                            .slotId(appointment.getTimeSlot().getSlotId())
                            .startTime(appointment.getTimeSlot().getStartTime())
                            .endTime(appointment.getTimeSlot().getEndTime())
                            .build())
                    .appointmentDate(appointment.getAppointmentDate())
                    .consultationType(appointment.getConsultationType())
                    .addressDetail(appointment.getAddressDetail())
                    .oldAppointment(oldAppointmentDetails)
                    .newAppointment(newAppointmentDetails)
                    .build();
            appointmentEventPublisher.publishRescheduleAppointmentEvent(ap);
            return ap;
        } catch (Exception e) {
            throw e;
        }
    }

    // Cập nhật trạng thái COMPLETED, CONFIRMED, REJECTED và NO_SHOW
    @Override
    public AppointmentResponse updateAppointmentStatus(String appointmentId, AppointmentStatus status) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) {
                throw new NotFoundException("Appointment not found");
            }
            appointment.setStatus(status);
            appointmentRepository.save(appointment);
            AppointmentResponse ap = AppointmentResponse.builder()
                    .appointmentId(appointment.getAppointmentId())
                    .doctor(userClient.getDoctorForClient(appointment.getDoctorId()))
                    .patient(userClient.getPatientForClient(appointment.getPatientId()))
                    .symptoms(appointment.getSymptoms())
                    .note(appointment.getNote())
                    .status(appointment.getStatus())
                    .timeSlot(TimeSlotDTO.builder()
                            .slotId(appointment.getTimeSlot().getSlotId())
                            .startTime(appointment.getTimeSlot().getStartTime())
                            .endTime(appointment.getTimeSlot().getEndTime())
                            .build())
                    .appointmentDate(appointment.getAppointmentDate())
                    .consultationType(appointment.getConsultationType())
                    .addressDetail(appointment.getAddressDetail())
                    .build();
            if (status == AppointmentStatus.CONFIRMED) {
                appointmentEventPublisher.publishConfirmStatusAppointmentEvent(ap);
            } else if (status == AppointmentStatus.COMPLETED) {
                appointmentEventPublisher.publishCompletedStatusAppointmentEvent(ap);
            } else if (status == AppointmentStatus.NO_SHOW) {
                appointmentEventPublisher.publishNoShowStatusAppointmentEvent(ap);
            } else {
                appointmentEventPublisher.publishConfirmStatusAppointmentEvent(ap);
            }
            return ap;
        } catch (Exception e) {
            throw e;
        }
    }


    @Override
    public AppointmentResponse getAppointmentDetailById(String appointmentId) {
        try {
            Appointment appointment = appointmentRepository.findById(appointmentId).orElse(null);
            if (appointment == null) {
                throw new NotFoundException("Appointment not found");
            } else {
                return AppointmentResponse.builder()
                        .appointmentId(appointment.getAppointmentId())
                        .doctor(userClient.getDoctorForClient(appointment.getDoctorId()))
                        .patient(userClient.getPatientForClient(appointment.getPatientId()))
                        .symptoms(appointment.getSymptoms())
                        .note(appointment.getNote())
                        .status(appointment.getStatus())
                        .timeSlot(TimeSlotDTO.builder()
                                .slotId(appointment.getTimeSlot().getSlotId())
                                .startTime(appointment.getTimeSlot().getStartTime())
                                .endTime(appointment.getTimeSlot().getEndTime())
                                .build())
                        .appointmentDate(appointment.getAppointmentDate())
                        .consultationType(appointment.getConsultationType())
                        .addressDetail(appointment.getAddressDetail())
                        .build();
            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public Page<AppointmentResponse> getAppointmentWithFilterPagination(String type, String status, int page, int size, String sortBy, String sortDir) {
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
            Page<Appointment> app = appointmentRepository.findAppointmentFilterWithPagination(type, status, pageable);

            // Check if there are any appointments before proceeding
            if (app.getContent().isEmpty()) {
                return Page.empty(pageable);
            }

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
                        .doctor(userClient.getDoctorForClient(appointment.getDoctorId()))
                        .patient(userClient.getPatientForClient(appointment.getPatientId()))
                        .symptoms(appointment.getSymptoms())
                        .note(appointment.getNote())
                        .status(appointment.getStatus())
                        .timeSlot(timeSlotDTO)
                        .appointmentDate(appointment.getAppointmentDate())
                        .consultationType(appointment.getConsultationType())
                        .addressDetail(appointment.getAddressDetail())
                        .build();
            });
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public List<AppointmentWeekFilterResponse> getAppointmentWeekFilterForDoctor(String doctorId, String weekStartDate, String weekEndDate) {
        try {
            Date start = Date.valueOf(weekStartDate);
            Date end = Date.valueOf(weekEndDate);
            List<Appointment> appointments = appointmentRepository.findAppointmentsInWeek(doctorId, start, end);
            return appointments.stream().map(appointment -> {
                // Map TimeSlot to TimeSlotDTO
                TimeSlotDTO timeSlotDTO = null;
                if (appointment.getTimeSlot() != null) {
                    timeSlotDTO = TimeSlotDTO.builder()
                            .slotId(appointment.getTimeSlot().getSlotId())
                            .startTime(appointment.getTimeSlot().getStartTime())
                            .endTime(appointment.getTimeSlot().getEndTime())
                            .build();
                }
                return AppointmentWeekFilterResponse.builder()
                        .appointmentId(appointment.getAppointmentId())
                        .note(appointment.getNote())
                        .status(appointment.getStatus())
                        .timeSlot(timeSlotDTO)
                        .patientName(userClient.getPatientForClient(appointment.getPatientId()).getFullName())
                        .date(appointment.getAppointmentDate())
                        .dayOfWeek(appointment.getDoctorSchedule().getWeekDay())
                        .build();
            }).toList();
        } catch(Exception e){
            throw e;
        }
    }
}