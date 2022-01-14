package com.tenniscourts.reservations;

import io.swagger.annotations.ApiModelProperty;
import lombok.*;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class ReservationFilterRequestDTO {

    @NotNull
    @ApiModelProperty(required = true)
    private LocalDateTime startDateTime;

    private LocalDateTime endDateTime;

}
