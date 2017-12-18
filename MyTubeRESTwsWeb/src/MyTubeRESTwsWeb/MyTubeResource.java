package MyTubeRESTwsWeb;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.enterprise.context.RequestScoped;
import javax.naming.InitialContext;
import javax.sql.DataSource;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import MyTubeRMI.*;
import classData.fileData;
import classData.serverData;

@RequestScoped
@Path("")
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
public class MyTubeResource {
	
    @Path("/text")
    @GET
    @Produces(MediaType.TEXT_PLAIN)
    public String hello()
    {
    	fileData f = new fileData();
        f.setKey("ID2");
        f.setname("NameFile2");
        f.setServerId("2");
        f.setdescription("Hola2");
        
        try {
            // All the information about the uploads will be stored
            // on the data base
            addToDataBase(f); 
        } catch (Exception e) {
            Logger.getLogger(MyTubeImpl.class.getName())
                    .log(Level.SEVERE, null, e);
        }
        
    	return "Hello everybody!";
    }
	// Handles the data base
	public Statement getStatement(){
		try {
			InitialContext cxt = new InitialContext();
			DataSource data = (DataSource) cxt.lookup("java:/PostgresXADS");
			Connection connection = data.getConnection();
			Statement statement = connection.createStatement();
			return statement;
				
		} catch (Exception e) {
			System.out.println("DB not loaded");
			return null;
		}
	}
	
	// POST a File to Database
	@POST
	@Path("/file")
	public Response addToDataBase(fileData f) throws SQLException {
		//try {
			Statement st = getStatement();
			String id = UUID.randomUUID().toString();
			
			try{
				st.executeUpdate("INSERT INTO file(key, name, description, server_id) VALUES ("
						+ "'" + f.getKey() + "'," 
						+ "'" + f.getName() +  "',"
						+ "'" + f.getDescription() +  "'," 
						+ "'" + f.getServerId() +  "');");
			} catch (Exception e) {  
	            System.err.println(e.getMessage()); 
	        } 

			st.close();
			return Response.status(201).entity(id).build();

	}
}
