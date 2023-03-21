package com.thinking.machines.nframework.server;
import java.net.*;
import com.thinking.machines.nframework.common.*;
import java.nio.charset.*;
import java.lang.reflect.*;
import java.io.*;
class RequestProcessor extends Thread
{
private NFrameworkServer server;
private Socket socket;
RequestProcessor(NFrameworkServer server, Socket socket)
{
this.server=server;
this.socket=socket;
start();
}
public void run()
{
try
{
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
byte header[]=new byte[1024];
byte []tmp=new byte[1024];
int x=0;
int y;
int i=0;
int readCount,j;
while(x<1024)
{
readCount=is.read(tmp);
if(readCount==-1) continue;
for(y=0;y<readCount;y++)
{
header[i]=tmp[y];
i++;
}
x=x+readCount;
}
byte ack[]=new byte[1];
ack[0]=1;
os.write(ack,0,1);
os.flush();
int requestLength=0;
i=1023;
j=1;
while(i>=0)
{
requestLength=requestLength+(header[i]*j);
j=j*10;
i--;
}
x=0;
i=0;
byte request[]=new byte[requestLength];
while(x<requestLength)
{
readCount=is.read(tmp);
if(readCount==-1) continue;
for(y=0;y<readCount;y++)
{
request[i]=tmp[y];
i++;
}
x=x+readCount;
}
String requestJsonString=new String(request,StandardCharsets.UTF_8);
Request requestObject=JSONUtil.fromJSON(requestJsonString,Request.class);
// the request object contains servicePath and arguments
// we want the reference of the TCPService that contains the 
// Class ref and Method ref
String servicePath=requestObject.getServicePath();
TCPService tcpService=this.server.getTCPService(servicePath);
Response responseObject=new Response();
if(tcpService==null)
{
responseObject.setSuccess(false);
responseObject.setResult("");
responseObject.setException(new RuntimeException("Invalid path : "+servicePath));
}
else
{
Class c=tcpService.c;
Method method=tcpService.method;
try
{
Object serviceObject=c.newInstance();
Object result=method.invoke(serviceObject,requestObject.getArguments());
responseObject.setSuccess(true);
responseObject.setResult(result);
responseObject.setException(null);
}catch(InstantiationException instantiationException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create object of service class associated with the path "+servicePath));
}catch(IllegalAccessException illegalAccessException)
{
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(new RuntimeException("Unable to create object of service class associated with the path "+servicePath));
}
catch(InvocationTargetException invocationTargetException)
{
Throwable t=invocationTargetException.getCause();
responseObject.setSuccess(false);
responseObject.setResult(null);
responseObject.setException(t);
}

}

String responseJsonString=JSONUtil.toJSON(responseObject);
System.out.println("Response json string : "+responseJsonString);
byte objectBytes[]=responseJsonString.getBytes(StandardCharsets.UTF_8);
int responseLength=objectBytes.length;
header=new byte[1024];
int num=responseLength;
i=1023;
while(num>0)
{
header[i]=(byte)(num%10);
num=num/10;
i--;
}
os.write(header,0,1024);
os.flush();
while(true)
{
readCount=is.read(ack);
if(readCount==-1) continue;
break;
}
x=0;
int bytesToWrite=responseLength;
int chunkSize=1024;
while(x<bytesToWrite)
{
if((bytesToWrite-x)<chunkSize) chunkSize=bytesToWrite-x;
os.write(objectBytes,x,chunkSize);
x=x+chunkSize;
}
while(true)
{
readCount=is.read(ack);
if(readCount==-1) continue;
break;
}
socket.close();
}catch(IOException e)
{
System.out.println(e);
}
}
}