package pt.unl.fct.di.apdc.avaliacaoAPDC.resources;


import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Entity.Builder;

import pt.unl.fct.di.apdc.avaliacaoAPDC.util.UserData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class RegisterResource {
	
	
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	
	public RegisterResource(){
		
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(UserData data) {
		if(!data.validRegistration()) {
			return Response.status(Status.BAD_REQUEST).entity("Wrong parameter(s)").build();
		}
		
		Transaction txn = datastore.newTransaction();
		try {	
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);	
			if(user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists").build();
			}
			else {
				Key attrKey = datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username))
						.setKind("UserAttrs").newKey(data.username);
				user = Entity.newBuilder(userKey)
						.set("username",data.username)
						.set("password", DigestUtils.sha512Hex(data.password))
						.set("email", data.email)
						.set("user_creation_time", Timestamp.now())
						.set("role", Roles.USER.toString())
						.set("state", true)
						.build();				
				Builder build2 = Entity.newBuilder(attrKey);
				if(data.profile!=null || data.visibility!=null) {
					if(data.visibility.matches("^PUBLIC|PRIVATE$") && data.profile!=null)
						build2.set("profile",data.profile,data.visibility.toUpperCase());
					else
						return Response.status(Status.BAD_REQUEST).entity("Occured an error while changing the profile").build();
				}
				if(data.phone!=null )
					if(data.phone())
						build2.set("phone",data.phone);
					else
						return Response.status(Status.BAD_REQUEST).entity("Occured an error while changing the phone number").build();
				if(data.mobile!=null)
					if(data.mobile())
						build2.set("mobile",data.mobile);
					else
						return Response.status(Status.BAD_REQUEST).entity("Occured an error while changing the mobile phone number").build();
				if(data.adress!=null)
					build2.set("adress",data.adress);
				if(data.adress2!=null)
					build2.set("adress2",data.adress2);					
				if(data.location!=null)
					build2.set("location",data.location);
				if(data.zipcode!=null)
					if(data.zipcode())
						build2.set("zipcode",data.zipcode);
					else
						return Response.status(Status.BAD_REQUEST).entity("Occured an error while changing the zipcode").build();				
				Entity attr = build2.build();
				txn.add(user,attr);
				txn.commit();
				return Response.ok("User succesfully registered!").build();
			}
		}finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}

