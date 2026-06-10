package com.e_commerce.module.ai.dto;

import lombok.Data;

import java.math.BigDecimal;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
/**
 * AI商品文案生成请求DTO
 * 用于接收商品信息，调用大模型生成商品标题、详情页文案、营销话术等
 */
@Data
@Schema(description = "AI商品文案生成请求")
public class GoodsDescRequest {

    // ==================== 【必填核心字段】生成文案的基础信息，必须填写 ====================
    @NotBlank(message = "商品名称不能为空")
    @Schema(description = "商品名称（必填）", example = "iPhone 15 Pro Max 256G 黑色")
    private String productName;

    @NotNull(message = "商品分类不能为空")
    @Schema(description = "商品分类ID（必填）", example = "1001")
    private Long categoryId;

    @Schema(description = "商品分类名称（用于AI生成，建议填写）", example = "手机/智能手机")
    private String categoryName;

    @NotNull(message = "商品价格不能为空")
    @Schema(description = "商品售价（必填）", example = "8999.00")
    private BigDecimal price;

    @Schema(description = "商品原价/市场价（用于突出优惠）", example = "9999.00")
    private BigDecimal originalPrice;

    // ==================== 【可选拓展字段】提升文案质量，按需填写 ====================
    @Schema(description = "商品卖点/核心优势（多卖点用逗号分隔）", example = "A17Pro芯片,钛金属边框,4800万像素,超长续航")
    private String sellingPoints;

    @Schema(description = "商品规格参数（JSON/文本格式）", example = "屏幕:6.7英寸,存储:256G,颜色:黑色,系统:iOS17")
    private String spec;

    @Schema(description = "商品适用人群/场景", example = "商务人士,摄影爱好者,游戏玩家")
    private String targetUser;

    @Schema(description = "商品品牌", example = "Apple")
    private String brand;

    @Schema(description = "商品标签（多标签用逗号分隔）", example = "新品,爆款,旗舰机,5G手机")
    private String tags;

    @Schema(description = "商品库存（用于生成稀缺性文案）", example = "100")
    private Integer stock;

    @Schema(description = "商品主图URL（用于AI视觉理解，可选）", example = "https://xxx.oss-cn-hangzhou.aliyuncs.com/product/xxx.jpg")
    private String pic;

    // ==================== 【生成需求控制字段】自定义文案风格和类型 ====================
    @Schema(description = "文案类型（可选，默认生成综合文案）", example = "TITLE/DETAIL/MARKETING/SEO",
            allowableValues = {"TITLE", "DETAIL", "MARKETING", "SEO", "ALL"})
    private String descType;

    @Schema(description = "文案风格（可选）", example = "专业/活泼/简洁/高端/接地气",
            allowableValues = {"专业", "活泼", "简洁", "高端", "接地气", "种草"})
    private String style;

    @Schema(description = "目标平台（用于适配不同平台文案）", example = "淘宝/抖音/小红书/朋友圈")
    private String platform;

    @Schema(description = "额外需求/自定义提示词（可选，用于个性化生成）", example = "突出性价比,适合学生党,强调拍照功能")
    private String extraRequirement;
}