package io.thingsofvalue.edu.service;

import java.io.IOException;
import java.text.ParseException;

import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

@Service
public class DashBoardService {
	//
	/**
	 *  플랫폼에서 전달 받은 JSON을 파싱하여 data 리턴
	 * @param body
	 * @return
	 * @throws Exception
	 */
	public String subscriptionParser(String body) throws Exception {
		JSONParser jsonParser = new JSONParser();
		jsonParser.parse(body);
		
		JSONObject result = (JSONObject) jsonParser.parse(body);
		JSONObject sgn = (JSONObject) result.get("m2m:sgn");
		JSONObject nev = (JSONObject) sgn.get("nev");
		JSONObject rep = (JSONObject) nev.get("rep");
		JSONObject om = (JSONObject) nev.get("om");
		if (om.get("op").toString().equals("1")) { //Create 된 데이터만 사용함. op가 4면 삭제된 데이터.
			JSONObject cin = (JSONObject) rep.get("m2m:cin");
			return (String) cin.get("con");
		} else {
			return "delete";
		}
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
	public void sendCommand(String iotPlatformUrl, String oid, String cmdName, String cmd, String accessToken) throws ParseException, IOException {
		String resourceUrl = iotPlatformUrl + "/controller-" + oid;
		System.out.println("iotPlatformResourceUrl : " + resourceUrl);
		System.out.println("OID : " + oid);
		System.out.println("commandName : " + cmdName);
		System.out.println("command : " + cmd);
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
					System.out.println(EntityUtils.toString(entity));
				} else {
					System.out.println("sendMgmt eerr");
				}
			} finally {
				res.close();
			}
		} finally {
			httpclient.close();
		}

	}
	
//	/**
//	 * <contentInstance> JSON을 파싱하여 data 값을 반환한다.
//	 * @param body
//	 * @return
//	 * @throws Exception
//	 */
//	public String contentInstanceParser(String body) throws Exception {
//		JSONParser jsonParser = new JSONParser();
//		JSONObject result = (JSONObject) jsonParser.parse(body);
//		JSONObject cin = (JSONObject) result.get("m2m:cin");
//		return (String) cin.get("con");
//	}
	
	public String getOnem2mData(String url, String oid, String accessToken) throws ClientProtocolException, IOException {
		//
		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpGet httpGet = new HttpGet(url);
			httpGet.setHeader("X-M2M-RI", "RQI0001"); //
			httpGet.setHeader("X-M2M-Origin", "/S" + oid); //
			httpGet.setHeader("Accept", "application/json");
			httpGet.setHeader("Authorization", "Bearer " + accessToken);

			CloseableHttpResponse res = httpclient.execute(httpGet);
			try {
				if (res.getStatusLine().getStatusCode() == 200) {
					org.apache.http.HttpEntity entity = (org.apache.http.HttpEntity) res.getEntity();
					String reasonPhrase = EntityUtils.toString(entity);
					System.out.println("CSE Instance Json : " + reasonPhrase);
					try {
						String result = reasonPhrase;
						return result;
					} catch (Exception e) {
						e.printStackTrace();
					}
					
				} else {
					System.out.println("Read Init Datas eerr");
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
 * @throws ParseException
 * @throws IOException
 * @throws org.json.simple.parser.ParseException 
 */
	public String ReadinitDatas(String iotPlatformUrl, String oid, String accessToken) throws ParseException, IOException, org.json.simple.parser.ParseException {
		String sensorsUrl = iotPlatformUrl + "/S" + oid + "/sensors/la?atr=con";
		String lightUrl = iotPlatformUrl + "/S"+ oid + "/light/la?atr=con";
		String sensors = this.getOnem2mData(sensorsUrl, oid, accessToken);
		String light = this.getOnem2mData(lightUrl, oid, accessToken);
		JSONParser jsonParser = new JSONParser();
		JSONObject sensorsObj = (JSONObject) jsonParser.parse(sensors);
		sensorsObj.put("light", light);
		
		return sensorsObj.toJSONString(); 
	}
}
