package vn.conghung.common.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * TraceIdFilter is deprecated and no longer propagates MDC trace IDs.
 * It is kept temporarily to avoid breaking changes in downstream consumer services.
 * Use alternative logging or trace propagation solutions (e.g., Spring Cloud Sleuth or Micrometer).
 *
 * <p>As of TS-018 gap G3 this filter is <b>no longer auto-registered</b>: the {@code @Component}
 * stereotype was removed so a no-op filter is not injected into every consumer's servlet chain.
 * A library should not rely on component-scan to wire infrastructure. The class is retained for
 * source compatibility and is slated for full removal in the next major release.
 *
 * @deprecated This filter is deprecated and will be removed in a future release.
 */
@Deprecated(since = "0.2.10", forRemoval = true)
public class TraceIdFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        filterChain.doFilter(request, response);
    }
}

