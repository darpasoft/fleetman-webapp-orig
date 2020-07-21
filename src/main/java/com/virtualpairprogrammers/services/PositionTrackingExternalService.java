package com.virtualpairprogrammers.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.netflix.hystrix.contrib.javanica.annotation.HystrixCommand;
import com.virtualpairprogrammers.controllers.Position;
import com.virtualpairprogrammers.data.VehicleRepository;
import com.virtualpairprogrammers.domain.Vehicle;

@Service
//@Transactional
public class PositionTrackingExternalService {
	
	//private DiscoveryClient discoveryService;
//	@Autowired
//	private LoadBalancerClient balancer;
	
	@Autowired
	private VehicleRepository repository;
	
	@Autowired
	private RemotePositionMicroserviceCalls remoteService;
	
	// if situation becomes "DIRE" this method will be automatically skipped
	//@HystrixCommand(fallbackMethod = "handleExternalServiceDown", commandProperties = {@HystrixProperty(name= "execution.isolation.strategy", value = "SEMAPHORE")})
	@HystrixCommand(fallbackMethod = "handleExternalServiceDown")
	public Position getLatestPositionForVehicleFromRemoteMicroservice(String name) {
		
		
		// System.out.println("Attemt to call REMOTE service");
		
		// get the current position for this vehicle from the microservice
		// RestTemplate rest = new RestTemplate(); // we can remove as feign knows about Ribbon
		
		// ServiceInstance service = balancer.choose("FLEETMAN-POSITION-TRACKER");
		
		// List<ServiceInstance> instances = discoveryService.getInstances("FLEETMAN-POSITION-TRACKER");
//		if (service == null)
//		{
//			System.out.println("NO service available");
//			
//			// Service has crashed. TODO handle this with a circuit breaker
//			throw new RuntimeException("Position tracker has crashed");
//		}
		//ServiceInstance service = instances.get(0);
		
		//System.out.println("Service found ...");
		
		// TODO this WILL be improved with Feign
		// String physicalLocation = service.getUri().toString();
		
		
		// save position in database
		// First Solution - @HystrixProperty(name= "execution.isolation.strategy"
//		Vehicle vehicleFromDb = repository.findByName(name);
//		
//		vehicleFromDb.setLat(response.getLat());
//		vehicleFromDb.setLongitude(response.getLongitude());
//		vehicleFromDb.setLastRecordedPosition(response.getTimestamp());
		
		// this object is dirty, when the transaction is committed, than JPA will dirty check object
		
		// Dirty check => if object is changed -> auto SQL update
		
		// 2nd approach - make Controller Transactional
		
		//System.out.println("Success !!!");
		
		//Position response = rest.getForObject(physicalLocation + "/vehicles/" + name, Position.class);
		
		// NEW VERSION WITH FEIGN
		
		Position response = remoteService.getLatestPositionForVehicle(name);
		
		response.setUpToDate(true);
		
		return response;
	}
	
		public Position handleExternalServiceDown(String name) {
			
		System.out.println("Running the Fallback");
		
		// depends entire about particular application
			
	   // TODO
			
	   // for now return random position
			
		Position position = new Position();
		
		Vehicle vehicle = repository.findByName(name);
		
		position.setLat(vehicle.getLat());
		position.setLongitude(vehicle.getLongitude());
		position.setTimestamp(vehicle.getLastRecordedPosition());
		
//		position.setLat(new BigDecimal("41.0"));
//		position.setLongitude(new BigDecimal("0"));
//		position.setTimestamp(new Date());
		
		position.setUpToDate(false);
		
		return position;
	}

}
