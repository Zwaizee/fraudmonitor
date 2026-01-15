package com.exam.fraudmonitorapp.controller;

import com.exam.fraudmonitorapp.model.dto.FraudAlertDto;
import com.exam.fraudmonitorapp.service.FraudDetectionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/fraud-alerts")
public class FraudAlertController {

    private final FraudDetectionService fraudService;

    public FraudAlertController( FraudDetectionService fraudService) {
        this.fraudService = fraudService;
    }

    @GetMapping
    public List<FraudAlertDto> alertList(@RequestParam(defaultValue = "OPEN") String status) {
        return fraudService.alertList(status);
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<?> close(@PathVariable Long id) {
        FraudAlertDto closed = fraudService.closeAlert(id);
        return ResponseEntity.ok(closed);
    }


}
