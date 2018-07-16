	$(document).ready(function() {
		connect();
		InitDatas();
		setInterval(function(){ InitDatas(); }, 5000);
		$("#response").html("");
		$("#chartdiv").html("");
		
		//====CHART====		
		var hours = 0;
		var mins = 0;
		var seconds = 0;
		var toDay = new Date();
		var firstDate = new Date(toDay.getFullYear(), toDay.getMonth(), toDay.getDate());
		var temperature_value = 0;
		var humidity_value = 0;
		var dust_value = 0;
		
		function InitDatas(){	
			//var url = '/initialize';
			var xhr = new XMLHttpRequest();
			xhr.open('GET', '/initialize');
			xhr.setRequestHeader('Content-Type', 'text/plain');
			xhr.onreadystatechange = function() {
			    if (this.readyState == 4 && this.status == 200) {
			    	var jsonObj = JSON.parse(this.responseText);
			    	temperature_value = jsonObj.temperature;
			    	humidity_value = jsonObj.humidity;
			    	dust_value = jsonObj.dust;
			    	light_value = jsonObj.light;
			    	var lightPretty = calculateLight(light_value);
			    	var dustPretty = calculateDust(dust_value);
			    	$('.temperature').empty();
			    	$('.temperature').append(temperature_value);
			    	$('.humidity').empty();
			    	$('.humidity').append(humidity_value);
			    	$('.dust').empty();
			    	$('.dust').append(dustPretty);
			    	$('.light').empty();
			    	$('.light').append(lightPretty);
			    	
			    }
			};
			xhr.send();
			//xhr.send("ON");
		}
//		function setData(arr){
//			temperature_value = arr;
//		}

		function connect() {

			var socket = new SockJS('/realtime');

			stompClient = Stomp.over(socket);
			stompClient.connect({}, function(frame) {
				console.log('Connected: ' + frame);
				stompClient.subscribe('/topic/subscribe', function(message) {
					var type = (JSON.parse(JSON.parse(message.body).body)).type;
					var data = (JSON.parse(JSON.parse(message.body).body)).data;
					if( type == 'sensor' ) 
					{
	 					temperature_value = JSON.parse(data).temperature;
						humidity_value = JSON.parse(data).humidity;
						dust_value = JSON.parse(data).dust;
						var dustPretty = calculateDust(dust_value);
					 	$('.temperature').empty();
				    	$('.temperature').append(temperature_value);
				    	$('.humidity').empty();
				    	$('.humidity').append(humidity_value);
				    	$('.dust').empty();
				    	$('.dust').append("DANGER");
					}
					if( type == 'command' ) {
						light_value = data;
						var lightPretty = calculateLight(light_value);
						$('.light').empty();
				    	$('.light').append(lightPretty);
					}
				});
			}, function(error) {
				console.log(error);
				disconnect();
			});

		}

		/**
		 * Function that generates random data
		 */
		function generateChartData() {

			var chartData = [];
			var nowTime = new Date();
			hours = nowTime.getHours();
			mins = nowTime.getMinutes();
			seconds = nowTime.getSeconds();
			//setData();
			for (var i = seconds; i < seconds + 30; i++) {
				var newDate = new Date(firstDate);
				newDate.setDate(firstDate.getDate());
				if(i >= 60){
					mins = mins + 1;
					if(mins >= 60){
						mins = 0;
						hours = hours + 1;
					}
					if(hours >= 24){
						hours = 0;
					}
					newDate.setHours(hours, mins, i-60);
				}
				else{
					newDate.setHours(hours, mins, i);
				}
				chartData.push({
					"time" : newDate,
					"temperature" : temperature_value,
					"humidity" : humidity_value,
					"dust" : dust_value
				});
			} 
			seconds = i;
			return chartData;
		}

		/**
		 * Create the chart
		 */
		var chart = AmCharts.makeChart("chartdiv", {
			"type" : "serial",
			"theme" : "light",
			"zoomOutButton" : {
				"backgroundColor" : '#000000',
				"backgroundAlpha" : 0.15
			},
			"dataProvider" : generateChartData(),
			"categoryField" : "time",
			"categoryAxis" : {
				"parseDates" : true,
				"minPeriod" : "ss",
				"dashLength" : 1,
				"gridAlpha" : 0.15,
				"axisColor" : "#DADADA"
			},
			"graphs" : [ {
				"id" : "g1",
				"valueField" : "temperature",
				//"bullet" : "round",
				"bulletBorderColor" : "#FFFFFF",
				"bulletBorderThickness" : 2,
				"lineThickness" : 2,
				"lineColor" : "#f45b5b",
				"negativeLineColor" : "#0352b5",
				"hideBulletsCount" : 50
			},{
				"id" : "g2",
				"valueField" : "humidity",
				//"bullet" : "round",
				"bulletBorderColor" : "#FFFFFF",
				"bulletBorderThickness" : 2,
				"lineThickness" : 2,
				"lineColor" : "#4f8bc6",
				"negativeLineColor" : "#0352b5",
				"hideBulletsCount" : 50
			},{
				"id" : "g3",
				"valueField" : "dust",
				//"bullet" : "round",
				"bulletBorderColor" : "#FFFFFF",
				"bulletBorderThickness" : 2,
				"lineThickness" : 2,
				"lineColor" : "#7FC470",
				"negativeLineColor" : "#0352b5",
				"hideBulletsCount" : 50
			} ],
			"chartCursor" : {
				"cursorPosition" : "mouse"
			}
		})
		/**
		 * Set interval to push new data points periodically
		 */
		// set up the chart to update every second
		setInterval(function() {
			// normally you would load new datapoints here,
			// but we will just generate some random values
			// and remove the value from the beginning so that
			// we get nice sliding graph feeling

			// add new one at the end
			var hours_value = 24;
			var mins_seconds_value = 60;
			var newDate = new Date(firstDate);
			
			seconds++;
			if(seconds >= 60){
				seconds = seconds - 60;
				mins = mins + 1;
				if(mins >= 60){
					mins = 0;
					hours = hours + 1;
					if(hours >= 24){
						hours = 0;
					}
				}
				
			}
			
			newDate.setHours(hours, mins, seconds);
			chart.dataProvider.push({
				time : newDate,
				temperature : temperature_value,
				humidity : humidity_value,
				dust : dust_value
			});

			// remove datapoint from the beginning
			chart.dataProvider.shift();
			chart.validateData();
		}, 1000);
	});
	//light
	function calculateLight(light_value){
		if(light_value == '1'){
			return "ON";
		}else if(light_value == '0'){
			return "OFF";
		}else{
			return "error";
		}
	}
	//dust
	function calculateDust(dust_value){
		var dustResult;
		  if (dust_value < 31) {
			  dustResult = "Good";
			  } else if (dust_value < 81 && dust_value > 30) {
				  dustResult = "Normal";
			  } else if ( dust_value < 101 && dust_value > 80 ) {
				  dustResult = "Bad";
			  } else {
				  dustResult = "Danger";
			  }
		return dustResult;
	}
	
	//====SWITCH===
	function sendCommand(command){
		var url = '/sendcommand';
		var xhr = new XMLHttpRequest();
		xhr.open('POST', url);
		xhr.setRequestHeader('Content-Type', 'text/plain');
		xhr.send(command);
	}