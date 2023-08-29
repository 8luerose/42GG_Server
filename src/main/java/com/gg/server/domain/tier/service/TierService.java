package com.gg.server.domain.tier.service;

import com.gg.server.domain.rank.data.Rank;
import com.gg.server.domain.rank.data.RankRepository;
import com.gg.server.domain.season.data.Season;
import com.gg.server.domain.season.service.SeasonFindService;
import com.gg.server.domain.tier.data.Tier;
import com.gg.server.domain.tier.data.TierRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class TierService {
    private final TierRepository tierRepository;
    private final RankRepository rankRepository;
    private final SeasonFindService seasonFindService;

    public void updateAllTier() {
        Season season = seasonFindService.findCurrentSeason(LocalDateTime.now());
        List<Rank> rankList = rankRepository.findAllBySeasonIdOrderByPppDesc(season.getId());
        Integer totalNumber = rankRepository.countAllBySeasonId(season.getId());
        List<Tier> tierList = tierRepository.findAll();

        // 1, 2, 3등 무지개
        int top10percentPpp = rankList.get((int) (totalNumber * 0.1)).getPpp();
        int top5percentPpp = rankList.get((int) (totalNumber * 0.05)).getPpp();
        for (Rank rank : rankList) {
            if (rank.getPpp() < 980) {
                rank.updateTier(tierList.get(0));
            } else if (rank.getPpp() < 1020) {
                rank.updateTier(tierList.get(1));
            } else if (rank.getPpp() < 1060) {
                rank.updateTier(tierList.get(2));
            } else if (rank.getPpp() < 1100) {
                rank.updateTier(tierList.get(3));
            } else if (rank.getPpp() > top10percentPpp && rank.getPpp() > 1100) {
                rank.updateTier(tierList.get(4));
            } else if (rank.getPpp() > top5percentPpp && rank.getPpp() > 1100) {
                rank.updateTier(tierList.get(5));
            } else {
                rank.updateTier(tierList.get(4));
            }
        }
    }
}
