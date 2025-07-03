package com.echotrail.gateway.filter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HeaderMapRequestWrapper extends HttpServletRequestWrapper {

    private final Map<String, String> headerMap = new HashMap<>();

    public HeaderMapRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    public void addHeader(String name, String value) {
        headerMap.put(name, value);
    }

    @Override
    public String getHeader(String name) {
        String headerValue = headerMap.get(name);
        if (headerValue != null) {
            return headerValue;
        }
        return ((HttpServletRequest) getRequest()).getHeader(name);
    }

    @Override
    public Enumeration<String> getHeaders(String name) {
        Set<String> values = new HashSet<>();
        if (headerMap.containsKey(name)) {
            values.add(headerMap.get(name));
        } else {
            Enumeration<String> headers = ((HttpServletRequest) getRequest()).getHeaders(name);
            while (headers.hasMoreElements()) {
                values.add(headers.nextElement());
            }
        }
        return Collections.enumeration(values);
    }

    @Override
    public Enumeration<String> getHeaderNames() {
        Set<String> names = new HashSet<>();
        names.addAll(headerMap.keySet());
        Enumeration<String> headerNames = ((HttpServletRequest) getRequest()).getHeaderNames();
        while (headerNames.hasMoreElements()) {
            names.add(headerNames.nextElement());
        }
        return Collections.enumeration(names);
    }
}
