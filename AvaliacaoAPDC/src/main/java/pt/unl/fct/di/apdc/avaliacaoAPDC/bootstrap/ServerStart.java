package pt.unl.fct.di.apdc.avaliacaoAPDC.bootstrap;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.avaliacaoAPDC.resources.Roles;

public class ServerStart implements ServletContextListener {

	
	private	final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();	
	
    @Override
    public void contextInitialized(ServletContextEvent sce) {	
		Transaction txn = datastore.newTransaction();
		try {	
			Key userKey = datastore.newKeyFactory().setKind("User").newKey("root");
			Entity user = txn.get(userKey);	
			if(user != null) {
				txn.rollback();
			}
			else {
				user = Entity.newBuilder(userKey)
						.set("username","root")
						.set("password", DigestUtils.sha512Hex("%GodM0de%"))
						.set("email", "")
						.set("user_creation_time", Timestamp.now())
						.set("role", Roles.SU.toString())
						.set("state", true)
						.build();
				txn.add(user);
				txn.commit();
			}
		}finally {
			if(txn.isActive())
				txn.rollback();
		}
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        // shutdown code here
    }
}

