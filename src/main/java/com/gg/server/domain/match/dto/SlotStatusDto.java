package com.gg.server.domain.match.dto;

import com.gg.server.domain.match.type.SlotStatus;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SlotStatusDto {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public SlotStatusDto(LocalDateTime startTime, LocalDateTime endTime, SlotStatus status) {
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status.getCode();
    }

    @Override
    public String toString() {
        return "SlotStatusDto{" +
                "startTime = " + startTime +
                "endTime = " + endTime +
                "status = " + status +
                "}";
    }
}