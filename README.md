# 🔐 SecureCrypt

**A Multi-Algorithm Text Encryption & Decryption Web Application**

Built as a mini-project for the *Fundamentals of Cyber Security* course (B.Tech 2nd Year).

SecureCrypt lets you encrypt and decrypt text using six different algorithms — three classical
ciphers implemented from scratch, and three industry-standard algorithms implemented using the
Java Cryptography Architecture (JCA) — through a clean, responsive, dark/light-mode web UI.

---

## ✨ Features

- 🔒 Encrypt & 🔓 Decrypt text with **6 algorithms**: Caesar, Vigenère, XOR, AES-128, DES, 3DES
- 🔑 Password-based key derivation (PBKDF2) for AES / DES / 3DES
- 🎲 Random key generator + live key-strength indicator
- 📋 Copy-to-clipboard, 📥 Download as `.txt`, 🧹 Clear button
- 📊 Session-only encryption history (never persisted or sent anywhere)
- 🌗 Dark / Light mode toggle, fully responsive Bootstrap 5 UI
- 🎬 Smooth animations — fade-in, hover effects, button ripple
- ✅ Full input validation, friendly error handling (no data ever stored server-side)

---

## 🛠️ Tech Stack

| Layer      | Technology |
|------------|------------|
| Language   | Java 21 |
| Backend    | Spring Boot 3.3 (Spring Web, Spring Validation) |
| Build Tool | Maven |
| Frontend   | HTML5, CSS3, Vanilla JavaScript (ES6), Bootstrap 5 |
| Crypto     | Manual implementations (Caesar/Vigenère/XOR) + Java Cryptography Architecture (AES/DES/3DES) |

---

## 📁 Project Structure

```
SecureCrypt/
├── pom.xml
├── README.md
├── src/
│   ├── main/
│   │   ├── java/com/securecrypt/
│   │   │   ├── SecureCryptApplication.java     # Spring Boot entry point
│   │   │   ├── controller/
│   │   │   │   └── CryptoController.java       # REST endpoints
│   │   │   ├── service/
│   │   │   │   ├── EncryptionService.java       # Strategy interface
│   │   │   │   ├── CaesarCipherService.java
│   │   │   │   ├── VigenereCipherService.java
│   │   │   │   ├── XORCipherService.java
│   │   │   │   ├── AESService.java
│   │   │   │   ├── DESService.java
│   │   │   │   ├── TripleDESService.java
│   │   │   │   └── CryptoServiceFactory.java    # Picks the right strategy
│   │   │   ├── model/
│   │   │   │   ├── AlgorithmType.java
│   │   │   │   ├── CryptoRequest.java
│   │   │   │   └── CryptoResponse.java
│   │   │   ├── utility/
│   │   │   │   ├── ValidationUtil.java
│   │   │   │   ├── KeyGeneratorUtil.java
│   │   │   │   └── KeyDerivationUtil.java
│   │   │   ├── config/
│   │   │   │   └── WebConfig.java
│   │   │   └── exception/
│   │   │       ├── CryptoException.java
│   │   │       └── GlobalExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/style.css
│   │           └── js/script.js
└── docs/                                        # Report, diagrams, viva Q&A
```

---

## ▶️ How to Run (IntelliJ IDEA)

1. Open IntelliJ IDEA → `File > Open` → select the `SecureCrypt` folder.
2. Let IntelliJ auto-import the Maven project (wait for indexing to finish).
3. Ensure **Project SDK** is set to **Java 21**: `File > Project Structure > Project SDK`.
4. Open `SecureCryptApplication.java` and click the green ▶ **Run** button
   (or right-click → `Run 'SecureCryptApplication'`).
5. Once you see:
   ```
   SecureCrypt is running!
   Open your browser -> http://localhost:8080
   ```
   open **http://localhost:8080** in your browser.

### Run from terminal (alternative)

```bash
mvn clean install
mvn spring-boot:run
```

Or build a runnable JAR:

```bash
mvn clean package
java -jar target/SecureCrypt.jar
```

---

## 🔗 REST API Reference

### `POST /api/encrypt`

```json
{
  "text": "Hello World",
  "algorithm": "AES",
  "key": "MySecretPassword123"
}
```

**Response**

```json
{
  "success": true,
  "result": "U2FsdGVkX1+abcd1234...",
  "algorithm": "AES",
  "message": "Operation completed successfully."
}
```

### `POST /api/decrypt`

```json
{
  "text": "U2FsdGVkX1+abcd1234...",
  "algorithm": "AES",
  "key": "MySecretPassword123"
}
```

### `GET /api/generate-key?length=16`
Returns a randomly generated strong key.

### `GET /api/key-strength?key=abc123`
Returns `WEAK`, `MEDIUM`, or `STRONG`.

---

## 🧪 Sample Inputs & Outputs

| Algorithm | Plain Text | Key | Cipher Text (example) |
|-----------|-----------|-----|------------------------|
| Caesar | `HELLO` | `3` | `KHOOR` |
| Vigenère | `ATTACKATDAWN` | `LEMON` | `LXFOPVEFRNHR` |
| XOR | `Hello` | `key` | Base64 string (binary output) |
| AES-128 | `Confidential Data` | `MyP@ssw0rd!` | Base64 string (IV + cipher text) |
| DES | `Secret Msg` | `des-key1` | Base64 string (IV + cipher text) |
| 3DES | `Top Secret` | `triple-des-key` | Base64 string (IV + cipher text) |

> Note: AES/DES/3DES outputs are **not reproducible across runs by hand** because a random IV
> is generated per-encryption for semantic security — decrypting with the same key on the
> exact output shown will always recover the correct plain text.

---

## 🔒 Security Notes

- No user text or keys are ever logged, cached, or persisted to disk/database.
- AES/DES/3DES keys are derived from passwords using **PBKDF2WithHmacSHA256** (65,536 iterations).
- Random IVs are generated per-encryption and prepended to the cipher text.
- Sensitive `char[]`/`byte[]` buffers are wiped after use where practical.
- DES and (to a lesser extent) 3DES are included **for academic comparison only** — they are
  not considered secure by modern standards. AES-128 is the recommended algorithm for real use.

---

## 📄 Additional Documentation

See the `docs/` folder for:
- Full Project Report (Abstract, Objectives, Architecture, Modules, etc.)
- Mermaid diagrams (Class, Sequence, Flowchart)
- Viva Questions & Answers (20 Q&A)

---

## 👨‍🎓 Author

Veshwik Jain - B.Tech 2nd Year — Cyber Security
Subject: Fundamentals of Cyber Security
Project: SecureCrypt (Mini Project)
#
