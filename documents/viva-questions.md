# SecureCrypt — Viva Questions & Answers (20 Q&A)

**1. What is the difference between encryption and decryption?**
Encryption converts readable plain text into an unreadable cipher text using an algorithm and
a key. Decryption is the reverse process — converting cipher text back into the original plain
text using the same (symmetric) or a related (asymmetric) key.

**2. What type of cipher is the Caesar Cipher, and how does it work?**
It is a classical **substitution cipher**. Every letter in the plain text is shifted forward by
a fixed number of positions in the alphabet (the "shift" or key). For example, with a shift of 3,
'A' becomes 'D'. Decryption shifts letters backward by the same amount.

**3. Why is the Caesar Cipher considered insecure?**
It has only 25 possible keys (shifts), making it trivial to break with a brute-force attack. It
is also vulnerable to frequency analysis since the language's natural letter frequency pattern
is preserved after encryption.

**4. How does the Vigenère Cipher improve upon the Caesar Cipher?**
Instead of a single fixed shift, it uses a repeating keyword where each letter of the keyword
determines a different shift value for the corresponding plain text letter. This breaks the
simple frequency pattern that makes Caesar ciphers easy to crack, though it is still vulnerable
to techniques like the Kasiski examination.

**5. What is the XOR cipher and why is XOR used?**
XOR (exclusive-OR) combines each byte of plain text with a byte of the key using the bitwise XOR
operator. XOR is used because it is reversible: `(A XOR B) XOR B = A`, so the same operation
serves for both encryption and decryption. It is fast but insecure with short/reused keys.

**6. What does AES stand for and what key size does this project use?**
AES stands for **Advanced Encryption Standard**. This project uses **AES-128**, meaning a
128-bit (16-byte) symmetric key, operating in CBC (Cipher Block Chaining) mode with PKCS5 padding.

**7. Why is AES considered more secure than DES?**
AES uses a larger key size (128/192/256-bit vs DES's 56-bit effective key) and a more modern,
well-analyzed substitution-permutation network structure, making brute-force attacks
computationally infeasible with current technology, unlike DES which can be brute-forced.

**8. What is DES and why is it considered obsolete?**
DES (Data Encryption Standard) is a symmetric block cipher developed in the 1970s using a
56-bit key. Its small key space (2^56 possible keys) can be brute-forced by modern hardware in
a practical amount of time, so it is no longer considered secure for real-world use.

**9. What is Triple DES (3DES) and how does it work?**
3DES applies the DES algorithm three times in sequence (Encrypt-Decrypt-Encrypt) using either
two or three distinct keys, effectively producing a 112-bit or 168-bit key strength. It is more
secure than plain DES but slower and less secure than AES.

**10. What is a symmetric key algorithm?**
An algorithm where the **same key** is used for both encryption and decryption. All six
algorithms implemented in SecureCrypt (Caesar, Vigenère, XOR, AES, DES, 3DES) are symmetric.

**11. What is an Initialization Vector (IV) and why is it used?**
An IV is a random value combined with the first block of plain text (in CBC mode) so that
encrypting the same plain text with the same key produces different cipher text each time.
This prevents pattern leakage and is essential for semantic security. In this project, a fresh
random IV is generated for every AES/DES/3DES encryption and stored alongside the cipher text.

**12. What is PKCS5 Padding and why is it needed?**
Block ciphers like AES/DES/3DES operate on fixed-size blocks (e.g., 16 bytes for AES). If the
plain text isn't an exact multiple of the block size, padding bytes are added to fill the last
block. PKCS5 padding fills the remaining bytes with a value equal to the number of padding
bytes added, which also allows unambiguous removal during decryption.

**13. What is PBKDF2 and why does this project use it?**
PBKDF2 (Password-Based Key Derivation Function 2) converts a human-chosen password into a
fixed-length cryptographic key by applying a hash function (HMAC-SHA256 here) repeatedly
(65,536 iterations in this project). This is used because raw passwords are not the correct
length or format required by AES/DES/3DES key specifications, and repeated hashing slows down
brute-force/dictionary attacks against weak passwords.

**14. What is the Java Cryptography Architecture (JCA)?**
JCA is a framework built into the Java platform that provides a unified API for cryptographic
operations — encryption, decryption, key generation, hashing, and digital signatures — without
requiring the developer to implement the underlying algorithms manually. This project uses JCA's
`Cipher`, `SecretKeyFactory`, and `SecretKeySpec` classes for AES, DES, and 3DES.

**15. Why were Caesar, Vigenère, and XOR implemented manually instead of using a library?**
To demonstrate a fundamental understanding of how classical ciphers work at the character/byte
level, as required by the assignment, rather than relying on a black-box library call.

**16. What design pattern is used to select the correct encryption algorithm at runtime?**
The **Strategy Pattern**, implemented via the `EncryptionService` interface and
`CryptoServiceFactory`. Each algorithm has its own service class implementing the same
interface, and the factory returns the correct implementation based on the algorithm requested,
avoiding a long if/else or switch chain in the controller.

**17. How does the application ensure user data privacy?**
No text, keys, or passwords are ever written to a database, log file, or disk. Every request is
processed entirely in memory and the result is returned directly to the client; the "Session
History" table is also purely client-side (JavaScript), so nothing is stored on the server at all.

**18. What HTTP methods and endpoints does the backend expose?**
`POST /api/encrypt` and `POST /api/decrypt` for the core operations, plus two convenience GET
endpoints: `/api/generate-key` (random key generator) and `/api/key-strength` (strength checker).

**19. How does the application handle an incorrect decryption key?**
When AES/DES/3DES decryption is attempted with the wrong key, the JCA `Cipher.doFinal()` call
throws a `BadPaddingException` because the padding bytes recovered are invalid. The service
layer catches this and throws a custom `CryptoException` with a friendly message, which the
`GlobalExceptionHandler` converts into a clean HTTP 400 JSON response instead of a stack trace.

**20. What are the main limitations of this project?**
(1) A fixed application-level salt is used for PBKDF2 rather than a unique per-message salt, a
simplification made for demo reproducibility. (2) DES/3DES are included for educational
comparison only and should never be used in production. (3) The app has no user authentication,
persistent storage, or file-encryption support — it is scoped to short text snippets for
demonstration purposes.
