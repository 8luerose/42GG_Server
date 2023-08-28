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
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TierService {
    private final TierRepository tierRepository;
    private final RankRepository rankRepository;
    private final SeasonFindService seasonFindService;

    public void updateAllTier() {
//        Season season = seasonFindService.findCurrentSeason(LocalDateTime.now());
        Season season = seasonFindService.findSeasonById(1L);
        List<Rank> rankList = rankRepository.findAllBySeasonIdOrderByPppDesc(season.getId());
        Integer totalNumber = rankRepository.countAllBySeasonId(season.getId());
        List<Tier> tierList = tierRepository.findAll();

        int cnt = 0;
        int top10percent = (int) (totalNumber * 0.1);
        int top5percent = (int) (totalNumber * 0.05);
        for (Rank rank : rankList) {
            if (cnt < top5percent) {
                rank.setTier(tierList.get(5));
                cnt += 1;
            } else if (cnt < top10percent) {
                rank.setTier(tierList.get(4));
                cnt += 1;
            } else if (rank.getPpp() > 1200) {
                rank.setTier(tierList.get(3));
            } else if (rank.getPpp() > 1100) {
                rank.setTier(tierList.get(2));
            } else if (rank.getPpp() > 1000) {
                rank.setTier(tierList.get(1));
            } else {
                rank.setTier(tierList.get(0));
            }
        }
    }
}
