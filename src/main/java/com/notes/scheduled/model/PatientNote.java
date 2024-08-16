package com.notes.scheduled.model;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "patient_note")
public class PatientNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDateTime createdDateTime;

    @Column(nullable = false)
    private LocalDateTime lastModifiedDateTime;

    @ManyToOne
    @JoinColumn(name = "created_by_user_id", referencedColumnName = "id")
    private CompanyUser createdByUser;

    @ManyToOne
    @JoinColumn(name = "last_modified_by_user_id", referencedColumnName = "id")
    private CompanyUser lastModifiedByUser;

    private String note;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private PatientProfile patient;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getCreatedDateTime() {
        return createdDateTime;
    }

    public void setCreatedDateTime(LocalDateTime createdDateTime) {
        this.createdDateTime = createdDateTime;
    }

    public LocalDateTime getLastModifiedDateTime() {
        return lastModifiedDateTime;
    }

    public void setLastModifiedDateTime(LocalDateTime lastModifiedDateTime) {
        this.lastModifiedDateTime = lastModifiedDateTime;
    }

    public CompanyUser getCreatedByUser() {
        return createdByUser;
    }

    public void setCreatedByUser(CompanyUser createdByUser) {
        this.createdByUser = createdByUser;
    }

    public CompanyUser getLastModifiedByUser() {
        return lastModifiedByUser;
    }

    public void setLastModifiedByUser(CompanyUser lastModifiedByUser) {
        this.lastModifiedByUser = lastModifiedByUser;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public PatientProfile getPatient() {
        return patient;
    }

    public void setPatient(PatientProfile patient) {
        this.patient = patient;
    }
}

