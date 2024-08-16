package com.notes.scheduled.repository;

import com.notes.scheduled.model.PatientNote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientNoteRepository extends JpaRepository<PatientNote, Long> {
    Optional<PatientNote> findByNoteAndPatientId(String note, Long patientId);
}
