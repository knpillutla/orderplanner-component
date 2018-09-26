package com.example.order.dto.responses;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.example.order.dto.BaseDTO;
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
public class OrderDTO  extends BaseDTO implements Serializable{
	Long id;
	String busName;
	Integer locnNbr;
	String company;
	String division;
	String busUnit;
	String externalBatchNbr;
	String batchNbr;
	String orderNbr;
	Integer statCode;
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
	Date updatedDttm;
	String updatedBy;
    String delFirstName;
    String delLastName;
    String delMiddleName;
    String delAddr1;
    String delAddr2;
    String delAddr3;
    String delCity;
    String delState;
    String delCountry;
    String delZipcode;
    String delPhoneNbr;
	
	List<OrderLineDTO> orderLines = new ArrayList<>();
	

    public void addOrderLine(OrderLineDTO orderLineDTO) {
    	orderLines.add(orderLineDTO);
    }
 
    public void removeOrderLine(OrderLineDTO orderLineDTO) {
    	orderLines.remove(orderLineDTO);
    }
}
