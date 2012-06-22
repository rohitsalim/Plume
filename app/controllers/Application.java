package controllers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import models.Friend;

import play.modules.facebook.*;

import play.mvc.Controller;
import play.mvc.Scope.Session;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import play.mvc.Http;

import com.google.gson.JsonObject;

public class Application extends Controller {
	
	//We should in reality store this in a database of friends
	private static HashMap<String, ArrayList<Friend>> LocationToFriends;
	
    public static void index() {
    	LocationToFriends = new HashMap<String, ArrayList<Friend>>(); 
        render();
    }
    
    public static void displayPage(String name){
    	render(name);
    }
    
    public static void facebookLogin() {
        String name = new String();
       
    	try {
            JsonObject profile = FbGraph.getObject("me"); // fetch the logged in user
            String userName = profile.get("username").getAsString(); // retrieve the email
            // do useful things
            Session.current().put("username", userName); // put the email into the session (for the Secure module)
            name = profile.get("name").getAsString();
        	populateLocationMap();
        	
        } catch (FbGraphException fbge) {
            flash.error(fbge.getMessage());
            if (fbge.getType() != null && fbge.getType().equals("OAuthException")) {
                Session.current().remove("username");
            }
        }
        displayPage(name);
    }

    public static void thingsToDo(String city){
    	ArrayList<Friend> arr = new ArrayList<Friend>();
    	if (LocationToFriends.containsKey(city)){
    		arr = LocationToFriends.get(city);
        	for (int i = 0; i< arr.size(); i++){
        		System.out.println("Size of arr "+arr.size() );
        		System.out.println(arr.get(i).toString());
        		System.out.println("\n\n\n");
        	}
    	}
    	render(city, arr);
    }
    
    public static boolean populateLocationMap(){
    	try {
			JsonArray friends = FbGraph.getConnection("me/friends");
			for(int i =0; i < 3; i++){
				
				JsonObject friend = friends.get(i).getAsJsonObject();
				Friend currentFbFriend = new Friend();
				currentFbFriend.id=Long.parseLong(friend.get("id").getAsString());
				currentFbFriend.fullName = friend.get("name").getAsString();
				String friendProfilePicURL = FbGraph.getPicture(currentFbFriend.id+"/"+"picture");
				currentFbFriend.profilePicURL= friendProfilePicURL;
				JsonObject friendAboutMe = FbGraph.getObject(currentFbFriend.id+"");
				
				//I now need to store the location and the hometown
				JsonElement loc = friendAboutMe.get("location");
				if (loc!= null){
					String location = loc.getAsJsonObject().get("name").getAsString();
					currentFbFriend.location= location;
				}
				else
					currentFbFriend.location="";
				
				JsonElement ht = friendAboutMe.get("hometown");
				if (ht != null){
					String hometown = ht.getAsJsonObject().get("name").getAsString();
					currentFbFriend.hometown= hometown;
				}
				else
					currentFbFriend.hometown="";
				
				if (currentFbFriend.location.equals("") && currentFbFriend.hometown.equals(""))
					continue;
				else{
					if (currentFbFriend.location.equals(currentFbFriend.hometown))
						addFriendToLocationMap(currentFbFriend, currentFbFriend.location);
					else{
						if (!currentFbFriend.location.equals(""))
							addFriendToLocationMap(currentFbFriend, currentFbFriend.location);	
						if (!currentFbFriend.hometown.equals(""))
							addFriendToLocationMap(currentFbFriend, currentFbFriend.hometown);
					}
				}
			}
		} catch (FbGraphException e) {
			e.printStackTrace();
			return false;
		}
    	return true;
    }
    
    public static boolean addFriendToLocationMap(Friend friend, String location){
    	if (LocationToFriends.containsKey(location)){
    		ArrayList<Friend> corrArr = LocationToFriends.get(location);
    		corrArr.add(friend);
    		LocationToFriends.put(location, corrArr);
    	}
    	else{
    		ArrayList<Friend> newArr = new ArrayList<Friend>();
    		newArr.add(friend);
    		LocationToFriends.put(location, newArr);
    	}
    	return true;
    }
    
    
    public static void facebookLogout() {
        Session.current().remove("username");
        FbGraph.destroySession();
        index();
    }

}