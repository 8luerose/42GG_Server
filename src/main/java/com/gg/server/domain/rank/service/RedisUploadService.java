package com.gg.server.domain.rank.service;

import com.gg.server.domain.rank.data.RankRepository;
import com.gg.server.domain.rank.redis.RankRedis;
import com.gg.server.domain.rank.redis.RankRedisRepository;
import com.gg.server.domain.rank.redis.RedisKeyManager;
import com.gg.server.domain.season.data.Season;
import com.gg.server.domain.season.data.SeasonRepository;
import com.gg.server.domain.season.exception.SeasonNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class RedisUploadService {
    private final RankRedisRepository redisRepository;
    private final SeasonRepository seasonRepository;
    private final RankRepository rankRepository;
    private final RedisTemplate<String,  Object> redisTemplate;

    @PostConstruct
    @Transactional
    public void uploadRedis() {
        Season currentSeason = seasonRepository.findCurrentSeason(LocalDateTime.now())
                .orElseThrow(() -> new SeasonNotFoundException());
        String hashKey = RedisKeyManager.getHashKey(currentSeason.getId());
        if(redisRepository.isEmpty(hashKey))
            upload();
    }

    private void upload() {

        redisTemplate.executePipelined((RedisCallback<Object>) connection ->{
            seasonRepository.findAll().forEach(season -> {
                String hashKey = RedisKeyManager.getHashKey(season.getId());
                String zSetKey = RedisKeyManager.getZSetKey(season.getId());
                rankRepository.findAllBySeasonId(season.getId()).forEach(rank -> {
                    RankRedis rankRedis = RankRedis.from(rank);
                    connection.hSet(keySerializer().serialize(hashKey),
                            hashKeySerializer().serialize(rank.getUser().getId().toString()),
                            hashValueSerializer().serialize(rankRedis));
                    if (rank.getWins() + rankRedis.getLosses() != 0){
                        connection.zAdd(keySerializer().serialize(zSetKey), rank.getPpp(),
                                valueSerializer().serialize(rank.getUser().getId().toString()));
                    }
                });
            });
            return null;
        });
    }

    private RedisSerializer valueSerializer() {
        return redisTemplate.getValueSerializer();
    }

    private RedisSerializer hashValueSerializer() {
        return redisTemplate.getHashValueSerializer();
    }

    private RedisSerializer hashKeySerializer() {
        return redisTemplate.getHashKeySerializer();
    }

    private RedisSerializer keySerializer() {
        return redisTemplate.getKeySerializer();
    }

}
