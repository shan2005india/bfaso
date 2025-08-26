package com.bng.ussd.dao;

import java.util.List;

import com.bng.ussd.wrapper.UssdConfiguration;

public interface UssdDao {
	public List<UssdConfiguration> getUssdConfiguration();
}
