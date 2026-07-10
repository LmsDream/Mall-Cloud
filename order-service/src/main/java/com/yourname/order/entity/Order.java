package com.yourname.order.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Date;

@Data
@TableName("orders")
public class Order implements Serializable {

    @TableId(type = IdType.AUTO)
    private Long id;
    private Long productId;
    private Integer quantity;
    private BigDecimal totalPrice;
    private String Status;
    private Date createTime;



}
