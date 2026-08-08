package com.zh.hengyi.common.exception;

import com.zh.hengyi.common.result.Result;
import com.zh.hengyi.common.result.ResultCode;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.transaction.TransactionSystemException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.sql.SQLException;
import java.sql.SQLTransientConnectionException;
import java.util.IllegalFormatException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // 1 自定义业务异常
    @ExceptionHandler(BusinessException.class)
    public Result<?> serviceExceptionHandler(BusinessException e) {
        log.error("[业务异常]", e);
        return Result.error(e.getCode(), e.getMessage());
    }

    // 2 @Valid 参数校验异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<?> validExceptionHandler(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldError().getDefaultMessage();
        log.error("[Valid框架：请求参数错误]", e);
        return Result.error(ResultCode.VALID_PARAM_ERROR.getCode(), message);
    }


    // 4 Mybatis、SQL异常
    @ExceptionHandler(DuplicateKeyException.class)
    public Result<?> handleDuplicateKeyException(DuplicateKeyException e) {
        log.error("[数据库-唯一索引冲突]", e);
        return Result.error(ResultCode.DB_DUPLICATE_KEY.getCode(), ResultCode.DB_DUPLICATE_KEY.getMsg());
    }

    // 4.2 字段约束异常：非空、超长、数值超限 4202
    @ExceptionHandler(DataIntegrityViolationException.class)
    public Result<?> handleFieldConstraintException(DataIntegrityViolationException e) {
        log.error("[数据库-字段约束异常]", e);
        return Result.error(ResultCode.DB_FIELD_CONSTRAINT_ERROR.getCode(), ResultCode.DB_FIELD_CONSTRAINT_ERROR.getMsg());
    }

    // 4.3 外键约束删除失败 4203
    @ExceptionHandler(ConstraintViolationException.class)
    public Result<?> handleForeignKeyException(ConstraintViolationException e) {
        log.error("[数据库-外键约束异常]", e);
        return Result.error(ResultCode.DB_FOREIGN_KEY_ERROR.getCode(), ResultCode.DB_FOREIGN_KEY_ERROR.getMsg());
    }

    // 4.4 数据库连接超时、断开 4205
    @ExceptionHandler(SQLTransientConnectionException.class)
    public Result<?> handleDbConnectException(SQLTransientConnectionException e) {
        log.error("[数据库-连接异常]", e);
        return Result.error(ResultCode.DB_CONNECT_ERROR.getCode(), ResultCode.DB_CONNECT_ERROR.getMsg());
    }

    // 4.5 SQL语法错误 4206
    @ExceptionHandler(BadSqlGrammarException.class)
    public Result<?> handleSqlGrammarException(BadSqlGrammarException e) {
        log.error("[数据库-SQL语法错误]", e);
        return Result.error(ResultCode.DB_SQL_GRAMMAR_ERROR.getCode(), ResultCode.DB_SQL_GRAMMAR_ERROR.getMsg());
    }

    // 4.6 事务异常 4207
    @ExceptionHandler(TransactionSystemException.class)
    public Result<?> handleTransactionException(TransactionSystemException e) {
        log.error("[数据库-事务异常]", e);
        return Result.error(ResultCode.DB_TRANSACTION_ERROR.getCode(), ResultCode.DB_TRANSACTION_ERROR.getMsg());
    }

    // 4.7 兜底所有SQL异常 4299
    @ExceptionHandler(SQLException.class)
    public Result<?> handleSqlException(SQLException e) {
        log.error("[数据库-通用SQL异常]", e);
        return Result.error(ResultCode.DB_COMMON_ERROR.getCode(), ResultCode.DB_COMMON_ERROR.getMsg());
    }

    // 5 系统未知异常
    @ExceptionHandler(Exception.class)
    public Result<?> handleAllException(Exception e) {
        log.error("[系统未知异常]", e);
        return Result.error();
    }
}