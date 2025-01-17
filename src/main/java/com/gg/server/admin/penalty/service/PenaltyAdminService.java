package com.gg.server.admin.penalty.service;

import com.gg.server.admin.penalty.data.PenaltyAdminRepository;
import com.gg.server.domain.penalty.data.Penalty;
import com.gg.server.domain.penalty.redis.RedisPenaltyUser;
import com.gg.server.admin.penalty.data.PenaltyUserAdminRedisRepository;
import com.gg.server.admin.penalty.dto.PenaltyListResponseDto;
import com.gg.server.admin.penalty.dto.PenaltyUserResponseDto;
import com.gg.server.domain.penalty.exception.PenaltyExpiredException;
import com.gg.server.domain.penalty.exception.PenaltyNotFoundException;
import com.gg.server.domain.penalty.exception.RedisPenaltyUserNotFoundException;
import com.gg.server.domain.penalty.type.PenaltyType;
import com.gg.server.domain.user.data.User;
import com.gg.server.domain.user.service.UserFindService;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PenaltyAdminService {
    private final PenaltyUserAdminRedisRepository penaltyUserAdminRedisRepository;
    private final UserFindService userFindService;
    private final PenaltyAdminRepository penaltyRepository;

    @Transactional
    public void givePenalty(String intraId, Integer penaltyTime, String reason) {
        User user = userFindService.findByIntraId(intraId);
        Optional<RedisPenaltyUser> redisPenaltyUser = penaltyUserAdminRedisRepository.findByIntraId(intraId);
        LocalDateTime releaseTime;
        RedisPenaltyUser penaltyUser;
        Penalty penalty;
        LocalDateTime now = LocalDateTime.now();
        if (redisPenaltyUser.isPresent()) {
            releaseTime = redisPenaltyUser.get().getReleaseTime().plusHours(penaltyTime);
            penaltyUser = new RedisPenaltyUser(intraId, redisPenaltyUser.get().getPenaltyTime() + penaltyTime * 60,
                    releaseTime, redisPenaltyUser.get().getStartTime(), reason);
            penalty = new Penalty(user, PenaltyType.NOSHOW, reason, redisPenaltyUser.get().getReleaseTime(), penaltyTime * 60);
        } else {
            releaseTime = now.plusHours(penaltyTime);
            penaltyUser = new RedisPenaltyUser(intraId, penaltyTime * 60, releaseTime, now, reason);
            penalty = new Penalty(user, PenaltyType.NOSHOW, reason, now, penaltyTime * 60);
        }
        penaltyRepository.save(penalty);
        penaltyUserAdminRedisRepository.addPenaltyUser(penaltyUser, releaseTime);
    }


    @Transactional(readOnly = true)
    public PenaltyListResponseDto getAllPenalties(Pageable pageable, Boolean current) {
        Page<Penalty> allPenalties;
        if (current) {
            allPenalties = penaltyRepository.findAllCurrent(pageable, LocalDateTime.now());
        } else {
            allPenalties = penaltyRepository.findAll(pageable);
        }
        Page<PenaltyUserResponseDto> responseDtos = allPenalties.map(PenaltyUserResponseDto::new);
        return new PenaltyListResponseDto(responseDtos.getContent(), responseDtos.getTotalPages());
    }


    @Transactional
    public void deletePenalty(Long penaltyId) {
        Penalty penalty = penaltyRepository.findById(penaltyId).orElseThrow(()
        -> new PenaltyNotFoundException());
        if (penalty.getStartTime().plusMinutes(penalty.getPenaltyTime()).isBefore(LocalDateTime.now())) {
            throw new PenaltyExpiredException();
        }
        RedisPenaltyUser penaltyUser = penaltyUserAdminRedisRepository
                .findByIntraId(penalty.getUser().getIntraId()).orElseThrow(()
                -> new RedisPenaltyUserNotFoundException());
        penaltyUserAdminRedisRepository.deletePenaltyInUser(penaltyUser,
                penalty.getPenaltyTime());//redis 시간 줄여주기
        //뒤에 있는 penalty 시간 당겨주기
        modifyStartTimeOfAfterPenalties(penalty);
        penaltyRepository.delete(penalty);
    }

    @Transactional(readOnly = true)
    public PenaltyListResponseDto getAllPenaltiesByIntraId(Pageable pageable, String intraId, Boolean current) {
        Page<Penalty> allPenalties;
        if (current) {
            allPenalties = penaltyRepository.findAllCurrentByIntraId(pageable, LocalDateTime.now(), intraId);
        } else {
            allPenalties = penaltyRepository.findAllByIntraId(pageable, intraId);
        }
        Page<PenaltyUserResponseDto> responseDtos = allPenalties.map(PenaltyUserResponseDto::new);
        return new PenaltyListResponseDto(responseDtos.getContent(), responseDtos.getTotalPages());
    }

    private void modifyStartTimeOfAfterPenalties(Penalty penalty) {
        List<Penalty> afterPenalties = penaltyRepository.findAfterPenaltiesByUser(penalty.getUser().getId(),
                penalty.getStartTime());
        LocalDateTime newStartTime;
        if (penalty.getStartTime().isAfter(LocalDateTime.now())) {
            newStartTime = penalty.getStartTime();
        } else {
            newStartTime = LocalDateTime.now();
        }
        for (Penalty afterPenalty : afterPenalties) {
            afterPenalty.updateStartTime(newStartTime);
            newStartTime = newStartTime.plusMinutes(afterPenalty.getPenaltyTime());
        }
    }
}
