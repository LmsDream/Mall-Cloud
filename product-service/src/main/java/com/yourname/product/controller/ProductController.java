package com.yourname.product.controller;

import com.yourname.product.entity.Product;
import com.yourname.product.service.ProductService;
import org.apache.shiro.authz.annotation.RequiresAuthentication;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/list")
    @RequiresAuthentication  //必须登录才能查商品信息
    public List<Product> list(){
        return productService.findAll();
    }

    @GetMapping("/get/{id}")
    public Product get(@PathVariable Long id){
        return productService.findById(id);
    }

    @PutMapping("/deduct/{id}/{quantity}")
    public String deduct(@PathVariable Long id,@PathVariable Integer quantity){
        boolean success = productService.deductStock(id, quantity);
        return success ? "扣库存成功" : "扣库存失败";
    }


}
