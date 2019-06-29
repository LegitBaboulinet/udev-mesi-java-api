package com.udev.mesi;

import com.udev.mesi.messages.WsGetFlightDetails;
import com.udev.mesi.services.FlightDetailsService;
import org.json.JSONException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/flightDetails")
public class FlightDetailsServlet {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response get() throws JSONException {
        WsGetFlightDetails response = FlightDetailsService.read();
        return Response.status(response.getCode()).entity(response).build();
    }
}
