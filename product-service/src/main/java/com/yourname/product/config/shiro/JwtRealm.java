package com.yourname.product.config.shiro;

import com.yourname.product.utils.JwtUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;

public class JwtRealm extends AuthorizingRealm {

    //必须重新这个，让Shiro知道如何匹配token
    public boolean supports(AuthenticationToken  token){
        return token instanceof JwtToken;
    }

    //新增：重写凭证匹配方法
    //因为jwt的签名校验和过期校验已经在doGetAuthorizationInfo中做了,
    //所以这里直接放行，不在比较token和info里的字符串是否相等
    public void assertCredentialsMatch(AuthenticationToken token,AuthenticationInfo info) throws AuthenticationException{
        //什么都不做处理，直接通过
    }


    //授权（先不实现，返回空）
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        return null;
    }

    //认证（验证JWT是否有效）
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
       String jwt = ((JwtToken) token).getToken();
       String username = JwtUtils.getUsername(jwt);
       if (username == null){
            throw new AuthenticationException("Token 无效或已过期");
       }
       //返回认证信息，密码传空字符串即可（因为JWT已验签）
        return new SimpleAuthenticationInfo(username,"",getName());
    }
}
