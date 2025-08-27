package com.bng.ussd.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;

import com.bng.ussd.cdr.SendCdr;
import com.bng.ussd.cdr.UssdCdr;
import com.bng.ussd.daoImpl.UssdDaoImpl;
import com.bng.ussd.exception.coreException;
import com.bng.ussd.redis.JedisClientImpl;
import com.bng.ussd.wrapper.RequestXml;
import com.bng.ussd.wrapper.Response;
import com.bng.ussd.wrapper.SoapResponse;
import com.bng.ussd.wrapper.UrlContent;
import com.bng.ussd.wrapper.UssdConfiguration;

public class Service {

	@Autowired
	private JedisClientImpl jedis;
	@Autowired
	private UssdDaoImpl ussdDaoImpl;
	@Lazy
	@Autowired
	private Utilities utilities;
	@Autowired
	private UssdConfiguration ussdConfiguration;
	@Autowired
	private GetMessageValue getmessagevalue;
	@Autowired
	private Properties prop;
	@Autowired
	private UrlHitter urlHitter;
	@Autowired
	private SoapResponse soapResponse;
	@Autowired
	private SendCdr sendCdr;
	int timeout;
	String cdrPath;
	private String country;

	private static HashMap<String, UssdConfiguration> properties = new HashMap<String, UssdConfiguration>();

	public void initConfig() {
		List<UssdConfiguration> list = ussdDaoImpl.getUssdConfiguration();
		properties = utilities.getHashMap(list);
		prop = getmessagevalue.getMessageValues();
		Logger.sysLog(LogValues.APP_INFO, this.getClass().getName(), "Size of loaded map : " + properties.size());
	}

	public Properties serviceIdMapping() {
		Properties pp = new Properties();
		String sprop[] = prop.getProperty("serviceids", "").split(",");
		for (String p : sprop) {
			String ss[] = p.split(":");
			if (ss.length > 1)
				pp.setProperty(ss[0], ss[1]);
		}

		return pp;
	}

	public Response processUserRequest(RequestXml request) {
		String input = null;
		boolean existingUser = false;
		String ussdCode = null;
		String response = null;
		String subsresponse = null;
		String message = null;
		String shortCode = null;
		int timetolive = 0;
		Response res = new Response();
		res.setMsisdn(request.getMsisdn());
		res.setFreeflow(
				"<freeflowState>FB</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>");

		// if request is not of pull type , i.e. request is of cleanup in which
		// we dont need to show
		// menu to user,remove user from redis/close his session
		if (!request.getType().startsWith("pull")) {
			// shortcode only extracted for cdr purpose
			shortCode = jedis.get(request.getSessionId() + "_" + request.getMsisdn()).substring(0, 3);
			// removing user from redis
			jedis.remove(request.getSessionId() + "_" + request.getMsisdn());
			Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId() + "_" + request.getMsisdn()
					+ ":user removed successfully on cleanup request");
			message = prop.getProperty("txnComplete");
		}

		// need to show menu to user
		else {
			existingUser = jedis.exists(request.getSessionId() + "_" + request.getMsisdn());

			if (existingUser) {
				jedis.append(request.getSessionId() + "_" + request.getMsisdn(), "*" + request.getSubscriberInput());
				Logger.sysLog(LogValues.info, this.getClass().getName(),
						request.getSessionId() + "_" + request.getMsisdn() + ":dtmf " + request.getSubscriberInput()
								+ " appended to saved ussdCode. ussdCode="
								+ jedis.get(request.getSessionId() + "_" + request.getMsisdn()));
			}
			// adding sessionId_msisdn into redis for first time for a user
			else {
				jedis.set(request.getSessionId() + "_" + request.getMsisdn(), request.getSubscriberInput(), timeout);
				Logger.sysLog(LogValues.info, this.getClass().getName(),
						"user added successfully with msisdn " + request.getMsisdn() + "and session Id "
								+ request.getSessionId() + "and ussd Code: " + request.getSubscriberInput());

			}

			ussdCode = jedis.get(request.getSessionId() + "_" + request.getMsisdn());
			Logger.sysLog(LogValues.info, this.getClass().getName(),
					request.getSessionId() + "_" + request.getMsisdn() + " user exists with ussd Code : " + ussdCode);

//			Logger.sysLog(LogValues.info, this.getClass().getName(),
//					request.getSessionId() + "_" + request.getMsisdn() + " zzz properties : " + properties);

//			ussdConfiguration = properties.get(ussdCode);
			ussdConfiguration = properties.get(PatternMatcher.matchPattern(properties.keySet(), ussdCode));
			Logger.sysLog(LogValues.info, this.getClass().getName(), ussdCode+" zzz UssdConfiguration fetched: "+ussdConfiguration);
			// if no configuration found for that ussd code in database
			// then remove from redis and show user message invalid code
			if (ussdConfiguration == null) {
				Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId() + "_"
						+ request.getMsisdn() + "invalid request,no such ussd code exists");
				shortCode = ussdCode;
				jedis.remove(request.getSessionId() + "_" + request.getMsisdn());
				jedis.remove("contenturl:"+request.getSessionId() +":" + request.getMsisdn());
				message = prop.getProperty("wrongUssdCode");
				Logger.sysLog(LogValues.info, this.getClass().getName(),
						request.getSessionId() + "_" + request.getMsisdn() + "user removed successfully");

			}

			else {
				request.setServiceid(ussdConfiguration.getService());

				if (ussdConfiguration.getResponseurl() == null) {
					res.setFreeflow(
							"<freeflowState>FC</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>");

					// in case the message is back remove the last two transactions of user from
					// redis
					// and re-get the object from map
					if (ussdConfiguration.getMessage().equalsIgnoreCase("back")) {
						ussdCode = jedis.substr(request.getSessionId() + "_" + request.getMsisdn());
						jedis.set(request.getSessionId() + "_" + request.getMsisdn(), ussdCode, timeout);
						ussdConfiguration = properties.get(PatternMatcher.matchPattern(properties.keySet(), ussdCode));
						String sInput = Optional.ofNullable(ussdCode)
				                .map(code -> code.contains("*") 
				                        ? code.substring(code.lastIndexOf("*") + 1) 
				                        : code)
				                .orElse("");
						request.setSubscriberInput(sInput);
						Logger.sysLog(LogValues.info, this.getClass().getName(), "subscriber input: "+request.getSubscriberInput() +", ussdCode: " + ussdCode+" ussdConfiguration after back: "+ussdConfiguration);
					} else if (ussdConfiguration.getMessage().equalsIgnoreCase("main")) {

						utilities.cleanupRequest(request.getMsisdn(), request.getMsisdn());

						request.setNewRequest("1");
						String scode = ussdCode;
						scode = scode.indexOf("*") >= 0 ? scode.substring(0, scode.indexOf("*")) : scode;

						Logger.sysLog(LogValues.info, this.getClass().getName(), "zzzz scode is : " + scode
								+ ",ussdCode: " + ussdCode + ", userInput: " + request.getSubscriberInput());
						request.setSubscriberInput(scode);

						return utilities.utilRespUssd(request.getMsisdn(), request.getSubscriberInput(),
								request.getSessionId(), request.getNewRequest(), request.getServiceid());
//						return processUserRequest(request);
					}
					message = ussdConfiguration.getMessage();
					Logger.sysLog(LogValues.APP_DEBUG, this.getClass().getName(), "Ussd message is : " + message);
					shortCode = jedis.get(request.getSessionId() + "_" + request.getMsisdn()).substring(0, 3);
					Logger.sysLog(LogValues.APP_DEBUG, this.getClass().getName(), "Short code is : " + shortCode);
				}

				else {
					Logger.sysLog(LogValues.info, this.getClass().getName(),
							request.getSessionId() + "_" + request.getMsisdn() + "subscription request for"
									+ ussdConfiguration.getService() + ussdConfiguration.getPack());

					if (country.equalsIgnoreCase("GHA")) {
						soapResponse.setOpType("3");
						soapResponse.setMsgType("2");
						urlHitter.sendSubscriptionRequest(request, ussdConfiguration.getResponseurl());
						message = ussdConfiguration.getMessage();
					}

					else {
						String[] subUrl = ussdConfiguration.getResponseurl().split("\\|");

						for (String sUrl : subUrl) {

							subsresponse = subscribehttp(request, sUrl);
							Logger.sysLog(LogValues.info, this.getClass().getName(),
									request.getSessionId() + "_" + request.getMsisdn() + ", URL[" + sUrl
											+ "] SubsResponse is [" + subsresponse + "]");

							// unable to get response from subscription
							if (subsresponse.equalsIgnoreCase("Error")) {
								Logger.sysLog(LogValues.error, this.getClass().getName(), request.getSessionId() + "_"
										+ request.getMsisdn() + "Unable to get response from subscription");
								message = prop.getProperty("noResponseFromSubscription");

								break; // skip hitting next url if this fails
							}

							else {
								JSONObject jsonstring = new JSONObject(subsresponse);
								String status = jsonstring.isNull("status") ? jsonstring.getString("reqStatus")
										: jsonstring.getString("status");
								Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId() + "_"
										+ request.getMsisdn() + "response from subscription url : " + status);

								if (status.equalsIgnoreCase("unsuccessful")) {
									String reason = jsonstring.getString("reason");

									if (reason.equalsIgnoreCase("alreadySubscribed")) {
										message = prop.getProperty("activeUser");
										Logger.sysLog(LogValues.info, this.getClass().getName(),
												request.getSessionId() + "_" + request.getMsisdn()
														+ ":user is already active for  service"
														+ ussdConfiguration.getService());
									}

									else if (reason.equalsIgnoreCase("pending")
											|| reason.equalsIgnoreCase("subscribedButNotActive")) {
										message = prop.getProperty("pendingUser");
										Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId()
												+ "_" + request.getMsisdn() + ":user in pending state");
									} else if (reason.equalsIgnoreCase("notSubscribed")) {
										message = prop.getProperty("notsubscribed");
										Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId()
												+ "_" + request.getMsisdn() + ":user in notSubscribed state");
									} else if (reason.equalsIgnoreCase("Low balance")) {
										message = prop.getProperty("lowBalanceUser");
										Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId()
												+ "_" + request.getMsisdn() + " : user has low balance");
									} else if (reason.equalsIgnoreCase("otherError")) {
										message = prop.getProperty("otherError");
										Logger.sysLog(LogValues.info, this.getClass().getName(),
												request.getSessionId() + "_" + request.getMsisdn()
														+ " : subscription response is unsucessful with otherError");
									}

									else {
										message = prop.getProperty("error");
										Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId()
												+ "_" + request.getMsisdn() + ":subscription response is unsucessful");
									}

									break; // skip hitting next url if this fails
								}

								else if (status.equalsIgnoreCase("unsubsuccess")) {
									message = ussdConfiguration.getMessage();
									Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId()
											+ "_" + request.getMsisdn()
											+ ":user unsubscription request processed successfully,User unsubscribed from "
											+ ussdConfiguration.getService());
								}

								else if (status.equalsIgnoreCase("successful") || status.equalsIgnoreCase("success")) {
									message = ussdConfiguration.getMessage();
									Logger.sysLog(LogValues.info, this.getClass().getName(),
											request.getSessionId() + "_" + request.getMsisdn()
													+ " : user subscribed successfully to service : "
													+ ussdConfiguration.getService() + " and pack : "
													+ ussdConfiguration.getPack() + ", message: " + message
													+ ", session: " + ussdConfiguration.getSession());
								}

								else if (status.equalsIgnoreCase("notSubscribed")) {
									if (ussdConfiguration.getUssdCode().contains("309")) {
										message = prop.getProperty("notSubscribed");
									}

									else {
										message = prop.getProperty("notSubscribed1");
									}
									Logger.sysLog(LogValues.info, this.getClass().getName(),
											request.getSessionId() + "_" + request.getMsisdn()
													+ ":user not subscribed for " + ussdConfiguration.getService());

									break; // skip hitting next url if this fails
								} else if (status.equalsIgnoreCase("failure")) {

									message = prop.getProperty("usernotallowed");
									Logger.sysLog(LogValues.info, this.getClass().getName(), request.getSessionId()
											+ "_" + request.getMsisdn() + "user not allowed to use service" + status);

									break; // skip hitting next url if this fails
								} else {
									message = prop.getProperty("error");
									Logger.sysLog(LogValues.info, this.getClass().getName(),
											request.getSessionId() + "_" + request.getMsisdn()
													+ "user not subscribed,response from subscription:" + status);

									break; // skip hitting next url if this fails

								}
							}

						}
					}

					String redisEntry = jedis.get(request.getSessionId() + "_" + request.getMsisdn());

					if (redisEntry.length() > 3)
						shortCode = redisEntry.substring(0, 3);
					else
						shortCode = ussdCode;

					if (ussdConfiguration.getSession() == 0) {
						jedis.remove(request.getSessionId() + "_" + request.getMsisdn());
						jedis.remove("contenturl:"+request.getSessionId() +":" + request.getMsisdn());
						Logger.sysLog(LogValues.info, this.getClass().getName(),
								request.getSessionId() + "_" + request.getMsisdn() + ":user removed successfully.");
					}
				}

				Logger.sysLog(LogValues.info, this.getClass().getName(),
						request.getSessionId() + "_" + request.getMsisdn() + ":zzz session: "
								+ ussdConfiguration.getSession() + ", message: " + message);
				if (ussdConfiguration.getSession() == 0) {
					utilities.cleanupRequest(request.getMsisdn(), request.getMsisdn());
					res.setFreeflow(
							"<freeflowState>FB</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>");
				} else {
					res.setFreeflow(
							"<freeflowState>FC</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>");
				}

			}

		}

		if (message.equalsIgnoreCase("exit")) {
			utilities.cleanupRequest(request.getMsisdn(), request.getMsisdn());
			res.setFreeflow(
					"<freeflowState>FB</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>");
			message = prop.getProperty("exit");
		}

		String service = ussdConfiguration.getService();
		String contentUrl = ussdConfiguration.getContenturl();
		Logger.sysLog(LogValues.info, this.getClass().getName(),
				"Content Url: " + contentUrl + ", ussdConfiguration: " + ussdConfiguration.toString());
		
		if (contentUrl != null) {
			String optString = request.getSubscriberInput();
			
			String jedisUrlContent = jedis.get("contenturl:" + request.getSessionId() + ":" + request.getMsisdn());
			if (service.equalsIgnoreCase("football") && jedisUrlContent != null) {
				
				UrlContent urlContent = new UrlContent();

				urlContent.setMsisdn(request.getMsisdn());
				urlContent.setSessionId(request.getSessionId());
				urlContent.setUserInput(request.getSubscriberInput());
				urlContent.setUssdAlias(urlContent.redisDeSerializer(jedisUrlContent));

				
				optString = urlContent.getUssdAlias()
						.getOrDefault(request.getSubscriberInput(), urlContent.instantiateUssdAlias()).getAliasCode();
			}
			Map<String, String> urlParser = new HashMap<>();
			urlParser.put("option", optString == null ? "" : optString);

			contentUrl = parseProperties(contentUrl, urlParser);
			Logger.sysLog(LogValues.info, this.getClass().getName(), optString+" parsed content url: "+contentUrl);
			
			Map<String, String> mContents = fetchContent(contentUrl, "GET", null, null);
			Logger.sysLog(LogValues.info, this.getClass().getName(), "zzz contents: " + mContents);

			if (service.equalsIgnoreCase("football") && ussdConfiguration.getSession()==1) {
				UrlContent urlContent = new UrlContent(request.getMsisdn(), request.getSessionId(),
						request.getSubscriberInput(), mContents.getOrDefault("content", ""));
				
				Logger.sysLog(LogValues.info, this.getClass().getName(), "zzz request.msisdn: "+request.getMsisdn()+", sessionId: "+request.getSessionId()+", subscriberInput: "+request.getSubscriberInput());
				Logger.sysLog(LogValues.info, this.getClass().getName(), "zzz urlContent.msisdn: "+urlContent.getMsisdn()+", sessionId: "+urlContent.getSessionId());
				jedis.set(urlContent.redisKey(), urlContent.redisSerializer(), timeout);
				Logger.sysLog(LogValues.info, this.getClass().getName(), "Written to Redis - urlContent.redisKey: "+urlContent.redisKey()+" --- redis content: "+urlContent.redisSerializer());
						
				mContents.put("content", urlContent.ussdResponse()==null?mContents.getOrDefault("content", ""):urlContent.ussdResponse());
				Logger.sysLog(LogValues.info, this.getClass().getName(), urlContent.redisKey()+", urlContent.ussdResponse: "+urlContent.ussdResponse()+", mContents: "+mContents);
			}
			Logger.sysLog(LogValues.info, this.getClass().getName(), "unparsed message: " + message+", mContents: "+mContents);
			message = parseProperties(message, mContents);
			Logger.sysLog(LogValues.info, this.getClass().getName(), "parsed message: " + message);
		}

		res.setApplicationResponse(message);
		UssdCdr cdr = utilities.getUssdCdr(request, message, ussdConfiguration, shortCode);
		sendCdr.sendUssdCdr(cdr);

		Logger.sysLog(LogValues.info, this.getClass().getName(),
				request.getSessionId() + "_" + request.getMsisdn() + ":zzz returning res: " + res.getFreeflow());
		return res;
	}

//	public static void main(String args[]) {
//		String contentUrl = "http://127.0.0.1:9020/api/v1/ussd/leagues/live?continents=1&page=0&size=5";
//		
//		String message = "<?xml version=\"\"1.0\"\" encoding=\"\"UTF-8\"\"?>\r\n"
//				+ "<html>\r\n"
//				+ " <head>\r\n"
//				+ " <title></title>\r\n"
//				+ " </head>\r\n"
//				+ " <body>\r\n"
//				+ "${content}\r\n"
//				+ " </body>\r\n"
//				+ "</html>";
//		
//		System.out.println("Message Init: "+message);
//		
//		System.out.println("Content Url: "+contentUrl);
//		if(contentUrl != null) {
//			Map<String, String> mContents = fetchContent(contentUrl, "GET", null, null);
//			System.out.println("contents: "+mContents);
//			message = parseProperties(message, mContents);
//			System.out.println("parsed message: "+message);
//		}
//	}

	public String parseProperties(String content, Map<String, String> props) {
		if (content == null || content.isEmpty())
			return content;

		Pattern pattern = Pattern.compile("\\$\\{([^}]+)\\}");
		Matcher matcher = pattern.matcher(content);
		StringBuffer sb = new StringBuffer();
		while (matcher.find()) {
			String key = matcher.group(1);
			String replacement;
			if ("newline".equals(key)) {
				replacement = "\n";
			} else {
				replacement = props.getOrDefault(key, matcher.group(0));
			}
			// Quote replacement to correctly handle backslashes, dollar signs, etc.
			matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
		}
		matcher.appendTail(sb);
		return sb.toString();
	}

	public Map<String, String> fetchContent(String contentUrl, String method, Map<String, String> headers,
			Map<String, String> params) {
		Map<String, String> result = new HashMap<>();
		try {
			// Build URL with query params for GET requests using streams
			if ("GET".equalsIgnoreCase(method) && params != null && !params.isEmpty()) {
				String paramString = params.entrySet().stream().map(e -> {
					try {
						return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}).collect(Collectors.joining("&"));
				if (contentUrl.contains("?")) {
					contentUrl += "&" + paramString;
				} else {
					contentUrl += "?" + paramString;
				}
			}

			URL url = new URL(contentUrl);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod(method.toUpperCase());

			// Set headers using streams
			if (headers != null) {
				headers.forEach(conn::setRequestProperty);
			}

			// For POST method, write params using streams
			if ("POST".equalsIgnoreCase(method) && params != null && !params.isEmpty()) {
				conn.setDoOutput(true);
				String paramString = params.entrySet().stream().map(e -> {
					try {
						return URLEncoder.encode(e.getKey(), "UTF-8") + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
					} catch (Exception ex) {
						throw new RuntimeException(ex);
					}
				}).collect(Collectors.joining("&"));
				try (OutputStream os = conn.getOutputStream()) {
					os.write(paramString.getBytes("UTF-8"));
					os.flush();
				}
			}

			// Read response
			BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			String response = in.lines().collect(Collectors.joining("\n"));
			in.close();

			String json = response != null ? response.trim() : "";
			if (json.startsWith("{") && json.endsWith("}")) {
				json = json.substring(1, json.length() - 1); // remove {}
				// Split using regex that respects quoted commas
				Arrays.stream(json.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)")).map(pair -> pair.split(":", 2))
						.filter(kv -> kv.length == 2).forEach(kv -> {
							String key = kv[0].trim();
							String value = kv[1].trim();
							if (key.startsWith("\"") && key.endsWith("\"")) {
								key = key.substring(1, key.length() - 1);
							}
							if (value.startsWith("\"") && value.endsWith("\"")) {
								value = value.substring(1, value.length() - 1);
								result.put(key, value);
							} else if (!value.startsWith("{") && !value.startsWith("[") && !"null".equals(value)) {
								result.put(key, value);
							}
						});
			} else {
				result.put("content", json);
			}
		} catch (Exception e) {
			e.printStackTrace(); // handle or log as needed
		}
		return result;
	}

	public String stripMsisdn(String msisdn) {
		if (msisdn != null && msisdn.length() > Integer.parseInt(prop.getProperty("msisdn.length", "8"))) {
			return msisdn.trim().substring(msisdn.length() - 8);
		} else
			return msisdn;
	}

	public String subscribehttp(RequestXml rxml, String suburl) {
		String urlString = suburl.replaceAll("<<msisdn>>", stripMsisdn(rxml.getMsisdn()));
		if (rxml.getServiceid() != null && !rxml.getServiceid().equals(""))
			urlString = urlString.replaceAll("<<serviceid>>", rxml.getServiceid());

		Logger.sysLog(LogValues.info, this.getClass().getName(), "Final Url For SUB  " + urlString);
		String resp = "Error";
		try {
			URL obj;
			obj = new URL(urlString);
			HttpURLConnection con = (HttpURLConnection) obj.openConnection();
			con.setRequestMethod("GET");
			int responseCode;
			responseCode = con.getResponseCode();
			BufferedReader in;
			in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();

			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			resp = "" + response;
			Logger.sysLog(LogValues.info, this.getClass().getName(), "url response   " + resp);
			resp = resp.trim();
			in.close();
		} catch (Exception ex) {
			Logger.sysLog(LogValues.error, this.getClass().getName(), coreException.GetStack(ex));
		}
		return resp;
	}

	public String getCdrPath() {
		return cdrPath;
	}

	public void setCdrPath(String cdrPath) {
		this.cdrPath = cdrPath;
	}

	public int getTimeout() {
		return timeout;
	}

	public void setTimeout(String timeout) {
		this.timeout = Integer.parseInt(timeout);
	}

	public String getCountry() {
		return country;
	}

	public void setCountry(String country) {
		this.country = country;
	}

	public String getXmlResponse(Response resp) {
		String response = utilities.convertToXML(resp);
		return response;
	}

	public String getMsgResponse(Response resp) {
		// TODO Auto-generated method stub
		String msg = resp.getApplicationResponse();
		return msg;
	}

	public String getStringResp(Response resp) {
		// TODO Auto-generated method stub
		String op = "continue";
		if (resp.getFreeflow().equals(
				"<freeflowState>FB</freeflowState><freeflowCharging>N</freeflowCharging><freeflowChargingAmount>0.0</freeflowChargingAmount>"))
			op = "end";
		String response = "op=" + op + "&text=" + resp.getApplicationResponse();
		return response;
	}

	public Properties getProp() {
		return prop;
	}

	public UssdConfiguration getUssdConfiguration() {
		return ussdConfiguration;
	}

	// public static void main(String args[]) {
	// //String subsresponse =
	// "{\"status\":\"successful\",\"reason\":null,\"serviceId\":null,\"serviceName\":\"MagicVoice\",\"price\":null,\"responseCode\":null,\"subServiceName\":\"MVWeeklyTrial\",\"subServiceId\":\"mvweeklytrial\",\"subType\":\"days\",\"subTimeLeft\":\"0\",\"msisdn\":null,\"transactionId\":null,\"subStartDate\":\"2019-02-04
	// 19:38:29.000\",\"cdrEndDate\":\"2019-02-11
	// 19:38:29.000\",\"currentStatus\":\"active\",\"language\":\"_K\",\"operator\":null,\"country\":null,\"ussdReason\":\"\",\"giftId\":null,\"callAttempts\":\"1\",\"successCallCount\":0,\"preferredFrequency\":null,\"preferredBgm\":null,\"channel\":null,\"otptoken\":null,\"church\":\"0\",\"lastsuccesscalltopastor\":\"null\",\"stations\":null}";
	// String subsresponse =
	// "{\"reqStatus\":\"success\",\"beRespCode\":null,\"beRespDesc\":null,\"msisdn\":\"85595858865\",\"transactionId\":\"15022725004651\",\"userAccBal\":null,\"userProfileId\":null,\"accountType\":null,\"price\":null,\"purchaseId\":null,\"dedicatedAccountID45\":null,\"dedicatedAccountID200\":null,\"dedicatedAccountID201\":null,\"otptoken\":null,\"reqtype\":null,\"token\":null,\"serviceinternalidresets\":null,\"parentaccountinternalid\":null,\"serviceinternalid\":null,\"adjustmentAmountRelative\":null,\"srvClassId\":null}";
	//
	// JSONObject jsonstring = new JSONObject(subsresponse);
	//
	// String status =
	// jsonstring.isNull("status")?jsonstring.getString("reqStatus"):jsonstring.getString("status");
	//
	// System.out.println(status);
	// }

}
