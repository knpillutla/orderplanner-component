package com.example.order.dto.events;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
@Data
public class ExceptionEvent extends BaseEvent{
	public String errorMsg;
	public Object exceptionObj;
	public ExceptionEvent(String name, String errorMsg) {
		super(name);
		this.errorMsg = errorMsg;
	}
	public ExceptionEvent(String name, String errorMsg, Object exceptionObj) {
		super(name);
		this.errorMsg = errorMsg;
		this.exceptionObj = exceptionObj;
	}
}
