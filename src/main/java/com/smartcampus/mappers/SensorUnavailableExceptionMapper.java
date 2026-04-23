package com.smartcampus.mappers;

import com.smartcampus.exceptions.SensorUnavailableException;
import com.smartcampus.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class SensorUnavailableExceptionMapper
        implements ExceptionMapper<SensorUnavailableException> {

    @Override
    public Response toResponse(SensorUnavailableException e) {
        return Response
                .status(Response.Status.FORBIDDEN)
                .entity(new ErrorResponse(403, "Forbidden", e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
