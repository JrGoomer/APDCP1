package pt.unl.fct.di.apdc.avaliacaoAPDC.util;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterData {
	public String username;
	public String password,password2;
	public String email;
	public Role role;
	public AuthToken token;
	public enum Role {
		USER,
	    GBO,
	    GA,
	    SU
	 }
	
	public RegisterData() {
		
	}
	
	public RegisterData(String username, String password,String password2, String email, String name) {
		this.username = username;
		this.password = password;
		this.password2 = password2;
		this.email = email;
		this.role = Role.USER;
	}
	
	public boolean password() {
		Pattern p = Pattern.compile("[^a-z0-9]", Pattern.CASE_INSENSITIVE);
		Pattern p2 = Pattern.compile("[0-9]", Pattern.CASE_INSENSITIVE);
		Matcher hasSymbols = p.matcher(password);
		Matcher hasNumbers = p2.matcher(password);
		return password.length()>8 && hasSymbols.find() && hasNumbers.find();
	}
	
	public boolean email() {
		Pattern p3 = Pattern.compile("^[a-zA-Z0-9]+@(.+)\\.(com|org|net|int|edu|gov|mil)$", Pattern.CASE_INSENSITIVE);
		Matcher validEmail = p3.matcher(email);
		return validEmail.find();
	}
	
	public boolean validRegistration() {
		Pattern p = Pattern.compile("[^a-zA-Z0-9]", Pattern.CASE_INSENSITIVE);
		Pattern p2 = Pattern.compile("[0-9]", Pattern.CASE_INSENSITIVE);
		Pattern p3 = Pattern.compile("^[a-zA-Z0-9]+@(.+)\\.(com|org|net|int|edu|gov|mil)$", Pattern.CASE_INSENSITIVE);
		Matcher hasSymbols = p.matcher(password);
		Matcher hasNumbers = p2.matcher(password);
		Matcher validEmail = p3.matcher(email);

		//return username.length()>6 && password.length()>8 && hasSymbols.find() && hasNumbers.find() && validEmail.find() && password==password2;
		return true;
	}
	
}
