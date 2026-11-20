package com.meridian.payments.andina;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.meridian.payments.SupportFiles;

@SpringBootTest
@ActiveProfiles("test")
class AndinaSignatureVerifierTest {

    @Autowired
    private AndinaSignatureVerifier verifier;

    @Test
    void acceptsOfficialTestVector() throws IOException {
        byte[] body = SupportFiles.referenceSample("andina-co", "samples", "02-transaccion-aprobada.json");
        String timestamp = "2026-11-28T14:03:11-05:00";
        String signature = "MFQjfviySi6CjqbWtQcy5spKtXxHIxAvmg5W3PTzPJk=";
        assertTrue(verifier.isValid(body, timestamp, signature));
    }

    @Test
    void rejectsWrongSecret() throws IOException {
        byte[] body = SupportFiles.referenceSample("andina-co", "samples", "02-transaccion-aprobada.json");
        assertFalse(verifier.isValid(body, "2026-11-28T14:03:11-05:00", "invalid"));
    }
}