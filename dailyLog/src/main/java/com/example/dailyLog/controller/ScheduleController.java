package com.example.dailyLog.controller;

import com.example.dailyLog.dto.ScheduleRequestDto;
import com.example.dailyLog.dto.ScheduleResponseDto;
import com.example.dailyLog.dto.UserResponseDto;
import com.example.dailyLog.entity.Schedule;
import com.example.dailyLog.service.ScheduleService;
import com.example.dailyLog.service.ScheduleServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/schedule")
@RequiredArgsConstructor
public class ScheduleController {

    private final ScheduleService scheduleService;


    // 홈페이지 첫화면 기본 창(월달력 조회)
    @GetMapping
    public ResponseEntity<List<Schedule>> getAllMonthSchedule(){

        List<Schedule> list = scheduleService.findAllMonthSchedule();
        return ResponseEntity.ok(list);
    }


    // 연달력 전체 일정 조회
    @GetMapping("/{year}")
    public ResponseEntity<List<Schedule>> getAllYearSchedule(@PathVariable int year){

        List<Schedule> list = scheduleService.findAllYearSchedule(year);
        return ResponseEntity.ok(list);
    }



//    @PostMapping(value = "/aa")
//    public String saveSchedule(@RequestBody Schedule schedule){
//
//    scheduleService.saveSchedule(schedule);
//
//    return "일정이 등록되었습니다.";
//    }


}
