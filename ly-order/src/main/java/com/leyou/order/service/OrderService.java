package com.leyou.order.service;

import com.leyou.order.dto.OrderDTO;
import com.leyou.order.pojo.Order;
import org.springframework.stereotype.Service;

@Service
public interface OrderService {
    Long createOrder(OrderDTO orderDTO);

    Order queryOrderById(Long id);
}
