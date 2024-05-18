package com.example.parsertest.rest.ressources;

import com.example.parsertest.entities.Epigraphe;
import com.example.parsertest.parser.EpicherchelParser;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

/**
 * REST resource class to handle Epigraphe related operations.
 */
@Path("/epigraphes")
public class EpigrapheResource {

    /**
     * Retrieves an Epigraphe by its ID.
     *
     * @param id The ID of the epigraphe.
     * @return Response containing the Epigraphe or an error message.
     */
    @GET
    @Path("/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getEpigrapheById(@PathParam("id") int id) {
        Epigraphe epigraphe = EpicherchelParser.getEpigraphe(id);
        if (epigraphe == null) {
            return Response.status(Response.Status.NOT_FOUND)
                           .entity("Epigraphe with ID " + id + " not found.")
                           .build();
        }

        return Response.ok(epigraphe).build();
    }
}
