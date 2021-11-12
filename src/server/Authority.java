package server;

import java.util.ArrayList;
import java.util.List;

public class Authority {

    String user;
    String pass;

    Authority(){
        // 初始化权限库
        authorities.add(new Authority("anonymous", ""));
        authorities.add(new Authority("test", "test"));
        authorities.add(new Authority("pikachu", "cute"));
    }

    Authority(String user, String pass){
        // 新增权限
        this.user = user;
        this.pass = pass;
    }

    private final List<Authority> authorities = new ArrayList<>();

    public boolean authorityLegal(String user, String pass){
        for(Authority authority : authorities){
            if(authority.user.equals(user)){
                return authority.pass.equals(pass);
            }
        }
        return false;
    }
}
