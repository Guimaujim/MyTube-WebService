package MyTubeRESTwsWeb;


import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
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

import java.net.HttpURLConnection;
import java.net.URL;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import com.google.gson.Gson;


@RequestScoped
@Path("")
@Produces({ "application/xml", "application/json" })
@Consumes({ "application/xml", "application/json" })
public class MyTubeResource {
    
	// Handles the data base requests
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
	
	// POST a File
	@POST
	@Path("/file")
	public Response postFile(fileData f) throws SQLException {
		Statement st = getStatement();
		String key = UUID.randomUUID().toString(); //Server generates a random unique key for the file
			
		try{
			st.executeUpdate("INSERT INTO file(key, name, description, server_id) VALUES ("
					+ "'" + key + "'," 
					+ "'" + f.getName() +  "',"
					+ "'" + f.getDescription() +  "'," 
					+ "'" + f.getServerId() +  "');");
		} catch (Exception e) {  
	        System.err.println(e.getMessage()); 
	    } 

		st.close();
		return Response.status(201).entity(key).build();
	}
	
	// GET a File by name
	@GET
	@Path("/filen/{name}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFileByName(@PathParam("name") String name){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT key, name, description, server_id FROM file "
					+ "WHERE name ='" + name + "';");
			List<fileData> af = new ArrayList<>();
			if(!rs.isBeforeFirst()){
				return Response.status(404).entity("File not found").build();
			}else{
				while(rs.next()){		
					fileData f = new fileData();
					f.setKey(rs.getString("key"));
					f.setName(name);
					f.setDescription(rs.getString("description"));
					f.setServerId(rs.getString("server_id"));
					af.add(f);
				}
				rs.next();
			}
			
			st.close();
			return Response.status(200).entity(af).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
	// GET a File by key
	@GET
	@Path("/filek/{key}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getFileByKey(@PathParam("key") String key){	 
		try {
			Statement st = getStatement();
			ResultSet rs = st.executeQuery("SELECT key, name, description, server_id FROM file "
					+ "WHERE key ='" + key + "';");
			fileData f = new fileData();
			if(!rs.isBeforeFirst()){
				return Response.status(404).entity("File not found").build();
			}else{
				rs.next();
				f.setKey(rs.getString("key"));
				f.setName(key);
				f.setDescription(rs.getString("description"));
				f.setServerId(rs.getString("server_id"));
			}
			
			st.close();
			return Response.status(200).entity(f).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
	// DELETE a File by key
	@DELETE
	@Path("/file/{key}/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteFile(@PathParam("key") String key){
		try{
			Statement st = getStatement();
			st.executeUpdate("DELETE FROM file WHERE key = '" + key + "';");
			st.close();
			return Response.status(204).build();
			
		}catch(SQLException ex){
			return Response.status(500).entity("Database ERROR").build();
		}
	}
    
	// POST a Server
	@POST
	@Path("/server")
	public Response postServer(serverData s) throws SQLException{
		Statement st = getStatement();
		
		try {
			st.executeUpdate("INSERT INTO server(server_id, ip, port) VALUES ("
							+ "'" + s.getId() + "'," 
							+ "'" + s.getIp() +  "',"
							+ "'" + s.getPort() +  "');");

		} catch (Exception e) {
			System.err.println(e.getMessage()); 
		}
		
		st.close();
		return Response.status(201).entity(s.getId()).build();		
	}
	
	// GET a Server by Id
	@GET
	@Path("/server/{server_id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getServer(@PathParam("server_id") String server_id){	 
		try {
			Statement state = getStatement();
			ResultSet resset = state.executeQuery("SELECT server_id, ip, port FROM server "
					+ "WHERE server_id ='" + server_id + "';");
			serverData server = new serverData();
			
			if(!resset.isBeforeFirst())
				return Response.status(404).entity("server not found").build();
			else{
				resset.next();
				server.setId(resset.getString("server_id"));
				server.setIp(resset.getString("ip"));
				server.setPort(resset.getString("port"));
			}
			state.close();
			return Response.status(200).entity(server).build();
			
		} catch (SQLException e) {
			return Response.status(500).entity("Database ERROR" + e.toString()).build();
		}
	}
	
	// DELETE a server by server_id
	@DELETE
	@Path("/server/{server_id}/delete")
	@Produces(MediaType.APPLICATION_JSON)
	public Response deleteServer(@PathParam("server_id") String server_id){
		try{
			Statement st = getStatement();
			st.executeUpdate("DELETE FROM server WHERE server_id = '" + server_id + "';");
			st.close();
			return Response.status(204).build();
			
		}catch(SQLException ex){
			return Response.status(500).entity("Database ERROR").build();
		}
	}
}
