package com.example.dailyLog.dto.response;

import com.example.dailyLog.constant.Color;
import com.example.dailyLog.entity.Calendars;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
public class ScheduleResponseMonthDto {

    private Long idx; // 스케줄 ID 추가
    private String title;

    private LocalDateTime start;
    private LocalDateTime end;


    private Color color;

}
