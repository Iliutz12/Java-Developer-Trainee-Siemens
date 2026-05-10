# Problem 2: Transportation Ticket Pricing

## The Problem
Selling tickets at one fixed price is a bad idea. If a train leaves with 50 empty seats, the company just lost money they could have made by offering a discount. On the flip side, if they sell out too early, they miss out on last-minute travelers who would have paid extra.
## The Solution
To maximize revenue, transportation companies should implement a time-based dynamic pricing curve. My solution calculates ticket prices based on the time remaining until departure:
1. **The Early Bird (6 to 12 months out):** Tickets are sold at their normal, lowest price. This gets people to commit early so the company has guaranteed passengers.
2. **Steady Climb (6 Months - 1 Month out):** Prices slowly rise as the baseline capacity fills.
3. **The Dip (3 - 4 Weeks out):** Prices temporarily drop. This incentivizes flexible leisure travelers to buy up the remaining empty seats.
4. **Spike (Last 1 - 2 Weeks):** Prices spike to their absolute highest. Travelers booking this late usually have urgent (e.g., business trips, emergencies) and are willing to pay a premium.