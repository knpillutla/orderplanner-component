package com.example.order.dto.requests;

import java.util.Date;

import com.example.inventory.dto.BaseDTO;
import com.example.order.dto.events.ExceptionEvent;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.responses.OrderDTO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
@Data
@AllArgsConstructor
public class OrderUpdateRequestDTO extends BaseDTO{
	Long id;
	String busName;
	String locnNbr;
	String orderNbr;
	String company;
	String division;
	String busUnit;
	Date orderDttm;
	Date shipByDttm;
	Date expectedDeliveryDttm;
	String deliveryType;
	boolean isGift;
	String giftMsg;
	String source;
	String transactionName;
	String refField1;
	String refField2;
	String userId;
}
