package org.example.gamestoreapp.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        int statusCode = status != null ? Integer.parseInt(status.toString()) : 500;

        if (statusCode == 401) {
            return "error/401";
        } else if (statusCode == 404) {
            return "error/404";
        } else if (statusCode == 405) {
            return "error/405";
        } else if (statusCode == 500) {
            return "error/500";
        }

        return "error";
    }
}
