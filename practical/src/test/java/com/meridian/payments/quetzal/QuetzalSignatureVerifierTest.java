package com.meridian.payments.quetzal;

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
class QuetzalSignatureVerifierTest {

    @Autowired
    private QuetzalSignatureVerifier verifier;

    @Test
    void acceptsOfficialTestVector() throws IOException {
        byte[] body = SupportFiles.referenceSample("quetzal-mx", "samples", "01-charge-succeeded.json");
        String header = "t=1795888803,v1=2d07770582c02f15235ac4e027b7732beef86be103c574a1c3b988a4275f5031";
        assertTrue(verifier.isValid(body, header));
    }

    @Test
    void rejectsTamperedBody() throws IOException {
        byte[] body = SupportFiles.referenceSample("quetzal-mx", "samples", "01-charge-succeeded.json");
        String header = "t=1795888803,v1=0000000000000000000000000000000000000000000000000000000000000000";
        assertFalse(verifier.isValid(body, header));
    }
}