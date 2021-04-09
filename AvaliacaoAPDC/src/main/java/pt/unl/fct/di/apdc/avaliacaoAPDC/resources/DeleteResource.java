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

import pt.unl.fct.di.apdc.avaliacaoAPDC.util.StateData;


@Path("/delete")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class DeleteResource {
	
	
	
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	
	
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response delete(StateData data) {	
		Transaction txn = datastore.newTransaction();
		try {	
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			Entity user = datastore.get(userKey);
			Key tokenKey = datastore.newKeyFactory()
					.addAncestor(PathElement.of("User", data.username))
					.setKind("Token").newKey(data.username);
			Entity token = txn.get(tokenKey);
			Key userKey2 = datastore.newKeyFactory().setKind("User").newKey(data.username2);
			Entity user2 = datastore.get(userKey2);
			if(user == null || user2==null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User not available.").build();
			}
			else if(token==null || token.getLong("expirationData") < System.currentTimeMillis() ) {
				txn.delete(tokenKey);
				txn.commit(); 
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User non existant").build();
			}
			else if((data.username.equals(data.username2) && user.getString("role").equals(Roles.USER.toString()) )
					|| user2.getString("role").equals(Roles.USER.toString()) 
					&& (user.getString("role").equals(Roles.GBO.toString()) ||user.getString("role").equals(Roles.GA.toString()) )){
				
				Key attr = datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username2))
						.setKind("UserAttrs").newKey(data.username2);
				Key tokenKey2 = datastore.newKeyFactory()
						.addAncestor(PathElement.of("User", data.username2))
						.setKind("Token").newKey(data.username2);
				Entity token2 = txn.get(tokenKey2);
				if(user.getString("role").equals(Roles.GA.toString()) && token2 !=null) {
					txn.delete(tokenKey2);
				}
				txn.delete(userKey2);
				txn.delete(attr);
				txn.commit();
				return Response.ok("User deleted").build();
			}
			else {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User has no premission to remove this account").build();
			}
		}finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
