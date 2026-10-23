package com.mrtovo.repository;

import com.mrtovo.entity.HistoriqueStatut;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Integer> {

    List<HistoriqueStatut> findByEntiteAndIdEntiteOrderByDateChangementAscIdAsc(String entite, Integer idEntite);
}
