package com.notes.scheduled.repository;

import com.notes.scheduled.model.PatientProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientProfileRepository extends JpaRepository<PatientProfile, Long> {
    List<PatientProfile> findByStatusIdIn(List<Short> statusIds);
    Optional<PatientProfile> findByOldClientGuidContaining(String oldClientGuid);
}
