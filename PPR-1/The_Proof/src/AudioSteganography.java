import java.io.*;
import javax.sound.sampled.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AudioSteganography {

    public static void hideText(File input, File output, String message, String key, java.util.function.Consumer<String> log) throws Exception {

        log.accept("=== AUDIO EMBEDDING PROCESS ===");

        // Step 1 Timestamp
        String timestamp = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("HH/MM/ss/dd/MM/yyyy"));

        log.accept("Step 1 - Current Timestamp: " + timestamp);

        // Step 2 Create full message
        String fullMessage = message + "#" + timestamp + "#@";

        log.accept("Step 2 - Full Message: " + fullMessage);

        // Step 3 XOR Encryption
        String encrypted = xorEncrypt(fullMessage, key);

        log.accept("Step 3 - XOR Encrypted Message");

        // Step 4 Convert to bits
        String bits = textToBits(encrypted) + "1111111111111110";

        log.accept("Step 4 - Total Bits To Embed: " + bits.length());

        AudioInputStream ais = AudioSystem.getAudioInputStream(input);

        byte[] audioBytes = ais.readAllBytes();

        log.accept("Step 5 - Audio Samples Loaded: " + audioBytes.length);

        if(bits.length() > audioBytes.length){
            throw new Exception("Message too large for audio file");
        }

        log.accept("Step 6 - Embedding bits into LSB of audio samples");

        for(int i=0;i<bits.length();i++){

            audioBytes[i] =
                    (byte)((audioBytes[i] & 0xFE) | (bits.charAt(i)-'0'));
        }

        ByteArrayInputStream bais =
                new ByteArrayInputStream(audioBytes);

        AudioInputStream outStream =
                new AudioInputStream(bais,
                        ais.getFormat(),
                        audioBytes.length);

        AudioSystem.write(outStream,
                AudioFileFormat.Type.WAVE,
                output);

        log.accept("=== AUDIO EMBEDDING COMPLETED ===");
    }

    public static String extractText(File audio, String key, java.util.function.Consumer<String> log) throws Exception {

        log.accept("=== AUDIO EXTRACTION PROCESS ===");

        AudioInputStream ais =
                AudioSystem.getAudioInputStream(audio);

        byte[] audioBytes = ais.readAllBytes();

        log.accept("Step 1 - Audio Samples Read: " + audioBytes.length);

        StringBuilder bits = new StringBuilder();

        for(byte b : audioBytes){
            bits.append(b & 1);
        }

        log.accept("Step 2 - Extracted LSB bits");

        String endMarker = "1111111111111110";

        int endIndex = bits.indexOf(endMarker);

        if(endIndex == -1){
            log.accept("No hidden message detected");
            return null;
        }

        log.accept("Step 3 - End marker detected");

        String secretBits = bits.substring(0,endIndex);

        String encrypted = bitsToText(secretBits);

        log.accept("Step 4 - Binary converted to text");

        String decrypted = xorEncrypt(encrypted,key);

        log.accept("Step 5 - XOR Decryption complete");

        return decrypted;
    }

    private static String xorEncrypt(String text, String key){

        StringBuilder output = new StringBuilder();

        for(int i=0;i<text.length();i++){

            char c = text.charAt(i);
            char k = key.charAt(i % key.length());

            output.append((char)(c ^ k));
        }

        return output.toString();
    }

    private static String textToBits(String text){

        StringBuilder bits = new StringBuilder();

        for(char c : text.toCharArray()){

            bits.append(String.format("%8s",
                    Integer.toBinaryString(c)).replace(' ','0'));
        }

        return bits.toString();
    }

    private static String bitsToText(String bits){

        StringBuilder text = new StringBuilder();

        for(int i=0;i<bits.length();i+=8){

            String byteStr = bits.substring(i,i+8);

            int val = Integer.parseInt(byteStr,2);

            text.append((char)val);
        }

        return text.toString();
    }
}