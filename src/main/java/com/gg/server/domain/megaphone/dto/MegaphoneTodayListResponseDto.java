package com.gg.server.domain.megaphone.dto;

import com.gg.server.domain.megaphone.redis.MegaphoneRedis;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MegaphoneTodayListResponseDto {
    private Long megaphoneId;
    private String content;
    private String intraId;

    public MegaphoneTodayListResponseDto(MegaphoneRedis megaphoneRedis) {
        this.megaphoneId = megaphoneRedis.getId();
        this.content = megaphoneRedis.getContent();
        this.intraId = megaphoneRedis.getIntraId();
    }
}
