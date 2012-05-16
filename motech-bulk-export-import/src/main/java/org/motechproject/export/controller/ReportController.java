package org.motechproject.export.controller;

import org.motechproject.export.model.AllReportDataSources;
import org.motechproject.export.model.ReportDataSource;
import org.motechproject.export.writer.ExcelWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.servlet.http.HttpServletResponse;

@RequestMapping(value = "/reports")
@Controller
public class ReportController {

    private AllReportDataSources allReportDataSources;
    private ExcelWriter excelWriter;

    @Autowired
    public ReportController(AllReportDataSources allReportDataSources, ExcelWriter excelWriter) {
        this.allReportDataSources = allReportDataSources;
        this.excelWriter = excelWriter;
    }

    @RequestMapping(method = RequestMethod.GET, value = "{groupName}/{reportName}.xls")
    public void createReport(@PathVariable("groupName") String groupName, @PathVariable("reportName") String reportName, HttpServletResponse response) {
        ReportDataSource reportDataSource = allReportDataSources.get(groupName);
        excelWriter.writeExcelToResponse(response, reportDataSource, reportName,  reportName + ".xls");
    }
}
