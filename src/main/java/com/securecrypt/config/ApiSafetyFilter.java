package com.securecrypt.config;
import jakarta.servlet.*;
import jakarta.servlet.http.*;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/** Bounds concurrent server crypto work; never stores caller input or per-client identifiers. */
@Component
public class ApiSafetyFilter extends OncePerRequestFilter {
    private final Semaphore slots = new Semaphore(2);
    private final java.util.ArrayDeque<Long> recent = new java.util.ArrayDeque<>();
    private synchronized boolean withinBudget() {
        long now = System.nanoTime();
        while (!recent.isEmpty() && now - recent.peekFirst() >= 60_000_000_000L) recent.removeFirst();
        if (recent.size() >= 120) return false;
        recent.addLast(now); return true;
    }
    @Override protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain) throws ServletException, IOException {
        response.setHeader("X-Content-Type-Options", "nosniff");
        response.setHeader("Referrer-Policy", "no-referrer");
        if (!request.getRequestURI().startsWith("/api/")) { chain.doFilter(request,response); return; }
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Pragma", "no-cache");
        if (request.getContentLengthLong() > 512000) { reject(response,413,"Request body is too large."); return; }
        if (!withinBudget()) { response.setHeader("Retry-After","60"); reject(response,429,"Shared API budget reached. Try again in a minute."); return; }
        if (!slots.tryAcquire()) { response.setHeader("Retry-After","2"); reject(response,429,"Server is busy. Try again shortly."); return; }
        try { chain.doFilter(request,response); } finally { slots.release(); }
    }
    private void reject(HttpServletResponse response,int code,String message) throws IOException {
        response.setStatus(code); response.setContentType("application/json");
        response.getWriter().write("{\"success\":false,\"message\":\""+message+"\"}");
    }
}

