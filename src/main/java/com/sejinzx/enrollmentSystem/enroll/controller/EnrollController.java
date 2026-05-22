package com.sejinzx.enrollmentSystem.enroll.controller;

import com.sejinzx.enrollmentSystem.enroll.service.EnrollService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/enroll")
@RequiredArgsConstructor
public class EnrollController {

    private final EnrollService enrollService;

    @Tag(name = "수강 신청")
    @PostMapping("/apply/{classSeq}")
    public ResponseEntity<?> createEnroll(@PathVariable Long classSeq,
                                          Authentication auth) {

        String userId = auth.getName();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(enrollService.createEnroll(classSeq, userId));

    }

    @Tag(name = "수강 신청 취소")
    @PutMapping("/delete/{enrollSeq}")
    public ResponseEntity<?> deleteClass(@PathVariable Long enrollSeq,
                                         Authentication auth) {

        String userId = auth.getName();

        return ResponseEntity.ok(
                enrollService.deleteEnroll(enrollSeq, userId)
        );

    }

    @Tag(name = "내 수강 신청 목록 조회")
    @GetMapping("/myList")
    public ResponseEntity<?> getListEnroll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication auth) {

        String userId = auth.getName();

        return ResponseEntity.ok(
                enrollService.getMyListEnroll(page, size, userId)
        );

    }

}
