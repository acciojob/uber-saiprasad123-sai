package com.driver.services.impl;

import com.driver.model.Driver;
import com.driver.model.TripBooking;
import com.driver.model.TripStatus;
import com.driver.services.CustomerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.driver.model.Customer;
import com.driver.repository.CustomerRepository;
import com.driver.repository.DriverRepository;
import com.driver.repository.TripBookingRepository;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	CustomerRepository customerRepository2;

	@Autowired
	DriverRepository driverRepository2;

	@Autowired
	TripBookingRepository tripBookingRepository2;

	@Override
	public Customer register(Customer customer) {
		//Save the customer in database
		customerRepository2.save(customer);
        return customer;
    }

	@Override
	public void deleteCustomer(Integer customerId) {
		// Delete customer without using deleteById function

		Customer customer = customerRepository2.findById(customerId).get();
		customerRepository2.delete(customer);
	}

	@Override
	public TripBooking bookTrip(int customerId, String fromLocation, String toLocation, int distanceInKm) throws Exception{
		//Book the driver with lowest driverId who is free (cab available variable
		// is Boolean.TRUE). If no driver is available, throw "No cab available!" exception
		//Avoid using SQL query
		List<Driver> driverList = driverRepository2.findAll();
		Collections.sort(driverList, Comparator.comparingInt(Driver::getDriverId));
		Driver driver = null;
		for(Driver driver1:driverList){
			if(driver1.getCab().getAvailable()) {
				driver = driver1;
				break;
			}
		}
		if(driver==null){
			throw new Exception("No cab available!");
		}

		TripBooking tripBooking = new TripBooking(fromLocation,toLocation,distanceInKm);
		Customer customer = customerRepository2.findById(customerId).get();
		tripBooking.setCustomer(customer);
		tripBooking.setDriver(driver);
		tripBooking.setStatus(TripStatus.CONFIRMED);
		tripBooking.setBill(distanceInKm*driver.getCab().getPerKmRate());

		List<TripBooking> customerList = customer.getTripBookingList();
		if(customerList==null)
			customerList = new ArrayList<>();
		customerList.add(tripBooking);
		customer.setTripBookingList(customerList);

		List<TripBooking> listOfDrivers = driver.getTripBookingList();
		if(listOfDrivers==null)
			listOfDrivers = new ArrayList<>();
		listOfDrivers.add(tripBooking);
		driver.setTripBookingList(listOfDrivers);


		customerRepository2.save(customer);
		driverRepository2.save(driver);

		tripBookingRepository2.save(tripBooking);

		return tripBooking;


	}

	@Override
	public void cancelTrip(Integer tripId){
		//Cancel the trip having given trip Id and update TripBooking attributes accordingly

		TripBooking tripBooking= tripBookingRepository2.findById(tripId).get();
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setBill(0);
		tripBooking.setStatus(TripStatus.CANCELED);
		tripBookingRepository2.save(tripBooking);
	}

	@Override
	public void completeTrip(Integer tripId){
		//Complete the trip having given trip Id and update TripBooking attributes accordingly
		TripBooking tripBooking = tripBookingRepository2.findById(tripId).get();
		tripBooking.getDriver().getCab().setAvailable(true);
		tripBooking.setStatus(TripStatus.COMPLETED);
		tripBookingRepository2.save(tripBooking);
	}
}
