package com.example.order.service;

import java.util.Date;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.order.db.Order;
import com.example.order.db.OrderLine;
import com.example.order.db.OrderLineRepository;
import com.example.order.db.OrderRepository;
import com.example.order.dto.converter.OrderDTOConverter;
import com.example.order.dto.events.OrderAllocatedEvent;
import com.example.order.dto.events.OrderCreatedEvent;
import com.example.order.dto.events.OrderCreationFailedEvent;
import com.example.order.dto.events.OrderLineAllocationFailedEvent;
import com.example.order.dto.events.OrderUpdateFailedEvent;
import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderLineStatusUpdateRequestDTO;
import com.example.order.dto.requests.OrderUpdateRequestDTO;
import com.example.order.dto.responses.OrderDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class OrderServiceImpl implements OrderService {
	@Autowired
	OrderRepository orderDAO;

	@Autowired
	OrderLineRepository orderLineDAO;

	@Autowired
	EventPublisher eventPublisher;

	@Autowired
	OrderDTOConverter orderDTOConverter;

	public enum OrderStatus {
		CREATED(100), READY(110), ALLOCATED(120), PARTIALLY_ALLOCATED(121), PICKED(130), PACKED(140), SHIPPED(150),
		SHORTED(160), CANCELLED(199);
		OrderStatus(Integer statCode) {
			this.statCode = statCode;
		}

		private Integer statCode;

		public Integer getStatCode() {
			return statCode;
		}
	}

	public enum OrderLineStatus {
		CREATED(100), READY(110), ALLOCATED(120), PICKED(130), PACKED(140), SHIPPED(150), SHORTED(160), CANCELLED(199);
		OrderLineStatus(Integer statCode) {
			this.statCode = statCode;
		}

		private Integer statCode;

		public Integer getStatCode() {
			return statCode;
		}
	}

	@Override
	@Transactional
	public OrderDTO updateOrder(OrderUpdateRequestDTO orderUpdateRequestDTO) throws Exception {
		OrderDTO orderDTO = null;
		try {
			Optional<Order> orderOptional = orderDAO.findById(orderUpdateRequestDTO.getId());
			if (!orderOptional.isPresent()) {
				throw new Exception("Order Update Failed. Order Not found to update");
			}
			Order orderEntity = orderOptional.get();
			orderDTOConverter.updateOrderEntity(orderEntity, orderUpdateRequestDTO);
			orderDTO = orderDTOConverter.getOrderDTO(orderDAO.save(orderEntity));
		} catch (Exception ex) {
			log.error("Created Order Error:" + ex.getMessage(), ex);
			eventPublisher.publish(
					new OrderUpdateFailedEvent(orderUpdateRequestDTO, "Update Order Error:" + ex.getMessage()));
			throw ex;
		}
		return orderDTO;
	}

	/*
	 * (non-Javadoc)
	 * 
	 */
	@Override
	@Transactional
	public OrderDTO createOrder(OrderCreationRequestDTO orderCreationRequestDTO) throws Exception {
		OrderDTO orderResponseDTO = null;
		try {
			Order order = orderDTOConverter.getOrderEntity(orderCreationRequestDTO);
			Order savedOrderObj = orderDAO.save(order);
			orderResponseDTO = orderDTOConverter.getOrderDTO(savedOrderObj);
			eventPublisher.publish(new OrderCreatedEvent(orderResponseDTO));
		} catch (Exception ex) {
			log.error("Created Order Error:" + ex.getMessage(), ex);
			eventPublisher.publish(
					new OrderCreationFailedEvent(orderCreationRequestDTO, "Created Order Error:" + ex.getMessage()));
			throw ex;
		}
		return orderResponseDTO;
	}

	@Override
	public OrderDTO findById(String busName, Integer locnNbr, Long id) throws Exception {
		Order orderEntity = orderDAO.findById(busName, locnNbr, id);
		return orderDTOConverter.getOrderDTO(orderEntity);
	}

	@Override
	public OrderDTO updateOrderLineStatusToReserved(OrderLineStatusUpdateRequestDTO orderLineStatusUpdReq)
			throws Exception {
		OrderDTO orderResponseDTO = null;
		try {
			Order orderEntity = orderDAO.findByBusNameAndLocnNbrAndOrderNbr(orderLineStatusUpdReq.getBusName(),
					orderLineStatusUpdReq.getLocnNbr(), orderLineStatusUpdReq.getOrderNbr());
			OrderLine orderLine = this.getOrderLine(orderEntity, orderLineStatusUpdReq.getId());
			orderLine.setStatCode(OrderLineStatus.ALLOCATED.getStatCode());
			orderEntity.setStatCode(OrderStatus.PARTIALLY_ALLOCATED.getStatCode());
			orderEntity = orderDAO.save(orderEntity);
			
			boolean isEntireOrderReservedForInventory = areAllOrderLinesSameStatus(orderEntity, OrderLineStatus.ALLOCATED.getStatCode());

			if (isEntireOrderReservedForInventory) {
				orderEntity.setStatCode(OrderStatus.ALLOCATED.getStatCode());
				orderEntity = orderDAO.save(orderEntity);
				eventPublisher.publish(new OrderAllocatedEvent(orderDTOConverter.getOrderDTO(orderEntity)));
			}
		} catch (Exception ex) {
			log.error("Order Line Allocation Failed Error:" + ex.getMessage(), ex);
			eventPublisher.publish(new OrderLineAllocationFailedEvent(orderLineStatusUpdReq,
					"Order Line Allocation Failed Error:" + ex.getMessage()));
			throw ex;
		}
		return orderResponseDTO;
	}
	
	public OrderLine getOrderLine(Order orderEntity, Long orderDtlId) {
		for (OrderLine orderLine : orderEntity.getOrderLines()) {
			if (orderLine.getId() == orderDtlId) {
				return orderLine;
			}
		}
		return null;
	}

	public boolean areAllOrderLinesSameStatus(Order orderEntity, Integer statCode) {
		for (OrderLine orderLine : orderEntity.getOrderLines()) {
			if (!(orderLine.getStatCode()==statCode)) {
				return false;
			}
		}
		return true;
	}
}
