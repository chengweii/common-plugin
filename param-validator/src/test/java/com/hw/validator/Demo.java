package com.hw.validator;

import com.hw.validator.hibernate.HibernateValidator;
import com.hw.validator.simple.CommonRegex;
import com.hw.validator.simple.SimpleValidator;
import lombok.Data;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * 描述信息
 *
 * @author chengwei11
 * @since 2019/1/24
 */
public class Demo {
    public static void main(String[] args) {
        simpleValidatorTest();
        hibernateValidatorTest();
    }

    public static void simpleValidatorTest() {
        Bean bean = new Bean();
        bean.setAppId("10e3");
        bean.setName("test");

        SimpleValidator.checkArgument(bean.getAppId(), CommonRegex.limitLengthNumber(1, 5), "appId不正确:%s", bean.getAppId());

        SimpleValidator.checkArgument(() -> {
            return "10e3".equals(bean.getAppId());
        }, "appId不正确:%s", bean.getAppId());
    }

    public static void hibernateValidatorTest() {
        Bean bean = new Bean();
        bean.setAppId("test");
        bean.setName("test");

        HibernateValidator.ValidResult validResult = HibernateValidator.validateBean(bean);
        if (validResult.hasErrors()) {
            String errors = validResult.getErrors();
            System.out.println(errors);
        }
    }

    @Data
    public static class Bean {
        @NotNull(message = "非空")
        private String appId;

        @Pattern(regexp = "[\u4e00-\u9fa5]+", message = "名称只能输入是中文字符")
        private String name;
    }
}
