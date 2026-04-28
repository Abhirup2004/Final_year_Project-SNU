import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class TextSteganography {

    public static void hideText(File input, File output, String message, String key,
                               java.util.function.Consumer<String> log) throws Exception {

        log.accept("=== TEXT EMBEDDING PROCESS ===");

        String content = new String(java.nio.file.Files.readAllBytes(input.toPath()));

        log.accept("Step 1 - Original file loaded");

        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH/MM/ss/dd/MM/yyyy"));

        String fullMessage = message + "#" + timestamp + "#@";

        log.accept("Step 2 - Full Message: " + fullMessage);

        String encrypted = xorEncrypt(fullMessage, key);

        log.accept("Step 3 - XOR Encryption complete");

        String stegoContent = content + "\n<!--STEGO:" + encrypted + "-->";

        log.accept("Step 4 - Message appended inside file");

        java.nio.file.Files.write(output.toPath(), stegoContent.getBytes());

        log.accept("=== TEXT EMBEDDING COMPLETED ===");
    }

    public static String extractText(File input, String key,
                                    java.util.function.Consumer<String> log) throws Exception {

        log.accept("=== TEXT EXTRACTION PROCESS ===");

        String content = new String(java.nio.file.Files.readAllBytes(input.toPath()));

        int start = content.indexOf("<!--STEGO:");
        int end = content.indexOf("-->");

        if (start == -1 || end == -1) {
            log.accept("No hidden message found");
            return null;
        }

        String encrypted = content.substring(start + 10, end);

        log.accept("Step 1 - Encrypted message found");

        String decrypted = xorEncrypt(encrypted, key);

        log.accept("Step 2 - XOR Decryption complete");

        return decrypted;
    }

    private static String xorEncrypt(String text, String key) {

        StringBuilder output = new StringBuilder();

        for (int i = 0; i < text.length(); i++) {
            char c = text.charAt(i);
            char k = key.charAt(i % key.length());
            output.append((char) (c ^ k));
        }

        return output.toString();
    }
}