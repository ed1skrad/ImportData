package com.notes.scheduled.aspect;

import com.notes.scheduled.model.PatientNote;
import com.notes.scheduled.model.PatientProfile;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.concurrent.atomic.AtomicInteger;

@Aspect
@Component
public class ImportServiceAspect {

    private static final Logger logger = LoggerFactory.getLogger(ImportServiceAspect.class);

    final AtomicInteger importedProfiles = new AtomicInteger(0);
    final AtomicInteger updatedProfiles = new AtomicInteger(0);
    final AtomicInteger deletedProfiles = new AtomicInteger(0);
    final AtomicInteger importedNotes = new AtomicInteger(0);
    final AtomicInteger updatedNotes = new AtomicInteger(0);

    @Pointcut("execution(* com.notes.scheduled.repository.PatientProfileRepository.save(..))")
    public void savePatientProfile() {}

    @Pointcut("execution(* com.notes.scheduled.repository.PatientProfileRepository.delete(..))")
    public void deletePatientProfile() {}

    @Pointcut("execution(* com.notes.scheduled.repository.PatientNoteRepository.save(..))")
    public void savePatientNote() {}

    @Pointcut("execution(* com.notes.scheduled.repository.PatientNoteRepository.delete(..))")
    public void deletePatientNote() {}

    @Pointcut("execution(* com.notes.scheduled.repository.CompanyUserRepository.save(..))")
    public void saveCompanyUser() {}

    @AfterReturning("savePatientProfile()")
    public void afterSavePatientProfile(JoinPoint joinPoint) {
        if (joinPoint.getArgs()[0] instanceof PatientProfile profile) {
            if (profile.getId() == null) {
                importedProfiles.incrementAndGet();
                log(joinPoint, "Patient profile imported.");
            } else {
                updatedProfiles.incrementAndGet();
                log(joinPoint, "Patient profile updated.");
            }
        }
    }

    @AfterReturning("deletePatientProfile()")
    public void afterDeletePatientProfile(JoinPoint joinPoint) {
        deletedProfiles.incrementAndGet();
        log(joinPoint, "Patient profile deleted.");
    }

    @AfterReturning("savePatientNote()")
    public void afterSavePatientNote(JoinPoint joinPoint) {
        if (joinPoint.getArgs()[0] instanceof PatientNote note) {
            if (note.getId() == null) {
                importedNotes.incrementAndGet();
                log(joinPoint, "Patient note imported.");
            } else {
                updatedNotes.incrementAndGet();
                log(joinPoint, "Patient note updated.");
            }
        }
    }

    @AfterReturning("deletePatientNote()")
    public void afterDeletePatientNote(JoinPoint joinPoint) {
        deletedProfiles.incrementAndGet();
        log(joinPoint, "Patient note deleted.");
    }

    @AfterReturning("saveCompanyUser()")
    public void afterSaveCompanyUser(JoinPoint joinPoint) {
        log(joinPoint, "Company user saved or updated.");
    }

    @Before("execution(* com.notes.scheduled.service.ImportService.importData(..))")
    public void resetCounters() {
        importedProfiles.set(0);
        updatedProfiles.set(0);
        deletedProfiles.set(0);
        importedNotes.set(0);
        updatedNotes.set(0);
    }

    @After("execution(* com.notes.scheduled.service.ImportService.importData(..))")
    public void logStatistics(JoinPoint joinPoint) {
        log(joinPoint, String.format("Import completed: %d profiles imported, %d profiles updated, %d profiles deleted, %d notes imported, %d notes updated.",
                importedProfiles.get(), updatedProfiles.get(), deletedProfiles.get(), importedNotes.get(), updatedNotes.get()));
    }

    private void log(JoinPoint joinPoint, String message) {
        logger.info("[{}] {}", joinPoint.getSignature().getName(), message);
    }
}
