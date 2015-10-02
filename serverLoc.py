from SimpleXMLRPCServer import SimpleXMLRPCServer
import datetime, socket, xmlrpclib, random, array, sys

#Create server
server = SimpleXMLRPCServer((str(sys.argv[1]), int(sys.argv[2])))

#XML-RPC methods
class MyFuncs:
        #The main Function of the system
        def getCoordinates(self, map):
                #print map
                #Array where network Data is stored ([0]=mac address of the hotspot, [1]=rssi in dbm, [2]=ssid of the network)
                networkData = []
                #Dictionary used in order to sort and process the data collected
                wifiDataDictionary = {}

                #The format of data that received are
                #map[clientMac] = mac address of the client
                #map[clientDate] = date of the scan of the client's clock
                print 'Client ',map['clientMac'],' at Date ',map['clientDate'],' listens to the following networks: '

                #The format of NETWORK data that received are
                #map[networksFound] = the number of wifi networks that client listens
                #map[network_X] = 'bssid ; rssi ; networkname ', where x is the network
                for network in range(map['networksFound']): 
                        networkData = map['network_'+str(network)].split(' ; ')
                        wifiDataDictionary[networkData[0]]=int(networkData[1])
                        print 'network: ',networkData[2]
                        print '         bssid: ',networkData[0]
                        print '         rssi: ',networkData[1]

                #Finally in wifiDataDictionary the rows are: wifiDataDictionary[bssid]=rssi
                #At the following two lines the strongestHotspot is extracted along to its rssi
                strongestHotspotBssid = max(wifiDataDictionary,key=wifiDataDictionary.__getitem__)
                maxRssi = max([value for (key, value) in sorted(wifiDataDictionary.items())])
                print 'STRONGEST Hotspot:', strongestHotspotBssid,'  with RSSI:', maxRssi
                map.clear()
                
                #Here is the if clause that according to the strongest hotspot, specific coordinates are sent back to client
                #Here administrator specifies which hotspot corresponds to coordinates
                #example
                #If strongest network is this, send those coordinates
                if strongestHotspotBssid == '00:13:49:a7:73:3b':  
                        map["latitude"] = 38.28346905497185
                        map["longitude"]= 21.764259338378906
                elif strongestHotspotBssid == '00:0e:6a:d3:d9:59':  
                        map["latitude"] = 39.15961564104208
                        map["longitude"]= 20.985431671142578
                #If you see this network send those coordinates (even though it may not be the strongest)
                elif 'dc:0b:1a:20:32:5d' in ([key for (key,value) in wifiDataDictionary.items()]):  
                        map["latitude"] = 33.96368875328558
                        map["longitude"]= 23.712615966796875
                #Else return something close to these coordinates (this is used to create variation for testing purposes)
                else:
                        map["latitude"] = '38.2' 
                        map["longitude"]= '21.7' 
                
                return map
        #This is used for IOS devices where only mac address of hotspot is sent and not a scan
        def getCoordinatesIOS(self, mac): 
                print 'Received Mac was:',mac
                strongestHotspotBssid = mac
                coordinatesString = ''
                map={}
                #If strongest network is this, send those coordinates
                if strongestHotspotBssid == '00:13:49:a7:73:3b':  
                        map["latitude"] = 38.29111551510991
                        map["longitude"]= 21.79641902446747
                        
                elif strongestHotspotBssid == '00:0e:6a:d3:d9:59':  
                        map["latitude"] = 39.15961564104208
                        map["longitude"]= 20.985431671142578
                        
                #Else return something close to these coordinates (this is used to create variation for testing purposes)
                else:
                        map["latitude"] = '38.2' 
                        map["longitude"]= '21.7' 

                coordinatesString = str(map["latitude"])+'-'+str(map["longitude"])
                return coordinatesString
    
server.register_instance(MyFuncs())

print 'Server started...'
#Run the server's main loop
server.serve_forever()



