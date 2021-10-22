# Rest API
> 다른 프로세스에서 REST 요청을 할 수 있도록 개발된 웹 서비스
- 일반적으로 데이터 전달 포맷은 JSON 사용


## 😍REST란?
> REpresentational  State Transfer
> 어떤 자원에 대해 CRUD 연산을 수행하기 위해  HTTP 요청 방식 + URL 조합으로 자원을 요청한다.
- HTTP 요청 방식에 따라 수행할 작업을 식별한다.
- URL은 작업할 데이터를 식별한다.

<img src="https://user-images.githubusercontent.com/47289479/138381382-59718fea-14cc-4ac6-baa9-2469277d7735.png" width=300>

## 🥰@RestController
> REST API를 위한 어노테이션
- 리턴된 객체를 JSON 형식으로 응답 제공
- @Controller  +  @ResponseBody 와 동일한 효과


## Return Type
- return type이 **객체와 Map**인 경우에는 **JSONObejct로 파싱**된다.
- return type이 **배열과 List**인 경우에는 모두 **JSONArray로 파싱**된다.


## 😘요청 데이터 전송 방식

### QueryString으로 전송
- GET/POST 방식 사용 가능하다.
```javascript
function join1() {
	var member = {
		mid: 		$("#joinForm [name=mid]").val(),
		mname: 		$("#joinForm [name=mname]").val(),
		mpassword: 	$("#joinForm [name=mpassword]").val(),
		mrole: 		$("#joinForm [name=mrole]").val(),
		memail: 	$("#joinForm [name=memail]").val(),
	};
	
	$.ajax({
		url: "[(@{/member/join1})]",
		method: "post",
		data: member // json 객체, 
		// processData : true 가 default -> queryString으로 변환
	}).done((data) => { 
		console.log(data);
	});
}
```

```java
@RequestMapping("/join1")
public Map<String, String> join1(Member member) {
	member.setMenabled(true);
	member.setMpassword(passwordEncoder.encode(member.getMpassword()));
	JoinResult jr = memberService.join(member);
	Map<String, String> result = new HashMap<>();
	if(jr == JoinResult.SUCCESS) {
		result.put("result","success");
	}
	else if(jr == JoinResult.DUPLICATED) {
		result.put("result","duplicated");
	}
	else if(jr == JoinResult.FAIL) {
		result.put("result","fail");
	}
	return result;
}
```
#### http header + body
```
POST /rest/sendQueryString HTTP/1.1
Host: localhost
Content-Type: application/x-www-form-urlencoded
Content-Length: 107

mid=fall&mname=%EA%B0%80%EC%9D%84&mpassword=12345&menabled=true&mrole=ROLE_USER&memail=fall%40mycompany.com
```

<hr/>

### RequestBody에 application/json 으로 전송
- json형태로 파라미터를 보내는 경우  @ReqeustBody를 해주면 객체로 파싱된다.
	- POST 방식으로만 요청한다.
```java
function join2() {
	var member = {
		mid: 		$("#joinForm [name=mid]").val(),
		mname: 		$("#joinForm [name=mname]").val(),
		mpassword: 	$("#joinForm [name=mpassword]").val(),
		mrole: 		$("#joinForm [name=mrole]").val(),
		memail: 	$("#joinForm [name=memail]").val(),
	};
	
	$.ajax({
		url: "[(@{/member/join2})]",
		method: "post",
		data: JSON.stringify(member),
		contentType: "application/json; charset=UTF-8", // json 타입으로 전송
		processData: false //data를 QueryString으로 표현하지 않음
	}).done((data) => {
		console.log(data);
	});
}
```
```java
@RequestMapping("/join2")
public Map<String, String> join2(@RequestBody Member member) {
	member.setMenabled(true);
	member.setMpassword(passwordEncoder.encode(member.getMpassword()));
	JoinResult jr = memberService.join(member);
	Map<String, String> result = new HashMap<>();
	if(jr == JoinResult.SUCCESS) {
		result.put("result","success");
	}
	else if(jr == JoinResult.DUPLICATED) {
		result.put("result","duplicated");
	}
	else if(jr == JoinResult.FAIL) {
		result.put("result","fail");
	}
	return result;
}
```
<hr/>

### RequestBody에 multipart/form-data으로 전송
- 첨부파일을 보낼 경우, POST  방식으로 요청한다.
```java
@RequestMapping("sendMultipartFormData")
public Map<String, String> sendMultipartFormData(String title, MultipartFile attach) {
	log.info("실행");
	Map<String, String> map = new HashMap<>();
	map.put("title", title);
	map.put("attachoname", attach.getOriginalFilename());
	map.put("attachtype", attach.getContentType());
	return map;
}
```
#### http header + body
```
POST /rest/sendMultipartFormData HTTP/1.1
Host: localhost
Content-Length: 308
Content-Type: multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW

----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="title"

아름다운 풍경
----WebKitFormBoundary7MA4YWxkTrZu0gW
Content-Disposition: form-data; name="attach"; filename="/C:/Users/seung/Pictures/Saved Pictures/히히/IU.png"
Content-Type: image/png

(data)
----WebKitFormBoundary7MA4YWxkTrZu0gW
```
<hr/>

### Request Header Authorization 헤더 추가
```java
@RequestMapping("sendAuth")
public Map<String, String> sendAuth(@RequestHeader("Authorization") String authorization) {
	log.info("실행");
	log.info(authorization);
	String jwt = authorization.substring(7);
	Map<String, String> map = new HashMap<>();
	map.put("jwt", jwt);
	return map;
}
```
#### http header 
```
POST /rest/sendAuth HTTP/1.1
Host: localhost
Authorization: Bearer abcdefg
```