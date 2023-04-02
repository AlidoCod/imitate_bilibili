package org.demo.core.service;

import jakarta.servlet.http.HttpServletRequest;
import org.demo.core.controller.dto.LoginDto;
import org.demo.core.controller.dto.RegisterDto;

public interface AuthenticationService {

    public String register(RegisterDto registerDto, HttpServletRequest request) throws Exception;

    public String authenticate(LoginDto loginDTO, HttpServletRequest request) throws Exception;
}
