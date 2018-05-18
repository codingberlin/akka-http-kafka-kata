package model.api

case class Booking(
    userId: UserId,
    persons: Seq[Person],
    price: Price,
    flightnumber: Flightnumber,
    provider: Provider
)
