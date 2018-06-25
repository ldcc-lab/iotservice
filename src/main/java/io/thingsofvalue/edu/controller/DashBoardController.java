package io.thingsofvalue.edu.controller;

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
import org.json.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import io.thingsofvalue.edu.service.DashBoardService;


@Controller
public class DashBoardController {
	@Value("${io.thingsofvalue.url}")
	private String url;
	@Value("${io.thingsofvalue.oid}")
	private String oid;
	@Value("${io.thingsofvalue.oid.accessToken}")
	private String accessToken;
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private DashBoardService dashboardService;

	@GetMapping("/")
	public String mainPage() {
		return "DashBoard";
	}

	@RequestMapping(value = "/dashboard", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void dashboard(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
		System.out.println("===플랫폼에서 전달 받은 데이터===");
		System.out.println("["+body+"]");
		String content = dashboardService.subscriptionParser(body);
		if (content.equals("delete")) {
			System.out.println("contentInstance is Deleted");
		} else {
			HttpEntity<String> entity = new HttpEntity<String>(content, headers);
			this.template.convertAndSend("/topic/subscribe", entity);
		}

	}

	

	/**
	 * 
	 * @param meesagePlatformUrl
	 *            : 메시지플랫폼url
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
	public int sendMesageAPI(String meesagePlatformUrl, String send_phone, String authKey, String sender_key,
			String message) throws Exception {

		CloseableHttpClient httpclient = HttpClients.createDefault();
		try {
			HttpPost httpPost = new HttpPost(meesagePlatformUrl);
			// httpPost.setHeader("Authorization", "Basic
			// Y2xhc3M6bm90X29wZW5fYXBp");
			httpPost.setHeader("Authorization", "Basic " + authKey);
			httpPost.setHeader("Content-Type", "application/json; charset=EUC-KR");
			String body2 = "{ \"msg_id\" : \"iot\", \"dest_phone\" : \"" + send_phone + "\", \"send_phone\" : \""
					+ send_phone + "\", \"sender_key\" : \"" + sender_key + "\", \"msg_body\" : \"" + message
					+ "\", \"ad_flag\" : \"N\" }";

			ByteArrayEntity entity = new ByteArrayEntity(body2.getBytes("UTF-8"));

			System.out.println("TO Kakao BODY Message : " + body2);
			httpPost.setEntity(entity);

			CloseableHttpResponse res = httpclient.execute(httpPost);

			try {
				if (res.getStatusLine().getStatusCode() == 200) {
					org.apache.http.HttpEntity entity2 = (org.apache.http.HttpEntity) res.getEntity();
					System.out.println(EntityUtils.toString(entity2));
				} else {
					System.out.println("eerr");
				}
			} finally {
				res.close();
			}
		} finally {
			httpclient.close();
		}
		return 0;

	}

	
	@RequestMapping(value = "/sendcommand", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void sendToplug(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
		System.out.println("in sendtoplug");
		System.out.println(body);
		if (body.equals("ON")) {
			dashboardService.sendCommand(url, oid, "switch", body, accessToken);
		} else {
			dashboardService.sendCommand(url, oid, "switch", body, accessToken);
		}

	}
	
	//페이지 로딩 시 데이터를 가져옴.
	@GetMapping("/initialize")
	@ResponseStatus(value = HttpStatus.OK)
	@ResponseBody
	public String ReadInitData() throws Exception {
		System.out.println("Init temperature Datas");
		String result = dashboardService.ReadinitDatas(url, oid, accessToken);
		System.out.println("Get init  Data : " + result);
		return result;
	}
}
