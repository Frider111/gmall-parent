package com.atguigu.gmall.common.execption;

import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author Blue Grass
 * @date 2020/8/24 - 9:43
 */
@Component
@ControllerAdvice
@Controller
public class GlobalExecption {

    @ExceptionHandler(value = Exception.class)
    public ModelAndView handleException(Exception execption)
    {
//        map.put("execption1","全局异常，请联系管理员"+execption);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModel().put("execption1","全局异常，请联系管理员"+ execption.getMessage());
        modelAndView.setViewName("error");
        return modelAndView ;
    }

    @ExceptionHandler(value = RuntimeException.class)
    public ModelAndView handleRuntimeException(RuntimeException e)
    {
//        map.put("execption1","运行时异常，请联系管理员"+execption);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModel().put("execption1","运行时异常，请联系管理员"+e.getMessage());
        modelAndView.setViewName("error");
        return modelAndView;
    }

    @ExceptionHandler(value = GmallExecption.class)
    public ModelAndView handleGmallException(GmallExecption e)
    {
//        map.put("execption1","商城异常，请联系管理员"+execption);
        ModelAndView modelAndView = new ModelAndView();
        modelAndView.getModel().put("execption1","商城异常，请联系管理员"+e.getMessage());
        modelAndView.setViewName("error");
        return modelAndView ;
    }

}

