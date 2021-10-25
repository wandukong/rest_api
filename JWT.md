# JWT
> JWT(Json Web Token)란 Json 포맷을 이용하여 사용자에 대한 속성을 저장하는 Claim 기반의 Web Token이다.  
- REST API에서 주로 사용한다.
- 멀티 서버간 인증 정보 공유 쉬움
	 - 통합 인증(Single-Sign On)에 유용
- 토큰 저장 위치 : 세션 스토리지
- 토큰 전달 방법
	- 요청 Authorization 헤더
	- QueryString
<hr/>

### 인증 절차
<img src="https://user-images.githubusercontent.com/47289479/138604671-7f953a79-c8a2-43c0-867b-5a8f047d7027.png" width=600>

```
1. 사용자 인증을 진행한다.
2. 비밀키로 JWT를 생성한다.
3. 사용자 인증이 성공되면 클라이언트에 응답으로 제공한다.
4. 클라이언트는 서버 재접근시 Authorization 헤더에 JWT를 포함하여 요청한다.
5. 서버는 JWT로 인증 여부를 확인한다.
6. 사용자에게 응답을 해준다.
```
<hr/>

### JWT 구조
> header, payload, signature로 구성된다.

 -   header : 토큰 종류와 해시 알고리즘 정보 포함
	   -   alg와 typ로 구성, alg는 서명시 사용하는 알고리즘, typ는 token type이다.
 -   payload : 토큰에 담을 정보, 담은 정보의 한 조각을 '클레임(claim)'이라고 부른다. 
	 - 클레임의 종류로는 registered, public, private 클레임이 존재한다.
    -   signature : 암호화된 서명(위조여부 확인용)이다.
        -   header의 인코딩값과 payload의 인코딩값을 합친 후, 주어진 비밀키로 해쉬하여 생성한다.

## 🍗dependency 추가
```xml
<dependency>
	<groupId>io.jsonwebtoken</groupId>
	<artifactId>jjwt</artifactId>
	<version>0.9.1</version>
</dependency>
```
## 🍖JWT 생성
```java
String token = Jwts.builder()
			.setHeaderParam("alg", "HS256") // 헤더 설정
			.setHeaderParam("typ", "JWT")
			.setExpiration(new Date(new Date().getTime() + 1000*60*60*24*365)) // 토큰의 유효기간
			.claim("mid",mid) // 페이로드 설정
			.claim("authority",authority)
			.signWith(SignatureAlgorithm.HS256, secretKey.getBytes("UTF-8")) // 서명 설정
			.compact(); // 토큰 생성
```

## 🥩Claim 얻기
```java
Claims claims = Jwts.parser()
			.setSigningKey(secretKey.getBytes("UTF-8"))
			.parseClaimsJws(token)
			.getBody();

String mid = claims.get("mid", String.class);
String authority = claims.get("authority", String.class));
```

## 🍨JWT Session에 저장
> 로그인이 성공되면, browser의 sessionStorage에 jwt를 저장한다.
```java
@RequestMapping("/login1")
public Map<String, String> login1(String mid, String mpassword) {
	log.info("실행");
	if(mid == null || mpassword == null) {
		throw new BadCredentialsException("아이디 또는 비밀번호가 제공되지 않았음"); 
		// spring security에서 아이디랑 비밀번호가 맞지 않은 경우 BadCredentialsException 발생
	}
	
	/*
	 * spring security 사용자 인증
	 */
	// 아이디와 패스워드를 통해 토큰을 만든다
	UsernamePasswordAuthenticationToken token = new UsernamePasswordAuthenticationToken(mid, mpassword); 
	// 아이디와 패스워드를 통해 만든 토큰을 통해 인증을 진행하여 성공 여부를 갖는다. 실패하면 401 에러를 발생시킨다.
	Authentication authentication  = authenticationManager.authenticate(token); 
	SecurityContext securityContext = SecurityContextHolder.getContext();
	securityContext.setAuthentication(authentication);
	
	// 사용자 권한 얻기
	String authority = authentication.getAuthorities().iterator().next().toString();
	
	Map<String, String> map = new HashMap<>();
	map.put("result", "sucess");
	map.put("mid", mid);
	map.put("jwt", JwtUtil.createToken(mid, authority));
	return map;
}
```

```javascript
function login1() {
	var mid = $("#loginForm [name=mid]").val();
	var mpassword = $("#loginForm [name=mpassword]").val();
	$.ajax({
		url: "[(@{/member/login1})]",
		method: "post",
		data: {mid, mpassword}
	}).done((data) => {
		console.log(data);
		sessionStorage.setItem("mid", data.mid);
	    sessionStorage.setItem("jwt", data.jwt);
	});
}
```

## 🍦JWT Filter 추가
> 로그인을 한 이후에는, sessionStorage에 있는 jwt를 request header에 포함하여 요청을 보낸다.
> 이때, jwt가 유효한 것인지 판단하기 위해서, **filter**를 추가한다.

#### WebSecurityConfig.java
```java
@Override
protected void configure(HttpSecurity http) throws Exception {	
	...
	//JwtCheckFilter 추가
	JwtCheckFilter jwtCheckFilter = new JwtCheckFilter();
	// UsernamePasswordAuthenticationFilter라는 필터 앞에 jwtCheckFilter를 추가한다.
	// jwtCheckFilter에서 인증이 성공되면, UsernamePasswordAuthenticationFilter를 건너뛴다.
	http.addFilterBefore(jwtCheckFilter, UsernamePasswordAuthenticationFilter.class); 
	...
}	
```
#### JwtCheckFilter.java
```java
package com.mycompany.webapp.security;

import java.io.IOException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class JwtCheckFilter extends OncePerRequestFilter {

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {

		String jwt = null;
		if (request.getHeader("Authorization") != null && request.getHeader("Authorization").startsWith("Bearer")) {
			jwt = request.getHeader("Authorization").substring(7);
		}else if(request.getParameter("jwt") != null) { 	// <img src="url?jwt=xxx" /> 일 때 처리
			jwt = request.getParameter("jwt");
		}

		if (jwt != null) {
			// jwt 유효성 검사
			Claims claims = JwtUtil.validateToken(jwt);
			if (claims != null) {
				String mid = JwtUtil.getMid(claims);
				String authority = JwtUtil.getAuthority(claims);

				// security 인증 처리
				// 아이디와 권한을 전달하여 객체를 만든다. 
				UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(mid, null, AuthorityUtils.createAuthorityList(authority));
				SecurityContext securityContext = SecurityContextHolder.getContext();
				securityContext.setAuthentication(authentication);
			}
		}
		// 다음 필터를 실행
		filterChain.doFilter(request, response);
	}
}
```