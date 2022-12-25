package com.mfi.excelDownload;

import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStream;
import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.mfi.exporter.CustomerListingExcelExporter;
import com.mfi.model.Customer;
import com.mfi.service.ReportService;

@Controller
public class CustomerListingExcelDownload {
	@Autowired
	ReportService reportService;
	@Autowired
	CustomerListingExcelExporter excel;
	@Autowired
	private ServletContext context;
	
	List<Customer> excelList=new ArrayList<Customer>();

	
	
	@RequestMapping("customerListing")
	public String customerListing(Model model) {
		LocalDate now = LocalDate.now();
		Date date = Date.valueOf(now);
		List<Customer> customerList= reportService.findCustomerByTodayDate(date);
		model.addAttribute("customerList",customerList);
		excelList.addAll(customerList);
		return "mfi/reports/MFI_RPT_CUS";
	}
	@PostMapping("/customerFilter")
	public String customerFilter(@Param("start")String start, @Param("end")String end, Model model) {
		Date startdate = Date.valueOf(start);
		Date enddate = Date.valueOf(end);
		List<Customer> customerList = reportService.customerFilter(startdate, enddate);
		model.addAttribute("customerList",customerList);
		excelList.addAll(customerList);
		return "mfi/reports/MFI_RPT_CUS";
	}
	@GetMapping(value="/customerListingReportExport")
	public void customerListingReportExport(HttpServletRequest request, HttpServletResponse response) {
		boolean isFlag=excel.customerListingExport(excelList, context, request, response);
		LocalDate date = LocalDate.now();
		if(isFlag) {
			String fullPath = request.getServletContext().getRealPath("/resources/reports/"+"CustomerListingReport"+".xls");
			filedownload(fullPath, response, "CustomerListingReport("+date+").xls");
		}
	
		excelList.removeAll(excelList);
		
	}
	
	
	
	private void filedownload(String fullPath, HttpServletResponse response, String fileName) {
		File file = new File(fullPath);
		System.out.println(file.exists());
		final int BUFFER_SIZE = 40976;
		if(file.exists()) {
			
			try {
				
				FileInputStream inputStream = new FileInputStream(file);
				String mimeType = context.getMimeType(fullPath);
				response.setContentType(mimeType);
				response.setHeader("content-disposition","attachment; filename="+fileName);
				OutputStream outputStream = response.getOutputStream();
				byte[] buffer = new byte[BUFFER_SIZE];
				int bytesRead = -1;
				while((bytesRead = inputStream.read(buffer))!= -1){
					outputStream.write(buffer,0,bytesRead);
				}
				inputStream.close();
				outputStream.close();
				file.delete();
				
			}catch (Exception e) {
 				e.printStackTrace();
			}
		}
		
	}

}
