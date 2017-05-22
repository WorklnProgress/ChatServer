# ChatServer
The chat server has been created using vertx, mysql for persistence layer, maven as build tool,
jquery and bootstrap for a basic web gui.  
The distribution is using a fat jar.
The server is hosted on localhost
The http port is configurable.

The application has following three apis as required:

1) Create User 
	Takes in a username and plain text password and stores it as a salted hash. 
	Please note that we are not authenticating the users in the application yet. Just creation.
	
2) Send Message (which stores the messages) 
	Takes a sender, receiver, message, message type(TEXT, VIDEO, IMAGE)
	and stores it in mysql db. Please note the above mentioned fields are mandatory.

3) Fetch Message (which gets the paginated messages) 
	Takes a sender, receiver and shows all messages between them. Alternatively if num of
	messages per page and which page number to show is given, then shows only those messages.
	For example, if user A and B have 4 messages between them with pagination of 2 messages
	per page.
	If we ask for second page, only the 3rd and 4th message between them will be retrieved 
	from the database.

Please note that preferred persistence would have been a noSQL db since the messages can 
be of different formats and with different attributes. In order to use Sql db, I have stored
a json stringified format of the message attributes. 

INSTRUCTIONS to run the server :
1) Create the sql schema (Two tables - USERS and CHAT) either using the sql dump or SqlCreation.txt
2) For now the application connects to the "chatserver" database using "root" user and pwd
"PWD123!". This is configurable in common.conf
3) In cmd, go to the directory where the jar and configuration files have been extracted and run:
	java -jar chat_server-1.0-SNAPSHOT-fat.jar -conf common.conf 
4) You can run many instances of the server at different ports on localhost. E.g.
	java -jar chat_server-1.0-SNAPSHOT-fat.jar -conf conf1.conf 
5) You can then go to http://localhost:8082/chat/index.html or http://localhost:8081/chat/index.html
	to access the client.
	
	
Time permitting, further plans that I had:
1) Nginx to be used as a load balancer in front of different instances of the server
2) More testing
3) User authentication 
4) Split the persistence to another verticle to leverage verticles as microservices.
5) Use of websockets/sockjs
