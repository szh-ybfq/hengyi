package com.zh.hengyi.admin.controller.user.pay;
import com.zh.hengyi.admin.model.dto.pay.PayCallbackDTO;
import com.zh.hengyi.admin.model.dto.pay.PayCreateDTO;
import com.zh.hengyi.admin.model.vo.pay.PayRecordVO;
import com.zh.hengyi.admin.service.pay.PayRecordService;
import com.zh.hengyi.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user/api/v1/pay")
@Tag(name = "用户支付模块")
@RequiredArgsConstructor
public class UserPayController {

    private final PayRecordService payRecordService;

    @PostMapping("/create")
    @Operation(summary = "创建支付单，跳转支付页面")
    public Result<PayRecordVO> createPay(@Valid @RequestBody PayCreateDTO dto) {
        return Result.success(payRecordService.createPayRecord(dto));
    }

    @PostMapping("/callback")
    @Operation(summary = "模拟支付回调接口（第三方支付调用）")
    public Result<Void> payCallback(@Valid @RequestBody PayCallbackDTO dto) {
        payRecordService.payCallback(dto);
        return Result.success();
    }

    @GetMapping("/order/{orderId}")
    @Operation(summary = "根据订单查询支付记录")
    public Result<PayRecordVO> getPayRecord(@PathVariable Long orderId) {
        return Result.success(payRecordService.getPayByOrderId(orderId));
    }
}
