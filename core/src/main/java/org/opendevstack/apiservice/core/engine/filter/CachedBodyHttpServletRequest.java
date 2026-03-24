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
 */
public class CachedBodyHttpServletRequest extends HttpServletRequestWrapper {

    private final byte[] cachedBody;

    public CachedBodyHttpServletRequest(HttpServletRequest request) throws IOException {
        super(request);
        this.cachedBody = StreamUtils.copyToByteArray(request.getInputStream());
    }

    public byte[] getBody() {
        return cachedBody;
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
