package com.smartcampus.mappers;

import com.smartcampus.exceptions.RoomNotEmptyException;
import com.smartcampus.models.ErrorResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

@Provider
public class RoomNotEmptyExceptionMapper implements ExceptionMapper<RoomNotEmptyException> {

    @Override
    public Response toResponse(RoomNotEmptyException e) {
        return Response
                .status(Response.Status.CONFLICT)
                .entity(new ErrorResponse(409, "Conflict", e.getMessage()))
                .type(MediaType.APPLICATION_JSON)
                .build();
    }
}
