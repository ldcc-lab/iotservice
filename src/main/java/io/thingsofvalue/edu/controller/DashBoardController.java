package io.thingsofvalue.edu.controller;



import java.util.HashMap;

import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;


import io.thingsofvalue.edu.service.DashBoardService;


@Controller
public class DashBoardController {
	
	@Autowired
	private SimpMessagingTemplate template;
	
	@Autowired
	private DashBoardService dashboardService;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String mainPage() {
		return "DashBoard";
	}

	@RequestMapping(value = "/dashboard/{type}", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void dashboard(@RequestBody String body, @RequestHeader HttpHeaders headers, @PathVariable String type) throws Exception {
		String content = dashboardService.subscriptionParser(body);
		if (content.equals("delete")) {
		} else {
			JSONObject result = new JSONObject();
			result.put("type", type);
			result.put("data", content);
			content = result.toString();
			HttpEntity<String> entity = new HttpEntity<String>(content, headers);
			this.template.convertAndSend("/topic/subscribe", entity);
		}

	}


	
	@RequestMapping(value = "/sendcommand", method = RequestMethod.POST)
	@ResponseStatus(value = HttpStatus.OK)
	public void sendToplug(@RequestBody String body, @RequestHeader HttpHeaders headers) throws Exception {
		if (body.equals("ON")) {
			dashboardService.sendCommand(body);
		} else {
			dashboardService.sendCommand(body);
		}

	}
	
	//페이지 로딩 시 데이터를 가져옴.
	@RequestMapping(value = "/initialize", method = RequestMethod.GET)
	@ResponseBody
	public String ReadInitData() throws Exception {
		String result = dashboardService.ReadinitDatas();
		return result;
	}
}
