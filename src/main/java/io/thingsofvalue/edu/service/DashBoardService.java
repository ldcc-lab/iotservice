package io.thingsofvalue.edu.service;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import io.thingsofvalue.edu.domain.KaKao;
import io.thingsofvalue.edu.util.JsonUtil;

@Service
public class DashBoardService {
	
	@Value("${mgmtcmd.prefix}")
	String mgmtCmdPrefix;
	
	@Value("${mgmtcmd.result.name}")
	String commandResultName;
	
	@Value("${sensor.name}")
	String sensorName;
	
	@Value("${mgmtcmd.command.name}")
	String cmdName;
	
	@Value("${slack.message.enable}")
	boolean slackEnable;
	
	@Value("${slack.message.url}")
	String slackUrl;
	
	@Value("${slack.message.token}")
	String slackToken;
	
	@Value("${slack.message.channel}")
	String slackChannel;
	
	
	@Value("${kakao.message.url}")
	String messagePlatformUrl;
	
	@Value("${kakao.message.enable}")
	boolean kakaoEnable;
	
	@Value("${kakao.message.send.phone}")
	String sendPhone;
	
	@Value("${kakao.message.sender.key}")
	String senderKey;
	
	@Value("${kakao.message.auth.key}")
	String authKey;
	
	@Value("${rule.temperature.value}")
	String ruleTemperatureValue;
	
	@Value("${rule.temperature.operator}")
	String ruleTemperatureOperator;
	
	@Value("${rule.temperature.message}")
	String ruleTemperatureMessage;
	
	@Value("${rule.temperature.action}")
	String ruleTemperatureAction;
	
	@Value("${rule.humidity.value}")
	String ruleHumidityValue;
	
	@Value("${rule.humidity.operator}")
	String ruleHumidityOperator;
	
	@Value("${rule.humidity.message}")
	String ruleHumidityMessage;
	
	@Value("${rule.humidity.action}")
	String ruleHumidityAction;
	
	@Value("${rule.dust.value}")
	String ruleDustValue;
	
	@Value("${rule.dust.operator}")
	String ruleDustOperator;
	
	@Value("${rule.dust.message}")
	String ruleDustMessage;
	
	@Value("${rule.dust.action}")
	String ruleDustAction;
	
	@Value("${io.thingsofvalue.url}")
	String iotPlatformUrl;
	
	@Value("${io.thingsofvalue.oid}")
	String oid;
	
	@Value("${io.thingsofvalue.oid.accessToken}")
	String accessToken;
	
	private static long beforeTemperature = 0;
	private static long beforeHumidity = 0;
	private static long beforeDust = 0;
	private static int temperatureCount = 0;
	private static int humidityCount = 0;
	private static int dustCount = 0;
	
	

	
	private Logger logger = LoggerFactory.getLogger(this.getClass());

	
	//
	/**
	 *  플랫폼에서 전달 받은 JSON을 파싱하여 data 리턴
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public String subscriptionParser(String body) throws Exception {
		logger.debug("[subscriptionParsing] body = {}", body);
		JSONParser jsonParser = new JSONParser();
		jsonParser.parse(body);
		JSONObject result = (JSONObject) jsonParser.parse(body);
		JSONObject sgn = (JSONObject) result.get("m2m:sgn");
		JSONObject nev = (JSONObject) sgn.get("nev");
		JSONObject rep = (JSONObject) nev.get("rep");
		JSONObject om = (JSONObject) nev.get("om");
		if (om.get("op").toString().equals("1")) { //Create 된 데이터만 사용함. op가 4면 삭제된 데이터.
			JSONObject cin = (JSONObject) rep.get("m2m:cin");
			String con = (String) cin.get("con");
			System.out.println("=1=1==");
			try {
				System.out.println("init");
				JSONObject contentJson = JsonUtil.fromJson(con, JSONObject.class); //JSON 형식의 스트링이 아닌 경우도 JSON OBJECT로 변환 한다.
				System.out.println("be");
				this.executeRule(contentJson);
				return JsonUtil.toJson(contentJson);  
			}catch (JsonSyntaxException e) {
				System.out.println("==11==");
				this.executeRule(con);
				return con;
			}
		} else {
			return "delete";
		}
	}
	
	private boolean operator(long value, String operator, long standardValue) {
		//
		logger.debug("[Operating] value={}, operator={}, standardValue={}",value, operator, standardValue);
		if(operator.equals(">")) {
			if(value > standardValue) {
				return true;
			}
		}else if(operator.equals("<")) {
			if(value < standardValue) {
				return true;
			}
		}else if(operator.equals("==")) {
			if(value == standardValue) {
				return true;
			}
		}else {
			return false;
		}
		return false;
	}
	
	private void executeRule(Object obj) throws Exception {
		if(obj instanceof JSONObject) {
			long temperature = (long) Float.parseFloat(((JSONObject) obj).get("temperature").toString());
			long humidity = (long) Float.parseFloat(((JSONObject) obj).get("humidity").toString());
			long dust = (long) Float.parseFloat(((JSONObject) obj).get("dust").toString());
			//이전에 전달받은 값과 비교해서 반복적으로 똑같은 메시지를 보내지 않음.
			//하나의 센서 값 당 최대 3번까지 보냄.
            if(beforeTemperature != temperature && temperatureCount < 4) {
            	//
            	if(this.operator(temperature, ruleTemperatureOperator, Long.parseLong(ruleTemperatureValue))) {
    				//메시지를 한번만 보냄.
            		   if(slackEnable) {
            			   this.sendSlackMessageAPI(ruleTemperatureMessage);
            		   }
            		   if(kakaoEnable) {
            			   this.sendKaKaoMessageAPI(ruleTemperatureMessage);
            		   }
    				if(!ruleTemperatureAction.equals("false")) {
    					this.sendCommand(ruleTemperatureAction);
    				}
    			}
            	beforeTemperature = temperature;
            	temperatureCount++;
            }
            if(beforeHumidity != humidity && humidityCount < 4) {
            	if(this.operator(humidity, ruleHumidityOperator, Long.parseLong(ruleHumidityValue))) {
         		   if(slackEnable) {
        			   this.sendSlackMessageAPI(ruleHumidityMessage);
        		   }
        		   if(kakaoEnable) {
        			   this.sendKaKaoMessageAPI(ruleHumidityMessage);
        		   }
    				if(!ruleHumidityAction.equals("false")) {
    					this.sendCommand(ruleHumidityAction);
    				}
    			}
            	beforeHumidity = humidity;
            	humidityCount++;
            }
            if(beforeDust != dust && dustCount < 4) {
    			if(this.operator(dust, ruleDustOperator, Long.parseLong(ruleDustValue))) {
    				   if(slackEnable) {
            			   this.sendSlackMessageAPI(ruleDustMessage);
            		   }
            		   if(kakaoEnable) {
            			   this.sendKaKaoMessageAPI(ruleDustMessage);
            		   }
            		   if(!ruleDustAction.equals("false")) {
    					this.sendCommand(ruleDustAction);
    				    }
    			}
            	beforeDust = dust;
            	dustCount++;
            }
	   }else {
			if(obj.equals("1")) {
				   if(slackEnable) {
        			   this.sendSlackMessageAPI("전구가 졌습니다.");
        		   }
        		   if(kakaoEnable) {
        			   this.sendKaKaoMessageAPI("전구가 켜졌습니다.");
        		   }
			}else if(obj.equals("0")) {
				   if(slackEnable) {
        			   this.sendSlackMessageAPI("전구가 꺼졌습니다.");
        		   }
        		   if(kakaoEnable) {
        			   this.sendKaKaoMessageAPI("전구가 졌습니다.");
        		   }
			}
		}
	}
	
	private String contentInstanceParser(String body) throws Exception {
		JSONParser jsonParser = new JSONParser();
		JSONObject result = (JSONObject) jsonParser.parse(body);
		JSONObject cin = (JSONObject) result.get("m2m:cin");
		return (String) cin.get("con");
		
	}
	
/**
 * 플랫폼에 디바이스 제어 커맨드를 전송한다.
 * @param iotPlatformUrl
 * @param oid
 * @param cmdName
 * @param cmd
 * @param accessToken
 * @throws ParseException
 * @throws IOException
 */
	public void sendCommand(String cmd) throws ParseException, IOException {
		String resourceUrl = iotPlatformUrl + "/"+ mgmtCmdPrefix + "-" + oid;
		logger.debug("[sendCommand] to = {}, oid= {}, commandKey = {}, commandValue = {}", resourceUrl, oid, cmdName, cmd);
			CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPut httpPut = new HttpPut(resourceUrl);
			httpPut.setHeader("X-M2M-RI", "RQI0001"); //
			httpPut.setHeader("X-M2M-Origin", "/S" + oid); //
			httpPut.setHeader("Accept", "application/json");
			httpPut.setHeader("Authorization", accessToken);
			httpPut.setHeader("Content-Type", "application/vnd.onem2m-res+json");
			String body = "{ \"m2m:mgc\": {\"cmt\": 4,\"exra\": { \"any\":[{\"nm\" :\"" + cmdName + "\", \"val\" : \""
					+ cmd + "\"} ]},\"exm\" : 1,\"exe\":true,\"pexinc\":false}}";
			httpPut.setEntity(new StringEntity(body));

			CloseableHttpResponse res = httpclient.execute(httpPut);

			try {
				if (res.getStatusLine().getStatusCode() == 200) {
					org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) res.getEntity();
					logger.debug(EntityUtils.toString(entity));
				} else {
					logger.error("sendMgmt eerr");
				}
			} finally {
				res.close();
			}
		} finally {
			httpclient.close();
		}

	}
	

	public String getOnem2mData(String url, String oid, String accessToken) throws Exception {
		//
		logger.debug("[getOnem2mData] to = {}, oid = {}, token = {}", url, oid, accessToken);
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("X-M2M-RI", "RQI0001"); //
			httpGet.setHeader("X-M2M-Origin", "/S" + oid); //
			httpGet.setHeader("Accept", "application/json");
			httpGet.setHeader("Authorization", accessToken);

			CloseableHttpResponse res = httpclient.execute(httpGet);
			try {
				if (res.getStatusLine().getStatusCode() == 200) {
					org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) res.getEntity();
					String reasonPhrase = EntityUtils.toString(entity);
					reasonPhrase = this.contentInstanceParser(reasonPhrase);
					logger.debug("[getOnem2mData] response = {} ", reasonPhrase);
					try {
						String result = reasonPhrase;
						return result;
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					logger.error("Read Init Datas eerr");
				}
			} finally {
				res.close();
			}
		} finally {
			httpclient.close();
		}
		return "error";
	}
	
/**
 * 초기 브라우저 렌더링을 위한 데이터를 플랫폼에서 조회 한다.
 * @param iotPlatformUrl
 * @param oid
 * @param accessToken
 * @return
 * @throws org.json.simple.parser.ParseException 
 * @throws Exception 
 */
	public String ReadinitDatas() throws Exception {
		String sensorsUrl = iotPlatformUrl + "/S" + oid + "/"+sensorName+"/la";
		String lightUrl = iotPlatformUrl + "/S"+ oid + "/"+commandResultName+"/la";
		String sensors = this.getOnem2mData(sensorsUrl, oid, accessToken);
		String light = this.getOnem2mData(lightUrl, oid, accessToken);
		JSONObject sensorsObj = JsonUtil.fromJson(sensors, JSONObject.class);
		sensorsObj.put("light", light);
		
		return sensorsObj.toJSONString(); 
	}
	
	/**
     * @param send_phone
	 *            : 카카오 메시지를 받을 핸드폰 번호
	 * @param sender_key
	 *            : API 발송 key d6b73318d4927aa80df1022e07fecf06c55b44bf
	 * @param authKey
	 *            : 인증키
	 * @param message
	 *            : 보낼 메시지
	 * @return
	 * @throws Exception
	 */
	public int sendKaKaoMessageAPI(String message) throws Exception {
		logger.debug("[sendKakaoMessage] to = {}, message = {}", messagePlatformUrl, message);
		
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(messagePlatformUrl);
			// httpPost.setHeader("Authorization", "Basic
			// Y2xhc3M6bm90X29wZW5fYXBp");
			httpPost.setHeader("Authorization", "Basic " + authKey);
			httpPost.setHeader("Content-Type", "application/json; charset=EUC-KR");
			KaKao kakao = new KaKao();
			kakao.setAd_flag("N");
			kakao.setMsg_id("iot");
			kakao.setDest_phone(sendPhone);
			kakao.setMsg_body(message);
			kakao.setSender_key(senderKey);
			kakao.setSend_phone(sendPhone);
			
//			String body2 = "{ \"msg_id\" : \"iot\", \"dest_phone\" : \"" + send_phone + "\", \"send_phone\" : \""
//					+ send_phone + "\", \"sender_key\" : \"" + sender_key + "\", \"msg_body\" : \"" + message
//					+ "\", \"ad_flag\" : \"N\" }";

			ByteArrayEntity entity = new ByteArrayEntity(kakao.toJson().getBytes("UTF-8"));
			logger.debug("[messageBody] {}", kakao.toJson());
			httpPost.setEntity(entity);

			CloseableHttpResponse res = httpclient.execute(httpPost);

			try {
				if (res.getStatusLine().getStatusCode() == 200) {
					org.apache.http.HttpEntity entity2 = (org.apache.http.HttpEntity) res.getEntity();
					logger.debug(EntityUtils.toString(entity2));
				} else {
					logger.error("[kakaoMessage]");
				}
			} finally {
				res.close();
			}
		} finally {
			httpclient.close();
		}
		return 0;

	}
	
	
	
	/**
	 */
	public int sendSlackMessageAPI(String message) throws Exception {
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			
			String requestUrl = slackUrl+"?token="+slackToken+"&channel=%23"+slackChannel;
			logger.debug("[sendSlackMessage] to = {}, token = {}, message = {}", requestUrl, slackToken, message);
			HttpPost httpPost = new HttpPost(requestUrl);
			ByteArrayEntity entity = new ByteArrayEntity(message.getBytes("UTF-8"));
			logger.debug("[messageBody] {}", message);
			httpPost.setEntity(entity);

			CloseableHttpResponse res = httpclient.execute(httpPost);

			try {
				if (res.getStatusLine().getStatusCode() == 200) {
					org.apache.http.HttpEntity entity2 = (org.apache.http.HttpEntity) res.getEntity();
					logger.debug(EntityUtils.toString(entity2));
				} else {
					logger.error("[slackMessage]");
				}
			} finally {
				res.close();
			}
		} finally {
			httpclient.close();
		}
		return 0;

	}
	
}
