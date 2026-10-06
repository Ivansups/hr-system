package ru.klimov.hrsystem.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klimov.hrsystem.dto.EmployeeDto;
import ru.klimov.hrsystem.exception.EntityNotFoundException;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.service.EmployeeService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class EmployeeServlet extends HttpServlet {
    private final EmployeeService employeeService;

    public EmployeeServlet(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(resp, HttpServletResponse.SC_OK, employeeService.getAll());
            } else {
                writeJson(resp, HttpServletResponse.SC_OK, employeeService.getById(parseId(pathInfo)));
            }
        } catch (EntityNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            EmployeeDto input = JsonMapper.fromJson(readBody(req), EmployeeDto.class);
            writeJson(resp, HttpServletResponse.SC_CREATED, employeeService.create(input));
        } catch (ValidationException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (EntityNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            long id = parseId(req.getPathInfo());
            EmployeeDto input = JsonMapper.fromJson(readBody(req), EmployeeDto.class);
            writeJson(resp, HttpServletResponse.SC_OK, employeeService.update(id, input));
        } catch (ValidationException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        } catch (EntityNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            employeeService.delete(parseId(req.getPathInfo()));
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (EntityNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private long parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new ValidationException("Employee id is required in the path");
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid employee id: " + pathInfo);
        }
    }

    private String readBody(HttpServletRequest req) throws IOException {
        return new String(req.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }

    private void writeJson(HttpServletResponse resp, int status, Object body) throws IOException {
        resp.setStatus(status);
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        resp.getWriter().write(JsonMapper.toJson(body));
    }

    private void writeError(HttpServletResponse resp, int status, String message) throws IOException {
        writeJson(resp, status, Map.of("error", message));
    }
}
