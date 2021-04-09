package pt.unl.fct.di.apdc.avaliacaoAPDC.util;

public class StateData {
	public String username;
	public String username2;
	public boolean newState;
	public String newRole;
	
	public StateData() {
		
	}
	
	public StateData(String username, String username2,boolean newState) {
		this.username = username;
		this.username2 = username2;
		this.newState = newState;
	}
}
