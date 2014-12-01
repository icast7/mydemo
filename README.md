TFTP RESTful Web Service
************************
This project is a simple example of using a Web Service to communicate with a TFTP Server

The WS API works as follows:
* Note: The curly brackets are used to denote path segments, e.g. http://localhost/{filename}/{blockNumber}

Use a GET ({filename}) method call to retrieve a file from the server, this operation is idempotent
a) This will return an error if the file does not exist OR
b) This will return:
	- A TFTP packet with the first section of the file (up to 512 bytes as defined in the RFC1350)
  	- A link to the next section of the file that can be retrieved using a GET method
  	- A link to the previous section of the file that can be retrieved using a GET method

Use a POST ({filename}) method call to create a new file on the server
a) This will return an error if the file already exists OR
b) This will return:
	- An ACK message with the path of the newly created file
  	- A link to the next section of the file that can be written using a POST ({filename}/{blockNumber}) method

Requirements:
********************
- JRE 1.7+
- Maven 3.0.4+


Running with project:
*********************
1. Open two bash windows and browse to the project root folder
2. On Window 1 start the webservice by running:
mvn jetty:run

3. On Window 2 start the TFTP server by running:
mvn exec:java

4. Run calls to the WS API with curl
Example GET with file path:
curl -v -X GET http://localhost:8086/tftpdemo/services/tftp/{filename}/{blockNumber}
curl -v -X GET http://localhost:8086/tftpdemo/services/tftp/test.txt/1

Example POST:
curl -v -X POST -H "Content-Type: application/xml"  --data "@./curl/post.xml" http://localhost:8086/tftpdemo/services/tftp/{filename}
curl -v -X POST -H "Content-Type: application/xml"  --data "@./curl/post.xml" http://localhost:8086/tftpdemo/services/tftp/test2.txt

Limitations/Known Issues:
*************************
Only the NETASCII Transfer Mode is supported
The POST methods are not yet fully implemented

What features did you choose to implement and why?
***************************************************************************
I chose to fully implement the structural components defined in the RFC1350, i.e. Packet Types, Error Codes, etc. in order to understand the TFTP component relationships at runtime and how they could be mapped to a RESTful API.

I then partially implemented some of the behavior defined on the RFC but have not completed this yet. I decided to switch to the WS API so I could test the communication process from a WS client to the TFTP server and back.


If you had to do this project again, what would you do differently and why?
***************************************************************************
I attempted to build the project using different methods:
1. First I tried developing the server using netty, being new to netty I was able to develop a working server that uses strings to communicate (https://github.com/ikaz/demo). I was not happy with this because I need to be able to implement the RFC using Datagrams at the byte level so I decided to change my approach and use JAVA sockets instead. Along the way I decided to use Maven to build/run the project instead of an eclipse project, also switched form Jersey to RESTeasy and from Tomcat to Jetty.

2. 