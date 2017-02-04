# Proxy-Server
Uses non-persistent TCP connection, and only understands GET requests



##Usage
Open terminal   
Naviagate to the same directory as "WebProxy.java"  
$javac WebProxy.java  
$java WebProxy <Portnum> where portnum can be any integer greater than 1024  
$From the client switch use ip: "localhost" and the same portnum and try it   

####Note: Only works on Unix OS for caching because directory navigation is handled using forward slash
