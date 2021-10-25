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
2. 사용자 인증이 성공되면, 비밀키로 JWT를 생성한다.
3. JWT를 클라이언트에게 응답에 포함하여 제공한다.
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
