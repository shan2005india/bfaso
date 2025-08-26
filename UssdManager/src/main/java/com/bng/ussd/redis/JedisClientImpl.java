package com.bng.ussd.redis;

import org.springframework.beans.factory.annotation.Autowired;

import com.bng.ussd.exception.coreException;
import com.bng.ussd.util.LogValues;
import com.bng.ussd.util.Logger;

import redis.clients.jedis.Jedis;

public class JedisClientImpl implements JedisClient {

	@Autowired
	private JRedisPool jredispool;

	public void setjRedisPool(JRedisPool jredispool) {
		this.jredispool = jredispool;
	}
	
	@Override
	public String get(String key) {
		// TODO Auto-generated method stub
		Jedis j = this.jredispool.getConnection();
		String value=null;
		try{
			value=j.get(key);
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		return value;
		
	}

	@Override
	public void remove(String key) {
		// TODO Auto-generated method stub
		if(exists(key)){
		Jedis j = this.jredispool.getConnection();
		try {
			j.del(key);
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		}
		else{
			Logger.sysLog(LogValues.info, this.getClass().getName(),key+": User does not exist");
		}
	}

	@Override
	public void set(String key, String value) {
		// TODO Auto-generated method stub
		Jedis j = this.jredispool.getConnection();
		try {
			j.set(key, value);
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
	}

	@Override
	public void set(String key, String value, int expiry_seconds) {
		// TODO Auto-generated method stub
		Jedis j = this.jredispool.getConnection();
		try {
			j.setex(key,expiry_seconds,value);
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		
		
	}
	public void append(String key,String value) {
		long length=0;
		Jedis j = this.jredispool.getConnection();
		try{
			length=j.append(key, value);
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
	}

	@Override
	public boolean exists(String key) {
		// TODO Auto-generated method stub
		boolean keyExists=false;
		Jedis j = this.jredispool.getConnection();
		try {
			keyExists=j.exists(key);
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		return keyExists;
	}

	@Override
	public int ttl(String key) {
		// TODO Auto-generated method stub
		long t=0;
		int time=0;
		Jedis j = this.jredispool.getConnection();
		try {
			t=j.ttl(key);
			time=(int)t;
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		return time;
	}

	@Override
	public String substr(String key) {
		// TODO Auto-generated method stub
		String ussdCode=null;
		Jedis j = this.jredispool.getConnection();
		try {
			int length=0;
			length=j.get(key).length();
			ussdCode=j.substr(key, 0, length-5);
		} catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		try{
			this.jredispool.disconnect(j);
		}catch (Exception e) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(e));
		}
		return ussdCode;
	}
	

}
