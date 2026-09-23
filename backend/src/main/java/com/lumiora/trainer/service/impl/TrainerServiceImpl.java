package com.lumiora.trainer.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.lumiora.attendance.entity.Attendance;
import com.lumiora.attendance.repository.AttendanceRepository;
import com.lumiora.batch.entity.Batch;
import com.lumiora.batch.repository.BatchRepository;
import com.lumiora.entity.auth.User;
import com.lumiora.repository.UserRepository;
import com.lumiora.trainer.service.TrainerService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class TrainerServiceImpl implements TrainerService {

    private final UserRepository userRepository;

    private final BatchRepository batchRepository;

    private final AttendanceRepository attendanceRepository;


    @Override
    public List<User> findAllTrainers() {

        return userRepository.findAllByRole_Name(
                "TRAINER"
        );
    }


    @Override
    public List<User> findAllTrainersByOrganization(
            Long organizationId
    ) {

        return userRepository
                .findAllByOrganization_IdAndRole_Name(
                        organizationId,
                        "TRAINER"
                );
    }


    @Override
    public Optional<User> findTrainerById(
            Long id
    ) {

        return userRepository
                .findById(id)
                .filter(this::isTrainer);
    }


    @Override
    public Optional<User> findTrainerByIdAndOrganization(
            Long id,
            Long organizationId
    ) {

        return userRepository
                .findByIdAndOrganization_Id(
                        id,
                        organizationId
                )
                .filter(this::isTrainer);
    }


    @Override
    public List<Batch> findTrainerBatches(
            Long organizationId,
            Long trainerId
    ) {

        return batchRepository
                .findAllByOrganization_IdAndTrainer_Id(
                        organizationId,
                        trainerId
                );
    }


    @Override
    public List<Attendance> findTrainerAttendance(
            Long organizationId,
            Long trainerId
    ) {

        return attendanceRepository
                .findAllByOrganization_IdAndBatch_Trainer_Id(
                        organizationId,
                        trainerId
                );
    }


    @Override
    public Optional<User> findByPhone(
            String phone
    ) {

        return userRepository.findByPhone(phone);
    }


    @Override
    public User save(
            User trainer
    ) {

        return userRepository.save(trainer);
    }


    private boolean isTrainer(
            User user
    ) {

        return user.getRole() != null
                && "TRAINER".equals(
                        user.getRole().getName()
                );
    }
}