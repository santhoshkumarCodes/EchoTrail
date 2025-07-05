package com.echotrail.capsulems.controller;

import com.echotrail.capsulems.DTO.*;
import com.echotrail.capsulems.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/capsules")
@RequiredArgsConstructor
public class CapsuleController {

    private final CapsuleService capsuleService;

    @PostMapping
    public ResponseEntity<CapsuleResponse> createCapsule(
            @RequestHeader("X-UserId") Long userId,
            @RequestBody CapsuleRequest request
    ) {
        CapsuleResponse createdCapsule = capsuleService.createCapsule(userId, request);
        return new ResponseEntity<>(createdCapsule, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CapsuleResponse> getCapsule(
            @RequestHeader("X-UserId") Long userId,
            @PathVariable Long id
    ) {
        return ResponseEntity.ok(capsuleService.getCapsule(userId, id));
    }

    @GetMapping
    public ResponseEntity<List<CapsuleResponse>> getUserCapsules(
            @RequestHeader("X-UserId") Long userId,
            @RequestParam(required = false) Boolean unlocked
    ) {
        return ResponseEntity.ok(capsuleService.getUserCapsules(userId, unlocked));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCapsule(
            @RequestHeader("X-UserId") Long userId,
            @PathVariable Long id
    ) {
        capsuleService.deleteCapsule(userId, id);
        return ResponseEntity.noContent().build();
    }
}