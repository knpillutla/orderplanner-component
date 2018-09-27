package com.example.order.dto.converter;

import org.springframework.stereotype.Component;

import com.example.inventory.dto.events.InventoryAllocatedEvent;
import com.example.inventory.dto.responses.InventoryDTO;
import com.example.order.dto.requests.OrderLineStatusUpdateRequestDTO;
import com.example.order.service.OrderServiceImpl;

@Component
public class OrderLineStatusUpdateDTOConverter {

	public static OrderLineStatusUpdateRequestDTO getOrderLineStatusUpdateDTO(InventoryAllocatedEvent inventoryAllocatedEvent) {
		OrderLineStatusUpdateRequestDTO req = new OrderLineStatusUpdateRequestDTO(inventoryAllocatedEvent.getOrderLineId(), inventoryAllocatedEvent.getOrderId(),
				inventoryAllocatedEvent.getOrderLineNbr(), inventoryAllocatedEvent.getBusName(), inventoryAllocatedEvent.getLocnNbr(), inventoryAllocatedEvent.getOrderNbr(), "", "",
				inventoryAllocatedEvent.getItemBrcd(), inventoryAllocatedEvent.getBusUnit(), inventoryAllocatedEvent.getQty(), 
				OrderServiceImpl.OrderLineStatus.ALLOCATED.getStatCode());
		return req;
	}
}
