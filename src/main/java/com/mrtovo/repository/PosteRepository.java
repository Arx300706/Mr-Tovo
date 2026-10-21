package com.mrtovo.repository;

import com.mrtovo.entity.Poste;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PosteRepository extends JpaRepository<Poste, Integer> {

    List<Poste> findByDepartementIdAndEstActifTrueOrderByLibelleAsc(Integer idDepartement);

    List<Poste> findByServiceIdAndEstOccupeFalseAndEstActifTrueOrderByLibelleAsc(Integer idService);

    List<Poste> findAllByOrderByDepartementLibelleAscLibelleAsc();
}
