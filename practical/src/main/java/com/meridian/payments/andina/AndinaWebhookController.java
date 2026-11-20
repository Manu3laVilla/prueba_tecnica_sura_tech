package com.meridian.payments.andina;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.meridian.payments.core.IntakeService;

@RestController
@RequestMapping("/webhooks/andina")
public class AndinaWebhookController {

    private final AndinaSignatureVerifier verifier;
    private final AndinaWebhookMapper mapper;
    private final IntakeService intakeService;

    public AndinaWebhookController(
            AndinaSignatureVerifier verifier, AndinaWebhookMapper mapper, IntakeService intakeService) {
        this.verifier = verifier;
        this.mapper = mapper;
        this.intakeService = intakeService;
    }

    @PostMapping
    public ResponseEntity<Void> receive(
            @RequestBody byte[] body,
            @RequestHeader(value = "X-Andina-Timestamp", required = false) String timestamp,
            @RequestHeader(value = "X-Andina-Signature", required = false) String signature) {
        if (!verifier.isValid(body, timestamp, signature)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        try {
            intakeService.accept(mapper.map(body));
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException ex) {
            return ResponseEntity.badRequest().build();
        } catch (RuntimeException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}