package com.smartcampus.mappers;

import com.smartcampus.models.ErrorResponse;
import javax.ws.rs.BadRequestException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

// Catches Jersey's BadRequestException thrown when a MessageBodyReader fails
// (e.g. malformed JSON sent by client). Without this, Jersey returns the raw
// Jackson error message including internal class names — an information disclosure.
@Provider
public class BadRequestExceptionMapper implements ExceptionMapper<BadRequestException> {

    @Override
    public Response toResponse(BadRequestException e) {
        return Response.status(Response.Status.BAD_REQUEST)
                .type(MediaType.APPLICATION_JSON)
                .entity(new ErrorResponse(400, "Bad Request",
                        "Request body is malformed or contains invalid JSON"))
                .build();
    }
}
