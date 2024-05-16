package com.hcmus.group11.novelaggregator.util;

import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

public class RequestAttributeUtil {

    public static void setAttribute(String name, Object value) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            requestAttributes.setAttribute(name, value, RequestAttributes.SCOPE_REQUEST);
        }
    }

    public static Object getAttribute(String name) {
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        if (requestAttributes != null) {
            return requestAttributes.getAttribute(name, RequestAttributes.SCOPE_REQUEST);
        }
        return null;
    }
}
