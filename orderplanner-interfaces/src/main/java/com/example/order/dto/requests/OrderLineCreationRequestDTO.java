package com.example.order.dto.requests;

import com.example.order.dto.BaseDTO;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonAutoDetect(fieldVisibility = Visibility.ANY)
@JsonInclude(JsonInclude.Include.NON_DEFAULT)
@NoArgsConstructor
@Data
@AllArgsConstructor
public class OrderLineCreationRequestDTO  extends BaseDTO{
	Integer orderLineNbr;
	String itemBrcd;
	Integer origOrderQty;
	Integer orderQty;
	String refField1;
	String refField2;
}
