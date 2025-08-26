package com.bng.ussd.redis;

public interface JedisClient {
			public String get(String key);
			public void remove(String key);
			public void set(String key, String value);
			public void set(String key, String value, int expiry_seconds);
			public void append(String key,String value);
			public boolean exists(String key);
			public int ttl(String key);
			public String substr(String key);

}
