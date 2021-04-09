package pt.unl.fct.di.apdc.avaliacaoAPDC.resources;



import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;


import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Entity.Builder;
import com.google.cloud.datastore.Key;

import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;


import pt.unl.fct.di.apdc.avaliacaoAPDC.util.UserData;
import pt.unl.fct.di.apdc.avaliacaoAPDC.util.StateData;
import pt.unl.fct.di.apdc.avaliacaoAPDC.util.AdditionalParametersResource;

@Path("/modify")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class ModifyAttributes {
		private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	

		@POST
		@Path("/attr")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
		public Response modifyAttr(UserData data) {					
			
			Transaction txn = datastore.newTransaction();
			try {	
				Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
				Entity user = txn.get(userKey);	
				Key tokenKey = datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username))
						.setKind("Token").newKey(data.username);
				Entity token = txn.get(tokenKey);
				if(user == null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User non existant").build();
				}
				else if(token==null || token.getLong("expirationData") < System.currentTimeMillis() ) {
					txn.delete(tokenKey);
					txn.commit(); 
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
				}
				else {
					Key attrKey = datastore.newKeyFactory()
							.addAncestor(PathElement.of("User", data.username))
							.setKind("UserAttrs").newKey(data.username);	
					Builder build = Entity.newBuilder(txn.get(userKey));
					Builder build2 = Entity.newBuilder(attrKey);
					if(data.password!=null) {	
						String hashedPWD = user.getString("password");
						if(hashedPWD.equals(DigestUtils.sha512Hex(data.oldPassword)) && data.password.equals(data.password2) && data.password()) {
							build.set("password",DigestUtils.sha512Hex(data.password));
							txn.delete(tokenKey);
						}
						else 
							return Response.status(Status.BAD_REQUEST).entity("Occured an error while changing the password").build();
					}
					if(data.email!=null) {
						if(data.email())
							build.set("email",data.email);	
						else
							return Response.status(Status.BAD_REQUEST).entity("Occured an error while changing the email").build();
					}
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
					user = build.build();
					Entity attr = build2.build();
					txn.update(user,attr);
					txn.commit();
					return Response.ok("Changes applied").build();
				}
			}finally {
				if(txn.isActive())
					txn.rollback();
			}
		}
		
		
		
		@POST
		@Path("/role")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
		public Response modifyRole(AdditionalParametersResource data) {					
			
			Transaction txn = datastore.newTransaction();
			try {	
				Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
				Key userKey2 = datastore.newKeyFactory().setKind("User").newKey(data.username2);
				Entity user = txn.get(userKey);
				Entity user2 = txn.get(userKey2);
				Key tokenKey = datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username))
						.setKind("Token").newKey(data.username);
				Entity token = txn.get(tokenKey);
				if(user == null || user2==null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User non existant").build();
				}
				else if(token==null || token.getLong("expirationData") < System.currentTimeMillis() ) {
					txn.delete(tokenKey);
					txn.commit(); 
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
				}
				else if(!data.checkRole()) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("Unrecognized role").build();
				}				
				else {					
					String role=user.getString("role");
					String role2=user2.getString("role");
					if(((role.equals(Roles.GA.toString()) || role.equals(Roles.SU.toString())) && role2.equals(Roles.USER.toString()) && data.newRole.equals(Roles.GBO.toString())) 
							|| (role.equals(Roles.SU.toString()) && data.newRole.equals(Roles.GA.toString()) && role2.equals(Roles.USER.toString()))) {
						user2 = Entity.newBuilder(txn.get(userKey2))
								.set("role", data.newRole)
								.build();
						txn.update(user2);
						txn.commit();
						return Response.ok("New Role applied!").build();
					}
					else{
						txn.rollback();
						return Response.status(Status.BAD_REQUEST).entity("User has no premissions").build();
					}			
				}
			}finally {
				if(txn.isActive())
					txn.rollback();
			}
		}
		
		
		@POST
		@Path("/state")
		@Consumes(MediaType.APPLICATION_JSON)
		@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
		public Response modifyState(StateData data) {					
			
			Transaction txn = datastore.newTransaction();
			try {	
				Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
				Key userKey2 = datastore.newKeyFactory().setKind("User").newKey(data.username2);
				Entity user = txn.get(userKey);
				Entity user2 = txn.get(userKey2);
				Key tokenKey = datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username))
						.setKind("Token").newKey(data.username);
				Entity token = txn.get(tokenKey);
				if(user == null || user2==null) {
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
				}
				else if(token==null || token.getLong("expirationData") < System.currentTimeMillis() ) {
					txn.delete(tokenKey);
					txn.commit(); 
					txn.rollback();
					return Response.status(Status.BAD_REQUEST).entity("User non existant").build();
				}
				else {		
					String role=user.getString("role");
					String role2=user2.getString("role");
					if((!role.equals(Roles.USER.toString())) || (role.equals(Roles.GA.toString()) || role.equals(Roles.SU.toString()) && role2.equals(Roles.GBO.toString())) || (role.equals(Roles.SU.toString()) && role2.equals(Roles.GA.toString()))) {
						Key tokenKey2 = datastore.newKeyFactory()
								.addAncestor(PathElement.of("User", data.username2))
								.setKind("Token").newKey(data.username2);
						Entity token2 = txn.get(tokenKey2);
						user2 = Entity.newBuilder(txn.get(userKey2)).set("state", data.newState)
							.build();
						if(role.equals(Roles.GA.toString()) && token2 !=null && !data.newState) {
							txn.delete(tokenKey2);
						}
					}
					
					txn.update(user2);
					txn.commit();
					return Response.ok("State changed with success").build();
				}
			}finally {
				if(txn.isActive())
					txn.rollback();
			}
		}
		
		
}
