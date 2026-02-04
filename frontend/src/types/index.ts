export interface Service {
  id: string
  name: string
  description?: string
  durationMinutes: number
  bufferMinutes?: number
  totalDurationMinutes?: number
  price: number
  displayOrder?: number
  active?: boolean
}

export interface Barber {
  id: string
  name: string
}

export interface User {
  id: string
  name: string
  email: string
  phone?: string
  role: 'ADMIN' | 'BARBER'
  active: boolean
}

export interface TimeSlot {
  dateTime: string
  time: string
  available: boolean
  barberId: string
  barberName: string
}

export interface DayAvailability {
  date: string
  hasAvailableSlots: boolean
  slots?: TimeSlot[]
}

export interface Appointment {
  id: string
  barberId: string
  barberName: string
  serviceId: string
  serviceName: string
  serviceDuration: number
  clientName: string
  clientPhone: string
  clientEmail?: string
  startTime: string
  endTime: string
  status: AppointmentStatus
  priceAtBooking: number
  notes?: string
  cancellationToken?: string
  createdByAdmin: boolean
  createdAt: string
}

export interface PublicAppointment {
  id: string
  barberName: string
  serviceName: string
  serviceDuration: number
  startTime: string
  endTime: string
  price: number
  cancellationToken: string
  canCancel: boolean
  canReschedule: boolean
}

export interface CalendarEvent {
  id: string
  title: string
  start: Date
  end: Date
  status: string
  clientName: string
  clientPhone: string
  serviceName: string
  barberName: string
  barberId: string
}

export type AppointmentStatus =
  | 'SCHEDULED'
  | 'CONFIRMED'
  | 'CANCELLED_BY_CLIENT'
  | 'CANCELLED_BY_ADMIN'
  | 'COMPLETED'
  | 'NO_SHOW'

export interface WorkingHours {
  id: string
  barberId: string
  dayOfWeek: DayOfWeek
  startTime: string
  endTime: string
  isWorking: boolean
}

export type DayOfWeek =
  | 'MONDAY'
  | 'TUESDAY'
  | 'WEDNESDAY'
  | 'THURSDAY'
  | 'FRIDAY'
  | 'SATURDAY'
  | 'SUNDAY'

export interface TimeBlock {
  id: string
  barberId: string
  barberName: string
  startTime: string
  endTime: string
  reason: string
}

export interface DashboardStats {
  todayAppointments: number
  weekAppointments: number
  pendingAppointments: number
  weekRevenue: number
  noShowsThisWeek: number
}

export interface PeriodReport {
  startDate: string
  endDate: string
  totalAppointments: number
  completedAppointments: number
  cancelledAppointments: number
  noShowAppointments: number
  estimatedRevenue: number
  actualRevenue: number
  noShowRate: number
  cancellationRate: number
}

export interface LoginResponse {
  accessToken: string
  tokenType: string
  expiresIn: number
  user: {
    id: string
    name: string
    email: string
    role: 'ADMIN' | 'BARBER'
  }
}

export interface NotificationLog {
  id: string
  appointmentId: string
  type: 'CONFIRMATION' | 'REMINDER_24H' | 'REMINDER_2H' | 'CANCELLATION' | 'RESCHEDULE'
  channel: string
  recipient: string
  status: 'PENDING' | 'SENT' | 'FAILED'
  errorMessage?: string
  sentAt?: string
  createdAt: string
  retryCount: number
}
