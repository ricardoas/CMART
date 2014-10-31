package populator;

/**
 * This class hold all of the data about a user
 * 
 * @author Andy (andrewtu@cmu.edu)
 * @version 0.1
 * @since 0.1
 * @date 04/05/2011
 */
//TODO: split address from this. User should be allowed multiple addresses
public class User {
	// Variables to hold the user data
	private long id;
	private String firstName = null;
	private String lastName = null;
	private String username = null;
	private String password = null;
	private String email = null;
	private String authToken = null;
	private String rating=null;

	//TODO: Andy
	// rating
	// amount of feedback
	
	/**
	 * Create a new user with all of the required data
	 * @param id
	 * @param firstName
	 * @param lastName
	 * @param username
	 * @param password
	 * @param email
	 * @param authToken
	 * @param rating
	 */
	public User(long id, String firstName, String lastName, String username, String password, String email, String authToken, String rating){
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.username = username;
		this.password = password;
		this.email = email;
		this.rating = rating;
	}
	
	//Create a new user with only the public information
	public User(long id, String username,String rating){
		this.id = id;
		this.username = username;
		this.rating=rating;
	}
	
	/**
	 * Returns the user's userID
	 * 
	 * @return int the userID
	 */
	public long getID(){
		return this.id;
	}
	
	/**
	 * Returns the user's firstname
	 * @return String the user's first name
	 */
	public String getFirstName(){
		return this.firstName;
	}
	
	/**
	 * Returns the user's lastname
	 * 
	 * @return String the user's last name
	 */
	public String getLastName(){
		return this.lastName;
	}
	
	/**
	 * Returns the user's username
	 * 
	 * @return String the user's username
	 */
	public String getUsername(){
		return this.username;
	}
	
	/**
	 * Returns the user's password
	 * 
	 * @return String the user's password
	 */
	public String getPassword(){
		return this.password;
	}
	
	/**
	 * Returns the user's email address
	 * 
	 * @return String the user's email address
	 */
	public String getEmail(){
		return this.email;
	}
	
	/**
	 * Returns the user's street address
	 * 
	 * @return String the user's street address
	 */
	/*public String getStreet(){
		return this.street;
	}
	
	/**
	 * Returns the user's town
	 * 
	 * @return String the user's town
	 * @author Andy (andrewtu@cmu.edu)
	 */
	/*public String getTown(){
		return this.town;
	}
	
	/**
	 * Returns the user's authToken
	 * 
	 * @return String the user's authToken
	 * @author Andy (andrewtu@cmu.edu)
	 */
	public String getAuthToken(){
		return this.authToken;
	}
	
	/**
	 * Returns the user's zip code
	 * 
	 * @return String the user's zip code
	 */
	/*public String getZip(){
		return this.zip;
	}
	
	/**
	 * Returns the user's stateID number
	 * 
	 * @return int the state's ID number
	 * @author Andy (andrewtu@cmu.edu)
	 */
	/*public int getState(){
		return this.state;
	}*/
	
	public String getRating(){
		return this.rating;
	}
}
