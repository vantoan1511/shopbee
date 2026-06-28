package com.shopbee.inventory.repository;

import com.shopbee.inventory.entity.Reservation;
import io.quarkus.hibernate.orm.panache.PanacheRepositoryBase;
import jakarta.enterprise.context.ApplicationScoped;

import java.util.UUID;

@ApplicationScoped
public class ReservationRepository implements PanacheRepositoryBase<Reservation, UUID> {
}
