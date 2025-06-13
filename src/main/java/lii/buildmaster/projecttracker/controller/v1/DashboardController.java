package lii.buildmaster.projecttracker.controller.v1;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
public class DashboardController {

    @GetMapping
    public String viewDashboard() {
        return "dashboardPage"; // maps to a file like `dashboardPage.html` or `dashboardPage.jsp`
    }
}

