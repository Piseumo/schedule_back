package com.example.dailyLog.service;

import com.example.dailyLog.constant.RepeatType;
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

            return schedules.stream()
                    .map(schedule -> ScheduleResponseMonthDto.builder()
                            .title(schedule.getTitle())
                            .start(schedule.getStart())
                            .color(schedule.getColor())
                            .build())
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

            return schedules.stream()
                    .map(schedule -> ScheduleResponseYearDto.builder()
                            .start(schedule.getStart())
                            .color(schedule.getColor())
                            .build())
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

            List<Schedule> schedules = scheduleRepository.findSchedulesInDay(startOfDay, endOfDay, idx);

            return schedules.stream()
                    .map(schedule -> ScheduleResponseDayDto.builder()
                            .idx(schedule.getIdx())
                            .title(schedule.getTitle())
                            .content(schedule.getContent())
                            .start(schedule.getStart())
                            .end(schedule.getEnd())
                            .location(schedule.getLocation())
                            .color(schedule.getColor())
                            .images(schedule.getScheduleImages().stream()
                                    .map(ScheduleImage::getImgUrl)
                                    .collect(Collectors.toList()))
                            .build())
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
            LocalDateTime currentStart = scheduleRequestInsertDto.getStart();
            LocalDateTime currentEnd = scheduleRequestInsertDto.getEnd();
            Long repeatGroupId = scheduleRequestInsertDto.getRepeatType() == RepeatType.NONE ? null : System.currentTimeMillis(); // 반복 그룹 ID 생성

            do {
                Schedule createSchedule = Schedule.builder()
                        .title(scheduleRequestInsertDto.getTitle())
                        .content(scheduleRequestInsertDto.getContent())
                        .start(currentStart)
                        .end(currentEnd)
                        .location(scheduleRequestInsertDto.getLocation())
                        .color(scheduleRequestInsertDto.getColor())
                        .calendars(calendar)
                        .repeatType(scheduleRequestInsertDto.getRepeatType())
                        .repeatEndDate(scheduleRequestInsertDto.getRepeatEndDate())
                        .repeatGroupId(repeatGroupId)
                        .build();
                scheduleRepository.save(createSchedule);

                //이미지 저장 로직
                for (MultipartFile file : imageFileList) {
                    if (!file.isEmpty()) {
                        ScheduleImage scheduleImage = imageService.saveScheduleImage(file, createSchedule);
                        scheduleImage.setSchedule(createSchedule);
                        scheduleImageRepository.save(scheduleImage);
                    }
                }

                if (scheduleRequestInsertDto.getRepeatType() == RepeatType.NONE) {
                    break; // 반복 없음 처리
                }

                switch (scheduleRequestInsertDto.getRepeatType()) {
                    case DAILY:
                        currentStart = currentStart.plusDays(1);
                        currentEnd = currentEnd.plusDays(1);
                        break;
                    case WEEKLY:
                        currentStart = currentStart.plusWeeks(1);
                        currentEnd = currentEnd.plusWeeks(1);
                        break;
                    case MONTHLY:
                        currentStart = currentStart.plusMonths(1);
                        currentEnd = currentEnd.plusMonths(1);
                        break;
                    case YEARLY:
                        currentStart = currentStart.plusYears(1);
                        currentEnd = currentEnd.plusYears(1);
                        break;
                    case NONE:
                        break;
                    default:
                        throw new IllegalArgumentException("Invalid repeat type");
                }
            } while (!currentStart.toLocalDate().isAfter(scheduleRequestInsertDto.getRepeatEndDate()));

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
            if (scheduleRequestUpdateDto.getRepeatType() == RepeatType.NONE) {
                scheduleRepository.deleteAfterDate(updateSchedule.getCalendars().getIdx(), updateSchedule.getStart());
                updateSchedule.setRepeatType(RepeatType.NONE);
                updateSchedule.setRepeatEndDate(null);
                updateSchedule.setRepeatGroupId(null);
            } else {
                // 기존 반복 일정 삭제 로직 수행 후 새로운 반복 일정 생성
                scheduleRepository.deleteAfterDate(updateSchedule.getCalendars().getIdx(), updateSchedule.getStart());

                LocalDateTime currentStart = scheduleRequestUpdateDto.getStart();
                LocalDateTime currentEnd = scheduleRequestUpdateDto.getEnd();
                Long newRepeatGroupId = System.currentTimeMillis();
                do {
                    Schedule newSchedule = Schedule.builder()
                            .title(scheduleRequestUpdateDto.getTitle())
                            .content(scheduleRequestUpdateDto.getContent())
                            .start(currentStart)
                            .end(currentEnd)
                            .location(scheduleRequestUpdateDto.getLocation())
                            .color(scheduleRequestUpdateDto.getColor())
                            .calendars(updateSchedule.getCalendars())
                            .repeatType(scheduleRequestUpdateDto.getRepeatType())
                            .repeatGroupId(newRepeatGroupId)
                            .build();
                    scheduleRepository.save(newSchedule);

                    // 반복 설정에 따른 날짜 계산
                    switch (scheduleRequestUpdateDto.getRepeatType()) {
                        case DAILY:
                            currentStart = currentStart.plusDays(1);
                            currentEnd = currentEnd.plusDays(1);
                            break;
                        case WEEKLY:
                            currentStart = currentStart.plusWeeks(1);
                            currentEnd = currentEnd.plusWeeks(1);
                            break;
                        case MONTHLY:
                            currentStart = currentStart.plusMonths(1);
                            currentEnd = currentEnd.plusMonths(1);
                            break;
                        case YEARLY:
                            currentStart = currentStart.plusYears(1);
                            currentEnd = currentEnd.plusYears(1);
                            break;
                        default:
                            throw new IllegalArgumentException("Invalid repeat type");
                    }
                } while (!currentStart.toLocalDate().isAfter(scheduleRequestUpdateDto.getRepeatEndDate()));
            }
            // 이미지 삭제
            List<String> deleteImageList = scheduleRequestUpdateDto.getDeletedImageList();
            if (deleteImageList != null && !deleteImageList.isEmpty()) {
                for (String imageId : deleteImageList) {
                    scheduleRepository.deleteByImgUrl(imageId);
                }
            }

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
    public void deleteSchedule(Long scheduleId, boolean deleteAllRepeats, boolean deleteOnlyThis, boolean deleteAfter) {
        Schedule schedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new ScheduleNotFoundException(ScheduleErrorCode.SCHEDULE_NOT_FOUND));

        try {
            if (deleteAllRepeats) {
                // 반복 일정 전체 삭제 로직
                List<Schedule> repeatSchedules = scheduleRepository.findByRepeatGroupId(schedule.getRepeatGroupId());
                scheduleRepository.deleteAll(repeatSchedules);
            } else if (deleteAfter) {
                // 현재 일정 포함 이후 일정 모두 삭제 로직
                scheduleRepository.deleteAfterDate(schedule.getRepeatGroupId(), schedule.getStart());
            } else if (deleteOnlyThis) {
                // 현재 일정만 삭제 로직
                scheduleRepository.delete(schedule);
            }
        } catch (Exception e) {
            throw new ServiceException("Failed to delete schedule in ScheduleService.deleteSchedule", e);
        }
    }
}
