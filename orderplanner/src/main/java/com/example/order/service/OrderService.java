package com.example.order.service;

import java.util.List;

import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderFulfillmentRequestDTO;
import com.example.order.dto.requests.OrderLineStatusUpdateRequestDTO;
import com.example.order.dto.requests.OrderUpdateRequestDTO;
import com.example.order.dto.responses.OrderDTO;
import com.example.order.dto.responses.OrderFulfillmentResponseDTO;

public interface OrderService {
	public OrderDTO findById(String busName, Integer locnNbr, Long id) throws Exception;
	public OrderDTO createOrder(OrderCreationRequestDTO orderCreationReq) throws Exception;
	public OrderDTO updateOrder(OrderUpdateRequestDTO orderUpdRequest) throws Exception;
	public OrderDTO updateOrderLineStatusToReserved(OrderLineStatusUpdateRequestDTO orderStatusUpdReq) throws Exception;
	public OrderFulfillmentResponseDTO startOrderFulfillment(OrderFulfillmentRequestDTO orderFulfillmentReq);
	OrderDTO updateRoutingCompleted(String busName, Integer locnNbr, Long orderId) throws Exception;
}