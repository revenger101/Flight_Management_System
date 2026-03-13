package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Passenger;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PassengerRepository extends JpaRepository<Passenger, Long> {
    List<Passenger> findByName(String firstName);

        @Query("""
                        select p from Passenger p
                        where (:name is null or lower(p.name) like lower(concat('%', :name, '%')))
                            and (:status is null or lower(p.status) = lower(:status))
                        """)
        Page<Passenger> search(@Param("name") String name,
                                                     @Param("status") String status,
                                                     Pageable pageable);

}
