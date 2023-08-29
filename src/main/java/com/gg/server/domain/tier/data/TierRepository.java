package com.gg.server.domain.tier.data;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TierRepository extends JpaRepository<Tier, Long> {
    Optional<Tier> findById(Long tierId);

    List<Tier> findAll();
}