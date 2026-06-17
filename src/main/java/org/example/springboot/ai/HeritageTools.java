package org.example.springboot.ai;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.example.springboot.entity.*;
import org.example.springboot.mapper.*;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.example.springboot.service.ActivitySignupService;
import org.example.springboot.dto.command.ActivitySignupCreateCommandDTO;

/**
 * 非遗智能助手工具函数
 * 
 * 职责：定义AI可以调用的外部工具，用于查询非遗相关数据
 * 
 * @author system
 */
@Slf4j
@Component
public class HeritageTools {

    @Resource
    private HeritageItemMapper heritageItemMapper;

    @Resource
    private InheritorMapper inheritorMapper;

    @Resource
    private ActivityMapper activityMapper;

    @Resource
    private CourseMapper courseMapper;

    @Resource
    private ShopProductMapper shopProductMapper;

    @Resource
    private ShopOrderMapper shopOrderMapper;

    @Resource
    private ActivitySignupService activitySignupService;


    // ==================== 非遗项目查询工具 ====================

    /**
     * 搜索非遗项目
     *
     * @param keyword 搜索关键词
     * @return 非遗项目列表
     */
    @Tool(
            name = "searchHeritageItems",
            description = "搜索非遗项目（如苏绣、蜀绣、皮影戏、景德镇瓷器等），支持按名称模糊搜索。" +
                    "当用户询问'有哪些非遗项目'、'XX非遗'、'查找XX'等问题时使用此工具。",
            returnDirect = false
    )
    public String searchHeritageItems(
            @ToolParam(description = "搜索关键词，如'刺绣'、'陶瓷'、'皮影'等") String keyword
    ) {
        try {
            log.info("AI工具调用: 搜索非遗项目, keyword={}", keyword);

            LambdaQueryWrapper<HeritageItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(HeritageItem::getTitle, keyword)
                    .or().like(HeritageItem::getDescription, keyword)
                    .eq(HeritageItem::getStatus, 1)  // 只查询已审核的
                    .orderByDesc(HeritageItem::getCreateTime)
                    .last("LIMIT 10");  // 限制返回数量

            List<HeritageItem> items = heritageItemMapper.selectList(wrapper);

            if (items.isEmpty()) {
                return String.format("未找到包含'%s'的非遗项目，您可以尝试其他关键词。", keyword);
            }

            StringBuilder result = new StringBuilder(String.format("找到 %d 个相关非遗项目：\n\n", items.size()));
            for (HeritageItem item : items) {
                result.append(String.format("**%s**\n", item.getTitle()));
                result.append(String.format("📂 类别：%s\n", item.getCategory()));
                result.append(String.format("📍 地域：%s\n", item.getRegion()));
                result.append(String.format("🔗 [查看详情](route://heritage?id=%s)\n", item.getId()));
                if (item.getDescription() != null && !item.getDescription().isEmpty()) {
                    String desc = item.getDescription().length() > 100
                            ? item.getDescription().substring(0, 100) + "..."
                            : item.getDescription();
                    result.append(String.format("📝 简介：%s\n", desc));
                }
                result.append("\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索非遗项目失败: {}", e.getMessage(), e);
            return "查询非遗项目时发生错误，请稍后重试。";
        }
    }

    /**
     * 按类别查询非遗项目
     *
     * @param category 非遗类别关键词
     * @return 非遗项目列表
     */
    @Tool(
            name = "getHeritageItemsByCategory",
            description = "按类别查询非遗项目。支持按类别关键词搜索，如'手工艺'、'表演艺术'、'民俗'、'刺绣'、'陶瓷'等。" +
                    "当用户询问'有哪些手工艺'、'刺绣类非遗'等问题时使用此工具。",
            returnDirect = false
    )
    public String getHeritageItemsByCategory(
            @ToolParam(description = "类别关键词，如'手工艺'、'刺绣'、'陶瓷'等") String category
    ) {
        try {
            log.info("AI工具调用: 按类别查询非遗项目, category={}", category);

            LambdaQueryWrapper<HeritageItem> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(HeritageItem::getCategory, category)
                    .eq(HeritageItem::getStatus, 2)  // 只查询已发布的
                    .orderByDesc(HeritageItem::getCreateTime)
                    .last("LIMIT 10");

            List<HeritageItem> items = heritageItemMapper.selectList(wrapper);

            if (items.isEmpty()) {
                return String.format("暂无'%s'类别的非遗项目。", category);
            }

            StringBuilder result = new StringBuilder(String.format("**%s**类别的非遗项目：\n\n", category));
            for (HeritageItem item : items) {
                result.append(String.format("- **%s**（📍 %s）\n", item.getTitle(), item.getRegion()));
                result.append(String.format("  🔗 [查看详情](route://heritage?id=%s)\n", item.getId()));
            }

            return result.toString();

        } catch (Exception e) {
            log.error("按类别查询非遗项目失败: {}", e.getMessage(), e);
            return "查询非遗项目时发生错误，请稍后重试。";
        }
    }

    // ==================== 传承人查询工具 ====================

    /**
     * 搜索非遗传承人
     *
     * @param keyword 搜索关键词
     * @return 传承人列表
     */
    @Tool(
            name = "searchInheritors",
            description = "搜索非遗传承人信息，支持按姓名、称号、地域搜索。" +
                    "当用户询问'XX传承人'、'有哪些大师'、'传承人信息'等问题时使用此工具。",
            returnDirect = false
    )
    public String searchInheritors(
            @ToolParam(description = "搜索关键词，如传承人姓名、称号或地域") String keyword
    ) {
        try {
            log.info("AI工具调用: 搜索传承人, keyword={}", keyword);

            LambdaQueryWrapper<Inheritor> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(Inheritor::getName, keyword)
                    .or().like(Inheritor::getTitle, keyword)
                    .or().like(Inheritor::getRegion, keyword)
                    .orderByDesc(Inheritor::getCreateTime)
                    .last("LIMIT 10");

            List<Inheritor> inheritors = inheritorMapper.selectList(wrapper);

            if (inheritors.isEmpty()) {
                return String.format("未找到包含'%s'的传承人信息。", keyword);
            }

            StringBuilder result = new StringBuilder(String.format("找到 %d 位传承人：\n\n", inheritors.size()));
            for (Inheritor inheritor : inheritors) {
                result.append(String.format("**%s**\n", inheritor.getName()));
                if (inheritor.getTitle() != null && !inheritor.getTitle().isEmpty()) {
                    result.append(String.format("🏆 称号：%s\n", inheritor.getTitle()));
                }
                if (inheritor.getRegion() != null && !inheritor.getRegion().isEmpty()) {
                    result.append(String.format("📍 地域：%s\n", inheritor.getRegion()));
                }
                if (inheritor.getBio() != null && !inheritor.getBio().isEmpty()) {
                    String bio = inheritor.getBio().length() > 80
                            ? inheritor.getBio().substring(0, 80) + "..."
                            : inheritor.getBio();
                    result.append(String.format("- 简介：%s\n", bio));
                }
                result.append(String.format("🔗 [查看详情](route://inheritor?id=%s)\n", inheritor.getId()));
                result.append("\n---\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索传承人失败: {}", e.getMessage(), e);
            return "查询传承人信息时发生错误，请稍后重试。";
        }
    }

    // ==================== 活动查询工具 ====================

    /**
     * 查询正在进行的活动
     *
     * @return 活动列表
     */
    @Tool(
            name = "getOngoingActivities",
            description = "查询非遗活动，支持按状态筛选。不指定活动类型时默认查询报名中的活动。" +
                    "当用户询问'有什么活动'、'最近的活动'、'可以参加的活动'等问题时使用此工具。",
            returnDirect = false
    )
    public String getOngoingActivities(
            @ToolParam(description = "活动状态：1=报名中 2=进行中（可选，不指定则默认查询报名中的活动）", required = false) Optional<Integer> status
    ) {
        try {
            log.info("AI工具调用: 查询活动, status={}", (status != null && status.isPresent()) ? status.get() : "默认(报名中)");

            LocalDateTime now = LocalDateTime.now();
            int targetStatus = (status != null && status.isPresent()) ? status.get() : 1;
            LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(Activity::getStatus, targetStatus)
                    .orderByAsc(Activity::getStartTime)
                    .last("LIMIT 10");

            List<Activity> activities = activityMapper.selectList(wrapper);

            log.info("查询结果数量: {}", activities.size());
            if (!activities.isEmpty()) {
                log.info("活动列表: {}", activities.stream().map(a -> a.getId() + "-" + a.getTitle() + "-status:" + a.getStatus()).collect(Collectors.joining(", ")));
            }

            if (activities.isEmpty()) {
                String statusName = targetStatus == 1 ? "报名中" : "进行中";
                return String.format("当前暂无%s的活动，请关注后续更新。", statusName);
            }

            String statusName = targetStatus == 1 ? "报名中" : "进行中";
            StringBuilder result = new StringBuilder(String.format("**%s的活动：**\n\n", statusName));
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

            for (Activity activity : activities) {
                result.append(String.format("**%s**\n", activity.getTitle()));
                result.append(String.format("⏰ 时间：%s 至 %s\n",
                        activity.getStartTime().format(formatter),
                        activity.getEndTime().format(formatter)));
                result.append(String.format("📌 地点：%s\n", activity.getLocation()));

                // 判断活动状态
                if (activity.getStartTime().isAfter(now)) {
                    result.append("⏳ 状态：即将开始\n");
                } else {
                    result.append("✅ 状态：进行中\n");
                }

                if (activity.getDescription() != null) {
                    String desc = activity.getDescription().length() > 80
                            ? activity.getDescription().substring(0, 80) + "..."
                            : activity.getDescription();
                    result.append(String.format("📝 简介：%s\n", desc));
                }
                result.append(String.format("🔗 [查看详情](route://activity?id=%s)\n", activity.getId()));
                result.append(String.format("📋 [立即报名](action://signup?id=%s)\n", activity.getId()));
                result.append("\n---\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("查询活动失败: {}", e.getMessage(), e);
            return "查询活动信息时发生错误，请稍后重试。";
        }
    }

    // ==================== 课程查询工具 ====================

    /**
     * 搜索课程
     *
     * @param keyword 搜索关键词
     * @return 课程列表
     */
    @Tool(
            name = "searchCourses",
            description = "搜索非遗课程，支持按课程名称、技艺类型搜索。" +
                    "当用户询问'有什么课程'、'学习XX'、'课程推荐'等问题时使用此工具。",
            returnDirect = false
    )
    public String searchCourses(
            @ToolParam(description = "搜索关键词，如课程名称或技艺类型") String keyword
    ) {
        try {
            log.info("AI工具调用: 搜索课程, keyword={}", keyword);

            LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(Course::getTitle, keyword)
                    .or().like(Course::getDescription, keyword)
                    .eq(Course::getStatus, 1)
                    .orderByDesc(Course::getCreateTime)
                    .last("LIMIT 10");

            List<Course> courses = courseMapper.selectList(wrapper);

            if (courses.isEmpty()) {
                return String.format("未找到包含'%s'的课程。", keyword);
            }

            StringBuilder result = new StringBuilder(String.format("找到 %d 个相关课程：\n\n", courses.size()));
            for (Course course : courses) {
                result.append(String.format("**%s**\n", course.getTitle()));
                if (course.getLevel() != null && !course.getLevel().isEmpty()) {
                    result.append(String.format("📊 难度：%s\n", course.getLevel()));
                }
                if (course.getDescription() != null) {
                    String desc = course.getDescription().length() > 80
                            ? course.getDescription().substring(0, 80) + "..."
                            : course.getDescription();
                    result.append(String.format("📝 简介：%s\n", desc));
                }
                result.append(String.format("🔗 [查看详情](route://course?id=%s)\n", course.getId()));
                result.append("\n---\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索课程失败: {}", e.getMessage(), e);
            return "查询课程信息时发生错误，请稍后重试。";
        }
    }

    // ==================== 商品查询工具 ====================

    /**
     * 搜索非遗手办商品
     *
     * @param keyword 搜索关键词
     * @return 商品列表
     */
    @Tool(
            name = "searchProducts",
            description = "搜索非遗手办商品，支持按商品名称搜索。" +
                    "当用户询问'有什么商品'、'想买XX'、'手办推荐'等问题时使用此工具。",
            returnDirect = false
    )
    public String searchProducts(
            @ToolParam(description = "搜索关键词，如商品名称或类型") String keyword
    ) {
        try {
            log.info("AI工具调用: 搜索商品, keyword={}", keyword);

            LambdaQueryWrapper<ShopProduct> wrapper = new LambdaQueryWrapper<>();
            wrapper.like(ShopProduct::getTitle, keyword)
                    .or().like(ShopProduct::getDetail, keyword)
                    .eq(ShopProduct::getStatus, 1)
                    .orderByDesc(ShopProduct::getCreateTime)
                    .last("LIMIT 10");

            List<ShopProduct> products = shopProductMapper.selectList(wrapper);

            if (products.isEmpty()) {
                return String.format("未找到包含'%s'的商品。", keyword);
            }

            StringBuilder result = new StringBuilder(String.format("找到 %d 个相关商品：\n\n", products.size()));
            for (ShopProduct product : products) {
                result.append(String.format("**%s**\n", product.getTitle()));
                result.append(String.format("💰 价格：¥%.2f\n", product.getPrice()));
                if (product.getStock() != null) {
                    result.append(String.format("📦 库存：%d\n", product.getStock()));
                }
                if (product.getDetail() != null) {
                    String detail = product.getDetail().length() > 80
                            ? product.getDetail().substring(0, 80) + "..."
                            : product.getDetail();
                    result.append(String.format("📝 详情：%s\n", detail));
                }
                result.append(String.format("🔗 [查看详情](route://shop?id=%s)\n", product.getId()));
                result.append("\n---\n");
            }

            return result.toString();

        } catch (Exception e) {
            log.error("搜索商品失败: {}", e.getMessage(), e);
            return "查询商品信息时发生错误，请稍后重试。";
        }
    }

    /**
     * 查询用户订单状态
     *
     * @param orderNo 订单号（可选）
     * @return 订单信息
     */
    @Tool(
            name = "searchOrderStatus",
            description = "查询订单状态信息，支持按订单号精确查询或查询用户最近的订单列表。" +
                    "当用户询问'我的订单'、'订单状态'、'查看订单XXX'等问题时使用此工具。",
            returnDirect = false
    )
    public String searchOrderStatus(
            @ToolParam(description = "订单号，如果不提供则返回最近5个订单", required = false) Optional<String> orderNo,
            @ToolParam(description = "用户ID，从对话上下文中自动获取", required = false) Optional<Long> userId
    ) {
        try {
            Long currentUserId = userId != null ? userId.orElse(null) : null;

            log.info("AI工具调用: 查询订单状态, userId={}, orderNo={}", currentUserId, orderNo != null ? orderNo.orElse("未指定") : "null");

            if (currentUserId == null) {
                return "⚠️ **请先登录**\n\n查询订单需要先登录账号，请您登录后再试。";
            }

            String orderNoStr = orderNo != null && orderNo.isPresent() ? orderNo.get() : null;

            if (orderNoStr != null && !orderNoStr.isEmpty()) {
                LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ShopOrder::getOrderNo, orderNoStr)
                        .eq(ShopOrder::getUserId, currentUserId)
                        .orderByDesc(ShopOrder::getCreateTime)
                        .last("LIMIT 1");

                List<ShopOrder> orders = shopOrderMapper.selectList(wrapper);

                if (orders.isEmpty()) {
                    return String.format("未找到订单号为'%s'的订单，请检查订单号是否正确，或确认该订单属于当前账号。", orderNoStr);
                }

                ShopOrder order = orders.get(0);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

                StringBuilder result = new StringBuilder("**订单详情**\n\n");
                result.append(String.format("- 订单号：%s\n", order.getOrderNo()));
                result.append(String.format("- 订单状态：**%s**\n", order.getStatusDisplayName()));
                result.append(String.format("- 订单金额：¥%.2f\n", order.getTotalAmount()));
                result.append(String.format("- 实付金额：¥%.2f\n", order.getPayAmount()));
                result.append(String.format("- 创建时间：%s\n", order.getCreateTime().format(formatter)));

                if (order.getPayTime() != null) {
                    result.append(String.format("- 支付时间：%s\n", order.getPayTime().format(formatter)));
                }

                if (order.getPayType() != null && !order.getPayType().isEmpty()) {
                    String payTypeText = switch (order.getPayType()) {
                        case "ALI" -> "支付宝";
                        case "WECHAT" -> "微信支付";
                        default -> order.getPayType();
                    };
                    result.append(String.format("- 支付方式：%s\n", payTypeText));
                }

                if (order.getLogisticsNo() != null && !order.getLogisticsNo().isEmpty()) {
                    result.append(String.format("- 物流单号：%s\n", order.getLogisticsNo()));
                }

                if (order.getRemark() != null && !order.getRemark().isEmpty()) {
                    result.append(String.format("- 备注：%s\n", order.getRemark()));
                }

                result.append("\n💡 **提示**：如需了解订单后续操作建议，可以告诉我您的具体需求。");

                return result.toString();

            } else {
                LambdaQueryWrapper<ShopOrder> wrapper = new LambdaQueryWrapper<>();
                wrapper.eq(ShopOrder::getUserId, currentUserId)
                        .orderByDesc(ShopOrder::getCreateTime);

                List<ShopOrder> orders = shopOrderMapper.selectList(wrapper);

                if (orders.isEmpty()) {
                    return "📦 **暂无订单记录**\n\n您当前还没有任何订单，可以去商城逛逛哦～";
                }

                StringBuilder result = new StringBuilder(String.format("**全部订单（共 %d 条）**\n\n", orders.size()));
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

                for (ShopOrder order : orders) {
                    result.append(String.format("📦 **%s**\n", order.getOrderNo()));
                    result.append(String.format("✅ 状态：**%s**\n", order.getStatusDisplayName()));
                    result.append(String.format("💰 金额：¥%.2f\n", order.getTotalAmount()));
                    result.append(String.format("⏰ 时间：%s\n", order.getCreateTime().format(formatter)));
                    result.append(String.format("  🔗 [查看详情](route://orders)\n"));
                result.append("\n---\n");
                }

                result.append("💡 **提示**：如需查看某个订单的详细信息，请提供订单号。");

                return result.toString();
            }

        } catch (Exception e) {
            log.error("查询订单状态失败: {}", e.getMessage(), e);
            return "查询订单信息时发生错误，请稍后重试。";
        }
    }

    /**
     * 跳转到指定页面
     * 当用户说"带我去看XX"、"跳转到XX"、"查看XX详情"时使用此工具。
     * 返回 route:// 协议指令，前端会自动拦截并执行页面跳转。
     */
    @Tool(
            name = "navigateToPage",
            description = "跳转到指定详情页面。当用户说'带我去看XX'、'跳转到XX页面'时使用此工具。" +
                    "支持跳转类型：heritage(非遗作品详情)、inheritor(传承人详情)、activity(活动详情)、" +
                    "course(课程详情)、shop(商品详情)、orders(订单列表)。如果提供了关键词会先搜索再跳转。",
            returnDirect = false
    )
    public String navigateToPage(
            @ToolParam(description = "页面类型：heritage(非遗作品), inheritor(传承人), activity(活动), course(课程), shop(商品), orders(订单列表)") String pageType,
            @ToolParam(description = "搜索关键词，如作品名称、传承人姓名等（可选）", required = false) Optional<String> keyword,
            @ToolParam(description = "目标ID（可选）", required = false) Optional<Long> targetId
    ) {
        try {
            log.info("AI工具调用: 跳转页面, pageType={}, keyword={}, targetId={}", pageType, keyword.orElse("无"), targetId.orElse(null));

            if (targetId.isPresent()) {
                return String.format("[查看详情](route://%s?id=%d)", pageType, targetId.get());
            }

            if (keyword.isPresent() && !keyword.get().isEmpty()) {
                String kw = keyword.get();
                switch (pageType) {
                    case "heritage": {
                        LambdaQueryWrapper<HeritageItem> wrapper = new LambdaQueryWrapper<>();
                        wrapper.like(HeritageItem::getTitle, kw).eq(HeritageItem::getStatus, 1).last("LIMIT 1");
                        HeritageItem item = heritageItemMapper.selectOne(wrapper);
                        if (item != null)
                            return String.format("找到 [**%s**](route://heritage?id=%s)", item.getTitle(), item.getId());
                        return String.format("未找到名称为'%s'的非遗作品，您可以尝试其他关键词。", kw);
                    }
                    case "inheritor": {
                        LambdaQueryWrapper<Inheritor> wrapper = new LambdaQueryWrapper<>();
                        wrapper.like(Inheritor::getName, kw).last("LIMIT 1");
                        Inheritor inheritor = inheritorMapper.selectOne(wrapper);
                        if (inheritor != null)
                            return String.format("找到 [**%s**](route://inheritor?id=%s)", inheritor.getName(), inheritor.getId());
                        return String.format("未找到名称为'%s'的传承人。", kw);
                    }
                    case "activity": {
                        LambdaQueryWrapper<Activity> wrapper = new LambdaQueryWrapper<>();
                        wrapper.like(Activity::getTitle, kw).last("LIMIT 1");
                        Activity activity = activityMapper.selectOne(wrapper);
                        if (activity != null)
                            return String.format("找到 [**%s**](route://activity?id=%s)", activity.getTitle(), activity.getId());
                        return String.format("未找到名称为'%s'的活动。", kw);
                    }
                    case "course": {
                        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
                        wrapper.like(Course::getTitle, kw).eq(Course::getStatus, 1).last("LIMIT 1");
                        Course course = courseMapper.selectOne(wrapper);
                        if (course != null)
                            return String.format("找到 [**%s**](route://course?id=%s)", course.getTitle(), course.getId());
                        return String.format("未找到名称为'%s'的课程。", kw);
                    }
                    case "shop": {
                        LambdaQueryWrapper<ShopProduct> wrapper = new LambdaQueryWrapper<>();
                        wrapper.like(ShopProduct::getTitle, kw).eq(ShopProduct::getStatus, 1).last("LIMIT 1");
                        ShopProduct product = shopProductMapper.selectOne(wrapper);
                        if (product != null)
                            return String.format("找到 [**%s**](route://shop?id=%s)", product.getTitle(), product.getId());
                        return String.format("未找到名称为'%s'的商品。", kw);
                    }
                    case "orders": {
                        return "[查看我的订单](route://orders)";
                    }
                    default:
                        return String.format("未知页面类型'%s'，支持：heritage、inheritor、activity、course、shop、orders", pageType);
                }
            }

            // 没有关键词和ID，跳转到列表页
            return String.format("[查看详情](route://%s)", pageType);

        } catch (Exception e) {
            log.error("跳转页面失败: {}", e.getMessage(), e);
            return "跳转页面时发生错误，请稍后重试。";
        }
    }

    /**
     * 用户报名活动
     * 当用户说"我要报名活动"、"帮我报名XX活动"、"参加活动"时使用此工具。
     * 需要用户已登录，报名前会检查活动是否存在以及是否可报名。
     */
    @Tool(
        name = "signUpForActivity",
        description = "报名参加活动。当用户说'我要报名活动'、'帮我报名XX'、'参加活动'等问题时使用此工具。",
        returnDirect = false
    )
    public String signUpForActivity(
            @ToolParam(description = "活动ID") String activityId,
            @ToolParam(description = "用户ID，从对话上下文中自动获取", required = false) Optional<Long> userId
    ) {
        try {
            Long currentUserId = userId != null ? userId.orElse(null) : null;

            log.info("AI工具调用: 报名活动, activityId={}, userId={}", activityId, currentUserId);

            if (currentUserId == null) {
                return "⚠️ **请先登录**\n\n报名活动需要先登录账号，请您登录后再试。";
            }

            // 检查活动是否存在并可报名
            Activity activity = activityMapper.selectById(activityId);
            if (activity == null) {
                return String.format("未找到ID为'%s'的活动，请确认活动ID是否正确。", activityId);
            }

            if (!activity.canSignup()) {
                return String.format("活动'%s'当前状态(%s)无法报名，仅报名中的活动才能报名。",
                        activity.getTitle(), activity.getStatusDisplayName());
            }

            // 调用业务层报名
            ActivitySignupCreateCommandDTO createDTO = new ActivitySignupCreateCommandDTO();
            createDTO.setActivityId(activityId);
            createDTO.setUserId(currentUserId);
            activitySignupService.createSignup(createDTO);

            log.info("活动报名成功: activityId={}, userId={}", activityId, currentUserId);

            StringBuilder result = new StringBuilder();
            result.append(String.format("✅ **报名成功！**\n\n"));
            result.append(String.format("您已成功报名活动 **%s**。\n\n", activity.getTitle()));
            result.append(String.format("- [查看活动详情](route://activity?id=%s)\n", activityId));
            result.append("\n💡 请留意活动通知，按时参加活动哦～");

            return result.toString();

        } catch (Exception e) {
            log.error("报名活动失败: {}", e.getMessage(), e);
            return String.format("报名活动失败：%s\n\n您也可以自行前往活动详情页查看是否满足报名条件：[查看详情](route://activity?id=%s)",
                    e.getMessage(), activityId);
        }
    }
}