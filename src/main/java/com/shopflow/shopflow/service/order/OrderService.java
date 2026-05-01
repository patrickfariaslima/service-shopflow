package com.shopflow.shopflow.service.order;

import java.util.List;

import com.shopflow.shopflow.dto.order.CheckoutRequest;
import com.shopflow.shopflow.dto.order.OrderResponse;
import com.shopflow.shopflow.dto.order.UpdateOrderStatusRequest;

public interface OrderService {
    OrderResponse checkout(CheckoutRequest request);

    List<OrderResponse> getOrderHistory();

    OrderResponse getOrderById(Long id);

    OrderResponse updateStatus(Long id, UpdateOrderStatusRequest request);

    List<OrderResponse> getAllOrders();
}
