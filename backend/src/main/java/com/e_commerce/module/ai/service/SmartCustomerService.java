package com.e_commerce.module.ai.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.e_commerce.module.oms.entity.OmsAfterSale;
import com.e_commerce.module.oms.entity.OmsLogistics;
import com.e_commerce.module.oms.entity.OmsLogisticsTrace;
import com.e_commerce.module.oms.entity.OmsOrder;
import com.e_commerce.module.oms.mapper.OmsAfterSaleMapper;
import com.e_commerce.module.oms.mapper.OmsLogisticsMapper;
import com.e_commerce.module.oms.mapper.OmsLogisticsTraceMapper;
import com.e_commerce.module.oms.mapper.OmsOrderMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Service
public class SmartCustomerService {

    @Autowired
    private KnowledgeBaseRagService ragService;

    @Autowired(required = false)
    private OmsOrderMapper orderMapper;

    @Autowired(required = false)
    private OmsLogisticsMapper logisticsMapper;

    @Autowired(required = false)
    private OmsLogisticsTraceMapper traceMapper;

    @Autowired(required = false)
    private OmsAfterSaleMapper afterSaleMapper;

    @Autowired
    private ChatClient chatClient;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public String handleCustomerMessage(String userMessage) {
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return "您好！请问有什么可以帮您的？";
        }

        String message = userMessage.trim();

        // 1. 订单查询——从真实数据库查询
        String orderResult = tryQueryOrder(message);
        if (orderResult != null) {
            return orderResult;
        }

        // 2. 物流查询——从真实数据库查询
        String logisticsResult = tryQueryLogistics(message);
        if (logisticsResult != null) {
            return logisticsResult;
        }

        // 3. 售后记录查询——从真实数据库查询
        String afterSaleResult = tryQueryAfterSale(message);
        if (afterSaleResult != null) {
            return afterSaleResult;
        }

        // 4. RAG 知识库检索——语义匹配 + AI 生成
        String ragResult = tryRagAnswer(message);
        if (ragResult != null) {
            return ragResult;
        }

        // 5. AI 兜底（RAG 增强）
        return tryAiAnswer(message);
    }

    // ==================== 真实数据查询 ====================

    private String tryQueryOrder(String message) {
        if (orderMapper == null) return null;
        if (!containsOrderIntent(message)) return null;

        String orderSn = extractOrderSn(message);
        if (orderSn == null) {
            return null; // 没有订单号，不是具体查询，交给后续流程处理
        }

        try {
            OmsOrder order = orderMapper.selectOne(
                    new LambdaQueryWrapper<OmsOrder>().eq(OmsOrder::getOrderSn, orderSn));
            if (order == null) {
                return "未找到订单编号为【" + orderSn + "】的订单，请检查订单号是否正确。";
            }
            return buildOrderInfo(order);
        } catch (Exception e) {
            log.error("查询订单失败: {}", e.getMessage());
            return "查询订单时出现异常，请稍后再试。";
        }
    }

    private String tryQueryLogistics(String message) {
        if (logisticsMapper == null) return null;
        if (!containsLogisticsIntent(message)) return null;

        String orderSn = extractOrderSn(message);
        String deliveryNo = extractDeliveryNo(message);

        try {
            OmsLogistics logistics = null;
            if (orderSn != null) {
                logistics = logisticsMapper.selectOne(
                        new LambdaQueryWrapper<OmsLogistics>().eq(OmsLogistics::getOrderSn, orderSn));
            } else if (deliveryNo != null) {
                logistics = logisticsMapper.selectOne(
                        new LambdaQueryWrapper<OmsLogistics>().eq(OmsLogistics::getDeliveryNo, deliveryNo));
            }

            if (logistics == null) {
                return null; // 无具体单号，交给AI回答
            }
            return buildLogisticsInfo(logistics);
        } catch (Exception e) {
            log.error("查询物流失败: {}", e.getMessage());
            return "查询物流时出现异常，请稍后再试。";
        }
    }

    private String tryQueryAfterSale(String message) {
        if (afterSaleMapper == null) return null;
        if (!containsAfterSaleIntent(message)) return null;

        String orderSn = extractOrderSn(message);
        if (orderSn == null) {
            return null; // 无具体订单号，交给AI回答
        }

        try {
            List<OmsAfterSale> afterSales = afterSaleMapper.selectList(
                    new QueryWrapper<OmsAfterSale>().eq("order_sn", orderSn).orderByDesc("create_time"));
            if (afterSales == null || afterSales.isEmpty()) {
                return "订单【" + orderSn + "】暂无售后记录。";
            }
            return buildAfterSaleInfo(afterSales);
        } catch (Exception e) {
            log.error("查询售后失败: {}", e.getMessage());
            return "查询售后时出现异常，请稍后再试。";
        }
    }

    // ==================== RAG 知识库检索 ====================

    private String tryRagAnswer(String message) {
        try {
            // 1. 向量检索相关知识库文档
            List<Document> relevantDocs = ragService.searchRelevant(message, 3);
            if (relevantDocs.isEmpty()) {
                return null;
            }

            // 2. 拼接上下文
            String context = ragService.buildContextFromDocs(relevantDocs);

            // 3. 用 AI 基于知识库内容生成回答（而非直接粘贴原文）
            String prompt = buildRagPrompt(context, message);
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.warn("RAG检索失败: {}", e.getMessage());
            return null;
        }
    }

    private String tryAiAnswer(String message) {
        try {
            // 尝试用 RAG 增强 prompt
            String enriched = ragService.enrichPrompt(message);
            String prompt;
            if (enriched != null) {
                prompt = enriched;
            } else {
                prompt = buildBasePrompt(message);
            }
            return chatClient.prompt().user(prompt).call().content();
        } catch (Exception e) {
            log.warn("AI服务调用失败: {}", e.getMessage());
            return buildFallbackResponse(message);
        }
    }

    // ==================== 意图识别 ====================

    private boolean containsOrderIntent(String message) {
        String[] keywords = {"订单", "购买记录", "下单", "订单号", "订单状态", "查订单", "我的订单"};
        for (String kw : keywords) {
            if (message.contains(kw)) return true;
        }
        return false;
    }

    private boolean containsLogisticsIntent(String message) {
        String[] keywords = {"物流", "快递", "发货", "运单号", "跟踪", "配送", "签收", "到哪了", "查物流"};
        for (String kw : keywords) {
            if (message.contains(kw)) return true;
        }
        return false;
    }

    private boolean containsAfterSaleIntent(String message) {
        String[] keywords = {"售后状态", "退款进度", "售后进度", "退款状态", "售后单", "退款单", "售后记录", "查售后"};
        for (String kw : keywords) {
            if (message.contains(kw)) return true;
        }
        return false;
    }

    // ==================== 提取信息 ====================

    private String extractOrderSn(String message) {
        Pattern p = Pattern.compile("ORD\\d{12,20}");
        Matcher m = p.matcher(message);
        if (m.find()) return m.group();

        p = Pattern.compile("\\d{14,20}");
        m = p.matcher(message);
        if (m.find()) return "ORD" + m.group();
        return null;
    }

    private String extractDeliveryNo(String message) {
        Pattern p = Pattern.compile("[A-Za-z]{2,}[0-9]{8,20}");
        Matcher m = p.matcher(message);
        if (m.find()) return m.group();
        return null;
    }

    // ==================== 构建信息（真实项目数据） ====================

    private String buildOrderInfo(OmsOrder order) {
        StringBuilder sb = new StringBuilder();
        sb.append("📦 订单查询结果\n━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("订单编号：").append(order.getOrderSn()).append("\n");
        sb.append("订单状态：").append(getOrderStatusText(order.getStatus())).append("\n");
        sb.append("订单金额：").append(order.getTotalAmount()).append(" 元\n");
        sb.append("收货地址：").append(order.getAddress()).append("\n");
        sb.append("收货人：").append(order.getReceiver()).append(" ").append(order.getPhone()).append("\n");
        sb.append("下单时间：").append(order.getCreateTime() != null ? order.getCreateTime().format(DATE_FORMATTER) : "未知").append("\n");
        if (order.getPayTime() != null) sb.append("支付时间：").append(order.getPayTime().format(DATE_FORMATTER)).append("\n");
        if (order.getDeliveryTime() != null) sb.append("发货时间：").append(order.getDeliveryTime().format(DATE_FORMATTER)).append("\n");
        if (order.getConfirmTime() != null) sb.append("确认收货：").append(order.getConfirmTime().format(DATE_FORMATTER)).append("\n");
        sb.append("\n").append(getOrderActionHint(order.getStatus()));
        return sb.toString();
    }

    private String buildLogisticsInfo(OmsLogistics logistics) {
        StringBuilder sb = new StringBuilder();
        sb.append("🚚 物流查询结果\n━━━━━━━━━━━━━━━━━━━━\n");
        sb.append("物流公司：").append(logistics.getDeliveryCompany()).append("\n");
        sb.append("运单号：").append(logistics.getDeliveryNo()).append("\n");
        sb.append("订单编号：").append(logistics.getOrderSn()).append("\n");
        sb.append("收件人：").append(logistics.getReceiver()).append("\n");
        sb.append("收件地址：").append(logistics.getAddress()).append("\n");

        if (traceMapper != null) {
            List<OmsLogisticsTrace> traces = traceMapper.selectList(
                    new LambdaQueryWrapper<OmsLogisticsTrace>()
                            .eq(OmsLogisticsTrace::getLogisticsId, logistics.getId())
                            .orderByDesc(OmsLogisticsTrace::getCreateTime));
            if (traces != null && !traces.isEmpty()) {
                sb.append("\n📋 物流轨迹：\n");
                for (int i = traces.size() - 1; i >= 0; i--) {
                    OmsLogisticsTrace trace = traces.get(i);
                    sb.append("  ").append(trace.getCreateTime().format(DATE_FORMATTER))
                            .append("  ").append(trace.getContent()).append("\n");
                }
            }
        }
        return sb.toString();
    }

    private String buildAfterSaleInfo(List<OmsAfterSale> afterSales) {
        StringBuilder sb = new StringBuilder();
        sb.append("🔄 售后查询结果\n━━━━━━━━━━━━━━━━━━━━\n");
        for (int i = 0; i < afterSales.size(); i++) {
            OmsAfterSale sale = afterSales.get(i);
            sb.append("售后单号：").append(sale.getAfterSaleSn()).append("\n");
            sb.append("售后类型：").append(sale.getType() == 1 ? "仅退款" : "退货退款").append("\n");
            sb.append("退款金额：").append(sale.getRefundAmount()).append(" 元\n");
            sb.append("状态：").append(getAfterSaleStatusText(sale.getStatus())).append("\n");
            sb.append("原因：").append(sale.getReason()).append("\n");
            if (sale.getRejectReason() != null && !sale.getRejectReason().isEmpty()) {
                sb.append("驳回原因：").append(sale.getRejectReason()).append("\n");
            }
            if (i < afterSales.size() - 1) sb.append("\n");
        }
        return sb.toString();
    }

    // ==================== 状态文本 ====================

    private String getOrderStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待付款";
            case 1: return "待发货（已支付）";
            case 2: return "已发货（待收货）";
            case 3: return "已完成";
            case 4: return "已取消";
            default: return "未知状态(" + status + ")";
        }
    }

    private String getAfterSaleStatusText(Integer status) {
        if (status == null) return "未知";
        switch (status) {
            case 0: return "待审核";
            case 1: return "审核通过";
            case 2: return "已退款";
            case 3: return "已驳回";
            case 4: return "用户已退货";
            case 5: return "商家收货完成";
            default: return "未知状态(" + status + ")";
        }
    }

    private String getOrderActionHint(Integer status) {
        if (status == null) return "";
        switch (status) {
            case 0: return "💡 温馨提示：您可以在订单详情页完成支付，超过30分钟未支付将自动取消";
            case 1: return "💡 温馨提示：商家正在备货中，预计1-3个工作日内发货";
            case 2: return "💡 温馨提示：您可以查看物流信息跟踪包裹，签收时请检查商品完好";
            case 3: return "💡 温馨提示：感谢您的购买！如有问题可在7天内申请售后";
            case 4: return "💡 温馨提示：订单已取消，金额将原路退回";
            default: return "";
        }
    }

    // ==================== Prompt 构建 ====================

    /**
     * RAG 增强 Prompt：严格基于知识库内容回答
     */
    private String buildRagPrompt(String context, String userMessage) {
        return """
            你是电商平台"小购商城"的智能客服助手"小购"。

            【重要指令】
            请严格根据以下知识库内容回答用户问题。不要编造知识库中没有的信息。
            如果知识库内容不足以回答用户问题，请诚实告知并建议联系人工客服。

            【知识库参考】
            %s

            用户问题：%s
            请给出专业回答：
            """.formatted(context, userMessage);
    }

    /**
     * 基础 AI Prompt（无知识库上下文时的兜底）
     */
    private String buildBasePrompt(String message) {
        return """
            你是"小购商城"电商平台的智能客服助手"小购"。

            【平台基本信息】
            - 订单编号格式：ORD + 时间戳（如 ORD2024060812000001）
            - 售后单号格式：AF + 时间戳
            - 物流单号格式：OL + 时间戳
            - 客服热线：400-888-8888
            - 客服邮箱：service@xiaogou.com

            【你能做的】
            1. 解答售前售后政策、物流规则、支付方式等通用问题
            2. 引导用户提供订单号以查询具体订单

            【你不能做的】
            1. 不要编造具体的订单信息、物流状态、退款金额等
            2. 不要回答与购物无关的问题
            3. 涉及具体订单/物流/售后查询时，引导用户提供订单号

            用户问题：%s
            请给出你的回答：
            """.formatted(message);
    }

    private String buildFallbackResponse(String message) {
        if (message.contains("你好") || message.contains("您好") || message.contains("在吗")) {
            return "您好！我是小购商城的智能客服小购，很高兴为您服务！\n\n您可以向我咨询：\n• 订单状态查询（请提供订单号）\n• 退换货政策\n• 物流配送规则\n• 售后申请流程\n\n请问有什么可以帮您的？";
        }
        if (message.contains("谢谢") || message.contains("感谢")) {
            return "不客气！很高兴能帮到您，如有其他问题随时找我哦~";
        }
        if (message.contains("再见") || message.contains("拜拜")) {
            return "再见！祝您生活愉快，期待下次为您服务！";
        }
        return "感谢您的咨询！我暂时无法为您提供更详细的解答。如需帮助，请拨打客服热线 400-888-8888 或发送邮件至 service@xiaogou.com。";
    }
}
