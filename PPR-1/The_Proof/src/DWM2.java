import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.imageio.ImageIO;

public class DWM2 extends JFrame {
    private JTextField watermarkField;
    private JPasswordField passwordField;
    private JTextArea logArea;
    private JButton embedButton, extractButton, loadImageButton, saveImageButton;
    private JLabel imageLabel, statusLabel, timestampLabel;
    private BufferedImage originalImage, watermarkedImage;
    private File currentImageFile;

    public DWM2() {
        initializeGUI();
    }

    private void initializeGUI() {
        setTitle("DWM2 - Row-Based Digital Watermarking (256 Watermarks Max)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create main panels
        JPanel topPanel = createInputPanel();
        JPanel centerPanel = createImagePanel();
        JPanel rightPanel = createLogPanel();
        JPanel bottomPanel = createButtonPanel();

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        add(bottomPanel, BorderLayout.SOUTH);

        pack();
        setLocationRelativeTo(null);
        setResizable(true);
    }

    private JPanel createInputPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBorder(new TitledBorder("Input Parameters"));
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Watermark input (16 chars max)
        gbc.gridx = 0;
        gbc.gridy = 0;
        panel.add(new JLabel("Watermark (max 16 chars):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        watermarkField = new JTextField(20);
        panel.add(watermarkField, gbc);

        // Password input (determines row 0-255)
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Password (selects row 0-255):"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        passwordField = new JPasswordField(20);
        panel.add(passwordField, gbc);

        // Current timestamp display
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.fill = GridBagConstraints.NONE;
        panel.add(new JLabel("Current Time:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        timestampLabel = new JLabel(getCurrentTimestamp());
        timestampLabel.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        panel.add(timestampLabel, gbc);

        // Update timestamp every second
        Timer timer = new Timer(1000, _ -> timestampLabel.setText(getCurrentTimestamp()));
        timer.start();

        return panel;
    }

    private JPanel createImagePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Image Preview (256x256 BMP)"));

        imageLabel = new JLabel("No image loaded", SwingConstants.CENTER);
        imageLabel.setPreferredSize(new Dimension(280, 280));
        imageLabel.setBorder(BorderFactory.createLoweredBevelBorder());
        panel.add(imageLabel, BorderLayout.CENTER);

        statusLabel = new JLabel("Status: Ready");
        panel.add(statusLabel, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createLogPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(new TitledBorder("Algorithm Log & MD5/ASCII Details"));

        logArea = new JTextArea(25, 40);
        logArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        logArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(logArea);
        panel.add(scrollPane, BorderLayout.CENTER);

        JButton clearLogButton = new JButton("Clear Log");
        clearLogButton.addActionListener(_ -> logArea.setText(""));
        panel.add(clearLogButton, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel createButtonPanel() {
        JPanel panel = new JPanel(new FlowLayout());

        loadImageButton = new JButton("Load 256x256 BMP Image");
        loadImageButton.addActionListener(this::loadImage);

        embedButton = new JButton("Embed Watermark");
        embedButton.addActionListener(this::embedWatermark);
        embedButton.setEnabled(false);

        extractButton = new JButton("Extract & Verify Watermark");
        extractButton.addActionListener(this::extractWatermark);
        extractButton.setEnabled(false);

        saveImageButton = new JButton("Save Watermarked Image");
        saveImageButton.addActionListener(this::saveImage);
        saveImageButton.setEnabled(false);

        panel.add(loadImageButton);
        panel.add(embedButton);
        panel.add(extractButton);
        panel.add(saveImageButton);

        return panel;
    }

    private String getCurrentTimestamp() {
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH/MM/ss/dd/MM/yyyy");
        return now.format(formatter);
    }

    private void loadImage(ActionEvent e) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "BMP Images", "bmp"));

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                currentImageFile = fileChooser.getSelectedFile();
                originalImage = ImageIO.read(currentImageFile);

                if (originalImage.getWidth() != 256 || originalImage.getHeight() != 256) {
                    JOptionPane.showMessageDialog(this,
                            "Image must be exactly 256x256 pixels!", "Invalid Image Size",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Display image
                ImageIcon icon = new ImageIcon(originalImage.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
                imageLabel.setIcon(icon);
                imageLabel.setText("");

                embedButton.setEnabled(true);
                extractButton.setEnabled(true);
                statusLabel.setText("Status: Image loaded successfully");
                log("=== IMAGE LOADED ===");
                log("File: " + currentImageFile.getName());
                log("Dimensions: " + originalImage.getWidth() + "x" + originalImage.getHeight());
                log("");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error loading image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private String calculateMD5Hash(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hashBytes = md.digest(input.getBytes());
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 algorithm not available", e);
        }
    }

    private int calculateXORFromHash(String hash) {
        int result = Integer.parseInt(hash.substring(0, 2), 16); // Start with first byte

        // XOR all subsequent byte pairs
        for (int i = 2; i < hash.length(); i += 2) {
            int nextByte = Integer.parseInt(hash.substring(i, i + 2), 16);
            result ^= nextByte;
        }

        return result; // Will be 0-255
    }

    private int calculateXORFirst12Bytes(String hash) {
        int result = Integer.parseInt(hash.substring(0, 2), 16); // Start with first byte

        // XOR first 12 bytes (24 hex chars)
        for (int i = 2; i < 24 && i < hash.length(); i += 2) {
            int nextByte = Integer.parseInt(hash.substring(i, i + 2), 16);
            result ^= nextByte;
        }

        return result; // Will be 0-255
    }

    private int calculateXORLast4Bytes(String hash) {
        int result = Integer.parseInt(hash.substring(24, 26), 16); // 13th byte

        // XOR last 4 bytes (starting from position 24)
        for (int i = 26; i < hash.length(); i += 2) {
            int nextByte = Integer.parseInt(hash.substring(i, i + 2), 16);
            result ^= nextByte;
        }

        return result; // Will be 0-255
    }

    private void embedWatermark(ActionEvent e) {
        String watermark = watermarkField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        // Validation
        if (watermark.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a watermark!");
            return;
        }
        if (watermark.length() > 16) {
            JOptionPane.showMessageDialog(this, "Watermark must be 16 characters or less!");
            return;
        }
        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a password!");
            return;
        }
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first!");
            return;
        }

        try {
            log("=== WATERMARK EMBEDDING PROCESS (DWM2 - ROW-BASED) ===");
            log("Input watermark: \"" + watermark + "\"");
            log("Input password: \"" + password + "\"");

            // Step 1: Get current timestamp
            String timestamp = getCurrentTimestamp();
            log("Step 1 - Current timestamp: " + timestamp);

            // Step 2: Create full watermark string with delimiter
            String fullWatermark = watermark + "#" + timestamp + "#@";
            log("Step 2 - Full watermark with delimiter: \"" + fullWatermark + "\"");
            log("  Structure: watermark + '#' (delimiter) + timestamp + '#@' (end marker)");

            // Step 3: Calculate row number from password (0-255)
            String passwordMD5Hash = calculateMD5Hash(password);
            int rowNumber = calculateXORFirst12Bytes(passwordMD5Hash);
            int startColumn = calculateXORLast4Bytes(passwordMD5Hash);

            log("Step 3 - Password MD5 Hash and Row Selection:");
            log("  Password: \"" + password + "\"");
            log("  Password MD5 Hash: " + passwordMD5Hash);
            log("  Row Number (first 12 bytes XOR): " + rowNumber + " (0x" + String.format("%02X", rowNumber) + ")");
            log("  Start Column (last 4 bytes XOR): " + startColumn + " (0x" + String.format("%02X", startColumn)
                    + ")");
            log("  This watermark will use ROW " + rowNumber + " (pixels " + (rowNumber * 256) + " to "
                    + ((rowNumber * 256) + 255) + ")");

            // Step 4: Create watermarked image
            watermarkedImage = new BufferedImage(originalImage.getWidth(),
                    originalImage.getHeight(), BufferedImage.TYPE_INT_RGB);
            Graphics2D g2d = watermarkedImage.createGraphics();
            g2d.drawImage(originalImage, 0, 0, null);
            g2d.dispose();

            // Step 4: Check if row is already occupied
            log("Step 4 - Check if Row " + rowNumber + " is already occupied:");

            // Check column 0 of the row for '$' occupation marker
            int checkColumn = 0;
            int existingRGB = originalImage.getRGB(checkColumn, rowNumber);
            int existingR = (existingRGB >> 16) & 0xFF;
            int existingG = (existingRGB >> 8) & 0xFF;
            int existingB = existingRGB & 0xFF;

            // Extract LSBs to check for '$' marker
            String existingRedBits = String.format("%3s", Integer.toBinaryString(existingR & 0x07)).replace(' ', '0');
            String existingGreenBits = String.format("%3s", Integer.toBinaryString(existingG & 0x07)).replace(' ', '0');
            String existingBlueBits = String.format("%2s", Integer.toBinaryString(existingB & 0x03)).replace(' ', '0');
            String existingBinary = existingRedBits + existingGreenBits + existingBlueBits;
            int existingASCII = Integer.parseInt(existingBinary, 2);

            if (existingASCII == 36) { // ASCII for '$'
                log("  ERROR: Row " + rowNumber + " is already occupied!");
                JOptionPane.showMessageDialog(this,
                        "Row Occupation Error!\n" +
                                "Row " + rowNumber + " is already occupied by another watermark.\n" +
                                "Please use a different password to select a different row.\n\n" +
                                "Password MD5: " + passwordMD5Hash + "\n" +
                                "Selected Row: " + rowNumber,
                        "Row Already Occupied", JOptionPane.ERROR_MESSAGE);
                return;
            }

            log("  Row " + rowNumber + " is available - No collision detected");

            // Step 5: Embed '$' marker at column 0 to mark row as occupied
            log("Step 5 - Mark row as occupied:");
            int dollarASCII = 36; // '$'
            String dollarBinary = String.format("%8s", Integer.toBinaryString(dollarASCII)).replace(' ', '0');

            int col0RGB = watermarkedImage.getRGB(0, rowNumber);
            int col0R = (col0RGB >> 16) & 0xFF;
            int col0G = (col0RGB >> 8) & 0xFF;
            int col0B = col0RGB & 0xFF;

            int dollarRedBits = Integer.parseInt(dollarBinary.substring(0, 3), 2);
            int dollarGreenBits = Integer.parseInt(dollarBinary.substring(3, 6), 2);
            int dollarBlueBits = Integer.parseInt(dollarBinary.substring(6, 8), 2);

            int newCol0R = (col0R & 0xF8) | dollarRedBits;
            int newCol0G = (col0G & 0xF8) | dollarGreenBits;
            int newCol0B = (col0B & 0xFC) | dollarBlueBits;

            int newCol0RGB = (newCol0R << 16) | (newCol0G << 8) | newCol0B;
            watermarkedImage.setRGB(0, rowNumber, newCol0RGB);

            log("  Embedded '$' (ASCII 36) at Row " + rowNumber + ", Column 0 (Pixel " + (rowNumber * 256) + ")");
            log("  This marks the row as OCCUPIED");

            // Step 6: Embed characters sequentially in the selected row
            log("Step 6 - Embedding watermark in Row " + rowNumber + ":");
            log("  Starting at column " + startColumn + ", wrapping within row if needed");

            // Build array with '*' marker + fullWatermark
            char[] fullWatermarkChars = fullWatermark.toCharArray();
            char[] chars = new char[fullWatermarkChars.length + 1];
            chars[0] = '*'; // Marker
            System.arraycopy(fullWatermarkChars, 0, chars, 1, fullWatermarkChars.length);

            // Check if watermark fits in one row (256 pixels)
            if (chars.length > 256) {
                log("  ERROR: Watermark too long! Length=" + chars.length + ", Max=256");
                JOptionPane.showMessageDialog(this,
                        "Watermark too long!\n" +
                                "Total length (watermark + timestamp + markers): " + chars.length + " characters\n" +
                                "Maximum allowed: 256 characters (one row)\n" +
                                "Please use a shorter watermark.",
                        "Watermark Too Long", JOptionPane.ERROR_MESSAGE);
                return;
            }

            int currentColumn = startColumn;

            for (int i = 0; i < chars.length; i++) {
                char c = chars[i];
                int ascii = (int) c;

                // Calculate position in the row (wrap around if needed)
                int x = (currentColumn + i) % 256;
                int y = rowNumber;

                log(String.format("  [%2d] '%c' (ASCII %3d) -> Row %3d, Column %3d (Pixel %5d)",
                        i, (ascii >= 32 && ascii <= 126) ? c : '?', ascii, y, x, y * 256 + x));

                // Get original pixel
                int originalRGB = watermarkedImage.getRGB(x, y);
                int originalR = (originalRGB >> 16) & 0xFF;
                int originalG = (originalRGB >> 8) & 0xFF;
                int originalB = originalRGB & 0xFF;

                // Embed character in LSBs (using 8 bits across RGB: R=3, G=3, B=2)
                String binary = String.format("%8s", Integer.toBinaryString(ascii)).replace(' ', '0');

                int redBits = Integer.parseInt(binary.substring(0, 3), 2);
                int greenBits = Integer.parseInt(binary.substring(3, 6), 2);
                int blueBits = Integer.parseInt(binary.substring(6, 8), 2);

                int newR = (originalR & 0xF8) | redBits;
                int newG = (originalG & 0xF8) | greenBits;
                int newB = (originalB & 0xFC) | blueBits;

                int newRGB = (newR << 16) | (newG << 8) | newB;
                watermarkedImage.setRGB(x, y, newRGB);
            }

            log("  Total watermark characters embedded: " + chars.length);
            log("  Embedded string: \"" + new String(chars) + "\"");
            log("  Row utilization: 1 (marker) + " + chars.length + " (watermark) = " + (chars.length + 1)
                    + "/256 pixels (" +
                    String.format("%.1f%%", ((chars.length + 1) * 100.0 / 256)) + ")");

            // Update display
            ImageIcon icon = new ImageIcon(watermarkedImage.getScaledInstance(256, 256, Image.SCALE_SMOOTH));
            imageLabel.setIcon(icon);

            extractButton.setEnabled(true);
            saveImageButton.setEnabled(true);
            statusLabel.setText("Status: Watermark embedded in Row " + rowNumber);
            log("=== EMBEDDING COMPLETED ===");

            JOptionPane.showMessageDialog(this,
                    "Watermark embedded successfully!\n\n" +
                            "Allocated Row: " + rowNumber + " (out of 256 rows)\n" +
                            "Row Marker: '$' at column 0 (occupation indicator)\n" +
                            "Start Column: " + startColumn + "\n" +
                            "Characters embedded: 1 (marker) + " + chars.length + " (watermark) = " + (chars.length + 1)
                            + "/256\n" +
                            "Password MD5: " + passwordMD5Hash.substring(0, 16) + "...\n\n" +
                            "Embedded string: \"" + new String(chars) + "\"",
                    "Success", JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error embedding watermark: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void extractWatermark(ActionEvent e) {
        String password = new String(passwordField.getPassword()).trim();

        if (password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter the password!");
            return;
        }
        if (originalImage == null) {
            JOptionPane.showMessageDialog(this, "Please load an image first!");
            return;
        }

        // Use watermarked image if available, otherwise use original (for
        // pre-watermarked images)
        BufferedImage imageToExtractFrom = (watermarkedImage != null) ? watermarkedImage : originalImage;

        try {
            log("=== WATERMARK EXTRACTION PROCESS (DWM2 - ROW-BASED) ===");
            log("Password: \"" + password + "\"");

            // Step 1: Calculate row number from password
            String passwordMD5Hash = calculateMD5Hash(password);
            int rowNumber = calculateXORFirst12Bytes(passwordMD5Hash);
            int startColumn = calculateXORLast4Bytes(passwordMD5Hash);

            log("Step 1 - Password MD5 Hash and Row Selection:");
            log("  Password MD5 Hash: " + passwordMD5Hash);
            log("  Row Number (first 12 bytes XOR): " + rowNumber);
            log("  Start Column (last 4 bytes XOR): " + startColumn);
            log("  Extracting from ROW " + rowNumber + " starting at column " + startColumn);

            // Step 2: Extract characters sequentially from the row
            log("Step 2 - Extracting from Row " + rowNumber + ":");

            StringBuilder extractedText = new StringBuilder();
            int currentColumn = startColumn;

            for (int i = 0; i < 256; i++) { // Maximum 256 pixels in a row
                int x = (currentColumn + i) % 256;
                int y = rowNumber;

                int rgb = imageToExtractFrom.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF;
                int g = (rgb >> 8) & 0xFF;
                int b = rgb & 0xFF;

                // Extract LSBs
                String redBits = String.format("%3s", Integer.toBinaryString(r & 0x07)).replace(' ', '0');
                String greenBits = String.format("%3s", Integer.toBinaryString(g & 0x07)).replace(' ', '0');
                String blueBits = String.format("%2s", Integer.toBinaryString(b & 0x03)).replace(' ', '0');

                String binaryChar = redBits + greenBits + blueBits;
                int ascii = Integer.parseInt(binaryChar, 2);
                char extractedChar = (char) ascii;

                log(String.format("  [%2d] Row %3d, Col %3d (Pixel %5d): RGB(%d,%d,%d) -> ASCII %3d -> '%c'",
                        i, y, x, y * 256 + x, r, g, b, ascii,
                        (ascii >= 32 && ascii <= 126) ? extractedChar : '?'));

                extractedText.append(extractedChar);

                // Check for end marker
                if (extractedText.toString().endsWith("#@")) {
                    log("  -> Found END marker '#@'");
                    break;
                }

                // Safety check - if we've gone too far without finding end marker
                if (i >= 100) {
                    log("  -> Safety limit reached, stopping extraction");
                    break;
                }
            }

            // Step 3: Validate extracted watermark
            String fullExtracted = extractedText.toString();
            log("Step 3 - Full extracted string: \"" + fullExtracted + "\"");

            if (!fullExtracted.endsWith("#@")) {
                JOptionPane.showMessageDialog(this, "No valid watermark found with the given input!",
                        "Extraction Failed", JOptionPane.ERROR_MESSAGE);
                log("ERROR: End marker '#@' not found!");
                return;
            }

            // Remove end marker
            String extractedContent = fullExtracted.substring(0, fullExtracted.length() - 2);

            // Remove the '*' marker from the beginning
            if (extractedContent.length() > 0 && extractedContent.charAt(0) == '*') {
                extractedContent = extractedContent.substring(1);
            }

            // Split using '#' delimiter
            String[] parts = extractedContent.split("#", 2);
            if (parts.length < 2) {
                log("ERROR: Delimiter '#' not found in extracted content!");
                JOptionPane.showMessageDialog(this, "Invalid watermark structure - delimiter not found!",
                        "Extraction Failed", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String extractedWatermark = parts[0];
            String extractedTimestamp = parts[1];

            log("Step 4 - Extracted components:");
            log("  Watermark: \"" + extractedWatermark + "\"");
            log("  Timestamp: \"" + extractedTimestamp + "\"");
            log("  Total characters: " + (extractedWatermark.length() + 1 + extractedTimestamp.length() + 2));
            log("=== EXTRACTION COMPLETED ===");

            String message = "Extraction Results:\n\n" +
                    "Extracted Watermark: \"" + extractedWatermark + "\"\n" +
                    "Extracted Timestamp: \"" + extractedTimestamp + "\"\n\n" +
                    "From Row: " + rowNumber + " (out of 256 rows)\n" +
                    "Start Column: " + startColumn + "\n" +
                    "Password MD5: " + passwordMD5Hash.substring(0, 16) + "...";

            JOptionPane.showMessageDialog(this, message, "Extraction Successful",
                    JOptionPane.INFORMATION_MESSAGE);

            statusLabel.setText("Status: Watermark extracted from Row " + rowNumber);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error extracting watermark: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private void saveImage(ActionEvent e) {
        if (watermarkedImage == null) {
            JOptionPane.showMessageDialog(this, "No watermarked image to save!");
            return;
        }

        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
                "BMP Images", "bmp"));
        fileChooser.setSelectedFile(new File("watermarked_image_dwm2.bmp"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try {
                File outputFile = fileChooser.getSelectedFile();
                if (!outputFile.getName().toLowerCase().endsWith(".bmp")) {
                    outputFile = new File(outputFile.getAbsolutePath() + ".bmp");
                }

                ImageIO.write(watermarkedImage, "BMP", outputFile);
                statusLabel.setText("Status: Watermarked image saved");
                log("Image saved: " + outputFile.getAbsolutePath());
                JOptionPane.showMessageDialog(this, "Watermarked image saved successfully!");

            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this, "Error saving image: " + ex.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void log(String message) {
        logArea.append(message + "\n");
        logArea.setCaretPosition(logArea.getDocument().getLength());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DWM2().setVisible(true);
        });
    }
}
