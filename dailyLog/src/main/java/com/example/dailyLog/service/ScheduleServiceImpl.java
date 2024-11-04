package com.example.dailyLog.service;

import com.example.dailyLog.dto.request.ScheduleRequestInsertDto;
import com.example.dailyLog.dto.request.ScheduleRequestUpdateDto;
import com.example.dailyLog.dto.response.ScheduleResponseDayDto;
import com.example.dailyLog.dto.response.ScheduleResponseMonthDto;
import com.example.dailyLog.dto.response.ScheduleResponseYearDto;
import com.example.dailyLog.entity.*;
import com.example.dailyLog.exception.calendarsException.CalendarsErrorCode;
import com.example.dailyLog.exception.calendarsException.CalendarsNotFoundException;
import com.example.dailyLog.exception.commonException.CommonErrorCode;
import com.example.dailyLog.exception.commonException.error.InvalidDay;
import com.example.dailyLog.exception.commonException.error.InvalidMonth;
import com.example.dailyLog.exception.commonException.error.InvalidYear;
import com.example.dailyLog.exception.scheduleException.ScheduleErrorCode;
import com.example.dailyLog.exception.scheduleException.ScheduleNotFoundException;
import com.example.dailyLog.exception.userException.UserErrorCode;
import com.example.dailyLog.exception.userException.UserNotFoundException;
import com.example.dailyLog.repository.CalendarRepository;
import com.example.dailyLog.repository.ScheduleImageRepository;
import com.example.dailyLog.repository.ScheduleRepeatRepository;
import com.example.dailyLog.repository.ScheduleRepository;
import lombok.RequiredArgsConstructor;
import org.hibernate.service.spi.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ScheduleServiceImpl implements ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final CalendarRepository calendarRepository;
    private final ScheduleImageRepository scheduleImageRepository;
    private final ScheduleRepeatRepository scheduleRepeatRepository;
    private final ImageService imageService;

    // 월달력 전체 일정 조회
    @Transactional
    @Override
    public List<ScheduleResponseMonthDto> findAllMonthSchedule(Long idx, int year, int month) {
        if (month < 1 || month > 12) {
            throw new IllegalArgumentException("Month must be between 1 and 12");
        }
        if (year < 1 || year > 9999) {
            throw new IllegalArgumentException("Year must be a positive number and within the range of valid years");
        }
        if (!calendarRepository.existsById(idx)) {
            throw new CalendarsNotFoundException(CalendarsErrorCode.CALENDARS_NOT_FOUND);
        }

        try {
            LocalDate startOfMonth = LocalDate.of(year, month, 1);
            LocalDate endOfMonth = startOfMonth.withDayOfMonth(startOfMonth.lengthOfMonth());

            List<Schedule> schedules = scheduleRepository.findByCalendarsUserIdxAndStartBetween(idx, startOfMonth.atStartOfDay(), endOfMonth.atTime(23, 59, 59));
            List<ScheduleRepeat> repeatingSchedules = scheduleRepeatRepository.findByEndDateAfter(startOfMonth);

            for (ScheduleRepeat repeat : repeatingSchedules) {
                Schedule schedule = repeat.getSchedule();
                LocalDate current = schedule.getStart().toLocalDate();

                while (!current.isAfter(endOfMonth) && !current.isAfter(repeat.getEndDate())) {
                    if (!current.isBefore(startOfMonth)) {
                        schedules.add(
                                Schedule.builder()
                                        .title(schedule.getTitle())
                                        .content(schedule.getContent())
                                        .start(current.atTime(schedule.getStart().toLocalTime()))
                                        .end(current.atTime(schedule.getEnd().toLocalTime()))
                                        .location(schedule.getLocation())
                                        .color(schedule.getColor())
                                        .calendars(schedule.getCalendars())
                                        .build()
                        );
                    }

                    switch (repeat.getRepeatType()) {
                        case DAILY:
                            current = current.plusDays(1);
                            break;
                        case WEEKLY:
                            current = current.plusWeeks(1);
                            break;
                        case MONTHLY:
                            current = current.plusMonths(1);
                            break;
                        case YEARLY:
                            current = current.plusYears(1);
                            break;
                    }
                }
            }

            return schedules.stream()
                    .map(schedule -> {
                        boolean isRepeat = scheduleRepeatRepository.findByScheduleIdx(schedule.getIdx()) != null;
                        return ScheduleResponseMonthDto.builder()
                                .title(schedule.getTitle())
                                .start(schedule.getStart())
                                .color(schedule.getColor())
                                .isRepeat(isRepeat)
                                .build();
                    })
                    .sorted(Comparator.comparing(ScheduleResponseMonthDto::getStart))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("Failed to find schedule in ScheduleService.findAllMonthSchedule", e);
        }
    }

    // 연달력 전체 일정 조회
    @Transactional
    @Override
    public List<ScheduleResponseYearDto> findAllYearSchedule(Long idx, int year) {
        if (!calendarRepository.existsById(idx)) {
            throw new CalendarsNotFoundException(CalendarsErrorCode.CALENDARS_NOT_FOUND);
        }
        if (year < 1 || year > 9999) {
            throw new InvalidYear(CommonErrorCode.INVALID_YEAR);
        }

        try {
            LocalDate startOfYear = LocalDate.of(year, 1, 1);
            LocalDate endOfYear = LocalDate.of(year, 12, 31);

            List<Schedule> schedules = scheduleRepository.findByStartBetween(startOfYear.atStartOfDay(), endOfYear.atTime(23, 59, 59));
            List<ScheduleRepeat> repeatingSchedules = scheduleRepeatRepository.findByEndDateAfter(startOfYear);

            for (ScheduleRepeat repeat : repeatingSchedules) {
                Schedule schedule = repeat.getSchedule();
                LocalDate current = schedule.getStart().toLocalDate();

                while (!current.isAfter(endOfYear) && !current.isAfter(repeat.getEndDate())) {
                    if (!current.isBefore(startOfYear)) {
                        schedules.add(
                                Schedule.builder()
                                        .title(schedule.getTitle())
                                        .content(schedule.getContent())
                                        .start(current.atTime(schedule.getStart().toLocalTime()))
                                        .end(current.atTime(schedule.getEnd().toLocalTime()))
                                        .location(schedule.getLocation())
                                        .color(schedule.getColor())
                                        .calendars(schedule.getCalendars())
                                        .build()
                        );
                    }

                    switch (repeat.getRepeatType()) {
                        case DAILY:
                            current = current.plusDays(1);
                            break;
                        case WEEKLY:
                            current = current.plusWeeks(1);
                            break;
                        case MONTHLY:
                            current = current.plusMonths(1);
                            break;
                        case YEARLY:
                            current = current.plusYears(1);
                            break;
                    }
                }
            }

            return schedules.stream()
                    .map(schedule -> {
                        boolean isRepeat = scheduleRepeatRepository.findByScheduleIdx(schedule.getIdx()) != null;
                        return ScheduleResponseYearDto.builder()
                                .start(schedule.getStart())
                                .color(schedule.getColor())
                                .isRepeat(isRepeat)
                                .build();
                    })
                    .sorted(Comparator.comparing(ScheduleResponseYearDto::getStart))
                    .collect(Collectors.toList());
        } catch (Exception e) {
            throw new ServiceException("Failed to find schedule in ScheduleService.findAllYearSchedule", e);
        }
    }

    // 개별 날짜 일정 조회
    @Transactional
    @Override
    public List<ScheduleResponseDayDto> findScheduleByDay(Long idx, int year, int month, int day) {
        if (!calendarRepository.existsById(idx)) {
            throw new CalendarsNotFoundException(CalendarsErrorCode.CALENDARS_NOT_FOUND);
        }
        if (month < 1 || month > 12) {
            throw new InvalidMonth(CommonErrorCode.INVALID_MONTH);
        }
        if (year < 1 || year > 9999) {
            throw new InvalidYear(CommonErrorCode.INVALID_YEAR);
        }
        int lastDayOfMonth = YearMonth.of(year, month).lengthOfMonth();
        if (day < 1 || day > lastDayOfMonth) {
            throw new InvalidDay(CommonErrorCode.INVALID_DAY);
        }

        try {
            LocalDate date = LocalDate.of(year, month, day);
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);

            List<Schedule> schedules = scheduleRepository.findSchedulesInDay(startOfDay, endOfDay);
            List<ScheduleRepeat> repeatingSchedules = scheduleRepeatRepository.findByEndDateAfter(date);

            for (ScheduleRepeat repeat : repeatingSchedules) {
                Schedule schedule = repeat.getSchedule();
                LocalDate current = schedule.getStart().toLocalDate();

                while (!current.isAfter(date) && !current.isAfter(repeat.getEndDate())) {
                    if (current.isEqual(date)) {
                        schedules.add(
                                Schedule.builder()
                                        .title(schedule.getTitle())
                                        .content(schedule.getContent())
                                        .start(current.atTime(schedule.getStart().toLocalTime()))
                                        .end(current.atTime(schedule.getEnd().toLocalTime()))
                                        .location(schedule.getLocation())
                                        .color(schedule.getColor())
                                        .calendars(schedule.getCalendars())
                                        .build()
                        );
                    }

                    switch (repeat.getRepeatType()) {
                        case DAILY:
                            current = current.plusDays(1);
                            break;
                        case WEEKLY:
                            current = current.plusWeeks(1);
                            break;
                        case MONTHLY:
                            current = current.plusMonths(1);
                            break;
                        case YEARLY:
                            current = current.plusYears(1);
                            break;
                    }
                }
            }

            return schedules.stream()
                    .map(schedule -> {
                        boolean isRepeat = scheduleRepeatRepository.findByScheduleIdx(schedule.getIdx()) != null;
                        return ScheduleResponseDayDto.builder()
                                .title(schedule.getTitle())
                                .content(schedule.getContent())
                                .start(schedule.getStart())
                                .end(schedule.getEnd())
                                .location(schedule.getLocation())
                                .color(schedule.getColor())
                                .isRepeat(isRepeat)
                                .build();
                    })
                    .sorted(Comparator.comparing(ScheduleResponseDayDto::getStart))
                    .collect(Collectors.toList());

        } catch (Exception e) {
            throw new ServiceException("Failed to find schedule in ScheduleService.findScheduleByDay", e);
        }
    }

    // 일정 입력
    @Transactional
    @Override
    public void saveSchedule(ScheduleRequestInsertDto scheduleRequestInsertDto, List<MultipartFile> imageFileList) {
        Calendars calendar = calendarRepository.findById(scheduleRequestInsertDto.getCalendarsIdx())
                .orElseThrow(() -> new CalendarsNotFoundException(CalendarsErrorCode.CALENDARS_NOT_FOUND));

        User user = calendar.getUser();
        if (user == null) {
            throw new UserNotFoundException(UserErrorCode.USER_NOT_FOUND);
        }

        try {
            Schedule createSchedule = Schedule.builder()
                    .title(scheduleRequestInsertDto.getTitle())
                    .content(scheduleRequestInsertDto.getContent())
                    .start(scheduleRequestInsertDto.getStart())
                    .end(scheduleRequestInsertDto.getEnd())
                    .location(scheduleRequestInsertDto.getLocation())
                    .color(scheduleRequestInsertDto.getColor())
                    .calendars(calendar)
                    .build();
            scheduleRepository.save(createSchedule);

            // 반복 일정이 설정된 경우 ScheduleRepeat 생성
            if (scheduleRequestInsertDto.getRepeatType() != null) {
                ScheduleRepeat scheduleRepeat = new ScheduleRepeat();
                scheduleRepeat.setSchedule(createSchedule);
                scheduleRepeat.setRepeatType(scheduleRequestInsertDto.getRepeatType());
                scheduleRepeat.setEndDate(scheduleRequestInsertDto.getRepeatEndDate());
                scheduleRepeatRepository.save(scheduleRepeat);
            }

            // 이미지 저장 로직
            for (MultipartFile file : imageFileList) {
                if (!file.isEmpty()) {
                    ScheduleImage scheduleImage = imageService.saveScheduleImage(file, createSchedule);
                    scheduleImage.setSchedule(createSchedule);
                    scheduleImageRepository.save(scheduleImage);
                }
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to save schedule in ScheduleService.saveSchedule", e);
        }
    }

    // 일정 수정
    @Transactional
    @Override
    public void updateSchedule(ScheduleRequestUpdateDto scheduleRequestUpdateDto, List<MultipartFile> imageFileList) {
        if (imageFileList == null) {
            imageFileList = Collections.emptyList();
        }

        Schedule updateSchedule = scheduleRepository.findById(scheduleRequestUpdateDto.getIdx())
                .orElseThrow(() -> new ScheduleNotFoundException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        try {
            // 일정 정보 업데이트
            if (scheduleRequestUpdateDto.getTitle() != null) {
                updateSchedule.setTitle(scheduleRequestUpdateDto.getTitle());
            }
            if (scheduleRequestUpdateDto.getContent() != null) {
                updateSchedule.setContent(scheduleRequestUpdateDto.getContent());
            }
            if (scheduleRequestUpdateDto.getStart() != null) {
                updateSchedule.setStart(scheduleRequestUpdateDto.getStart());
            }
            if (scheduleRequestUpdateDto.getEnd() != null) {
                updateSchedule.setEnd(scheduleRequestUpdateDto.getEnd());
            }
            if (scheduleRequestUpdateDto.getLocation() != null) {
                updateSchedule.setLocation(scheduleRequestUpdateDto.getLocation());
            }
            if (scheduleRequestUpdateDto.getColor() != null) {
                updateSchedule.setColor(scheduleRequestUpdateDto.getColor());
            }

            // 반복 정보 업데이트
            ScheduleRepeat scheduleRepeat = scheduleRepeatRepository.findByScheduleIdx(updateSchedule.getIdx());
            if (scheduleRequestUpdateDto.getRepeatType() != null) {
                if (scheduleRepeat == null) {
                    scheduleRepeat = new ScheduleRepeat();
                    scheduleRepeat.setSchedule(updateSchedule);
                }
                scheduleRepeat.setRepeatType(scheduleRequestUpdateDto.getRepeatType());
                scheduleRepeat.setEndDate(scheduleRequestUpdateDto.getRepeatEndDate());
                scheduleRepeatRepository.save(scheduleRepeat);
            } else if (scheduleRepeat != null) {
                // 반복 일정 해제 처리
                scheduleRepeatRepository.delete(scheduleRepeat);
            }

            scheduleRepository.save(updateSchedule);

            // 이미지 업데이트 로직
            for (MultipartFile file : imageFileList) {
                if (!file.isEmpty()) {
                    ScheduleImage scheduleImage = imageService.saveScheduleImage(file, updateSchedule);
                    scheduleImage.setSchedule(updateSchedule);
                    scheduleImageRepository.save(scheduleImage);
                }
            }

        } catch (Exception e) {
            throw new ServiceException("Failed to update schedule in ScheduleService.updateSchedule", e);
        }
    }

    // 일정 삭제
    @Transactional
    @Override
    public void deleteSchedule(Long idx) {
        Schedule schedule = scheduleRepository.findById(idx)
                .orElseThrow(() -> new ScheduleNotFoundException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));
        try {
            // 관련된 반복 일정 삭제
            ScheduleRepeat scheduleRepeat = scheduleRepeatRepository.findByScheduleIdx(schedule.getIdx());
            if (scheduleRepeat != null) {
                scheduleRepeatRepository.delete(scheduleRepeat);
            }

            scheduleRepository.delete(schedule);
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schedule in ScheduleService.deleteSchedule", e);
        }
    }
}
