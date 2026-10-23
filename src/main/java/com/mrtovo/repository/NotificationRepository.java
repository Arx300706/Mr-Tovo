package com.mrtovo.repository;

import com.mrtovo.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {

    List<Notification> findByCompteIdOrderByDateCreationDesc(Integer idCompte);

    List<Notification> findByEmployeIdOrderByDateCreationDesc(Integer idEmploye);

    long countByCompteIdAndLueFalse(Integer idCompte);

    long countByEmployeIdAndLueFalse(Integer idEmploye);
}
