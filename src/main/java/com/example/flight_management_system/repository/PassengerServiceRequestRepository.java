package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.PassengerServiceRequest;
import com.example.flight_management_system.entity.enums.ServiceRequestStatus;
import com.example.flight_management_system.entity.enums.ServiceRequestType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PassengerServiceRequestRepository extends JpaRepository<PassengerServiceRequest, Long> {
    List<PassengerServiceRequest> findByPassengerIdOrderByCreatedAtDesc(Long passengerId);
    List<PassengerServiceRequest> findByBookingIdOrderByCreatedAtDesc(Long bookingId);
    List<PassengerServiceRequest> findByTypeAndStatusOrderByCreatedAtDesc(ServiceRequestType type, ServiceRequestStatus status);
}
