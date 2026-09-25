package com.lumiora.timetable.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lumiora.dto.response.ApiResponse;

import com.lumiora.timetable.dto.TimetableCreateRequest;
import com.lumiora.timetable.dto.TimetableResponse;
import com.lumiora.timetable.dto.TimetableUpdateRequest;
import com.lumiora.timetable.service.TimetableService;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/timetable")
@RequiredArgsConstructor
public class TimetableController {

    private final TimetableService timetableService;


    // =========================================================
    // CREATE
    // =========================================================

    @PostMapping
    public ResponseEntity<ApiResponse<TimetableResponse>>
            create(
                    @Valid @RequestBody TimetableCreateRequest request,
                    Authentication authentication
            ) {

        TimetableResponse response =
                timetableService.create(
                        request,
                        authentication
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(
                        ApiResponse.success(
                                "Timetable entry created successfully",
                                response
                        )
                );
    }


    // =========================================================
    // GET ALL
    // =========================================================

    @GetMapping
    public ResponseEntity<
            ApiResponse<List<TimetableResponse>>>
            getAll(
                    Authentication authentication
            ) {

        List<TimetableResponse> responses =
                timetableService.findAll(
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Timetable fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // MY TIMETABLE
    // =========================================================

    @GetMapping("/me")
    public ResponseEntity<
            ApiResponse<List<TimetableResponse>>>
            getMyTimetable(
                    Authentication authentication
            ) {

        List<TimetableResponse> responses =
                timetableService.findMyTimetable(
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "My timetable fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // BY BATCH
    // =========================================================

    @GetMapping("/batch/{batchId}")
    public ResponseEntity<
            ApiResponse<List<TimetableResponse>>>
            getByBatch(
                    @PathVariable Long batchId,
                    Authentication authentication
            ) {

        List<TimetableResponse> responses =
                timetableService.findByBatch(
                        batchId,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Batch timetable fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // BY TRAINER
    // =========================================================

    @GetMapping("/trainer/{trainerId}")
    public ResponseEntity<
            ApiResponse<List<TimetableResponse>>>
            getByTrainer(
                    @PathVariable Long trainerId,
                    Authentication authentication
            ) {

        List<TimetableResponse> responses =
                timetableService.findByTrainer(
                        trainerId,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Trainer timetable fetched successfully",
                        responses
                )
        );
    }


    // =========================================================
    // UPDATE
    // =========================================================

    @PutMapping("/{id}")
    public ResponseEntity<
            ApiResponse<TimetableResponse>>
            update(
                    @PathVariable Long id,
                    @Valid @RequestBody TimetableUpdateRequest request,
                    Authentication authentication
            ) {

        TimetableResponse response =
                timetableService.update(
                        id,
                        request,
                        authentication
                );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Timetable entry updated successfully",
                        response
                )
        );
    }


    // =========================================================
    // DEACTIVATE
    // =========================================================

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>>
            deactivate(
                    @PathVariable Long id,
                    Authentication authentication
            ) {

        timetableService.deactivate(
                id,
                authentication
        );

        return ResponseEntity.ok(
                ApiResponse.success(
                        "Timetable entry deactivated successfully",
                        null
                )
        );
    }
}