import Foundation
import FrameZero

// Bridges the shared `LocalDate` to Foundation for native date pickers. A `LocalDate` has no time
// zone, so both directions go through the user's calendar at its local midnight.

extension Kotlinx_datetimeLocalDate {
    convenience init(_ date: Date, calendar: Calendar = .current) {
        let parts = calendar.dateComponents([.year, .month, .day], from: date)
        self.init(year: Int32(parts.year!), month: Int32(parts.month!), day: Int32(parts.day!))
    }

    func date(in calendar: Calendar = .current) -> Date {
        let parts = DateComponents(year: Int(year), month: Int(month.ordinal) + 1, day: Int(day))
        return calendar.date(from: parts)!
    }
}
