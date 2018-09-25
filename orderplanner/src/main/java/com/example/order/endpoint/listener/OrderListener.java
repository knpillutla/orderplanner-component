package com.example.order.endpoint.listener;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.stream.annotation.StreamListener;
import org.springframework.stereotype.Component;

import com.example.inventory.dto.events.InventoryAllocatedEvent;
import com.example.order.dto.converter.OrderLineStatusUpdateDTOConverter;
import com.example.order.dto.events.OrderDownloadEvent;
import com.example.order.service.OrderService;
import com.example.order.streams.OrderStreams;

import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class OrderListener {
	@Autowired
	OrderService orderService;

	@StreamListener(target=OrderStreams.ORDERS_INPUT, condition = "headers['eventName']=='OrderDownloadEvent'")
	public void handleNewOrder(OrderDownloadEvent orderDownloadEvent) { // OrderCreationRequestDTO
																					// orderCreationRequestDTO) {
		log.info("Received OrderCreationRequest Msg: {}" + ": at :" + new java.util.Date(), orderDownloadEvent);
		long startTime = System.currentTimeMillis();
		try {
			orderService.createOrder(orderDownloadEvent.getOrderCreationRequestDTO());
			long endTime = System.currentTimeMillis();
			log.info("Completed OrderCreationRequest for : " + orderDownloadEvent + ": at :" + new java.util.Date()
					+ " : total time:" + (endTime - startTime) / 1000.00 + " secs");
		} catch (Exception e) {
			e.printStackTrace();
			long endTime = System.currentTimeMillis();
			log.error("Error Completing OrderCreationRequest for : " + orderDownloadEvent + ": at :"
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

}
