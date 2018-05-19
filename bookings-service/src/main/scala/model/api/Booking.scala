package model.api

import java.util.UUID

case class Booking(bookingId: UUID,
                   userId: UserId,
                   numberOfPersons: NumberOfPersons,
                   flightnumber: Flightnumber,
)
