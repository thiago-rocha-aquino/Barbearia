'use client'

import { useState, useMemo } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { api, getErrorMessage } from '@/lib/api'
import { useToast } from '@/hooks/use-toast'
import type { CalendarEvent, Appointment } from '@/types'
import { Calendar, dateFnsLocalizer, Views } from 'react-big-calendar'
import { format, parse, startOfWeek, getDay, addDays, subDays } from 'date-fns'
import { ptBR } from 'date-fns/locale'
import 'react-big-calendar/lib/css/react-big-calendar.css'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from '@/components/ui/dialog'
import { ChevronLeft, ChevronRight, Loader2, Phone, Mail, Clock, User } from 'lucide-react'
import { formatCurrency, formatPhone } from '@/lib/utils'

const locales = { 'pt-BR': ptBR }

const localizer = dateFnsLocalizer({
  format,
  parse,
  startOfWeek: () => startOfWeek(new Date(), { weekStartsOn: 0 }),
  getDay,
  locales,
})

export default function AgendamentosPage() {
  const { toast } = useToast()
  const queryClient = useQueryClient()
  const [currentDate, setCurrentDate] = useState(new Date())
  const [view, setView] = useState<'week' | 'day' | 'month'>(Views.WEEK)
  const [selectedEvent, setSelectedEvent] = useState<CalendarEvent | null>(null)
  const [detailsOpen, setDetailsOpen] = useState(false)

  const startDate = format(subDays(currentDate, 7), 'yyyy-MM-dd')
  const endDate = format(addDays(currentDate, 30), 'yyyy-MM-dd')

  const { data: events, isLoading } = useQuery({
    queryKey: ['appointments', startDate, endDate],
    queryFn: async () => {
      const { data } = await api.get<CalendarEvent[]>('/api/admin/appointments', {
        params: { startDate, endDate },
      })
      return data
    },
  })

  const { data: appointmentDetails } = useQuery({
    queryKey: ['appointment', selectedEvent?.id],
    queryFn: async () => {
      const { data } = await api.get<Appointment>(`/api/admin/appointments/${selectedEvent?.id}`)
      return data
    },
    enabled: !!selectedEvent?.id,
  })

  const cancelMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.post(`/api/admin/appointments/${id}/cancel`)
    },
    onSuccess: () => {
      toast({ title: 'Agendamento cancelado' })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
      setDetailsOpen(false)
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const completeMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.post(`/api/admin/appointments/${id}/complete`)
    },
    onSuccess: () => {
      toast({ title: 'Agendamento concluído' })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
      setDetailsOpen(false)
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const noShowMutation = useMutation({
    mutationFn: async (id: string) => {
      await api.post(`/api/admin/appointments/${id}/no-show`)
    },
    onSuccess: () => {
      toast({ title: 'Marcado como no-show' })
      queryClient.invalidateQueries({ queryKey: ['appointments'] })
      setDetailsOpen(false)
    },
    onError: (error) => {
      toast({
        variant: 'destructive',
        title: 'Erro',
        description: getErrorMessage(error),
      })
    },
  })

  const calendarEvents = useMemo(() => {
    return events?.map((e) => ({
      ...e,
      start: new Date(e.start),
      end: new Date(e.end),
    })) || []
  }, [events])

  const getStatusBadge = (status: string) => {
    switch (status) {
      case 'CONFIRMED':
        return <Badge variant="success">Confirmado</Badge>
      case 'COMPLETED':
        return <Badge variant="secondary">Concluído</Badge>
      case 'CANCELLED_BY_CLIENT':
      case 'CANCELLED_BY_ADMIN':
        return <Badge variant="destructive">Cancelado</Badge>
      case 'NO_SHOW':
        return <Badge variant="warning">Não compareceu</Badge>
      default:
        return <Badge>{status}</Badge>
    }
  }

  const eventStyleGetter = (event: CalendarEvent) => {
    let backgroundColor = '#3b82f6'
    if (event.status === 'COMPLETED') backgroundColor = '#6b7280'
    if (event.status === 'CANCELLED_BY_CLIENT' || event.status === 'CANCELLED_BY_ADMIN')
      backgroundColor = '#ef4444'
    if (event.status === 'NO_SHOW') backgroundColor = '#f59e0b'

    return {
      style: {
        backgroundColor,
        borderRadius: '4px',
        opacity: 0.9,
        border: 'none',
      },
    }
  }

  const handleSelectEvent = (event: CalendarEvent) => {
    setSelectedEvent(event)
    setDetailsOpen(true)
  }

  const isActive = appointmentDetails?.status === 'CONFIRMED' || appointmentDetails?.status === 'SCHEDULED'

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Agenda</h1>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            size="icon"
            onClick={() => setCurrentDate(subDays(currentDate, view === 'day' ? 1 : 7))}
          >
            <ChevronLeft className="h-4 w-4" />
          </Button>
          <Button variant="outline" onClick={() => setCurrentDate(new Date())}>
            Hoje
          </Button>
          <Button
            variant="outline"
            size="icon"
            onClick={() => setCurrentDate(addDays(currentDate, view === 'day' ? 1 : 7))}
          >
            <ChevronRight className="h-4 w-4" />
          </Button>
          <div className="ml-4 flex gap-1">
            <Button
              variant={view === 'day' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setView(Views.DAY)}
            >
              Dia
            </Button>
            <Button
              variant={view === 'week' ? 'default' : 'outline'}
              size="sm"
              onClick={() => setView(Views.WEEK)}
            >
              Semana
            </Button>
          </div>
        </div>
      </div>

      <Card>
        <CardContent className="p-4">
          {isLoading ? (
            <div className="flex items-center justify-center h-96">
              <Loader2 className="h-8 w-8 animate-spin" />
            </div>
          ) : (
            <div className="h-[600px]">
              <Calendar
                localizer={localizer}
                events={calendarEvents}
                startAccessor="start"
                endAccessor="end"
                view={view}
                onView={(v) => setView(v as 'week' | 'day' | 'month')}
                date={currentDate}
                onNavigate={setCurrentDate}
                onSelectEvent={handleSelectEvent}
                eventPropGetter={eventStyleGetter}
                messages={{
                  today: 'Hoje',
                  previous: 'Anterior',
                  next: 'Próximo',
                  month: 'Mês',
                  week: 'Semana',
                  day: 'Dia',
                  agenda: 'Agenda',
                  noEventsInRange: 'Nenhum agendamento neste período',
                }}
                min={new Date(0, 0, 0, 7, 0, 0)}
                max={new Date(0, 0, 0, 21, 0, 0)}
              />
            </div>
          )}
        </CardContent>
      </Card>

      {/* Appointment Details Dialog */}
      <Dialog open={detailsOpen} onOpenChange={setDetailsOpen}>
        <DialogContent className="max-w-md">
          <DialogHeader>
            <DialogTitle>Detalhes do Agendamento</DialogTitle>
          </DialogHeader>
          {appointmentDetails && (
            <div className="space-y-4">
              <div className="flex items-center justify-between">
                <span className="font-medium">{appointmentDetails.serviceName}</span>
                {getStatusBadge(appointmentDetails.status)}
              </div>

              <div className="space-y-3">
                <div className="flex items-center gap-2">
                  <User className="h-4 w-4 text-slate-500" />
                  <span>{appointmentDetails.clientName}</span>
                </div>
                <div className="flex items-center gap-2">
                  <Phone className="h-4 w-4 text-slate-500" />
                  <span>{formatPhone(appointmentDetails.clientPhone)}</span>
                </div>
                {appointmentDetails.clientEmail && (
                  <div className="flex items-center gap-2">
                    <Mail className="h-4 w-4 text-slate-500" />
                    <span>{appointmentDetails.clientEmail}</span>
                  </div>
                )}
                <div className="flex items-center gap-2">
                  <Clock className="h-4 w-4 text-slate-500" />
                  <span>
                    {format(new Date(appointmentDetails.startTime), 'dd/MM/yyyy HH:mm')} -{' '}
                    {format(new Date(appointmentDetails.endTime), 'HH:mm')}
                  </span>
                </div>
              </div>

              <div className="pt-4 border-t">
                <div className="flex justify-between">
                  <span>Valor</span>
                  <span className="font-bold">
                    {formatCurrency(appointmentDetails.priceAtBooking)}
                  </span>
                </div>
              </div>

              {appointmentDetails.notes && (
                <div className="pt-4 border-t">
                  <p className="text-sm text-slate-500">{appointmentDetails.notes}</p>
                </div>
              )}
            </div>
          )}
          <DialogFooter className="flex-col gap-2 sm:flex-row">
            {isActive && (
              <>
                <Button
                  variant="outline"
                  onClick={() => completeMutation.mutate(appointmentDetails!.id)}
                  disabled={completeMutation.isPending}
                >
                  Concluir
                </Button>
                <Button
                  variant="outline"
                  onClick={() => noShowMutation.mutate(appointmentDetails!.id)}
                  disabled={noShowMutation.isPending}
                >
                  No-show
                </Button>
                <Button
                  variant="destructive"
                  onClick={() => cancelMutation.mutate(appointmentDetails!.id)}
                  disabled={cancelMutation.isPending}
                >
                  Cancelar
                </Button>
              </>
            )}
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </div>
  )
}
