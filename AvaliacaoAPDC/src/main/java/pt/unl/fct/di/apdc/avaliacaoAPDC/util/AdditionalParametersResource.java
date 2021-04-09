package pt.unl.fct.di.apdc.avaliacaoAPDC.util;

public class AdditionalParametersResource {
	public String username;
	public String username2;
	public String newRole;
	
	public AdditionalParametersResource() {
		
	}
	
	public AdditionalParametersResource(String username, String username2,String newRole) {
		this.username = username;
		this.username2 = username2;
		this.newRole = newRole;
	}
	
	public boolean checkRole() {
		return this.newRole.matches("^SU|GA|GBO|USER$");
	}
}
