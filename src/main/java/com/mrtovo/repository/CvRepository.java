package com.mrtovo.repository;

import com.mrtovo.entity.Cv;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CvRepository extends JpaRepository<Cv, Integer> {

    Optional<Cv> findFirstByCandidatureIdOrderByDateDepotDesc(Integer idCandidature);

    @Query("select cv from Cv cv join fetch cv.candidature c join fetch c.offre o join fetch c.compte order by cv.dateDepot desc")
    List<Cv> findTousAvecCandidature();
}
