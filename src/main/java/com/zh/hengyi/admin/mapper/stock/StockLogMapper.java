package com.zh.hengyi.admin.mapper.stock;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zh.hengyi.admin.model.entity.stock.StockLog;
import org.apache.ibatis.annotations.Mapper;

/**
* @author HENGGE
* @description 针对表【stock_log(库存变更流水表)】的数据库操作Mapper
* @createDate 2026-08-14 09:37:47
* @Entity generator.domain.StockLog
*/
@Mapper
public interface StockLogMapper extends BaseMapper<StockLog> {

}




