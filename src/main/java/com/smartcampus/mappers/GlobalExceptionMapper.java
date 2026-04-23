package com.smartcampus.mappers;

import com.smartcampus.models.ErrorResponse;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;
import java.util.logging.Logger;

// CRITICAL: <Throwable> not <Exception>
// Exception misses Error subclasses (OutOfMemoryError, StackOverflowError)
@Provider
public class GlobalExceptionMapper implements ExceptionMapper<Throwable> {

    private static final Logger LOGGER = Logger.getLogger(GlobalExceptionMapper.class.getName());

    @Override
    public Response toResponse(Throwable e) {
        if (e instanceof WebApplicationException) {
            WebApplicationException wae = (WebApplicationException) e;
            int status = wae.getResponse().getStatus();
            String reason = Response.Status.fromStatusCode(status) != null
                    ? Response.Status.fromStatusCode(status).getReasonPhrase()
                    : "Error";

            // Build a clean, sanitised message — never expose internal class names or stack frames
            String clientMessage = sanitise(status, e.getMessage());

            return Response.status(status)
                    .type(MediaType.APPLICATION_JSON)
                    .entity(new ErrorResponse(status, reason, clientMessage))
                    .build();
        }

        // Log full details server-side for debugging
        LOGGER.severe("Unhandled exception: [" + e.getClass().getName() + "] " + e.getMessage());

        // Return ONLY a generic message to the client — never expose internals
        return Response
                .status(Response.Status.INTERNAL_SERVER_ERROR)
                .entity(new ErrorResponse(500, "Internal Server Error",
                        "An unexpected error occurred. Please try again later."))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }

    private String sanitise(int status, String raw) {
        if (raw == null) return "Request could not be processed";
        // Strip any internal Java class names that might appear in Jersey/Jackson messages
        if (raw.contains("org.glassfish") || raw.contains("com.fasterxml") || raw.contains("java.")) {
            return status == 400
                    ? "Request body is malformed or contains invalid JSON"
                    : "Request could not be processed";
        }
        // Strip the leading "HTTP NNN Reason" prefix that WebApplicationException adds
        if (raw.matches("HTTP \\d{3} .+")) {
            return raw.replaceFirst("HTTP \\d{3} ", "");
        }
        return raw;
    }
}
