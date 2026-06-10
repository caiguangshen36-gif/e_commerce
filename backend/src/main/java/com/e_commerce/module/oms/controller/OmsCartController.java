package com.e_commerce.module.oms.controller;

import com.e_commerce.common.utils.Result;
import com.e_commerce.common.utils.ThreadLocalUtil;
import com.e_commerce.module.oms.dto.OmsCartDto;
import com.e_commerce.module.oms.service.OmsCartService;
import com.e_commerce.module.oms.vo.CartVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

/**
 * 购物车控制器
 * 处理购物车相关的HTTP请求，包括获取购物车列表、添加商品、更新数量、删除商品、清空购物车和结算等功能
 */
@RestController
@RequestMapping("/cart")
public class OmsCartController {

    @Autowired
    private OmsCartService cartService; // 购物车服务接口，用于处理购物车相关的业务逻辑

    // 获取购物车列表
    @GetMapping("/list")
    public Result<List<CartVo>> getCartList() {
        Long userId = ThreadLocalUtil.getUserId();
        List<CartVo> cartList = cartService.getCartList(userId);
        return Result.success(cartList);
    }

    // 添加商品到购物车
    @PostMapping("/add")
    public Result<String> add(@RequestBody OmsCartDto cartDto) {
        Long userId = ThreadLocalUtil.getUserId();

        // 参数校验
        if (cartDto.getSkuId() == null || cartDto.getSkuId() <= 0) {
            return Result.error("SKU不能为空");
        }
        if (cartDto.getQuantity() == null || cartDto.getQuantity() <= 0) {
            return Result.error("商品数量必须大于0");
        }

        cartService.add(userId, cartDto);
        return Result.success("添加成功");
    }

    //更新购物车数量
    @PostMapping("/update")
    public Result<String> update(@RequestBody Map<String, Object> request) {
        Long userId = ThreadLocalUtil.getUserId();
        Long id = Long.valueOf(request.get("id").toString());
        Integer quantity = (Integer) request.get("quantity");

        if (quantity <= 0) {
            return Result.error("数量必须大于0");
        }

        // 校验权限 + 更新
        boolean success = cartService.updateQuantity(userId, id, quantity);
        if (!success) {
            return Result.error("无权限或商品不存在");
        }
        return Result.success("更新成功");
    }

    // 删除购物车项
    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> request) {
        Long userId = ThreadLocalUtil.getUserId();
        Long id = request.get("id");

        boolean success = cartService.deleteById(userId, id);
        if (!success) {
            return Result.error("无权限或商品不存在");
        }
        return Result.success("删除成功");
    }

    //  清空购物车
    @PostMapping("/clear")
    public Result<String> clear() {
        Long userId = ThreadLocalUtil.getUserId();
        cartService.clearUserCart(userId);
        return Result.success("清空成功");
    }

    //  结算购物车
    @PostMapping("/settle")
    public Result<Map<String, Object>> settleCart(@RequestBody List<Long> cartIds) {
        Long userId = ThreadLocalUtil.getUserId();
        Map<String, Object> result = cartService.settleCart(userId, cartIds);
        return Result.success(result);
    }
}