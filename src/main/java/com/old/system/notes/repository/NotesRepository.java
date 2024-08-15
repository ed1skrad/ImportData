package com.old.system.notes.repository;

import com.old.system.notes.model.Notes;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface NotesRepository extends JpaRepository<Notes, String> {

    @Query("SELECT n FROM Notes n JOIN n.client c WHERE c.guid = :clientGuid AND c.agency = :agency AND n.createdDateTime BETWEEN :dateFrom AND :dateTo")
    List<Notes> findByClientGuidAndAgencyAndCreatedDateTimeBetween(
            @Param("clientGuid") String clientGuid,
            @Param("agency") String agency,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo);
}
