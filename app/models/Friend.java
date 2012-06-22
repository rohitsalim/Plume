package models;

public class Friend {
	public long id;
	public String fullName;
	public String profilePicURL;
	public String hometown;
	public String location;
	//		#{friendsBox friend1:friend, as:'expanded' /}

	public String toString(){
		return ("Id: "+id+"\nFull Name: "+ fullName+"\nProfile Pic URL: "+profilePicURL+"\nHometown: "+hometown+"\nLocation: "+location);
	}
}
