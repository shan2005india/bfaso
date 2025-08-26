package com.bng.ussd.redis;

import java.util.concurrent.LinkedBlockingQueue;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JRedisPool {
	@Autowired
	private JedisPool jedisPool;
	@Autowired
	private JedisPoolConfig jedisPoolConfig;

	private static LinkedBlockingQueue<Jedis> queue = new LinkedBlockingQueue<Jedis>();
	//private Logger logger = Logger.getLogger(getClass());
	
	@PostConstruct
	public void init(){
		for(int i=0;i<this.jedisPoolConfig.getMaxTotal();i++){
			try {
				JRedisPool.queue.put(this.jedisPool.getResource());
			} catch (InterruptedException e) {
			}
		}
	}
	
	public Jedis getConnection(){
		Jedis jedis = null;
		try {
			jedis = queue.take();
		} catch (InterruptedException e) {
		}
		return jedis;
	}
	
	public void disconnect(Jedis jedis){
		try {
			queue.put(jedis);
		} catch (InterruptedException e) {}
		//notify();
	}
	
	@PreDestroy
	public void destroy(){
		for(int i=0;i<queue.size();i++){
			try {
				queue.take().disconnect();
			} catch (InterruptedException e) {
			}
		}
	}
}
