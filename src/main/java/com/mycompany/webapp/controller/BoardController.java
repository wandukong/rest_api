package com.mycompany.webapp.controller;

import java.util.Date;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mycompany.webapp.dto.Board;

import lombok.extern.slf4j.Slf4j;

@RestController
@RequestMapping("/board")
@Slf4j
public class BoardController {
	/*
	@RequestMapping("/test")
	public Board getObject() {
		log.info("실행");

		return board;
	}*/
}
