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
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;
import com.google.gson.Gson;

import pt.unl.fct.di.apdc.avaliacaoAPDC.util.AuthToken;
import pt.unl.fct.di.apdc.avaliacaoAPDC.util.LoginData;


@Path("/login")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf8")
public class LoginResource {
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	private final Gson g = new Gson();	
	public LoginResource() {}

	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response loginV1(LoginData data) {
		LOG.fine("Login attempt by user: " + data.username);
		Transaction txn = datastore.newTransaction();
		Key tokenKey = datastore.newKeyFactory()
				.addAncestor(PathElement.of("User", data.username))
				.setKind("Token").newKey(data.username);
		Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
		Entity user = txn.get(userKey);
		try {	
			if(user !=null && user.getBoolean("state")) {
				String hashedPWD = user.getString("password");
				if(hashedPWD.equals(DigestUtils.sha512Hex(data.password))){
					String tokenId =UUID.randomUUID().toString();
					long currTime =System.currentTimeMillis();
					AuthToken token2 = new AuthToken(data.username,user.getString("role")); 
					Entity token = Entity.newBuilder(tokenKey)
							.set("role", user.getString("role"))
							.set("token", tokenId)
							.set("creationData", currTime)
							.set("expirationData", currTime + AuthToken.EXPIRATION_TIME)
							.build();
					txn.put(token);
					txn.commit();
					return Response.ok(g.toJson(token2)).build();
				}
				else {
					txn.rollback();
					return Response.status(Status.FORBIDDEN).build();
				}
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
