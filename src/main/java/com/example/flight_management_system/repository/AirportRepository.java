package com.example.flight_management_system.repository;

import com.example.flight_management_system.entity.Airport;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AirportRepository extends JpaRepository<Airport, Long> {
		@Query("""
						select a from Airport a
						where (:country is null or lower(a.country) like lower(concat('%', :country, '%')))
							and (:name is null or lower(a.name) like lower(concat('%', :name, '%')))
							and (:airlineId is null or a.airline.id = :airlineId)
						""")
		Page<Airport> search(@Param("country") String country,
												 @Param("name") String name,
												 @Param("airlineId") Long airlineId,
												 Pageable pageable);
}
