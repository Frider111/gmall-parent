package com.atguigu.gmall.common.execption;

/**
 * @author Blue Grass
 * @date 2020/8/24 - 9:44
 */
public class GmallExecption extends RuntimeException {

    private static final long serialVersionUID = 2256477558399996007L;

    public GmallExecption(String name){
        super(name);
    }

    public GmallExecption(){
        super();
    }

}
