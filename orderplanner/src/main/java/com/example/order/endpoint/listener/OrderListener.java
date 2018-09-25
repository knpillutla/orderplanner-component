package com.example.order.endpoint.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

import com.example.inventory.dto.events.InventoryAllocatedEvent;
import com.example.order.dto.converter.CustomerOrderDTOConverter;
import com.example.order.dto.converter.LowPickEventConverter;
import com.example.order.dto.converter.OrderLineStatusUpdateDTOConverter;
import com.example.order.dto.events.CustomerOrderCreatedEvent;
import com.example.order.dto.responses.OrderFulfillmentResponseDTO;
import com.example.order.service.OrderService;
import com.example.order.streams.OrderStreams;
import com.example.picking.dto.events.LowPickEvent;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderListener {
	@Autowired
	OrderService orderService;

	@StreamListener(target=OrderStreams.CUSTOMER_ORDERS_OUTPUT, condition = "headers['eventName']=='CustomerOrderCreatedEvent'")
	public void handleNewOrder(CustomerOrderCreatedEvent customerOrderCreatedEvent) { // OrderCreationRequestDTO
																					// orderCreationRequestDTO) {
		log.info("Received CustomerOrderCreatedEvent Msg: {}" + ": at :" + new java.util.Date(), customerOrderCreatedEvent);
		long startTime = System.currentTimeMillis();
		try {
			orderService.createOrder(CustomerOrderDTOConverter.getOrderCreationRequestDTO(customerOrderCreatedEvent));
			long endTime = System.currentTimeMillis();
			log.info("Completed CustomerOrderCreatedEvent for : " + customerOrderCreatedEvent + ": at :" + new java.util.Date()
					+ " : total time:" + (endTime - startTime) / 1000.00 + " secs");
		} catch (Exception e) {
			e.printStackTrace();
			long endTime = System.currentTimeMillis();
			log.error("Error Completing CustomerOrderCreatedEvent for : " + customerOrderCreatedEvent + ": at :"
					+ new java.util.Date() + " : total time:" + (endTime - startTime) / 1000.00 + " secs", e);
		}
	}

	@StreamListener(target = OrderStreams.INVENTORY_OUTPUT, 
			condition = "headers['eventName']=='InventoryAllocatedEvent'")
	public void handleAllocatedInventoryEvent(InventoryAllocatedEvent inventoryAllocatedEvent) { 
		log.info("Received InventoryAllocatedEvent for: {}" + ": at :" + new java.util.Date(), inventoryAllocatedEvent);
		long startTime = System.currentTimeMillis();
		try {
			orderService.updateOrderLineStatusToReserved(
					OrderLineStatusUpdateDTOConverter.getOrderLineStatusUpdateDTO(inventoryAllocatedEvent));
			long endTime = System.currentTimeMillis();
			log.info("Completed InventoryAllocatedEvent for: " + inventoryAllocatedEvent + ": at :"
					+ new java.util.Date() + " : total time:" + (endTime - startTime) / 1000.00 + " secs");
		} catch (Exception e) {
			e.printStackTrace();
			long endTime = System.currentTimeMillis();
			log.error("Error Completing InventoryAllocatedEvent for: " + inventoryAllocatedEvent + ": at :"
					+ new java.util.Date() + " : total time:" + (endTime - startTime) / 1000.00 + " secs", e);
		}
	}

	@StreamListener(target = OrderStreams.PICK_OUTPUT, 
			condition = "headers['eventName']=='LowPickEvent'")
	public void handleLowPickEvent(LowPickEvent lowPickEvent) { 
		log.info("Received handleLowPickEvent for: {}" + ": at :" + new java.util.Date(), lowPickEvent);
		long startTime = System.currentTimeMillis();
		try {
			OrderFulfillmentResponseDTO orderFulfillmentResponse = orderService.startOrderFulfillment(LowPickEventConverter.getOrderFulfillmentRequestDTO(lowPickEvent));
			log.info("output of lowpick event:" +orderFulfillmentResponse);
			long endTime = System.currentTimeMillis();
			log.info("Completed handleLowPickEvent for: " + lowPickEvent + ": at :"
					+ new java.util.Date() + " : total time:" + (endTime - startTime) / 1000.00 + " secs");
		} catch (Exception e) {
			e.printStackTrace();
			long endTime = System.currentTimeMillis();
			log.error("Error Completing handleLowPickEvent for: " + lowPickEvent + ": at :"
					+ new java.util.Date() + " : total time:" + (endTime - startTime) / 1000.00 + " secs", e);
		}
	}	
}
