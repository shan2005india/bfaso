package com.bng.ussd.daoImpl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;

import com.bng.ussd.dao.UssdDao;
import com.bng.ussd.util.LogValues;
import com.bng.ussd.util.Logger;
import com.bng.ussd.wrapper.UssdConfiguration;

public class UssdDaoImpl implements UssdDao {
	
	private DataSource dataSource;
	private  JdbcTemplate jdbctemplate;
	
	public void setDataSource(DataSource dataSource){
		this.dataSource=dataSource;
		this.jdbctemplate = new JdbcTemplate(dataSource);
		
	}
	
	public List<UssdConfiguration> getUssdConfiguration() {
		// TODO Auto-generated method stub
		String sql="select * from ussd_config";
		Logger.sysLog(LogValues.info, this.getClass().getName()," for database query is "+sql);
		return jdbctemplate.query(sql, new Ussdmapper());
	}
	
	private static final class Ussdmapper implements RowMapper<UssdConfiguration>{

		public UssdConfiguration mapRow(ResultSet rs, int index) throws SQLException {
			// TODO Auto-generated method stub
			UssdConfiguration uc=new UssdConfiguration();
			uc.setOperator(rs.getString("operator"));
			uc.setPack(rs.getString("pack"));
			uc.setProtocol(rs.getString("protocol"));
			uc.setMessage(rs.getString("response"));
			uc.setService(rs.getString("service"));
			uc.setUssdCode(rs.getString("ussd_code"));
			uc.setResponseurl(rs.getString("response_url"));
			uc.setContenturl(rs.getString("contenturl"));
			uc.setSession(rs.getInt("session"));
			return uc;
		}
	}
	
	//for SC 307
	

}
