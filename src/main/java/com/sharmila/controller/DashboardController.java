package com.sharmila.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import com.sharmila.crawler.GDev;

@RequestMapping("/scrapper")
public class DashboardController {

	@Autowired
	GDev gdev;

	@RequestMapping(method = RequestMethod.GET)
	public ModelAndView getStat() {
		ModelAndView modelAndView = new ModelAndView();
		// get total Number of job data from ES

		modelAndView.addObject("totaljob", gdev.getTotalJobs());
		return new ModelAndView("index");
	}

}
