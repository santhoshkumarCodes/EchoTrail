package com.echotrail.capsulems.controller;

import com.echotrail.capsulems.DTO.CapsuleChainDTO;
import com.echotrail.capsulems.DTO.LinkNextRequest;
import com.echotrail.capsulems.DTO.LinkPreviousRequest;
import com.echotrail.capsulems.service.CapsuleChainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/capsule-chains")
@RequiredArgsConstructor
public class CapsuleChainController {

    private final CapsuleChainService capsuleChainService;

    @GetMapping("/{capsuleId}")
    public ResponseEntity<CapsuleChainDTO> getCapsuleChainById(@PathVariable Long capsuleId, @RequestHeader("X-UserId") Long userId) {
        return capsuleChainService.getCapsuleChainById(capsuleId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{capsuleId}/previous")
    public ResponseEntity<CapsuleChainDTO> getPreviousCapsule(@PathVariable Long capsuleId, @RequestHeader("X-UserId") Long userId) {
        return capsuleChainService.getPreviousCapsule(capsuleId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{capsuleId}/next")
    public ResponseEntity<CapsuleChainDTO> getNextCapsule(@PathVariable Long capsuleId, @RequestHeader("X-UserId") Long userId) {
        return capsuleChainService.getNextCapsule(capsuleId, userId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/link/previous")
    public ResponseEntity<Void> setPreviousCapsule(@RequestBody LinkPreviousRequest linkDTO, @RequestHeader("X-UserId") Long userId) {
        capsuleChainService.setPreviousCapsuleId(linkDTO.getCapsuleId(), linkDTO.getPreviousCapsuleId(), userId);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/link/next")
    public ResponseEntity<Void> setNextCapsule(@RequestBody LinkNextRequest linkDTO, @RequestHeader("X-UserId") Long userId) {
        capsuleChainService.setNextCapsuleId(linkDTO.getCapsuleId(), linkDTO.getNextCapsuleId(), userId);
        return ResponseEntity.ok().build();
    }
}
