package com.sejinzx.enrollmentSystem.enroll.controller;

import com.sejinzx.enrollmentSystem.enroll.service.EnrollService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jdk.jfr.Description;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
                .body(
                        Map.of("enrollSeq", enrollService.createEnroll(classSeq, userId),
                                "message", "success registration")
                );

    }

    @Tag(name = "수강 신청 취소")
    @PutMapping("/delete/{enrollSeq}")
    public ResponseEntity<?> deleteClass(@PathVariable Long enrollSeq,
                                         Authentication auth) {

        String userId = auth.getName();

        return ResponseEntity.ok(
                Map.of("enrollSeq", enrollService.deleteEnroll(enrollSeq, userId),
                        "message", "success cancel")
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

    @Tag(name = "강의 별 수강생 목록 조회")
    @Description("본인이 개설한 강의의 수강생 목록만 조회 가능")
    @GetMapping("/{classSeq}/userList")
    public ResponseEntity<?> getClassEnrollUserList(@RequestParam(defaultValue = "0") int page,
                                                    @RequestParam(defaultValue = "10") int size,
                                                    @PathVariable Long classSeq, Authentication auth){

        String userId = auth.getName();

        return ResponseEntity.ok(
                enrollService.getClassEnrollUserList(page, size, classSeq, userId)
        );

    }

}
