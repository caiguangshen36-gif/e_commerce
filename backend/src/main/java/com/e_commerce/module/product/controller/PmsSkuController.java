package com.e_commerce.module.product.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.e_commerce.common.utils.Result;
import com.e_commerce.common.vo.PageVo;
import com.e_commerce.module.notice.dto.SysNoticeDto;
import com.e_commerce.module.notice.service.SysNoticeService;
import com.e_commerce.module.oms.entity.OmsOrderItem;
import com.e_commerce.module.oms.mapper.OmsOrderItemMapper;
import com.e_commerce.module.product.dto.PmsSkuDto;
import com.e_commerce.module.product.entity.PmsSku;
import com.e_commerce.module.product.mapper.PmsSkuMapper;
import com.e_commerce.module.product.service.PmsSkuService;
import com.e_commerce.module.product.vo.PmsSkuVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/product/sku")
public class PmsSkuController {

    @Autowired
    private PmsSkuService skuService;

    @Autowired
    private PmsSkuMapper skuMapper;

    @Autowired
    private OmsOrderItemMapper orderItemMapper;

    @Autowired
    private SysNoticeService sysNoticeService;

    /**
     * 根据商品ID获取SKU列表
     * @param params 包含productId的参数
     * @return SKU视图对象列表
     */
    @PostMapping("/list")
    public Result<List<PmsSkuVo>> listByProductId(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId");
        return Result.success(skuService.getSkusByProductId(productId));
    }

    @GetMapping("/allList")
    public Result<List<PmsSkuVo>> listOfAll() {
        return Result.success(skuService.listOfAll());
    }

    @PostMapping("/admin/list")
    public Result<PageVo<PmsSkuVo>> adminList(@RequestBody Map<String, Object> params) {
        Long pageNum = params.get("pageNum") != null ? Long.valueOf(params.get("pageNum").toString()) : 1L;
        Long pageSize = params.get("pageSize") != null ? Long.valueOf(params.get("pageSize").toString()) : 10L;
        String skuCode = (String) params.get("skuCode");
        Long productId = params.get("productId") != null ? Long.valueOf(params.get("productId").toString()) : null;
        Integer status = params.get("status") != null ? Integer.valueOf(params.get("status").toString()) : null;

        System.out.println("前端传过来的 skuCode：" + skuCode);
        System.out.println("前端传过来的 productId：" + productId);
        System.out.println("前端传过来的 status：" + status);

        return Result.success(skuService.listByCondition(pageNum, pageSize, skuCode, productId, status));
    }

    /**
     * 根据SKU ID获取SKU详情
     * @param params 包含id的参数
     * @return SKU视图对象
     */
    @PostMapping("/detail")
    public Result<PmsSkuVo> detail(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        PmsSkuVo skuVo = skuService.getSkuById(id);
        if (skuVo == null) {
            return Result.error("SKU不存在");
        }
        return Result.success(skuVo);
    }

    /**
     * 添加SKU
     * @param params 包含productId和skuDto的参数
     * @return 操作结果
     */
    @PostMapping("/add")
    public Result<String> add(@RequestBody Map<String, Object> params) {
        Long productId = Long.valueOf(params.get("productId").toString());
        PmsSkuDto skuDto = new PmsSkuDto();
        skuDto.setSkuCode((String) params.get("skuCode"));
        skuDto.setPrice(new java.math.BigDecimal(params.get("price").toString()));
        if (params.get("costPrice") != null) {
            skuDto.setCostPrice(new java.math.BigDecimal(params.get("costPrice").toString()));
        }
        skuDto.setStock((Integer) params.get("stock"));
        if (params.get("stockWarning") != null) {
            skuDto.setStockWarning((Integer) params.get("stockWarning"));
        }
        if (params.get("pic") != null) {
            skuDto.setPic((String) params.get("pic"));
        }
        if (params.get("weight") != null) {
            skuDto.setWeight(new java.math.BigDecimal(params.get("weight").toString()));
        }
        if (params.get("volume") != null) {
            skuDto.setVolume(new java.math.BigDecimal(params.get("volume").toString()));
        }
        if (params.get("status") != null) {
            skuDto.setStatus((Integer) params.get("status"));
        }
        if (params.get("skuAttrList") != null) {
            skuDto.setSkuAttrList((List) params.get("skuAttrList"));
        }

        skuService.addSku(productId, skuDto);
        return Result.success("添加成功");
    }

    /**
     * 更新SKU信息
     * @param skuDto SKU数据传输对象
     * @return 操作结果
     */
    @PostMapping("/update")
    public Result<String> update(@RequestBody @Validated PmsSkuDto skuDto) {
        skuService.updateSku(skuDto);
        return Result.success("更新成功");
    }

    /**
     * 更新SKU状态
     * @param params 包含id和status的参数
     * @return 操作结果
     */
    @PostMapping("/updateStatus")
    public Result<String> updateStatus(@RequestBody Map<String, Object> params) {
        Long id = Long.valueOf(params.get("id").toString());
        Integer status = Integer.valueOf(params.get("status").toString());
        skuService.updateSkuStatus(id, status);
        return Result.success("更新成功");
    }

    /**
     * 批量更新SKU库存
     * @return 操作结果
     */
    @PostMapping("/updateStocks")
    public Result<String> updateStocks(@RequestParam Long orderId) {
        try {
            int successCount = skuService.updateSkuStocksByOrderId(orderId);
            if (successCount > 0) {
                // 检查库存预警
                checkAndSendStockWarning(orderId);
                return Result.success("库存更新成功，已更新 " + successCount + " 个SKU");
            } else {
                return Result.error("库存更新失败，可能库存不足");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 检查库存预警并发送通知
     */
    private void checkAndSendStockWarning(Long orderId) {
        // 查询订单项
        List<OmsOrderItem> orderItems = orderItemMapper.selectList(
                new LambdaQueryWrapper<OmsOrderItem>().eq(OmsOrderItem::getOrderId, orderId));
        if (orderItems == null || orderItems.isEmpty()) {
            return;
        }

        // 检查每个SKU的库存是否低于预警值
        List<String> warningSkus = new ArrayList<>();
        for (OmsOrderItem item : orderItems) {
            PmsSku sku = skuMapper.selectById(item.getSkuId());
            if (sku != null && sku.getStockWarning() != null && sku.getStockWarning() > 0
                    && sku.getStock() <= sku.getStockWarning()) {
                warningSkus.add(sku.getSkuCode() + "(库存:" + sku.getStock() + ", 预警值:" + sku.getStockWarning() + ")");
            }
        }

        // 如果有库存低于预警值，发送通知
        if (!warningSkus.isEmpty()) {
            try {
                SysNoticeDto dto = new SysNoticeDto();
                dto.setNoticeType(5); // 库存预警通知
                dto.setTitle("库存预警通知");
                dto.setContent("以下商品库存低于预警值，请及时补货：" + String.join("；", warningSkus));
                List<Long> roleIds = new ArrayList<>();
                roleIds.add(1L);
                roleIds.add(5L);
                dto.setRoleIds(roleIds);
                sysNoticeService.sendNotice(dto);
            } catch (Exception e) {
                // 发送通知失败不影响主流程
                System.err.println("发送库存预警通知失败：" + e.getMessage());
            }
        }
    }

    @PostMapping("/rollbackStocks")
    public Result<String> rollbackStocks(@RequestParam Long orderId) {
        try {
            int successCount = skuService.rollbackSkuStocksByOrderId(orderId);
            if (successCount > 0) {
                return Result.success("库存回滚成功，已恢复 " + successCount + " 个SKU");
            } else {
                return Result.error("库存回滚失败");
            }
        } catch (RuntimeException e) {
            return Result.error(e.getMessage());
        }
    }

    /**
     * 删除SKU
     * @param params 包含id的参数
     * @return 操作结果
     */
    @PostMapping("/delete")
    public Result<String> delete(@RequestBody Map<String, Long> params) {
        Long id = params.get("id");
        skuService.deleteSku(id);
        return Result.success("删除成功");
    }

    /**
     * 根据商品ID删除所有SKU
     * @param params 包含productId的参数
     * @return 操作结果
     */
    @PostMapping("/deleteByProductId")
    public Result<String> deleteByProductId(@RequestBody Map<String, Long> params) {
        Long productId = params.get("productId");
        skuService.deleteSkusByProductId(productId);
        return Result.success("删除成功");
    }

    /**
     * 库存预警列表
     * @return 库存不足的SKU列表
     */
    @GetMapping("/admin/stockWarning")
    public Result<List<PmsSkuVo>> stockWarning() {
        return Result.success(skuService.getStockWarningList());
    }
}
