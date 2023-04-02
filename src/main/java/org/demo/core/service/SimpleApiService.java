package org.demo.core.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public interface SimpleApiService {

    void generateVerifyCode(HttpServletRequest request, HttpServletResponse httpServletResponse) throws IOException;
}
