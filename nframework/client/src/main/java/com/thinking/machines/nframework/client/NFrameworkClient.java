package com.thinking.machines.nframework.client;
import com.thinking.machines.nframework.common.*;
import com.thinking.machines.nframework.common.exceptions.*;
import java.nio.charset.*;
import java.io.*;
import java.net.*;
public class NFrameworkClient
{
public Object execute(String servicePath, Object ...arguments) throws Throwable
{
try
{
Request requestObject=new Request();
requestObject.setServicePath(servicePath);
requestObject.setArguments(arguments);
String requestJSONString=JSONUtil.toJSON(requestObject);

byte objectBytes[]=requestJSONString.getBytes(StandardCharsets.UTF_8);
int requestLength=objectBytes.length;
byte header[]=new byte[1024];
int num=requestLength;
int i,j;
i=1023;
while(num>0)
{
header[i]=(byte)(num%10);
num=num/10;
i--;
}
Socket socket;
OutputStream os;
InputStream is;
socket=new Socket("localhost",5500);
os=socket.getOutputStream();
is=socket.getInputStream();
os.write(header,0,1024);
os.flush();
int readCount;
byte ack[]=new byte[1];
while(true)
{
readCount=is.read(ack);
if(readCount==-1) continue;
break;
}
int x=0;
int bytesToWrite=requestLength;
int chunkSize=1024;
while(x<bytesToWrite)
{
if((bytesToWrite-x)<chunkSize) chunkSize=bytesToWrite-x;
os.write(objectBytes,x,chunkSize);
x=x+chunkSize;
}
header=new byte[1024];
byte []tmp=new byte[1024];
x=0;
int y;
i=0;
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
ack[0]=1;
os.write(ack,0,1);
os.flush();
int responseLength=0;
i=1023;
j=1;
while(i>=0)
{
responseLength=responseLength+(header[i]*j);
j=j*10;
i--;
}
x=0;
i=0;
byte response[]=new byte[responseLength];
while(x<responseLength)
{
readCount=is.read(tmp);
if(readCount==-1) continue;
for(y=0;y<readCount;y++)
{
response[i]=tmp[y];
i++;
}
x=x+readCount;
}
ack[0]=1;
os.write(ack,0,1);
os.flush();
socket.close();
String responseJSONString=new String(response,StandardCharsets.UTF_8);
Response responseObject=JSONUtil.fromJSON(responseJSONString,Response.class);
if(responseObject.getSuccess())
{
return responseObject.getResult();
}
else
{
throw responseObject.getException();
}

}catch(IOException ioException)
{
System.out.println(ioException.getMessage());
}
return null;
}

}