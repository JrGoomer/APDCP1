package pt.unl.fct.di.apdc.avaliacaoAPDC.util;

public class RoleQuery {
	public String username;
	public String role;
	public RoleQuery() {
		
	}
	
	public boolean checkRole() {
		return this.role.matches("^SU|GA|GBO|USER$");
	}
}
