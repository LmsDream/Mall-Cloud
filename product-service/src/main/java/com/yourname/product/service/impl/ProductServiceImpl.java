package com.yourname.product.service.impl;

import com.yourname.product.entity.Product;
import com.yourname.product.mapper.ProductMapper;
import com.yourname.product.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Slf4j
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Cacheable(value = "product",key = "'allList'")
    public List<Product> findAll(){
        log.info("正在查询 MySQL 数据库（缓存未命中）...");
        return productMapper.selectList(null);
    }

    @Cacheable(value = "product",key = "#id")
    public Product findById(Long id){
        log.info("正在查询 MySQL 数据库（缓存未命中）...");
        return productMapper.selectById(id);
    }

    //添加扣减库存的接口
    @Transactional //添加事务
    @Caching(evict = {
            //1、清除单个商品的缓存
            @CacheEvict(value = "product",key ="#id"),
            //2、清除商品列表的缓存
            @CacheEvict(value = "product",key = "'allList'")
    })
    public boolean deductStock(Long id,Integer quantity){
        //根据商品id查询商品信息
        Product product = productMapper.selectById(id);
        if (product==null){
            throw new RuntimeException("商品不存在");
        }
        if (product.getStock()<quantity){
            throw new RuntimeException("库存不足！当亲库存："+product.getStock());
        }
        //开始扣减库存
        product.setStock(product.getStock() - quantity);
        int rows = productMapper.updateById(product);
        log.info("扣减库存成功，商品：{}，剩余：{}",product.getName(),product.getStock());
        return rows>0;
    }




}
