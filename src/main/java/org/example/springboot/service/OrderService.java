package org.example.springboot.service;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;

import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.Product;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeWapPayResponse;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.config.AlipayProperties;
import org.example.springboot.dto.command.OrderCreateCommandDTO;
import org.example.springboot.dto.response.OrderListResponseDTO;
import org.example.springboot.dto.response.OrderResponseDTO;
import org.example.springboot.entity.ShopOrder;
import org.example.springboot.entity.ShopOrderItem;
import org.example.springboot.entity.ShopProduct;
import org.example.springboot.entity.UserAddress;
import org.example.springboot.enums.OrderStatus;
import org.example.springboot.enums.PayType;
import org.example.springboot.exception.BusinessException;
import org.example.springboot.mapper.ShopOrderItemMapper;
import org.example.springboot.mapper.ShopOrderMapper;
import org.example.springboot.mapper.ShopProductMapper;
import org.example.springboot.service.convert.OrderConvert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 订单服务
 * @author system
 */
@Slf4j
@Service
public class OrderService {

    @Resource
    private ShopOrderMapper shopOrderMapper;

    @Resource
    private ShopOrderItemMapper shopOrderItemMapper;

    @Resource
    private ShopProductMapper shopProductMapper;

    @Resource
    private UserAddressService userAddressService;

    @Autowired
    private AlipayProperties alipayProperties;

    /**
     * 创建订单
     */
    @Transactional(rollbackFor = Exception.class)
    public OrderResponseDTO createOrder(OrderCreateCommandDTO dto, Long userId) {
        log.info("创建订单，用户ID: {}, 商品ID: {}, 数量: {}", userId, dto.getProductId(), dto.getQuantity());
        
        // 1. 验证商品
        ShopProduct product = shopProductMapper.selectById(dto.getProductId());
        if (product == null) {
            throw new BusinessException("商品不存在");
        }
        if (!product.canPurchase()) {
            throw new BusinessException("商品已下架或库存不足");
        }
        if (!product.checkStock(dto.getQuantity())) {
            throw new BusinessException("商品库存不足");
        }
        
        // 2. 验证收货地址
        UserAddress address = userAddressService.getAddressEntityById(dto.getAddressId());
        if (!address.getUserId().equals(userId)) {
            throw new BusinessException("无权使用该收货地址");
        }
        
        // 3. 计算订单金额
        BigDecimal totalAmount = product.getPrice().multiply(BigDecimal.valueOf(dto.getQuantity()));
        BigDecimal payAmount = totalAmount; // 暂不考虑优惠
        
        // 4. 生成订单号
        String orderNo = generateOrderNo(userId);
        
        // 5. 创建订单主表
        ShopOrder order = ShopOrder.builder()
                .orderNo(orderNo)
                .userId(userId)
                .totalAmount(totalAmount)
                .payAmount(payAmount)
                .status(OrderStatus.PENDING.getCode())
                .receiverAddressId(address.getId())
                .remark(dto.getRemark())
                .build();
        shopOrderMapper.insert(order);
        
        // 6. 创建订单明细
        ShopOrderItem orderItem = ShopOrderItem.builder()
                .orderId(order.getId())
                .productId(product.getId())
                .skuId(product.getId()) // 暂无SKU，使用商品ID
                .title(product.getTitle())
                .skuTitle(product.getSubtitle())
                .price(product.getPrice())
                .quantity(dto.getQuantity())
                .subtotal(totalAmount)
                .build();
        shopOrderItemMapper.insert(orderItem);
        
        // 7. 扣减库存
//        boolean stockReduced = product.reduceStock(dto.getQuantity());
//        if (!stockReduced) {
//            throw new BusinessException("库存扣减失败");
//        }
        shopProductMapper.updateById(product);
        
        log.info("订单创建成功，订单号: {}", orderNo);
        
        // 8. 返回订单详情
        List<ShopOrderItem> items = new ArrayList<>();
        items.add(orderItem);
        return OrderConvert.toResponseDTO(order, address, items);
    }

    /**
     * 获取订单详情
     */
    public OrderResponseDTO getOrderDetail(Long orderId, Long userId, boolean isAdmin) {
        log.info("获取订单详情，订单ID: {}, 用户ID: {}, 是否管理员: {}", orderId, userId, isAdmin);
        
        // 查询订单
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 权限校验（管理员可以查看所有订单）
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BusinessException("无权查看该订单");
        }
        
        // 查询订单明细
        LambdaQueryWrapper<ShopOrderItem> itemWrapper = new LambdaQueryWrapper<>();
        itemWrapper.eq(ShopOrderItem::getOrderId, orderId);
        List<ShopOrderItem> items = shopOrderItemMapper.selectList(itemWrapper);
        
        // 查询收货地址
        UserAddress address = null;
        if (order.getReceiverAddressId() != null) {
            address = userAddressService.getAddressEntityById(order.getReceiverAddressId());
        }
        
        return OrderConvert.toResponseDTO(order, address, items);
    }

    /**
     * 分页查询订单列表（用户）
     */
    public Page<OrderListResponseDTO> getUserOrderPage(Long current, Long size, Integer status, Long userId) {
        log.info("分页查询用户订单，用户ID: {}, 页码: {}, 大小: {}, 状态: {}", userId, current, size, status);
        
        // 构建分页对象
        Page<ShopOrder> page = new Page<>(current, size);
        
        // 构建查询条件
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopOrder::getUserId, userId);
        if (status != null) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        wrapper.orderByDesc(ShopOrder::getCreateTime);
        
        // 查询订单
        Page<ShopOrder> orderPage = shopOrderMapper.selectPage(page, wrapper);
        
        // 查询所有订单的明细
        List<Long> orderIds = orderPage.getRecords().stream()
                .map(ShopOrder::getId)
                .collect(Collectors.toList());
        
        Map<Long, List<ShopOrderItem>> itemsMap = getOrderItemsMap(orderIds);
        
        // 转换为DTO
        Page<OrderListResponseDTO> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderListResponseDTO> records = orderPage.getRecords().stream()
                .map(order -> OrderConvert.toListResponseDTO(order, itemsMap.get(order.getId())))
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        
        return resultPage;
    }

    /**
     * 分页查询订单列表（管理员）
     */
    public Page<OrderListResponseDTO> getAdminOrderPage(Long current, Long size, Integer status, String orderNo) {
        log.info("分页查询管理员订单，页码: {}, 大小: {}, 状态: {}, 订单号: {}", current, size, status, orderNo);
        
        // 构建分页对象
        Page<ShopOrder> page = new Page<>(current, size);
        
        // 构建查询条件
        LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
        if (status != null) {
            wrapper.eq(ShopOrder::getStatus, status);
        }
        if (orderNo != null && !orderNo.trim().isEmpty()) {
            wrapper.like(ShopOrder::getOrderNo, orderNo);
        }
        wrapper.orderByDesc(ShopOrder::getCreateTime);
        
        // 查询订单
        Page<ShopOrder> orderPage = shopOrderMapper.selectPage(page, wrapper);
        
        // 查询所有订单的明细
        List<Long> orderIds = orderPage.getRecords().stream()
                .map(ShopOrder::getId)
                .collect(Collectors.toList());
        
        Map<Long, List<ShopOrderItem>> itemsMap = getOrderItemsMap(orderIds);
        
        // 转换为DTO
        Page<OrderListResponseDTO> resultPage = new Page<>(orderPage.getCurrent(), orderPage.getSize(), orderPage.getTotal());
        List<OrderListResponseDTO> records = orderPage.getRecords().stream()
                .map(order -> OrderConvert.toListResponseDTO(order, itemsMap.get(order.getId())))
                .collect(Collectors.toList());
        resultPage.setRecords(records);
        
        return resultPage;
    }

    /**
     * 支付订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void payOrder(Long orderId, Long userId, String payType) {
        log.info("支付订单，订单ID: {}, 用户ID: {}, 支付方式: {}", orderId, userId, payType);
        
        // 查询订单
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 权限校验
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        
        // 状态校验
        if (!OrderStatus.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.PAID.getCode());
        order.setPayType(payType);
        order.setPayTime(LocalDateTime.now());
        shopOrderMapper.updateById(order);
        
        log.info("订单支付成功，订单号: {}", order.getOrderNo());
    }

    /**
     * 取消订单
     */
    @Transactional(rollbackFor = Exception.class)
    public void cancelOrder(Long orderId, Long userId, boolean isAdmin) {
        log.info("取消订单，订单ID: {}, 用户ID: {}, 是否管理员: {}", orderId, userId, isAdmin);
        
        // 查询订单
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 权限校验
        if (!isAdmin && !order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        
        // 状态校验
        if (!order.canCancel()) {
            throw new BusinessException("订单状态不允许取消");
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.CLOSED.getCode());
        shopOrderMapper.updateById(order);
        
        // 恢复库存
        restoreOrderStock(orderId);
        
        log.info("订单取消成功，订单号: {}", order.getOrderNo());
    }

    /**
     * 发货
     */
    @Transactional(rollbackFor = Exception.class)
    public void shipOrder(Long orderId, String logisticsNo) {
        log.info("订单发货，订单ID: {}, 物流单号: {}", orderId, logisticsNo);
        
        // 查询订单
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 状态校验
        if (!order.canShip()) {
            throw new BusinessException("订单状态不允许发货");
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.SHIPPED.getCode());
        order.setLogisticsNo(logisticsNo);
        shopOrderMapper.updateById(order);
        
        log.info("订单发货成功，订单号: {}", order.getOrderNo());
    }

    /**
     * 确认收货
     */
    @Transactional(rollbackFor = Exception.class)
    public void confirmOrder(Long orderId, Long userId) {
        log.info("确认收货，订单ID: {}, 用户ID: {}", orderId, userId);
        
        // 查询订单
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }
        
        // 权限校验
        if (!order.getUserId().equals(userId)) {
            throw new BusinessException("无权操作该订单");
        }
        
        // 状态校验
        if (!order.canConfirm()) {
            throw new BusinessException("订单状态不允许确认收货");
        }
        
        // 更新订单状态
        order.setStatus(OrderStatus.COMPLETED.getCode());
        shopOrderMapper.updateById(order);
        
        log.info("确认收货成功，订单号: {}", order.getOrderNo());
    }

    /**
     * 生成订单号
     */
    private String generateOrderNo(Long userId) {
        // 格式：时间戳(yyyyMMddHHmmss) + 用户ID后4位 + 4位随机数
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String userIdSuffix = String.format("%04d", userId % 10000);
        String randomSuffix = String.format("%04d", (int) (Math.random() * 10000));
        return timestamp + userIdSuffix + randomSuffix;
    }

    /**
     * 批量查询订单明细
     */
    private Map<Long, List<ShopOrderItem>> getOrderItemsMap(List<Long> orderIds) {
        if (orderIds == null || orderIds.isEmpty()) {
            return Map.of();
        }
        
        LambdaQueryWrapper<ShopOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(ShopOrderItem::getOrderId, orderIds);
        List<ShopOrderItem> items = shopOrderItemMapper.selectList(wrapper);
        
        return items.stream().collect(Collectors.groupingBy(ShopOrderItem::getOrderId));
    }

    /**
     * 恢复订单库存
     */
    private void restoreOrderStock(Long orderId) {
        // 查询订单明细
        LambdaQueryWrapper<ShopOrderItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ShopOrderItem::getOrderId, orderId);
        List<ShopOrderItem> items = shopOrderItemMapper.selectList(wrapper);
        
        // 恢复库存
        for (ShopOrderItem item : items) {
            ShopProduct product = shopProductMapper.selectById(item.getProductId());
            if (product != null) {
                product.addStock(item.getQuantity());
                shopProductMapper.updateById(product);
            }
        }
    }

    public String alipay(Long orderId, Long userId, String payType) {
        log.info("支付订单，订单ID: {}, 用户ID: {}, 支付方式: {}", orderId, userId, payType);

        // 查询订单
        ShopOrder order = shopOrderMapper.selectById(orderId);
        if (order == null) {
            throw new BusinessException("订单不存在");
        }

        // 权限校验
        if (!order.getUserId().equals(userId)) {
            log.error("【alipay 方法】无权操作该订单 - 订单用户 ID: {}, 当前用户 ID: {}", order.getUserId(), userId);
            throw new BusinessException("无权操作该订单");
        }

        // 状态校验
        if (!OrderStatus.PENDING.getCode().equals(order.getStatus())) {
            throw new BusinessException("订单状态不允许支付");
        }

        // 查询订单明细
        ShopOrderItem shopOrderItem = shopOrderItemMapper.selectById(orderId);
        // 封装支付宝的支付接口
        BigDecimal totalPrice = shopOrderItem.getPrice().multiply(BigDecimal.valueOf(shopOrderItem.getQuantity()));
        double totalPriceDouble = totalPrice.doubleValue();
        // 封装商品描述
        String subject = shopOrderItem.getFullTitle();

        String form = submitAlipay(order.getOrderNo(),totalPriceDouble,subject);

        // 更新订单状态
//        order.setStatus(OrderStatus.PAID.getCode());
//        order.setPayType(payType);
//        order.setPayTime(LocalDateTime.now());
//        shopOrderMapper.updateById(order);

        log.info("订单支付成功，订单号: {}", order.getOrderNo());
        return form;
    }

    public String submitAlipay(String orderNo,double totalPrice,String subject) {
        log.info("构建支付宝支付请求 - 订单号：{}, 金额：{}, 商品：{}", orderNo, totalPrice, subject);

        // 获得初始化的 AlipayClient
        AlipayClient alipayClient = new DefaultAlipayClient(
                alipayProperties.getAlipayUrl(),
                alipayProperties.getAppId(),
                alipayProperties.getAppPrivateKey(),
                AlipayProperties.format,
                AlipayProperties.charset,
                alipayProperties.getAlipayPublicKey(),
                AlipayProperties.sign_type);

        // 创建 API 对应的 request
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setReturnUrl(alipayProperties.getReturnPaymentUrl());
        alipayRequest.setNotifyUrl(alipayProperties.getNotifyPaymentUrl());

        // 填充业务参数
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderNo);
        map.put("product_code", "QUICK_WAP_WAY");
        map.put("total_amount", totalPrice);
        map.put("subject", subject);

        // 使用 JSON 库序列化数据
        try {
            String bizContent = JSON.toJSONString(map);
            log.info("业务参数：{}", bizContent);
            alipayRequest.setBizContent(bizContent);
        } catch (Exception e) {
            log.error("JSON 序列化失败", e);
            throw new RuntimeException("支付请求参数格式错误");
        }

        String form = null;
        try {
            AlipayTradeWapPayResponse response = alipayClient.pageExecute(alipayRequest);
            form = response.getBody();

            log.info("支付宝响应 - 成功：{}, 代码：{}, 消息：{}",
                    response.isSuccess(), response.getCode(), response.getMsg());
            log.info("支付宝响应 Body: {}", form);

            if (!response.isSuccess()) {
                log.error("支付宝调用失败 - 子码：{}, 子消息：{}", response.getSubCode(), response.getSubMsg());
            }
        } catch (AlipayApiException e) {
            log.error("支付宝 API 异常", e);
            throw new RuntimeException("支付宝调用失败：" + e.getMessage());
        }

        return form;
    }

//    public void notify(HttpServletRequest request) {
//        try{
//            // 获取支付宝POST过来反馈信息
//            Map<String, String> params = new HashMap<>();
//            Map<String, String[]> requestParams = request.getParameterMap();
//            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
//                String name = iter.next();
//                String[] values = requestParams.get(name);
//                String valueStr = "";
//                for (int i = 0; i < values.length; i++) {
//                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
//                }
//                params.put(name, valueStr);
//            }
//            // 验证签名
//            boolean signVerified = AlipaySignature.rsaCheckV1(params,
//                    alipayProperties.getAlipayPublicKey(),
//                    AlipayProperties.charset,
//                    AlipayProperties.sign_type);
//
//            if (!signVerified) {
//                return ; // 签名验证失败
//            }
//
//            // 获取必要参数
//            String orderNo = params.get("out_trade_no");
//            String tradeNo = params.get("trade_no");
//            String tradeStatus = params.get("trade_status");
//
//            // 记录日志
//            System.out.println("=========== 支付宝异步回调 ===========");
//            System.out.println("订单号: " + orderNo);
//            System.out.println("支付宝交易号: " + tradeNo);
//            System.out.println("交易状态: " + tradeStatus);
//            System.out.println("==================================");
//
//            // 处理业务逻辑
//            if ("TRADE_SUCCESS".equals(tradeStatus)) {
//                // 更新订单状态为已支付
//                LambdaQueryWrapper<ShopOrder> queryWrapper = new LambdaQueryWrapper<>();
//                ShopOrder order = shopOrderMapper.selectOne(queryWrapper.eq(ShopOrder::getOrderNo, orderNo));
//                order.setStatus(OrderStatus.PAID.getCode());
//                order.setPayTime(LocalDateTime.now());
//                order.setPayType("ALI");
//                shopOrderMapper.updateById(order);
//
//                // 处理其他业务逻辑，如更新库存、发送通知等
//                ShopOrderItem shopOrderItem = shopOrderItemMapper.selectOne(new LambdaQueryWrapper<ShopOrderItem>().eq(ShopOrderItem::getOrderId, order.getId()));
//                String productId = shopOrderItem.getProductId();
//
//                ShopProduct shopProduct = shopProductMapper.selectById(productId);
//                shopProduct.setStock(shopProduct.getStock() - shopOrderItem.getQuantity());
//                shopProductMapper.updateById(shopProduct);
//
//                return ;
//            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
//                // 交易关闭
//                // paymentInfoService.updatePaymentStatus(orderNo, 3);
//                return ;
//            } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
//                // 等待用户付款
//                return ;
//            } else {
//                return ; // 其他状态也返回success，避免支付宝重复通知
//            }
//        }catch (Exception e) {
//            e.printStackTrace();
//            return ;
//        }
//    }

    public String notify(HttpServletRequest request) {
        try{
            Map<String, String> params = new HashMap<>();
            Map<String, String[]> requestParams = request.getParameterMap();
            for (Iterator<String> iter = requestParams.keySet().iterator(); iter.hasNext();) {
                String name = iter.next();
                String[] values = requestParams.get(name);
                String valueStr = "";
                for (int i = 0; i < values.length; i++) {
                    valueStr = (i == values.length - 1) ? valueStr + values[i] : valueStr + values[i] + ",";
                }
                params.put(name, valueStr);
            }

            log.info("========== 支付宝异步回调参数开始 ==========");
            for (Map.Entry<String, String> entry : params.entrySet()) {
                log.info("{} = {}", entry.getKey(), entry.getValue());
            }
            log.info("==================================");

            boolean signVerified = AlipaySignature.rsaCheckV1(params,
                    alipayProperties.getAlipayPublicKey(),
                    AlipayProperties.charset,
                    AlipayProperties.sign_type);

            if (!signVerified) {
                log.error("支付宝异步回调签名验证失败！");
                return "failure";
            }

            String orderNo = params.get("out_trade_no");
            String tradeNo = params.get("trade_no");
            String tradeStatus = params.get("trade_status");

            log.info("订单号：{}, 支付宝交易号：{}, 状态：{}", orderNo, tradeNo, tradeStatus);

            if ("TRADE_SUCCESS".equals(tradeStatus)) {
                LambdaQueryWrapper<ShopOrder> queryWrapper = new LambdaQueryWrapper<>();
                ShopOrder order = shopOrderMapper.selectOne(queryWrapper.eq(ShopOrder::getOrderNo, orderNo));

                if (order == null) {
                    log.error("订单不存在，订单号：{}", orderNo);
                    return "success";
                }

                if (order.getStatus() == OrderStatus.PAID.getCode()) {
                    log.info("订单已支付，无需重复处理，订单号：{}", orderNo);
                    return "success";
                }

                order.setStatus(OrderStatus.PAID.getCode());
                order.setPayTime(LocalDateTime.now());
                order.setPayType("ALI");
                shopOrderMapper.updateById(order);

                log.info("订单支付成功，更新订单状态，订单号：{}", orderNo);

                ShopOrderItem shopOrderItem = shopOrderItemMapper.selectOne(new LambdaQueryWrapper<ShopOrderItem>().eq(ShopOrderItem::getOrderId, order.getId()));
                if (shopOrderItem != null) {
                    String productId = shopOrderItem.getProductId();
                    ShopProduct shopProduct = shopProductMapper.selectById(productId);
                    if (shopProduct != null) {
                        Integer stock = shopProduct.getStock();
                        log.info("商品 ID: {}, 库存：{}", productId, stock);
                        Integer quantity = shopOrderItem.getQuantity();
                        log.info("商品 ID: {}, 购买数量：{}", productId, quantity);
                        shopProduct.setStock(stock - quantity);
                        shopProductMapper.updateById(shopProduct);
                        log.info("商品库存已扣减，商品 ID: {}, 剩余库存：{}", productId, shopProduct.getStock());
                    }
                }

                return "success";
            } else if ("TRADE_CLOSED".equals(tradeStatus)) {
                log.info("交易关闭，订单号：{}", orderNo);
                return "success";
            } else if ("WAIT_BUYER_PAY".equals(tradeStatus)) {
                log.info("等待用户付款，订单号：{}", orderNo);
                return "success";
            } else {
                log.info("其他交易状态：{}, 订单号：{}", tradeStatus, orderNo);
                return "success";
            }
        }catch (Exception e) {
            log.error("支付宝异步回调处理异常", e);
            return "failure";
        }
    }

    public void returnCallback(HttpServletRequest request, HttpServletResponse response) {
        log.info("========== 支付宝同步回调开始 ==========");

        // 获取支付宝回调参数
        Map<String, String[]> paramMap = request.getParameterMap();
        StringBuilder paramsLog = new StringBuilder();
        for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
            paramsLog.append(entry.getKey()).append("=");
            if (entry.getValue() != null && entry.getValue().length > 0) {
                paramsLog.append(String.join(",", entry.getValue()));
            }
            paramsLog.append("; ");
        }
        log.info("同步回调参数：{}", paramsLog.toString());

        // 验证签名（可选，建议加上）
        try {
            Map<String, String> params = new HashMap<>();
            for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
                params.put(entry.getKey(), entry.getValue()[0]);
            }

            boolean signVerified = com.alipay.api.internal.util.AlipaySignature.rsaCheckV1(
                    params,
                    alipayProperties.getAlipayPublicKey(),
                    AlipayProperties.charset,
                    AlipayProperties.sign_type
            );

            if (!signVerified) {
                log.error("同步回调签名验证失败！");
                response.sendRedirect("/error?msg=签名验证失败");
                return;
            }

            log.info("同步回调签名验证通过");

            // 获取订单号
            String outTradeNo = request.getParameter("out_trade_no");
            String tradeNo = request.getParameter("trade_no");
            String tradeStatus = request.getParameter("trade_status");

            log.info("订单号：{}, 支付宝交易号：{}, 状态：{}", outTradeNo, tradeNo, tradeStatus);

            // 重定向到前端订单详情页面或支付成功页面
            // 方式 1: 重定向到前端页面（推荐）
            response.sendRedirect("http://localhost:5173/frontend/order/success?orderNo=" + outTradeNo);

            // 方式 2: 如果你想显示一个简单的成功页面
            // response.setContentType("text/html;charset=utf-8");
            // response.getWriter().write("<html><body><h1>支付成功！</h1><p>订单号：" + outTradeNo + "</p></body></html>");

        } catch (Exception e) {
            log.error("同步回调处理异常", e);
            try {
                response.sendRedirect("/error?msg=系统异常");
            } catch (IOException ex) {
                log.error("重定向失败", ex);
            }
        }
    }
}

