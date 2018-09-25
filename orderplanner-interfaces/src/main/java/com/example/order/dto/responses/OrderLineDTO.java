package com.example.order.dto.responses;

import java.io.Serializable;
import java.util.Date;

import com.example.inventory.dto.BaseDTO;
import com.example.order.dto.events.ExceptionEvent;
import com.example.order.dto.requests.OrderLineCreationRequestDTO;
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
public class OrderLineDTO  extends BaseDTO implements Serializable{
	Long id;
	Integer locnNbr;
	Long orderId;
	Integer orderLineNbr;
	String itemBrcd;
	Integer origOrderQty;
	Integer orderQty;
	Integer cancelledQty;
	Integer shortQty;
	Integer pickedQty;
	Integer packedQty;
	Integer shippedQty;
	Integer statCode;
	String olpn;
	String source;
	String transactionName;
	String refField1;
	String refField2;
	Date updatedDttm;
	String updatedBy;
}
