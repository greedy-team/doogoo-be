package com.doogoo.doogoo.support;

import com.doogoo.doogoo.common.error.DoogooException;
import com.doogoo.doogoo.common.error.ErrorCode;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/test/error")
public class ExceptionTriggerController {

    @GetMapping("/doogoo")
    public void doogooException() {
        throw new DoogooException(ErrorCode.TOKEN_NOT_FOUND);
    }

    @GetMapping("/generic")
    public void genericException() {
        throw new RuntimeException("trigger");
    }
}
