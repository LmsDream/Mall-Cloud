package com.yourname.gateway.filter;

import com.yourname.gateway.utils.JwtUtils;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Arrays;
import java.util.List;

@Component
public class JwtGlobalFilter implements GlobalFilter, Ordered {

    // 白名单路径（不需要 Token）
    private static final List<String> WHITELIST = Arrays.asList(
            "/api/auth/login",
            "/api/product/get/**",
            "/api/product/deduct/**"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        System.out.println("=== Gateway JwtGlobalFilter 被调用了 ===");
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();
        String path = request.getPath().value();
        System.out.println("=== Gateway 拦截路径: " + path);
        // 1. 白名单放行
        for (String pattern : WHITELIST) {
            if (path.startsWith(pattern)) {
                System.out.println("=== 白名单放行: " + path);
                return chain.filter(exchange);
            }
        }

        // 2. 取 Token
        String authHeader = request.getHeaders().getFirst("Authorization");
        System.out.println("=== Authorization Header: " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println("=== 未携带 Token，返回 401");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }

        String token = authHeader.substring(7);
        String username = JwtUtils.getUsername(token);

        if (username == null) {
            System.out.println("=== Token 无效或已过期，返回 401");
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            return response.setComplete();
        }
        System.out.println("=== Token 验证通过，用户名: " + username);
        // 3. 验证通过，把用户名透传给下游服务
        ServerHttpRequest mutatedRequest = request.mutate()
                .header("X-Username", username)
                .build();

        return chain.filter(exchange.mutate().request(mutatedRequest).build());
    }

    @Override
    public int getOrder() {
        return -1;  // 优先级最高
    }
}