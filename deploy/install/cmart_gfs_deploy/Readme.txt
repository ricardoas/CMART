The MongoDB can be started using mongod command under the /mongodb-linux-x86_64-2.0.4-rc0/bin/ folder.


https config

	
%JAVA_HOME%\bin\keytool -genkeypair -alias MyCert -keyalg RSA /keystore "C:\Users\(yourname)\Documents\MyCert.cert"

Change the server.xml file in tomcat.

http://tomcat.apache.org/tomcat-7.0-doc/ssl-howto.html
http://www.youtube.com/watch?v=yVAjpdLeqbw


I have changed DBQuery, MySQLDBQuery, ConfirmFriendRequestController, ConfirmMessageController, GetNewsFeedController, GetWallPostController, ReadFriendRequestController, ReadMessageController, SellItemImagesController, SendFriendRequestController, SendMessageController, ConfirmFriendRequestServlet, ConfirmMessageServlet, GetImageServlet, GetNewsFeedServlet, GetWallPostServlet, ReadFriendRequestServlet, ReadMessageServlet, SendFriendRequestServlet, SendMessageServlet, CheckInputs, Message, WallPost.

The /getimage page requires a imagefilename to be passed in the URL.

The /getwallpost page requires a toID to be passed in the URL.
