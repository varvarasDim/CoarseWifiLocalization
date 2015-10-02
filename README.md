# CoarseWifiLocalization
WiFi Localization Android app

This is a Server-Client communication between the android app and the python server (serverLoc.py)
Client (Android app) uses its wifi in order to gather information about the surrounding wifi access points that are in its range. This list is being sent to the server with use of xml-rpc. Server decides the position of the user by taking into account the strongest wifi network the client has listened to. 

If you want to execute it at your own machine you can edit the serverLoc.py file in order to correspond to the mac addresses of your access points in use. 

<b>HOT TO USE</b>

You may execute serverLoc.py with the following format:

  python serverLoc.py \\<ip_address_of_your_server> /<port_that_server_is_going_to_listen>
  
For the xml-rpc functionality the Very thin xmlrpc client library for Android platform was used:
    https://code.google.com/p/android-xmlrpc/
    
