package com.zh.hengyi.config.sentinel;

import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static com.alibaba.csp.sentinel.slots.block.RuleConstant.*;

@Configuration
public class SentinelRuleConfig {

    // 资源名称，和@SentinelResource的值保持一致！！
    private static final String SECKILL_SUBMIT_RESOURCE = "seckillSubmitResource";
    private static final String GOODS_QUERY_RESOURCE = "getSeckillGoodsResource";

    /**
     * PostConstruct：Bean初始化完成后自动加载Sentinel全部规则
     */
    @PostConstruct
    public void initSentinelRules(){
        // 1、QPS全局流控规则（普通限流）
        initFlowRules();
        // 2、热点参数限流规则（秒杀核心：按seckillGoodsId热点限流）
        initParamFlowRules();
        // 3、熔断降级规则（服务故障熔断）
        initDegradeRules();
        // 4、系统保护规则（CPU阈值保护）
        initSystemRules();
    }

    /**
     * 1. 普通流控规则：seckillSubmitResource 全局QPS限流，单机QPS阈值=100
     */
    private void initFlowRules(){
        List<FlowRule> flowRules = new ArrayList<>();
        FlowRule flowRule = new FlowRule();
        flowRule.setResource(SECKILL_SUBMIT_RESOURCE);
        flowRule.setGrade(FLOW_GRADE_QPS);
        flowRule.setCount(100); //单机最大QPS=100
        flowRule.setControlBehavior(CONTROL_BEHAVIOR_DEFAULT);
        flowRules.add(flowRule);
        FlowRuleManager.loadRules(flowRules);
    }

    /**
     * 2. 热点参数限流（秒杀爆款商品 seckillGoodsId）
     * 注解@SentinelResource方法入参是 dto，参数索引0；
        * 注意：热点参数限流**只能识别基本类型/包装类，不能直接解析DTO对象内部字段！！
        * 如果方法入参是DTO对象，默认不能直接拿dto.seckillGoodsId做热点参数。
     * 两种解决方案：
        * 方案A：改造接口，把seckillGoodsId单独提出来作为方法参数 ✅️
        * 方案B：自定义参数解析器（复杂）
     */
    private void initParamFlowRules(){
        List<ParamFlowRule> paramRules = new ArrayList<>();
        ParamFlowRule paramFlowRule = new ParamFlowRule();
        paramFlowRule.setResource(SECKILL_SUBMIT_RESOURCE);
        paramFlowRule.setParamIdx(0); // 参数索引0（代表方法第一个参数）
        paramFlowRule.setCount(20); //同一个参数值，每秒最多20请求
        paramFlowRule.setDurationInSec(1); //统计窗口1s
        paramRules.add(paramFlowRule);
        ParamFlowRuleManager.loadRules(paramRules);
    }

    /**
     * 3、熔断降级规则：商品查询资源熔断规则
     * 统计10s内，异常比例>50%，熔断窗口5s，熔断期间直接快速失败
     */
    private void initDegradeRules(){
        List<DegradeRule> degradeRules = new ArrayList<>();
        DegradeRule degradeRule = new DegradeRule();
        degradeRule.setResource(GOODS_QUERY_RESOURCE);
        degradeRule.setGrade(DEGRADE_GRADE_EXCEPTION_RATIO);// 熔断策略：异常比例
        degradeRule.setCount(0.5); //异常比例阈值
        degradeRule.setTimeWindow(5); //熔断窗口，单位秒。熔断打开后5s内直接拒绝
        degradeRule.setStatIntervalMs(10 * 1000); //统计时长10s
        degradeRules.add(degradeRule);
        DegradeRuleManager.loadRules(degradeRules);
    }

    /**
     * 4、系统保护规则：CPU使用率超过80%，限流所有入口请求
     */
    private void initSystemRules(){
        List<SystemRule> systemRules = new ArrayList<>();
        SystemRule systemRule = new SystemRule();
        systemRule.setHighestCpuUsage(0.8); //CPU阈值0.8=80%
        systemRules.add(systemRule);
        SystemRuleManager.loadRules(systemRules);
    }
}

