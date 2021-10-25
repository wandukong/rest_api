# CORS
> Cross-Origin Resource Sharing, 교차 출처 리소스 공유   
> 추가 헤더를 사용하여, 한 서버에서 실행 중인 웹 애플리케이션이 다른 서버의 선택한 자원에 접근할 수 있는 권한을 부여하도록 브라우저에 알려주는 체제이다.    
> 웹 애플리케이션은 리소스가 자신의 도메인과 다를 때 교차 출처 HTTP 요청을 실행한다.
- 보안 상의 이유로, 브라우저는 스크립트에서 시작한 교차 출처 HTTP 요청을 제한한다.  즉, 이 API를 사용하는 웹 애플리케이션은 자신의 출처와 동일한 리소스만 불러올 수 있다.
- 다른 출처의 리소스를 불러오려면 그 출처에서 올바른 CORS 헤더를 포함한 응답을 반환해야 한다.

## 🔛CORS 허용
```java
@Override
protected void configure(HttpSecurity http) throws Exception {	
	...
	//Cors 설정 활성화
	http.cors();
}	
```
```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
	CorsConfiguration conf = new CorsConfiguration();
	// 모든 요청 사이트 허용
	conf.addAllowedOrigin("*");
	// 모든 요청 방식 허용
	conf.addAllowedMethod("*");
	// 모든 요청 헤더 허용
	conf.addAllowedHeader("*");
	UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
	
	// 모든 경로에 대하여 위 내용을 적용
	source.registerCorsConfiguration("/**", conf); 
	return source;
}
```