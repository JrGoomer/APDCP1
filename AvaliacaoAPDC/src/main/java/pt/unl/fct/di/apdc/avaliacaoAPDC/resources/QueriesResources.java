package pt.unl.fct.di.apdc.avaliacaoAPDC.resources;


import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.StructuredQuery.PropertyFilter;
import com.google.cloud.datastore.Query;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.QueryResults;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;


import pt.unl.fct.di.apdc.avaliacaoAPDC.util.RoleQuery;

@Path("/query")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class QueriesResources {

	
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	private final Gson g = new Gson();
	
	
	@POST
	@Path("/byRole")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response byRole(RoleQuery data) {
		
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
				return Response.status(Status.BAD_REQUEST).entity("User does not exist").build();
			}
			if(!user.getString("role").equals(Roles.GBO.toString())) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User has no permission to execute this action").build();
			}
			else if(token==null || token.getLong("expirationData") < System.currentTimeMillis() ) {
				txn.delete(tokenKey);
				txn.commit(); 
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User not logged in").build();
			}	
			else if(!data.checkRole()) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("Role not supported").build();
			}
			else {
				String role = data.role;
				
                Query<Entity> query = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter( 
                                PropertyFilter.eq("role", role)
                        )
                .build();

                QueryResults<Entity> logs = datastore.run(query);

                
                Map<String,String> creationDates = new HashMap<String,String>();
                logs.forEachRemaining(userlog->{
                    creationDates.put(userlog.getString("username"), userlog.getString("role"));
                });
        
                
                
                return Response.ok(g.toJson(creationDates)).build();
			}
		}finally {
			if(txn.isActive())
				txn.rollback();
		}
	}

}
