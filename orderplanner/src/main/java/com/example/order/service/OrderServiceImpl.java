package com.example.order.service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
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
import com.example.order.dto.events.OrderPlannedEvent;
import com.example.order.dto.events.OrderUpdateFailedEvent;
import com.example.order.dto.requests.OrderCreationRequestDTO;
import com.example.order.dto.requests.OrderFulfillmentRequestDTO;
import com.example.order.dto.requests.OrderLineStatusUpdateRequestDTO;
import com.example.order.dto.requests.OrderUpdateRequestDTO;
import com.example.order.dto.responses.OrderDTO;
import com.example.order.dto.responses.OrderFulfillmentResponseDTO;

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
		CREATED(100), READY(110), WAVED(120), ALLOCATED(130), PARTIALLY_ALLOCATED(131), PICKED(140), PACKED(150), SHIPPED(160),
		SHORTED(170), CANCELLED(199);
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
	
	@Override
	public OrderFulfillmentResponseDTO startOrderFulfillment(OrderFulfillmentRequestDTO orderFulfillmentReq) {
		OrderFulfillmentResponseDTO responseDTO = new OrderFulfillmentResponseDTO(orderFulfillmentReq);
		List<OrderDTO> orderDTOList= new ArrayList();
		List orderFailureDTOList= new ArrayList();
		
		String pattern = "yyyyMMddhhmmss";
		SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern); 
		String batchNbr = simpleDateFormat.format(new Date());
		String userId = orderFulfillmentReq.getUserId();
		if(orderFulfillmentReq.getOrderIdList() !=null && orderFulfillmentReq.getOrderIdList().size()>0) {
			// created pick list based on order ids
			// get all the orderids
			for(Long orderId:orderFulfillmentReq.getOrderIdList()) {
				Order orderEntity = orderDAO.findByBusNameAndLocnNbrAndOrderId(orderFulfillmentReq.getBusName(), orderFulfillmentReq.getLocnNbr(), orderId);
				OrderDTO orderDTO;
				try {
					orderDTO = startOrderFulfillment(batchNbr, orderEntity, userId);
					orderDTOList.add(orderDTO);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					orderFailureDTOList.add(orderId);
				}
			}
		}
		else
		if(orderFulfillmentReq.getOrderNbrList() !=null && orderFulfillmentReq.getOrderNbrList().size()>0) {
			// created pick list based on order Nbrs
			// get all the orderids
			for(String orderNbr:orderFulfillmentReq.getOrderNbrList()) {
				Order orderEntity = orderDAO.findByBusNameAndLocnNbrAndOrderNbr(orderFulfillmentReq.getBusName(), orderFulfillmentReq.getLocnNbr(), orderNbr);
				OrderDTO orderDTO;
				try {
					orderDTO = startOrderFulfillment(batchNbr, orderEntity, userId);
					orderDTOList.add(orderDTO);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					orderFailureDTOList.add(orderNbr);
				}
			}
		}
		else {
			// create pick list based on number of orders
			int numOfOrders = orderFulfillmentReq.getNumOfOrders();
			if(numOfOrders==0) {
				numOfOrders = 10;
			}
			
			String orderSelectionOption = orderFulfillmentReq.getOrderSelectionOption();
			if(orderSelectionOption.equalsIgnoreCase("byAreaZoneAisle")) {
				
			}
			else
			if(orderSelectionOption.equalsIgnoreCase("deliveryType")) {
				
			}
			log.info("No options selected, using num of orders to fetch from DB" + numOfOrders);
			List<Order> orderEntityList = orderDAO.findByBusNameAndLocnNbrOrderByOrderId(orderFulfillmentReq.getBusName(), orderFulfillmentReq.getLocnNbr());
			log.info("Retreived " + orderEntityList.size() + "from db");
			OrderDTO orderDTO;
			for(Order orderEntity:orderEntityList) {
				try {
					orderDTO = startOrderFulfillment(batchNbr, orderEntity, userId);
					orderDTOList.add(orderDTO);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		responseDTO.setBatchNbr(batchNbr);
		responseDTO.setSuccessList(orderDTOList);
		responseDTO.setFailureList(orderFailureDTOList);
		return responseDTO;
	}
	
	@Transactional
	public OrderDTO startOrderFulfillment(String batchNbr, Order orderEntity, String userId) throws Exception{
		orderEntity.setStatCode(OrderStatus.WAVED.getStatCode());
		orderEntity.setBatchNbr(batchNbr);
		orderEntity.setUpdatedDttm(new java.util.Date());
		orderEntity.setUpdatedBy(userId);
		orderEntity = orderDAO.save(orderEntity);
		OrderDTO  orderDTO = orderDTOConverter.getOrderDTO(orderEntity);
		eventPublisher.publish(new OrderPlannedEvent(orderDTO));
		return orderDTO;
	}
	
}
