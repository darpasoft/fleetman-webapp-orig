package com.virtualpairprogrammers.controllers;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.servlet.ModelAndView;

import com.virtualpairprogrammers.data.VehicleRepository;
import com.virtualpairprogrammers.domain.Vehicle;
import com.virtualpairprogrammers.services.PositionTrackingExternalService;

@Controller
@Transactional
@RequestMapping("/website/vehicles")
public class VehicleController 
{
	@Autowired
	private VehicleRepository data;
	
	@Autowired
	private PositionTrackingExternalService service;
	
	@RequestMapping(value="/newVehicle.html",method=RequestMethod.POST)
	public String newVehicle(Vehicle vehicle)
	{
		data.save(vehicle);
		return "redirect:/website/vehicles/list.html";
	}
	
	@RequestMapping(value="/deleteVehicle.html", method=RequestMethod.POST)
	public String deleteVehicle(@RequestParam Long id)
	{
		data.delete(id);
		return "redirect:/website/vehicles/list.html";		
	}
	
	@RequestMapping(value="/newVehicle.html",method=RequestMethod.GET)
	public ModelAndView renderNewVehicleForm()
	{
		Vehicle newVehicle = new Vehicle();
		return new ModelAndView("newVehicle","form",newVehicle);
	} 
	
	@RequestMapping(value="/list.html", method=RequestMethod.GET)	
	public ModelAndView vehicles()
	{
		List<Vehicle> allVehicles = data.findAll();
		return new ModelAndView("allVehicles", "vehicles", allVehicles);
	}
	  
	@RequestMapping(value="/vehicle/{name}")
	public ModelAndView showVehicleByName(@PathVariable("name") String name)
	{
		Vehicle vehicle = data.findByName(name);
		
		Position latestPosition = service.getLatestPositionForVehicleFromRemoteMicroservice(name);
		
		// if successful than update database
		if(latestPosition.isUpToDate()) {
			
			vehicle.setLat(latestPosition.getLat());
			vehicle.setLongitude(latestPosition.getLongitude());
			vehicle.setLastRecordedPosition(latestPosition.getTimestamp());
			
		}
		
		//int port = service.getPort();
		
		Map<String,Object> model = new HashMap<>();
		model.put("vehicle", vehicle);
		//model.put("port", port);
		model.put("position", latestPosition);
		return new ModelAndView("vehicleInfo", "model",model);
	}
	
}
