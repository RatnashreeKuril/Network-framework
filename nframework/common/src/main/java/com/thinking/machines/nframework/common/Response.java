package com.thinking.machines.nframework.common;
public class Response implements java.io.Serializable
{
private boolean success;
private Throwable exception;
private Object result;
public void setSuccess(boolean success)
{
this.success=success;
}
public boolean getSuccess()
{
return this.success;
}
public void setException(Throwable exception)
{
this.exception=exception;
}
public Throwable getException()
{
return this.exception;
}
public void setResult(Object result)
{
this.result=result;
}
public Object getResult()
{
return this.result;
}
public boolean hasException()
{
return this.success==false;
}
}