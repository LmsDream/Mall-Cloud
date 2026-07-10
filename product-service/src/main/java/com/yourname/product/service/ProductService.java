package com.yourname.product.service;

import com.yourname.product.entity.Product;

import java.util.List;

public interface ProductService {

    public List<Product> findAll();

    public Product findById(Long id);

    //扣减库存
    public boolean deductStock(Long id,Integer quantity);

}
