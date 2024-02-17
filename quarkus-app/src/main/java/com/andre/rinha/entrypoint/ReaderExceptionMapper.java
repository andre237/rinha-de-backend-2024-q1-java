package com.andre.rinha.entrypoint;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import jakarta.ws.rs.ext.Provider;
import org.jboss.resteasy.spi.ReaderException;



/**
 * Maps a ReaderException to a response.  A ReaderException is most often thrown
 * when we get a bad JSON packet.
 *
 */
@Provider
public class ReaderExceptionMapper implements ExceptionMapper<ReaderException> {

    @Override
    public Response toResponse(ReaderException exception) {
        return Response.status(422)
                .entity(exception.getMessage())
                .type(MediaType.TEXT_PLAIN_TYPE)
                .build();
    }
}
