package com.sejinzx.enrollmentSystem.classmgmt.controller;

import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestAddClass;
import com.sejinzx.enrollmentSystem.classmgmt.dto.RequestUpdateClass;
import com.sejinzx.enrollmentSystem.classmgmt.entity.ClassState;
import com.sejinzx.enrollmentSystem.classmgmt.service.ClassService;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassService classService;

    @Tag(name = "Class 등록")
    @PostMapping("/reg")
    public ResponseEntity<?> createClass(@RequestBody RequestAddClass requestAddClass,
                                         Authentication auth) {

        String userId = auth.getName();

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(
                        Map.of("classSeq", classService.createClass(requestAddClass, userId),
                        "message", "강의 등록 완료" )
                );

    }

    @Tag(name = "Class 수정")
    @PutMapping("/update/{classSeq}")
    public ResponseEntity<?> updateClass(@PathVariable Long classSeq,
                                         @RequestBody RequestUpdateClass requestUpdateClass,
                                         Authentication auth) {

        String userId = auth.getName();

        return ResponseEntity.ok(
                Map.of("classSeq", classService.updateClass(classSeq, requestUpdateClass, userId),
                        "message", "강의 수정 완료" )
        );

    }

    @Tag(name = "Class 목록 조회")
    @GetMapping("/list")
    public ResponseEntity<?> getListClass(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) ClassState state) {

        return ResponseEntity.ok(
                classService.getListClass(page, size, state)
        );

    }

    @Tag(name = "Class 상세 조회")
    @GetMapping("/detail/{classSeq}")
    public ResponseEntity<?> getDetailClass(@PathVariable Long classSeq) {

        return ResponseEntity.ok(
                classService.getDetailClass(classSeq)
        );

    }

    @Tag(name = "Class 삭제")
    @PutMapping("/delete/{classSeq}")
    public ResponseEntity<?> deleteClass(@PathVariable Long classSeq,
                                         Authentication auth) {

        String userId = auth.getName();

        return ResponseEntity.ok(
                Map.of("classSeq", classService.deleteClass(classSeq, userId),
                        "message", "강의 삭제 완료" )
        );

    }

}