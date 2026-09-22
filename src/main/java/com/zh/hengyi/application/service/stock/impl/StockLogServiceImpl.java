package com.zh.hengyi.application.service.stock.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zh.hengyi.application.mapper.stock.StockLogMapper;
import com.zh.hengyi.application.model.entity.stock.StockLog;
import com.zh.hengyi.application.service.stock.StockLogService;
import org.springframework.stereotype.Service;

/**
* @author HENGGE
* @description 针对表【stock_log(库存变更流水表)】的数据库操作Service实现
* @createDate 2026-08-14 09:37:47
*/
@Service
public class StockLogServiceImpl extends ServiceImpl<StockLogMapper, StockLog> implements StockLogService {

}




