/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fr.atlantis.datamgmt.facade;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import fr.atlantis.datamgmt.domain.Calculated;
import fr.atlantis.datamgmt.domain.Device;
import fr.atlantis.datamgmt.domain.Measure;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeoutException;
import javax.ejb.EJB;
import javax.enterprise.context.RequestScoped;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.GenericEntity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

/**
 * REST Web Service
 *
 * @author gmartin
 */
@Path("data")
@RequestScoped
public class DataResource {
    final static String QUEUE_NAME = "commandQueue";
    
    
    @EJB
    private DataServiceRemote dataService;
    
    @Path("devices/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getUserDevices(@PathParam("id") String userId){
        List<Device> devices = dataService.lookUpAllUserDevices(userId);
        GenericEntity<List<Device>> genericList = new GenericEntity<List<Device>>(devices){};
        Response resp = Response.ok(genericList).build();
        return resp;
    }
    
    @Path("measures/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDeviceMeasures(@PathParam("id") String deviceId){
        List<Measure> measures = dataService.lookUpAllDeviceMeasures(deviceId);
        GenericEntity<List<Measure>> genericList = new GenericEntity<List<Measure>>(measures){};
        Response resp = Response.ok(genericList).build();
        return resp;
    }
    
    @Path("calculated/{id}")
    @GET
    @Produces({MediaType.APPLICATION_JSON})
    public Response getDeviceCalculated(@PathParam("id") String deviceId){
        List<Calculated> calculated = dataService.lookUpAllDeviceCalculated(deviceId);
        GenericEntity<List<Calculated>> genericList = new GenericEntity<List<Calculated>>(calculated){};
        Response resp = Response.ok(genericList).build();
        return resp;
    }
    
    @Path("sendCommand")
    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Response sendCommand(String content) throws IOException, TimeoutException{
        try {
            ConnectionFactory factory = new ConnectionFactory();
            factory.setHost("35.205.104.50");
            factory.setPort(5672);
            factory.setUsername("admin");
            factory.setPassword("password");
            Connection connection = factory.newConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(QUEUE_NAME, false, false, false, null);

            channel.basicPublish("", QUEUE_NAME, null, content.getBytes());
            System.out.println(" [x] Sent '" + content + "'");
            
            channel.close();
            connection.close();

            return Response.accepted().build();
        } catch (IOException e) {
            return Response.serverError().build();
        }
    }
}
