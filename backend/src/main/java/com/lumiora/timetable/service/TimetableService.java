package com.lumiora.timetable.service;

import java.util.List;

import org.springframework.security.core.Authentication;

import com.lumiora.timetable.dto.TimetableCreateRequest;
import com.lumiora.timetable.dto.TimetableResponse;
import com.lumiora.timetable.dto.TimetableUpdateRequest;

public interface TimetableService {

    TimetableResponse create(
            TimetableCreateRequest request,
            Authentication authentication
    );

    List<TimetableResponse> findAll(
            Authentication authentication
    );

    List<TimetableResponse> findByBatch(
            Long batchId,
            Authentication authentication
    );

    List<TimetableResponse> findByTrainer(
            Long trainerId,
            Authentication authentication
    );

    List<TimetableResponse> findMyTimetable(
            Authentication authentication
    );

    TimetableResponse update(
            Long id,
            TimetableUpdateRequest request,
            Authentication authentication
    );

    void deactivate(
            Long id,
            Authentication authentication
    );
}