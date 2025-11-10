package fit.iut.student.paymentservice.utils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class PayOSSignatureUtils {

    private static final String HMAC_SHA256 = "HmacSHA256";

    /**
     * Generate HMAC SHA256 signature
     */
    public static String generateSignature(String data, String secretKey) {
        try {
            Mac mac = Mac.getInstance(HMAC_SHA256);
            SecretKeySpec secretKeySpec = new SecretKeySpec(
                    secretKey.getBytes(StandardCharsets.UTF_8),
                    HMAC_SHA256
            );
            mac.init(secretKeySpec);
            byte[] hash = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Error generating HMAC signature", e);
        }
    }

    /**
     * Verify HMAC SHA256 signature
     */
    public static boolean verifySignature(String data, String signature, String secretKey) {
        String expectedSignature = generateSignature(data, secretKey);
        return expectedSignature.equalsIgnoreCase(signature);
    }

    /**
     * Convert bytes to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /**
     * Create data string for PayOS webhook verification
     * Format: orderCode + amount + description + transactionDateTime
     */
    public static String createWebhookDataString(String orderCode, Integer amount,
                                                  String description, String transactionDateTime) {
        return String.format("%s%d%s%s",
                orderCode != null ? orderCode : "",
                amount != null ? amount : 0,
                description != null ? description : "",
                transactionDateTime != null ? transactionDateTime : ""
        );
    }
}
