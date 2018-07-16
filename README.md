# iotservice
LOTTE IoT Platform 인 ThingsOfValue 교육 외 해당 프로젝트를 사용할 수 없습니다.

application.properties 환경 변수 내용

server.port=9090 => iotservice의 구동 포트


#config
##IoT플랫폼 주소
io.thingsofvalue.url=http://lottehotel.koreacentral.cloudapp.azure.com/education/base

##IoT 플랫폼에 등록한 OID
io.thingsofvalue.oid=0000000000000000_01033492780     

##IoT 플랫폼에 OID 등록 시 발급 받은 AccessToken
io.thingsofvalue.oid.accessToken=129f47b7-e92d-3347-f1ad-67681a5145e3

#mgmtCmdPrefix
##명령어 등록 시 입력 했던 액츄에이터 이름
mgmtcmd.prefix=controller

##명령어 등록 시 입력 했던 커맨드키
mgmtcmd.command.name=switch

#container
##센서 이름
sensor.name=detect

##명령어 실행 후 해당 결과를 올려주는 데이터 저장 공간(센서)의 이름
mgmtcmd.result.name=light


#messageplatformUrl
##카카오 메시지를 보낼 L.Message 플랫폼 주소
message.url=http://210.93.181.229:9090/v1/send/kakao-friend

##카카오 메시지를 전달 받을 전화번호
message.send.phone=01033492780

##L.Message 플랫폼을 사용하기 위한 키
message.sender.key=d6b73318d4927aa80df1022e07fecf06c55b44bf

##L.Message 플랫폼을 사용하기 위한 인증키
message.auth.key=

#rule
##룰을 실행할 온도의 기준 값
rule.temperature.value=30 

##룰을 실행할 연산자 입력.   >, <, ==   3개만 사용 가능
rule.temperature.operator=>

##설정한 기준에 온도 값이 도달 하면 보낼 카카오 메시지 내용
rule.temperature.message=Danger!! Temperature is 30 degree. Turn On Air Conditioner

##설정한 기준에 온도 값이 도달할 경우 action을 실행함. action은 전구를 껏다 켜는 행위 (ON, OFF, false) 만 사용가능
##false일 경우 action을 실행하지 않음. ON일 경우 전구를 킴, OFF일 경우 전구를 끔.
rule.temperature.action=ON

##StandardValueOfHumidity
rule.humidity.value=50
rule.humidity.operator=>
rule.humidity.message=Humidity is Higher than 50 %
rule.humidity.action=OFF

##StandardValueOfDust
rule.dust.value=200
rule.dust.operator=>
rule.dust.message=Danger
