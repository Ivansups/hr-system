package ru.klimov.hrsystem.web;

import jakarta.servlet.ServletContainerInitializer;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRegistration;
import ru.klimov.hrsystem.repository.DepartmentRepository;
import ru.klimov.hrsystem.repository.EmployeeRepository;
import ru.klimov.hrsystem.repository.PositionRepository;
import ru.klimov.hrsystem.repository.jdbc.DataSourceFactory;
import ru.klimov.hrsystem.repository.jdbc.JdbcDepartmentRepository;
import ru.klimov.hrsystem.repository.jdbc.JdbcEmployeeRepository;
import ru.klimov.hrsystem.repository.jdbc.JdbcPositionRepository;
import ru.klimov.hrsystem.service.DepartmentService;
import ru.klimov.hrsystem.service.EmployeeService;
import ru.klimov.hrsystem.service.PositionService;

import javax.sql.DataSource;
import java.util.Set;

public final class AppInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> classes, ServletContext context) throws ServletException {
        DataSource dataSource = DataSourceFactory.create();
        DepartmentRepository departmentRepository = new JdbcDepartmentRepository(dataSource);
        PositionRepository positionRepository = new JdbcPositionRepository(dataSource);
        EmployeeRepository employeeRepository = new JdbcEmployeeRepository(dataSource, departmentRepository, positionRepository);

        ServletRegistration.Dynamic departments = context.addServlet("departments",
                new DepartmentServlet(new DepartmentService(departmentRepository)));
        departments.addMapping("/departments/*");

        ServletRegistration.Dynamic positions = context.addServlet("positions",
                new PositionServlet(new PositionService(positionRepository)));
        positions.addMapping("/positions/*");

        ServletRegistration.Dynamic employees = context.addServlet("employees",
                new EmployeeServlet(new EmployeeService(employeeRepository, departmentRepository, positionRepository)));
        employees.addMapping("/employees/*");
    }
}
