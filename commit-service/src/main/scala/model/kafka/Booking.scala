package model.kafka

import java.util.UUID

import model.api.{Flightnumber, Person, Price, UserId}

case class Booking(
    bookingId: UUID,
    successful: Successful,
    userId: UserId,
    persons: Seq[Person],
    price: Price,
    flightnumber: Flightnumber,
)
