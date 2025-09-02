package fit.iuh.student.schedulingservice.services.Impl;

import fit.iuh.student.schedulingservice.dtos.requests.BulkCreateScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.requests.CreateDoctorScheduleRequest;
import fit.iuh.student.schedulingservice.dtos.requests.UpdateDoctorSchedule;
import fit.iuh.student.schedulingservice.dtos.responses.BulkCreateDoctorScheduleResponse;
import fit.iuh.student.schedulingservice.dtos.responses.DoctorScheduleResponse;
import fit.iuh.student.schedulingservice.entities.DoctorSchedule;
import fit.iuh.student.schedulingservice.entities.TimeSlot;
import fit.iuh.student.schedulingservice.exceptions.errors.BadRequestException;
import fit.iuh.student.schedulingservice.exceptions.errors.DuplicationDoctorScheduleException;
import fit.iuh.student.schedulingservice.exceptions.errors.UserNotFoundException;
import fit.iuh.student.schedulingservice.mappers.DoctorScheduleMapper;
import fit.iuh.student.schedulingservice.repositories.DoctorScheduleRepository;
import fit.iuh.student.schedulingservice.repositories.TimeSlotRepository;
import fit.iuh.student.schedulingservice.services.DoctorScheduleService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class DoctorScheduleServiceImpl implements DoctorScheduleService {
    private final DoctorScheduleRepository doctorScheduleRepository;
    private final TimeSlotRepository timeSlotRepository;
    private final DoctorScheduleMapper doctorScheduleMapper;

    public DoctorScheduleServiceImpl(DoctorScheduleRepository doctorScheduleRepository, TimeSlotRepository timeSlotRepository, DoctorScheduleMapper doctorScheduleMapper) {
        this.doctorScheduleRepository = doctorScheduleRepository;
        this.timeSlotRepository = timeSlotRepository;
        this.doctorScheduleMapper = doctorScheduleMapper;
    }

    @Override
    @Transactional
    public DoctorScheduleResponse createDoctorSchedule(CreateDoctorScheduleRequest request) {
        if(request.getWorkDate().toLocalDate().isBefore(LocalDate.now())){
            throw new BadRequestException("Work date cannot be in the past");
        }
        // Kiểm tra xem doctor đã có schedule cho workDate này chưa
        boolean exists = doctorScheduleRepository.existsByDoctorIdAndWorkDate(
                request.getDoctorId(),
                request.getWorkDate()
        );
        if (exists) {
            throw new DuplicationDoctorScheduleException("Doctor " + request.getDoctorId() + " already has a schedule on " + request.getWorkDate());
        }

        // Tạo DoctorSchedule từ request
        DoctorSchedule doctorSchedule = new DoctorSchedule();
        doctorSchedule.setDoctorId(request.getDoctorId());
        doctorSchedule.setWeekDay(request.getWeekDay());
        doctorSchedule.setWorkDate(request.getWorkDate());
        doctorSchedule.setAvailable(request.isAvailable());

        // Xử lý time slots
        List<Integer> timeSlots = request.getTimeSlotIds();
        if (timeSlots != null && !timeSlots.isEmpty()) {
            // Lấy các timeSlot
            List<TimeSlot> existingTimeSlots = timeSlotRepository.findAllById(timeSlots);
            // thêm time slot vào doctorSchedule
            for (TimeSlot timeSlot : existingTimeSlots) {
                    doctorSchedule.addTimeSlot(timeSlot);
            }
        }

        try {
            // Lưu doctorSchedule vào cơ sở dữ liệu
            DoctorSchedule savedSchedule = doctorScheduleRepository.save(doctorSchedule);

            // Tạo response trả về
            return DoctorScheduleResponse.builder()
                    .scheduleId(savedSchedule.getScheduleId())
                    .doctorId(savedSchedule.getDoctorId())
                    .weekDay(savedSchedule.getWeekDay())
                    .workDate(savedSchedule.getWorkDate())
                    .isAvailable(savedSchedule.isAvailable())
                    .timeSlots(new ArrayList<>(savedSchedule.getTimeSlots()))
                    .build();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    @Transactional
    public BulkCreateDoctorScheduleResponse bulkCreateDoctorSchedule(BulkCreateScheduleRequest request) {
        List<DoctorScheduleResponse> createdSchedules = new ArrayList<>();
        if (request.getDateSchedules() == null || request.getDateSchedules().isEmpty()) {
            return BulkCreateDoctorScheduleResponse.builder()
                    .createdSchedules(createdSchedules)
                    .build();
        }
        // Lấy tất cả TimeSlot
        List<TimeSlot> allTimeSlots = timeSlotRepository.findAll();
        for (BulkCreateScheduleRequest.DateScheduleDTO dateSchedule : request.getDateSchedules()) {
            boolean exists = doctorScheduleRepository.existsByDoctorIdAndWorkDate(
                    request.getDoctorId(),
                    dateSchedule.getWorkDate()
            );
            if (exists) {
                throw new DuplicationDoctorScheduleException("Doctor " + request.getDoctorId() +
                        " already has schedule on " + dateSchedule.getWorkDate());
            }
            DoctorSchedule doctorSchedule = new DoctorSchedule();
            doctorSchedule.setDoctorId(request.getDoctorId());
            doctorSchedule.setWeekDay(dateSchedule.getWeekDay());
            doctorSchedule.setWorkDate(dateSchedule.getWorkDate());
            doctorSchedule.setAvailable(true);
            doctorSchedule.setTimeSlots(allTimeSlots);
            DoctorSchedule savedSchedule = doctorScheduleRepository.save(doctorSchedule);
            DoctorScheduleResponse response = DoctorScheduleResponse.builder()
                    .scheduleId(savedSchedule.getScheduleId())
                    .doctorId(savedSchedule.getDoctorId())
                    .weekDay(savedSchedule.getWeekDay())
                    .workDate(savedSchedule.getWorkDate())
                    .isAvailable(savedSchedule.isAvailable())
                    .timeSlots(new ArrayList<>(savedSchedule.getTimeSlots()))
                    .build();
            createdSchedules.add(response);
        }
        return BulkCreateDoctorScheduleResponse.builder()
                .createdSchedules(createdSchedules)
                .build();
    }

    @Override
    public DoctorScheduleResponse getDoctorScheduleByDate(String doctorId, Date date) {
        return doctorScheduleMapper.doctorScheduleToResponse(doctorScheduleRepository.getDoctorScheduleByDate(doctorId,date));
    }

    @Override
    public List<String> getDoctorIdsByDate(Date date) {
        return doctorScheduleRepository.findDoctorIdsByDate(date);
    }

    @Override
    public boolean updateDoctorSchedule(UpdateDoctorSchedule request) {
        try{
            DoctorSchedule doctorSchedule = doctorScheduleRepository.findById(request.getScheduleId())
                    .orElseThrow(() -> new UserNotFoundException("Doctor schedule not found"));
            List<TimeSlot> timeSlotList = timeSlotRepository.findAllById(request.getTimeSlotIds());
            for (TimeSlot timeSlot : timeSlotList) {
                doctorSchedule.removeTimeSlot(timeSlot);
            }
            doctorScheduleRepository.save(doctorSchedule);
            return true;
        }catch (Exception e){
            throw e;
        }
    }
}
