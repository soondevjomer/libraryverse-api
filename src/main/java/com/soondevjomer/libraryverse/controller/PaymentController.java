package com.soondevjomer.libraryverse.controller;

import com.soondevjomer.libraryverse.dto.PaymentRequestDto;
import com.soondevjomer.libraryverse.dto.PaymentResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

@RestController
@RequestMapping("/payments")
@RequiredArgsConstructor
public class PaymentController {

    @PostMapping("/simulate")
    public ResponseEntity<?> simulatePayment(@RequestBody PaymentRequestDto paymentRequestDto) {

        try {
            Thread.sleep(1000);
        } catch (InterruptedException ignored) {

        }

//        boolean success = true;
//
//        if (success) {
//            return ResponseEntity.ok(new PaymentResponseDto(true, "Payment completed successfully"));
//        } else {
//            return ResponseEntity.ok(new PaymentResponseDto(false, "Payment failed"));
//        }
        return ResponseEntity.ok(new PaymentResponseDto(true, "Payment completed successfully"));
    }
}
