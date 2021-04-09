package pt.unl.fct.di.apdc.avaliacaoAPDC.resources;

import java.util.UUID;
import java.util.logging.Logger;

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
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.cloud.datastore.Entity.Builder;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.avaliacaoAPDC.util.AdditionalUserData;
import pt.unl.fct.di.apdc.avaliacaoAPDC.util.AuthToken;
import pt.unl.fct.di.apdc.avaliacaoAPDC.util.ChangePassword;

@Path("/password")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class ChangePasswordResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	private final Gson g = new Gson();
	private	final KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");	

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response changePassword(ChangePassword data) {					
		
		Transaction txn = datastore.newTransaction();
		try {
			return Response.ok("{}").build();
			/*
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);
			Key tokenKey = datastore.newKeyFactory()
					.addAncestor(PathElement.of("User", data.username))
					.setKind("Token").newKey(data.username);
			return Response.ok("{}").build();
			//Entity token = txn.get(tokenKey);
			
			
			/*
			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User non existant").build();
			}
			else if(token==null || token.getLong("expirationData") < System.currentTimeMillis() ) {
				txn.delete(tokenKey);
				txn.commit(); 
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User non existant").build();
			}
			else {					
				String hashedPWD = user.getString("password");
				if(hashedPWD.equals(DigestUtils.sha512Hex(data.oldPassword))&& data.password == data.password2){
					user = Entity.newBuilder(txn.get(userKey))
							.set("password", data.password)
							.build();
					txn.update(user);
					txn.commit();
					return Response.ok("{}").build();
				}
				LOG.warning("Wrong password for username: " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
			*/
		}finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
