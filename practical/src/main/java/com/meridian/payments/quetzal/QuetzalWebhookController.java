package com.meridian.payments.quetzal;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meridian.payments.core.IntakeDecision;
import com.meridian.payments.core.IntakeService;

@RestController
@RequestMapping("/webhooks/quetzal")
public class QuetzalWebhookController {

    private final QuetzalSignatureVerifier verifier;
    private final QuetzalWebhookMapper mapper;
    private final IntakeService intakeService;

    public QuetzalWebhookController(
            QuetzalSignatureVerifier verifier, QuetzalWebhookMapper mapper, IntakeService intakeService) {
        this.verifier = verifier;
        this.mapper = mapper;
        this.intakeService = intakeService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody byte[] body, @RequestHeader(value = "Quetzal-Signature", required = false) String signature) {
        if (!verifier.isValid(body, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            intakeService.accept(mapper.map(body));
            return ResponseEntity.ok().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}