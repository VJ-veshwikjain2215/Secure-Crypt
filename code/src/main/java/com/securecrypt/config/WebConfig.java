package com.securecrypt.config;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/** Same-origin API by default. No unrestricted third-party browser access is advertised. */
@Configuration
public class WebConfig implements WebMvcConfigurer {}
