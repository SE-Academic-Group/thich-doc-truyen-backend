package com.hcmus.group11.novelaggregator.interceptor;

import com.hcmus.group11.novelaggregator.type.ApiResponse;
import com.hcmus.group11.novelaggregator.type.ResponseMetadata;
import com.hcmus.group11.novelaggregator.util.RequestAttributeUtil;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;


@ControllerAdvice
public class ResponseBodyInterceptor implements ResponseBodyAdvice<Object> {


    @Override
    public boolean supports(MethodParameter returnType, Class<? extends HttpMessageConverter<?>> converterType) {
        return true;  // Apply to all responses, can be refined to specific ones if needed
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class<? extends HttpMessageConverter<?>> selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        // Skip processing for Swagger endpoints
        String path = request.getURI().getPath();
        if (path.startsWith("/swagger-ui") || path.startsWith("/v3/api-docs")) {
            return body;
        }

        ResponseMetadata metadata = (ResponseMetadata) RequestAttributeUtil.getAttribute("metadata");
        if (metadata != null) {
            ApiResponse<Object> responseObject = new ApiResponse<>(body, metadata.getMetadata());
            return responseObject;
        }
        return new ApiResponse<>(body);
    }

//    @Override
//    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType,
//                                  Class<? extends HttpMessageConverter<?>> selectedConverterType,
//                                  HttpInputMessage inputMessage, HttpOutputMessage outputMessage) {
//        Metadata metadata = (Metadata) RequestAttributeUtil.getAttribute("metadata");
//        if (metadata != null) {
//            ApiResponse<Object> responseObject = new ApiResponse<>(body);
//            responseObject.setMetadata(metadata);
//            return responseObject;
//        }
//        return body;
//    }
}
