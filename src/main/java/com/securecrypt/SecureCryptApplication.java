package com.securecrypt;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SecureCrypt - Main Application Entry Point
 *
 * A mini cyber security project built to demonstrate the working of classical
 * and modern encryption algorithms (Caesar, Vigenere, XOR, AES-128, DES, 3DES)
 * through a simple Spring Boot REST backend and a Bootstrap based front end.
 *
 * Author  : (Your Name)
 * Course  : B.Tech - Cyber Security, 2nd Year
 * Subject : Fundamentals of Cyber Security
 */
@SpringBootApplication
public class SecureCryptApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecureCryptApplication.class, args);
        System.out.println("=================================================");
        System.out.println("  SecureCrypt is running!");
        System.out.println("  Open your browser -> http://localhost:8080");
        System.out.println("=================================================");
    }
}
