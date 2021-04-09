package pt.unl.fct.di.apdc.avaliacaoAPDC.resources;



import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;



import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.avaliacaoAPDC.util.AdditionalParametersResource;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class LogoutResource {
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	
	public LogoutResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginV1(AdditionalParametersResource data) {
		Transaction txn = datastore.newTransaction();
		try {	
			Key tokenKey = datastore.newKeyFactory()
					.addAncestor(PathElement.of("User", data.username))
					.setKind("Token").newKey(data.username);
			Entity token = txn.get(tokenKey);
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = txn.get(userKey);	
			if(user == null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User non existant").build();
			}
			if(token !=null) {
					txn.delete(tokenKey);
					txn.commit(); 
					return Response.ok("Logged out successfully").build();
			}
			else {
					txn.rollback();
					return Response.status(Status.FORBIDDEN).build();
			}	
			
		}finally {
			if(txn.isActive())
				txn.rollback();
		}
	}
}
