package vn.conghung.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceIdFilter is deprecated and no longer propagates MDC trace IDs.
 * It is kept temporarily to avoid breaking changes in downstream consumer services.
 * Use alternative logging or trace propagation solutions (e.g., Spring Cloud Sleuth or Micrometer).
 *
 * @deprecated This filter is deprecated and will be removed in a future release.
 */
@Deprecated(since = "0.2.10", forRemoval = true)
@Component
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}

