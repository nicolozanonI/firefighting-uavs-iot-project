package it.iot.project.drone.resources;

import io.dropwizard.jersey.errors.ErrorMessage;
import it.iot.project.drone.services.AppConfig;

import javax.ws.rs.*;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;



@Path("/api/iot/project/mission")
public class MessageResource {

    public AppConfig appConfig;

    public MessageResource(AppConfig appConfig) {
        this.appConfig = appConfig;
    }


    @GET
    @Path("/")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDevices(@Context ContainerRequestContext req) {

        try {

            List<String> missionsList = this.appConfig.getMessageManager().getMissionsList();

            if(missionsList == null)
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                                "Not Found !"))
                        .build();

            return Response.ok(missionsList).build();


        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON_TYPE).
                    entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            "Internal Server Error !")).build();
        }
    }

    @GET
    @Path("/{mission_id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getDeviceWithId(@Context ContainerRequestContext req,
                                    @PathParam("mission_id") String missionId) {

        try {

            System.out.println("Loading mission with id: " + missionId);

            Object messageList = this.appConfig.getMessageManager().getMessageList(missionId);

            if(messageList == null)
                return Response.status(Response.Status.NOT_FOUND)
                        .type(MediaType.APPLICATION_JSON_TYPE)
                        .entity(new ErrorMessage(Response.Status.NOT_FOUND.getStatusCode(),
                                "Not Found !"))
                        .build();

            return Response.ok(messageList).build();

        } catch (Exception e){
            e.printStackTrace();
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .entity(new ErrorMessage(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(),
                            "Internal Server Error !")).build();
        }
    }

}
