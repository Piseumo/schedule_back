package com.example.dailyLog.controller;

import com.example.dailyLog.dto.request.ScheduleRequestInsertDto;
import com.example.dailyLog.dto.request.ScheduleRequestUpdateDto;
import com.example.dailyLog.dto.response.ScheduleResponseDayDto;
import com.example.dailyLog.dto.response.ScheduleResponseMonthDto;
import com.example.dailyLog.dto.response.ScheduleResponseYearDto;
import com.example.dailyLog.entity.Calendars;
import com.example.dailyLog.exception.calendarsException.CalendarsErrorCode;
import com.example.dailyLog.exception.calendarsException.CalendarsNotFoundException;
import com.example.dailyLog.repository.CalendarRepository;
import com.example.dailyLog.service.ScheduleService;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.List;


@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;

    // 홈페이지 첫화면 기본 창(월달력 조회)
    @GetMapping("/{calendarIdx}/{year}/{month}")
    public ResponseEntity<List<ScheduleResponseMonthDto>> getAllMonthSchedule(
            @PathVariable(name = "calendarIdx") Long calendarIdx,
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month) {

        List<ScheduleResponseMonthDto> scheduleResponseMonthDto = scheduleService.findAllMonthSchedule(calendarIdx, year, month);
        return ResponseEntity.ok(scheduleResponseMonthDto);
    }

    // 연달력 전체 일정 조회
    @GetMapping("/{calendarIdx}/{year}")
    public ResponseEntity<List<ScheduleResponseYearDto>> getAllYearSchedule(
            @PathVariable(name = "calendarIdx") Long calendarIdx,
            @PathVariable(name = "year") int year) {

        List<ScheduleResponseYearDto> scheduleResponseYearDto = scheduleService.findAllYearSchedule(calendarIdx, year);
        return ResponseEntity.ok(scheduleResponseYearDto);
    }

    // 일달력 조회
    @GetMapping("/{calendarIdx}/{year}/{month}/{day}")
    public ResponseEntity<List<ScheduleResponseDayDto>> getAllDaySchedule(
            @PathVariable(name = "calendarIdx") Long calendarIdx,
            @PathVariable(name = "year") int year,
            @PathVariable(name = "month") int month,
            @PathVariable(name = "day") int day) {

        List<ScheduleResponseDayDto> scheduleResponseDayDto = scheduleService.findScheduleByDay(calendarIdx, year, month, day);
        return ResponseEntity.ok(scheduleResponseDayDto);
    }


    // 일정 1개 조회
    @GetMapping("/{idx}")
    public ResponseEntity<ScheduleResponseDayDto> getOneSchedule(
            @PathVariable(name = "idx") Long idx){
        ScheduleResponseDayDto scheduleResponseDayDto = scheduleService.findScheduleByOne(idx);
        return ResponseEntity.ok(scheduleResponseDayDto);
    }


    // 일정 입력
    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> saveSchedule(
            @RequestPart(name = "scheduleRequest") @Valid ScheduleRequestInsertDto scheduleRequestInsertDto,
            @RequestPart(name = "imageFiles", required = false) @Schema(type = "array", format = "binary", description = "이미지 파일들") List<MultipartFile> imageFileList) {

        if (imageFileList == null) {
            imageFileList = Collections.emptyList();
        }

        scheduleService.saveSchedule(scheduleRequestInsertDto, imageFileList);
        return ResponseEntity.status(HttpStatus.CREATED).body("Schedule created successfully");
    }


    // 일정 수정
    @PostMapping(value = "/update", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updateSchedule(@RequestPart(name = "scheduleRequest") @Valid ScheduleRequestUpdateDto scheduleRequestUpdateDto,
                                                 @RequestPart(name = "imageFiles", required = false) List<MultipartFile> imageFileList) {
        scheduleService.updateSchedule(scheduleRequestUpdateDto, imageFileList);
        return ResponseEntity.status(HttpStatus.OK).body("Schedule updated successfully");
    }


    // 일정 삭제
    @DeleteMapping(value = "/delete/{idx}")
    public ResponseEntity<String> deleteSchedule(
            @PathVariable(name = "idx") Long idx,
            @RequestParam(name = "deleteAllRepeats", required = false, defaultValue = "false") boolean deleteAllRepeats,
            @RequestParam(name = "deleteOnlyThis", required = false, defaultValue = "false") boolean deleteOnlyThis,
            @RequestParam(name = "deleteAfter", required = false, defaultValue = "false") boolean deleteAfter) {
        scheduleService.deleteSchedule(idx, deleteAllRepeats, deleteOnlyThis, deleteAfter);
        return ResponseEntity.status(HttpStatus.OK).body("Schedule deleted successfully");
    }

    //드래그 앤 드랍 관련
    @PutMapping("/{idx}")
    public ResponseEntity<String> updateScheduleDate(
            @PathVariable(name = "idx") Long scheduleIdx,
            @RequestBody ScheduleRequestUpdateDto scheduleRequestUpdateDto) {

        scheduleService.updateScheduleDate(scheduleIdx, scheduleRequestUpdateDto);
        return ResponseEntity.ok("Schedule date updated successfully");
    }

}
