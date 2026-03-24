package org.opendevstack.apiservice.core.engine.filter;

import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.springframework.util.StreamUtils;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * Request wrapper that eagerly reads and caches the body, allowing it to be
 * read multiple times (e.g. once by a policy evaluator and again by the controller).
 *
 * <p>The cached bytes are also stored as a request attribute under
 * {@link #CACHED_BODY_ATTR} so that downstream components can retrieve
 * them regardless of how many {@link HttpServletRequestWrapper} layers
 * (e.g. Spring Security's {@code FirewalledRequest}) sit on top.
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    /**
     * Request attribute key under which the cached body {@code byte[]} is stored.
     * Use {@link #getCachedBody(HttpServletRequest)} for convenient, wrapper-safe access.
     */
    public static final String CACHED_BODY_ATTR = "oas.cachedRequestBody";

    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
        request.setAttribute(CACHED_BODY_ATTR, this.cachedBody);
    }

    public byte[] getBody() {
        return cachedBody;
    }

    /**
     * Retrieves the cached body from any request, regardless of wrapper layers.
     *
     * @param request the current request (may be wrapped multiple times)
     * @return the cached body bytes, or {@code null} if the body was not cached
     */
    public static byte[] getCachedBody(HttpServletRequest request) {
        Object attr = request.getAttribute(CACHED_BODY_ATTR);
        return (attr instanceof byte[] bytes) ? bytes : null;
    }

    @Override
    public ServletInputStream getInputStream() {
        return new CachedBodyServletInputStream(cachedBody);
    }

    @Override
    public BufferedReader getReader() {
        String encoding = getCharacterEncoding();
        Charset charset = (encoding != null) ? Charset.forName(encoding) : StandardCharsets.UTF_8;
        return new BufferedReader(new InputStreamReader(new ByteArrayInputStream(cachedBody), charset));
    }

    private static class CachedBodyServletInputStream extends ServletInputStream {

        private final InputStream source;

        CachedBodyServletInputStream(byte[] body) {
            this.source = new ByteArrayInputStream(body);
        }

        @Override
        public int read() throws IOException {
            return source.read();
        }

        @Override
        public boolean isFinished() {
            try {
                return source.available() == 0;
            } catch (IOException e) {
                return true;
            }
        }

        @Override
        public boolean isReady() {
            return true;
        }

        @Override
        public void setReadListener(ReadListener readListener) {
            throw new UnsupportedOperationException();
        }
    }
}
