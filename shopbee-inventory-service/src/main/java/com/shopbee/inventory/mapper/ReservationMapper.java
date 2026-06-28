package com.shopbee.inventory.mapper;

import com.shopbee.inventory.dto.GetReservationById200Response;
import com.shopbee.inventory.entity.Reservation;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = MappingConstants.ComponentModel.CDI, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ReservationMapper {

    GetReservationById200Response toGetReservationById200Response(Reservation reservation);
}
