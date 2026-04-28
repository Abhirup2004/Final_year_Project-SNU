# DWM2 - Digital Watermarking and Steganography

This folder contains a Java Swing application for hiding and extracting secret information from three kinds of files:

1. BMP images
2. WAV audio files
3. Text files

The main program is `DWM2.java`. It opens a graphical user interface where a user can load a file, enter a watermark/message, enter a password/key, and then embed or extract hidden information.

This README is written for a beginner. It explains what every file does, how the project works internally, how to run it, and what limitations to remember.

## 1. What This Project Does

The project demonstrates digital watermarking and steganography.

Digital watermarking means placing identifying information inside a file. The watermark can prove ownership, show authenticity, or store metadata such as a timestamp.

Steganography means hiding information inside another file so that the file still looks or sounds mostly normal.

This project supports:

| File type | Input format | What is hidden | Technique used |
|---|---|---|---|
| Image | 256 x 256 BMP | Watermark + timestamp | Pixel LSB modification |
| Audio | WAV | Message + timestamp | Audio byte LSB modification |
| Text | Any text file | Message + timestamp | Appended hidden comment with XOR text |

## 2. Folder Contents

The folder currently contains these files:

| File | Type | Purpose |
|---|---|---|
| `DWM2.java` | Java source code | Main GUI application. Handles buttons, input fields, image watermarking, and calls audio/text helper classes. |
| `AudioSteganography.java` | Java source code | Contains static methods to hide and extract text from WAV audio files. |
| `TextSteganography.java` | Java source code | Contains static methods to hide and extract text from normal text files. |
| `DWM2.class` | Compiled Java bytecode | Generated from `DWM2.java` after compilation. |
| `AudioSteganography.class` | Compiled Java bytecode | Generated from `AudioSteganography.java` after compilation. |
| `TextSteganography.class` | Compiled Java bytecode | Generated from `TextSteganography.java` after compilation. |
| `stego_text.txt` | Generated/sample output | Example text file containing a hidden message marker. |
| `watermarked_audio.wav` | Generated/sample output | Example WAV file after audio embedding. |
| `.gitignore` | Git config file | Tells Git to ignore generated files such as `.class`, `.wav`, `.bmp`, and `.log`. |
| `Readme.md` | Documentation | This explanation file. |

The `.java` files are the important source files. The `.class`, `.wav`, `.bmp`, `.txt`, and `.log` files are usually generated while running or testing the project.

## 3. Requirements

You need:

1. Java JDK installed, not only JRE.
2. A terminal or PowerShell.
3. A system that can display Java Swing GUI windows.
4. For image watermarking: a BMP image that is exactly `256 x 256` pixels.
5. For audio watermarking: a WAV audio file, preferably uncompressed PCM WAV.
6. For text watermarking: any readable text file.

Check Java installation:

```powershell
java -version
javac -version
```

If `javac` is not recognized, install a JDK and add it to the system PATH.

## 4. How To Compile And Run

Open PowerShell in this folder:

```powershell
cd C:\Users\LOQ\Final_year_Project-SNU\PPR-1\The_Proof\src
```

Compile all Java files:

```powershell
javac DWM2.java AudioSteganography.java TextSteganography.java
```

Run the application:

```powershell
java DWM2
```

After running, a GUI window opens.

## 5. Main GUI Overview

The GUI is created in `DWM2.java`.

It contains:

| GUI area | Purpose |
|---|---|
| Watermark field | The text/message to hide. For image watermarking, the GUI limits this to 16 characters. |
| Password field | Used to select the image row and start column, or used as the XOR key for audio/text. |
| Current time label | Shows the current timestamp used during embedding. |
| Image preview | Shows the loaded BMP image and the watermarked image. |
| Log area | Shows detailed algorithm steps. |
| Image buttons | Load Image, Embed Watermark, Extract Watermark, Save Image. |
| Audio buttons | Load Audio, Embed Audio, Extract Audio. |
| Text buttons | Load Text, Embed Text, Extract Text. |

The log area is very useful for learning because it prints each major step of the algorithm.

## 6. Important Beginner Concepts

### 6.1 Bit

A bit is the smallest unit of data. It is either `0` or `1`.

Example:

```text
01000001
```

This is 8 bits.

### 6.2 Byte

A byte is 8 bits.

Most simple English characters can be represented in 1 byte using ASCII.

Example:

```text
A = ASCII 65 = binary 01000001
```

### 6.3 ASCII

ASCII is a numeric code for characters.

Examples:

| Character | ASCII value | Binary |
|---|---:|---|
| `A` | 65 | `01000001` |
| `#` | 35 | `00100011` |
| `@` | 64 | `01000000` |
| `$` | 36 | `00100100` |
| `*` | 42 | `00101010` |

### 6.4 LSB

LSB means Least Significant Bit.

It is the last bit in a binary number. Changing the LSB changes the value very slightly.

Example:

```text
10010110 = 150
10010111 = 151
```

Only the last bit changed, so the value changed by 1.

This is useful for steganography because small changes in image pixels or audio samples are usually hard to notice.

### 6.5 XOR

XOR is a simple bit operation. It is used here as a basic reversible encryption/obfuscation method.

XOR rules:

| A | B | A XOR B |
|---|---|---|
| 0 | 0 | 0 |
| 0 | 1 | 1 |
| 1 | 0 | 1 |
| 1 | 1 | 0 |

Important property:

```text
original XOR key = encrypted
encrypted XOR key = original
```

That is why the same method can encrypt and decrypt.

This is not strong modern cryptography. It is useful for demonstrating the concept, but it should not be used to protect highly sensitive data.

### 6.6 MD5

MD5 is a hash algorithm. It converts input text into a fixed-length hexadecimal string.

In this project, the image watermarking system uses MD5 of the password to choose:

1. Which image row will store the watermark.
2. Which column in that row will be the starting point.

The password is not stored directly in the image.

## 7. Overall Project Architecture

The project has one main class and two helper classes.

```text
DWM2.java
  |
  |-- Handles GUI
  |-- Handles image watermarking directly
  |-- Calls AudioSteganography for WAV files
  |-- Calls TextSteganography for text files

AudioSteganography.java
  |
  |-- hideText(...)
  |-- extractText(...)
  |-- XOR encryption/decryption
  |-- Text to bits and bits to text

TextSteganography.java
  |
  |-- hideText(...)
  |-- extractText(...)
  |-- XOR encryption/decryption
```

The main idea is:

```text
User input -> GUI -> algorithm -> output file
```

For extraction:

```text
Watermarked/stego file -> GUI -> algorithm -> recovered hidden message
```

## 8. Timestamp Format

During embedding, the code adds a timestamp to the hidden message.

The format used in the code is:

```text
HH/MM/ss/dd/MM/yyyy
```

The hidden data is built like this:

```text
message + "#" + timestamp + "#@"
```

Example structure:

```text
hello#14/04/32/26/04/2026#@
```

Important Java note: in `DateTimeFormatter`, uppercase `MM` means month. Lowercase `mm` means minutes. The current code uses `MM`, so the second field is month, not minutes.

## 9. Image Watermarking Approach

Image watermarking is implemented mainly inside `DWM2.java`.

### 9.1 Input Rules

The image must be:

1. BMP format.
2. Exactly `256 x 256` pixels.

Why `256 x 256`?

The algorithm uses one image row to store one watermark. A 256 pixel tall image has 256 rows, so the design allows up to 256 possible row positions.

### 9.2 Data Stored In The Image

The full stored data contains:

```text
* + watermark + # + timestamp + #@
```

Where:

| Symbol | Meaning |
|---|---|
| `*` | Start marker before the hidden watermark data. |
| `#` | Separator between watermark and timestamp. |
| `#@` | End marker used to stop extraction. |
| `$` | Row occupation marker stored at column 0. |

Example:

```text
*owner#14/04/32/26/04/2026#@
```

### 9.3 Password To Row And Column

The password controls where the watermark is stored.

Step 1:

The password is converted into an MD5 hash.

Example:

```text
password -> 5f4dcc3b5aa765d61d8327deb882cf99
```

Step 2:

The first 12 bytes of the hash are XORed together to produce the row number.

```text
rowNumber = XOR of first 12 MD5 bytes
```

The result is always between `0` and `255`.

Step 3:

The last 4 bytes of the hash are XORed together to produce the starting column.

```text
startColumn = XOR of last 4 MD5 bytes
```

The result is also between `0` and `255`.

So the same password always selects the same row and same start column.

### 9.4 Row Occupation Check

Before embedding, the code checks whether the selected row is already used.

It reads pixel `(column 0, selected row)`.

From that pixel it extracts 8 hidden bits:

```text
Red channel   -> last 3 bits
Green channel -> last 3 bits
Blue channel  -> last 2 bits
```

Those 8 bits are converted to an ASCII character.

If the character is `$`, the row is considered occupied.

If the row is occupied, the program stops and asks the user to use a different password.

### 9.5 How One Character Is Stored In One Pixel

Each image pixel has three color channels:

```text
Red, Green, Blue
```

Each channel is a number from `0` to `255`, which means each channel has 8 bits.

The algorithm stores one character using 8 bits:

```text
First 3 bits  -> Red channel LSBs
Next 3 bits   -> Green channel LSBs
Last 2 bits   -> Blue channel LSBs
```

Example:

```text
Character A = 01000001

Red gets:   010
Green gets: 000
Blue gets:  01
```

The code keeps the main color information and changes only the smallest bits:

```java
newR = (originalR & 0xF8) | redBits;
newG = (originalG & 0xF8) | greenBits;
newB = (originalB & 0xFC) | blueBits;
```

Meaning:

1. `0xF8` keeps the upper 5 bits of red/green and clears the lower 3 bits.
2. `0xFC` keeps the upper 6 bits of blue and clears the lower 2 bits.
3. The hidden bits are placed into the cleared positions.

### 9.6 Image Embedding Steps

When the user clicks `Embed Watermark`:

1. The program reads the watermark text.
2. The program reads the password.
3. It checks that the watermark is not empty.
4. It checks that the watermark is at most 16 characters.
5. It checks that an image is loaded.
6. It creates a timestamp.
7. It creates the full watermark string:

```text
watermark#timestamp#@
```

8. It calculates the password MD5 hash.
9. It calculates the row number from the first 12 hash bytes.
10. It calculates the start column from the last 4 hash bytes.
11. It checks if the row already contains the `$` marker.
12. It writes `$` at column 0 of the selected row.
13. It creates a character array beginning with `*`.
14. It embeds each character into one pixel.
15. If the end of the row is reached, it wraps back to column 0 in the same row.
16. It updates the preview image.
17. It enables the Save Image button.

### 9.7 Image Extraction Steps

When the user clicks `Extract Watermark`:

1. The program reads the password.
2. It calculates the same MD5 hash.
3. It calculates the same row number.
4. It calculates the same start column.
5. It reads pixels from that row, starting from the start column.
6. It extracts 3 bits from red, 3 bits from green, and 2 bits from blue.
7. It combines those bits into one ASCII character.
8. It keeps reading characters until it finds the end marker `#@`.
9. It removes the `#@` end marker.
10. It removes the starting `*` marker if present.
11. It splits the result at `#`.
12. The first part is the watermark.
13. The second part is the timestamp.
14. It displays the result in a popup and in the log.

### 9.8 Image Capacity

One pixel stores one character.

One row has 256 pixels.

So one selected row can store up to 256 characters in theory.

The GUI restricts the main watermark to 16 characters, so the actual stored image string is usually much shorter:

```text
* + watermark + # + timestamp + #@
```

With a 16 character watermark and the current timestamp format, the stored string is around 39 characters.

### 9.9 Image Output

The watermarked image is saved only when the user clicks `Save Image`.

The default file name is:

```text
watermarked_image_dwm2.bmp
```

The user can choose another location or name in the save dialog.

## 10. Audio Steganography Approach

Audio hiding and extraction are implemented in `AudioSteganography.java`.

The GUI methods in `DWM2.java` call:

```java
AudioSteganography.hideText(...)
AudioSteganography.extractText(...)
```

### 10.1 Input Rules

The audio file should be:

1. WAV format.
2. Preferably uncompressed PCM.

The code uses Java Sound API:

```java
AudioSystem.getAudioInputStream(input)
```

### 10.2 Audio Data Format

The hidden audio message is built as:

```text
message#timestamp#@
```

Then it is XOR-encrypted using the password/key.

Then every encrypted character is converted into 8 binary bits.

Finally, this end marker is added:

```text
1111111111111110
```

That marker tells the extractor where the hidden data ends.

### 10.3 Audio Embedding Steps

When the user clicks `Embed Audio`:

1. The program checks that a WAV file is loaded.
2. It reads the message from the watermark field.
3. It reads the key from the password field.
4. It creates a timestamp.
5. It creates the full message:

```text
message#timestamp#@
```

6. It XOR-encrypts the full message.
7. It converts the encrypted text into bits.
8. It appends the end marker bits.
9. It reads the audio bytes.
10. It checks that the audio has enough bytes to store all bits.
11. It stores one hidden bit in the LSB of each audio byte.
12. It writes the result to:

```text
watermarked_audio.wav
```

### 10.4 Audio LSB Example

Suppose one audio byte is:

```text
10101100
```

To hide bit `1`, the code changes only the last bit:

```text
10101101
```

The sound change is very small.

In Java, the embedding line is:

```java
audioBytes[i] = (byte)((audioBytes[i] & 0xFE) | (bits.charAt(i) - '0'));
```

Meaning:

1. `audioBytes[i] & 0xFE` clears the last bit.
2. `bits.charAt(i) - '0'` converts character `'0'` or `'1'` into number `0` or `1`.
3. `|` places the hidden bit into the last bit.

### 10.5 Audio Extraction Steps

When the user clicks `Extract Audio`:

1. The program checks that a WAV file is loaded.
2. It reads the password/key.
3. It reads all audio bytes.
4. It collects the LSB from each byte.
5. It searches for the end marker:

```text
1111111111111110
```

6. If no marker is found, it reports that no hidden message was detected.
7. It takes all bits before the marker.
8. It converts every 8 bits into one encrypted character.
9. It XOR-decrypts the text using the key.
10. It returns the hidden message.

### 10.6 Audio Capacity

One audio byte stores one hidden bit.

One character needs 8 bits.

The rough capacity is:

```text
audioBytes / 8 characters
```

The code also needs 16 bits for the end marker.

Example:

```text
80,000 audio bytes can store about 10,000 characters minus marker overhead.
```

### 10.7 Audio Output

The output file is created with this fixed name:

```text
watermarked_audio.wav
```

It is saved in the current working directory. If you run the program from the `src` folder, the file appears in the `src` folder.

If a file with the same name already exists, it can be overwritten.

## 11. Text Steganography Approach

Text hiding and extraction are implemented in `TextSteganography.java`.

The GUI methods in `DWM2.java` call:

```java
TextSteganography.hideText(...)
TextSteganography.extractText(...)
```

### 11.1 Input Rules

The input can be a normal text file.

The method reads the entire file into memory:

```java
Files.readAllBytes(input.toPath())
```

### 11.2 Text Data Format

The hidden text message is built as:

```text
message#timestamp#@
```

Then it is XOR-encrypted using the password/key.

Then it is appended to the original text file inside this marker:

```text
<!--STEGO:encryptedData-->
```

This looks like an HTML/XML comment.

### 11.3 Text Embedding Steps

When the user clicks `Embed Text`:

1. The program checks that a text file is loaded.
2. It reads the message from the watermark field.
3. It reads the key from the password field.
4. It reads the original text file content.
5. It creates a timestamp.
6. It creates the full hidden message:

```text
message#timestamp#@
```

7. It XOR-encrypts the message.
8. It appends the encrypted message to the file:

```text
original content
<!--STEGO:encryptedData-->
```

9. It writes the result to:

```text
stego_text.txt
```

### 11.4 Text Extraction Steps

When the user clicks `Extract Text`:

1. The program checks that a text file is loaded.
2. It reads the key from the password field.
3. It reads the full file content.
4. It searches for:

```text
<!--STEGO:
```

5. It searches for:

```text
-->
```

6. It extracts the encrypted text between those markers.
7. It XOR-decrypts the encrypted text using the key.
8. It returns the hidden message.

### 11.5 Text Output

The output file is created with this fixed name:

```text
stego_text.txt
```

It is saved in the current working directory.

If a file with the same name already exists, it can be overwritten.

## 12. How The Password/Key Is Used

The password field has different roles depending on the file type.

| File type | Password/key role |
|---|---|
| Image | Used to calculate row number and start column using MD5 and XOR. |
| Audio | Used as XOR encryption/decryption key. |
| Text | Used as XOR encryption/decryption key. |

For audio and text:

```text
same key used for embedding = correct extraction
wrong key = unreadable extracted text
```

The key should not be empty. An empty key can cause an error because the XOR method uses:

```java
key.charAt(i % key.length())
```

If `key.length()` is `0`, the modulo operation is invalid.

## 13. Complete Beginner Workflows

### 13.1 Embed Watermark Into Image

1. Run the application using `java DWM2`.
2. Enter a watermark in the Watermark field.
3. Enter a password in the Password field.
4. Click `Load Image`.
5. Select a `256 x 256` BMP image.
6. Click `Embed Watermark`.
7. Read the log to see the selected row, start column, and embedded characters.
8. Click `Save Image`.
9. Choose where to save the watermarked BMP.

### 13.2 Extract Watermark From Image

1. Run the application.
2. Enter the same password that was used during embedding.
3. Click `Load Image`.
4. Select the watermarked BMP image.
5. Click `Extract Watermark`.
6. The program shows the hidden watermark and timestamp.

### 13.3 Embed Message Into Audio

1. Run the application.
2. Enter the message in the Watermark field.
3. Enter a key in the Password field.
4. Click `Load Audio`.
5. Select a WAV file.
6. Click `Embed Audio`.
7. The output is saved as `watermarked_audio.wav`.

### 13.4 Extract Message From Audio

1. Run the application.
2. Enter the same key used during embedding.
3. Click `Load Audio`.
4. Select `watermarked_audio.wav`.
5. Click `Extract Audio`.
6. The program shows the hidden message and timestamp.

### 13.5 Embed Message Into Text

1. Run the application.
2. Enter the message in the Watermark field.
3. Enter a key in the Password field.
4. Click `Load Text`.
5. Select a text file.
6. Click `Embed Text`.
7. The output is saved as `stego_text.txt`.

### 13.6 Extract Message From Text

1. Run the application.
2. Enter the same key used during embedding.
3. Click `Load Text`.
4. Select `stego_text.txt`.
5. Click `Extract Text`.
6. The program shows the hidden message and timestamp.

## 14. File-By-File Code Explanation

### 14.1 `DWM2.java`

This is the main class.

It extends:

```java
JFrame
```

That means it creates a desktop window.

Main responsibilities:

1. Create the GUI.
2. Load and preview BMP images.
3. Embed image watermarks.
4. Extract image watermarks.
5. Save watermarked images.
6. Load WAV files.
7. Call `AudioSteganography`.
8. Load text files.
9. Call `TextSteganography`.
10. Print detailed logs.

Important methods:

| Method | Purpose |
|---|---|
| `initializeGUI()` | Builds the main window layout. |
| `createInputPanel()` | Creates watermark, password, and timestamp fields. |
| `createImagePanel()` | Creates the image preview area. |
| `createLogPanel()` | Creates the log output area. |
| `createButtonPanel()` | Creates image, audio, and text buttons. |
| `getCurrentTimestamp()` | Returns timestamp string. |
| `loadImage(...)` | Loads and validates a 256 x 256 BMP image. |
| `calculateMD5Hash(...)` | Creates MD5 hash from password. |
| `calculateXORFirst12Bytes(...)` | Calculates selected image row. |
| `calculateXORLast4Bytes(...)` | Calculates selected start column. |
| `embedWatermark(...)` | Embeds image watermark into pixel LSBs. |
| `extractWatermark(...)` | Extracts image watermark from pixel LSBs. |
| `saveImage(...)` | Saves watermarked BMP image. |
| `loadAudio(...)` | Loads WAV audio file. |
| `embedAudio(...)` | Calls audio embedding helper. |
| `extractAudio(...)` | Calls audio extraction helper. |
| `loadText(...)` | Loads text file. |
| `embedText(...)` | Calls text embedding helper. |
| `extractText(...)` | Calls text extraction helper. |
| `main(...)` | Starts the Swing application. |

### 14.2 `AudioSteganography.java`

This class contains static helper methods for WAV audio.

Important methods:

| Method | Purpose |
|---|---|
| `hideText(...)` | Hides a message inside audio bytes. |
| `extractText(...)` | Extracts hidden message from audio bytes. |
| `xorEncrypt(...)` | XOR-encrypts or XOR-decrypts text. |
| `textToBits(...)` | Converts text into binary bits. |
| `bitsToText(...)` | Converts binary bits back into text. |

It does not create a GUI. It receives a `log` function from `DWM2.java`, so it can print steps into the GUI log area.

### 14.3 `TextSteganography.java`

This class contains static helper methods for text files.

Important methods:

| Method | Purpose |
|---|---|
| `hideText(...)` | Hides a message by appending an encrypted marker block. |
| `extractText(...)` | Finds the marker block and decrypts it. |
| `xorEncrypt(...)` | XOR-encrypts or XOR-decrypts text. |

Like the audio class, it does not create a GUI. It only performs the algorithm.

## 15. Generated Files

### 15.1 `.class` Files

Files ending in `.class` are generated by `javac`.

Example:

```text
DWM2.java -> DWM2.class
```

You normally do not edit `.class` files manually.

If needed, delete them and compile again:

```powershell
Remove-Item *.class
javac DWM2.java AudioSteganography.java TextSteganography.java
```

### 15.2 `watermarked_audio.wav`

This is a generated WAV file created by audio embedding.

### 15.3 `stego_text.txt`

This is a generated text file created by text embedding.

The current sample contains normal text followed by a hidden marker:

```text
<!--STEGO:...-->
```

The encrypted part may contain strange-looking characters because XOR encryption can produce non-printable characters.

## 16. `.gitignore`

The `.gitignore` file contains:

```text
*.class
*.wav
*.bmp
*.log
```

This means Git should ignore newly generated:

1. Java bytecode files.
2. WAV output files.
3. BMP output files.
4. Log files.

If a generated file was already tracked by Git before being added to `.gitignore`, Git may still show it as modified.

## 17. Troubleshooting

### 17.1 `javac` Is Not Recognized

Install a Java JDK and add it to PATH.

Check again:

```powershell
javac -version
```

### 17.2 Image Must Be Exactly 256 x 256

The image algorithm depends on 256 rows and 256 columns.

Use an image editor to resize or create a BMP image with:

```text
width  = 256
height = 256
```

### 17.3 No Valid Watermark Found

Possible reasons:

1. Wrong password was entered.
2. The loaded image/audio/text file does not contain hidden data.
3. The file was modified after embedding.
4. The wrong output file was selected.

### 17.4 Row Already Occupied

For images, the password selected a row that already contains the `$` marker.

Use a different password to select a different row.

### 17.5 Audio Message Too Large

The audio file does not have enough bytes to store the message bits.

Use:

1. A shorter message.
2. A larger WAV file.

### 17.6 Extracted Text Looks Wrong

Possible reasons:

1. Wrong key was used.
2. Empty key was used.
3. The stego file was edited manually.
4. XOR produced non-printable characters in the stored marker.

### 17.7 Output File Not Found

Audio and text outputs are created in the current working directory.

If you ran:

```powershell
java DWM2
```

from the `src` folder, look in:

```text
C:\Users\LOQ\Final_year_Project-SNU\PPR-1\The_Proof\src
```

## 18. Current Limitations And Notes

This project is good for learning and demonstration. It is not a production-grade security system.

Important limitations:

1. XOR encryption is weak compared to modern encryption.
2. Empty audio/text keys can cause errors.
3. Image watermarking only accepts `256 x 256` BMP files.
4. Image watermark text is limited to 16 characters by the GUI.
5. Audio output always uses the fixed name `watermarked_audio.wav`.
6. Text output always uses the fixed name `stego_text.txt`.
7. Text extraction searches for the first `-->` marker, so files containing earlier comment endings may confuse extraction.
8. The timestamp pattern uses uppercase `MM`, which means month, not minutes.
9. The image row occupation marker can be overwritten if the selected start column is `0`.
10. Adding multiple image watermarks in one session should be done carefully. Save and reload the latest watermarked BMP before embedding another watermark, because the embedding code starts from the currently loaded original image.

## 19. Possible Future Improvements

Useful improvements for the project:

1. Validate that the password/key is not empty for audio and text.
2. Use `mm` for minutes in the timestamp if minutes are intended.
3. Save audio and text output using a file chooser instead of fixed names.
4. Use stronger encryption such as AES for audio/text messages.
5. Improve text extraction by searching for `-->` after the `<!--STEGO:` start marker.
6. Support image sizes other than `256 x 256`.
7. Support PNG or other lossless image formats.
8. Add a command-line mode for automated testing.
9. Add unit tests for text/audio conversion and image row selection.
10. Store metadata about algorithm version inside the hidden message.

## 20. Summary

This project is a Java Swing based demonstration of hiding and extracting information from images, audio, and text.

The image method uses row-based pixel LSB watermarking. A password is converted to an MD5 hash, then XOR calculations choose the row and start column. Each hidden character is stored across the least significant bits of one pixel.

The audio method uses LSB steganography. It encrypts the message with XOR, converts it to bits, and stores one bit in each audio byte.

The text method appends an XOR-encrypted hidden message inside a `<!--STEGO:...-->` marker.

The same password/key used during embedding is required during extraction.
