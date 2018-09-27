package com.example.order.dto.requests;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
@Data
@AllArgsConstructor
public class OrderLineInfoDTO {
	Long id;
	Long orderId;
	Integer orderLineNbr;
	String busName;
	Integer locnNbr;
	String orderNbr;
	String company;
	String division;
	String itemBrcd;
	String busUnit;
	Integer qty;
}
