package com.mycompany.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Resource;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Member;
import com.mycompany.webapp.security.JwtUtil;
import com.mycompany.webapp.service.MemberService;
import com.mycompany.webapp.service.MemberService.JoinResult;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/member")
@Slf4j
public class MemberController {
	@Resource
	private MemberService memberService;
	
	@Resource
	private PasswordEncoder passwordEncoder;
	
	@Resource
	private AuthenticationManager authenticationManager;
	
	@RequestMapping("/join1")
	public Map<String, String> join1(Member member) {
		log.info("실행");
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
	
	@RequestMapping("/join2")
	public Map<String, String> join2(@RequestBody Member member) {
		return join1(member);
	}
	
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
		map.put("result", "success");
		map.put("mid", mid);
		map.put("jwt", JwtUtil.createToken(mid, authority));
		return map;
	}
	
	@RequestMapping("/login2")
	public Map<String, String> login2(@RequestBody Member member) {

		return login1(member.getMid(), member.getMpassword());
	}
}
