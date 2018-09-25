package com.example.order.service;

import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderLineStatusUpdateRequestDTO;
import com.example.order.dto.requests.OrderUpdateRequestDTO;
import com.example.order.dto.responses.OrderDTO;

public interface OrderService {
	public OrderDTO findById(String busName, Integer locnNbr, Long id) throws Exception;
	public OrderDTO createOrder(OrderCreationRequestDTO orderCreationReq) throws Exception;
	public OrderDTO updateOrder(OrderUpdateRequestDTO orderUpdRequest) throws Exception;
	public OrderDTO updateOrderLineStatusToReserved(OrderLineStatusUpdateRequestDTO orderStatusUpdReq) throws Exception;
}