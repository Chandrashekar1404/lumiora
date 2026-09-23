package com.lumiora.trainer.service;

import java.util.List;
import java.util.Optional;

import com.lumiora.attendance.entity.Attendance;
import com.lumiora.batch.entity.Batch;
import com.lumiora.entity.auth.User;

public interface TrainerService {

    List<User> findAllTrainers();

    List<User> findAllTrainersByOrganization(
            Long organizationId
    );

    Optional<User> findTrainerById(Long id);

    Optional<User> findTrainerByIdAndOrganization(
            Long id,
            Long organizationId
    );

    List<Batch> findTrainerBatches(
            Long organizationId,
            Long trainerId
    );

    List<Attendance> findTrainerAttendance(
            Long organizationId,
            Long trainerId
    );

    Optional<User> findByPhone(String phone);

    User save(User trainer);
}