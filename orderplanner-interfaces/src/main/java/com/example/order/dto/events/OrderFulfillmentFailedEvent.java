package com.example.order.dto.events;

import com.example.order.dto.requests.OrderFulfillmentRequestDTO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Data;
import lombok.NoArgsConstructor;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
@Data
public class OrderFulfillmentFailedEvent extends ExceptionEvent{
	public OrderFulfillmentRequestDTO orderFulfillmentReq;
	private static String EVENT_NAME = "OrderFulfillmentFailedEvent";
	
	public OrderFulfillmentFailedEvent(OrderFulfillmentRequestDTO req, String errorMsg) {
		super(EVENT_NAME, errorMsg);
		this.orderFulfillmentReq = req;
		this.addHeader("eventName", getEventName());
		this.addHeader("busName", req.getBusName());
		this.addHeader("locnNbr", req.getLocnNbr());
		this.addHeader("company", req.getCompany());
		this.addHeader("division", req.getDivision());
		this.addHeader("busUnit", req.getBusUnit());
	}

}
