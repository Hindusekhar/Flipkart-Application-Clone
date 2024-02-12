package com.jsp.fc.util;

import org.springframework.stereotype.Component;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@Component
@AllArgsConstructor
@NoArgsConstructor
public class SimpleStructure {
	private int status;
	private String message;

}
