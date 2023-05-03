package com.gg.server.domain.noti.data;

import com.gg.server.domain.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface NotiRepository extends JpaRepository<Noti, Long>, NotiRepositoryCustom {
    List<Noti> findByUser(User user);
    Noti findByIdAndUser(Long notiId, User user);
    List<Noti> findAllByUser(User user);
    List<Noti> findAllByUserOrderByIdDesc(User user);
    void deleteAllByUser(User user);
}
