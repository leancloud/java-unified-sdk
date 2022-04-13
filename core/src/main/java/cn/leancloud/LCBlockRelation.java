package cn.leancloud;

import cn.leancloud.annotation.LCClassName;

@LCClassName("_Blocklist")
public class LCBlockRelation extends LCObject {
    public static final String CLASS_NAME = "_Blocklist";

    public static final String ATTR_BLOCK_USER = "blockedUser";// user who was added in block list

    public LCBlockRelation() {
        super(CLASS_NAME);
    }

    public LCBlockRelation(LCObject object) {
        super(object);
        setClassName(CLASS_NAME);
    }

    public LCUser getUser(){
        return getLCObject(ATTR_BLOCK_USER);
    }

    public void setUser(LCUser blockedUser){
        put(ATTR_BLOCK_USER, blockedUser);
    }


}
