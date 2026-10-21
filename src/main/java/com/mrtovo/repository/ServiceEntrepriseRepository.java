package com.mrtovo.repository;

import com.mrtovo.entity.ServiceEntreprise;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ServiceEntrepriseRepository extends JpaRepository<ServiceEntreprise, Integer> {

    List<ServiceEntreprise> findByDepartementIdOrderByLibelleAsc(Integer idDepartement);

    List<ServiceEntreprise> findAllByOrderByDepartementLibelleAscLibelleAsc();
}
