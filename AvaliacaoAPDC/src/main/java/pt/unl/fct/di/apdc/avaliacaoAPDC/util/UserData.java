package pt.unl.fct.di.apdc.avaliacaoAPDC.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;



public class UserData {
	public String password;
	public String password2;
	public String oldPassword;
	public String username;
	public String email;
	public String profile;
	public String phone;
	public String mobile;
	public String adress;
	public String adress2;
	public String location;
	public String zipcode;
	public String visibility;
	public boolean publ;
	
	public UserData() {
	}
	
	public boolean mobile() {
		Pattern p = Pattern.compile("^\\+351 (91|93|96)[0-9]{7}$", Pattern.CASE_INSENSITIVE);
		Matcher validPhone = p.matcher(mobile);
		return validPhone.find();
	}
	
	public boolean phone() {
		Pattern p = Pattern.compile("^\\+351 [0-9]{9}$", Pattern.CASE_INSENSITIVE);
		Matcher validPhone = p.matcher(phone);
		return validPhone.find();
	}
	
	public boolean zipcode() {
		Pattern p = Pattern.compile("^[0-9]{4}-[0-9]{3}$", Pattern.CASE_INSENSITIVE);
		Matcher validZip = p.matcher(zipcode);
		return validZip.find();
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
		return username.length()>6 && password.length()>8 && email() && password() && password.equals(password2);
	}
	
}

