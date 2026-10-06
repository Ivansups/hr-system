package ru.klimov.hrsystem.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ru.klimov.hrsystem.dto.PositionDto;
import ru.klimov.hrsystem.exception.EntityNotFoundException;
import ru.klimov.hrsystem.exception.ValidationException;
import ru.klimov.hrsystem.service.PositionService;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

public final class PositionServlet extends HttpServlet {
    private final PositionService positionService;

    public PositionServlet(PositionService positionService) {
        this.positionService = positionService;
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String pathInfo = req.getPathInfo();
        try {
            if (pathInfo == null || pathInfo.equals("/")) {
                writeJson(resp, HttpServletResponse.SC_OK, positionService.getAll());
            } else {
                writeJson(resp, HttpServletResponse.SC_OK, positionService.getById(parseId(pathInfo)));
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
            PositionDto input = JsonMapper.fromJson(readBody(req), PositionDto.class);
            PositionDto created = positionService.create(input);
            writeJson(resp, HttpServletResponse.SC_CREATED, created);
        } catch (ValidationException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            positionService.delete(parseId(req.getPathInfo()));
            resp.setStatus(HttpServletResponse.SC_NO_CONTENT);
        } catch (EntityNotFoundException e) {
            writeError(resp, HttpServletResponse.SC_NOT_FOUND, e.getMessage());
        } catch (ValidationException e) {
            writeError(resp, HttpServletResponse.SC_BAD_REQUEST, e.getMessage());
        }
    }

    private long parseId(String pathInfo) {
        if (pathInfo == null || pathInfo.equals("/")) {
            throw new ValidationException("Position id is required in the path");
        }
        try {
            return Long.parseLong(pathInfo.substring(1));
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid position id: " + pathInfo);
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
