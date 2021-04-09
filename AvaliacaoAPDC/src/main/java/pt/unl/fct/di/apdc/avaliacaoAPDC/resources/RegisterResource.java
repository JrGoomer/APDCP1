package pt.unl.fct.di.apdc.avaliacaoAPDC.resources;

import java.util.logging.Logger;

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

import pt.unl.fct.di.apdc.avaliacaoAPDC.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class RegisterResource {
	
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	
	public RegisterResource(){
		
	}
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response register(RegisterData data) {
		LOG.fine("Register attempt by user: " + data.username);
		
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
						.set("role", "USER")
						.set("state", true)
						.build();
				Entity attr = Entity.newBuilder(attrKey)
							.set("profile","","")
							.set("phone","")
							.set("mobile", "")
							.set("adress","")
							.set("adress2","")
							.set("location","")
							.set("zipcode","")
							.build();
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

