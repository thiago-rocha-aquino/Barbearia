'use client'

import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'
import { formatCurrency } from '@/lib/utils'
import type { DashboardStats, CalendarEvent } from '@/types'
import { format } from 'date-fns'
import { ptBR } from 'date-fns/locale'

import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Badge } from '@/components/ui/badge'
import { Calendar, Clock, DollarSign, Users, AlertTriangle, Loader2 } from 'lucide-react'

export default function DashboardPage() {
  const today = new Date()
  const startDate = format(today, 'yyyy-MM-dd')
  const endDate = format(today, 'yyyy-MM-dd')

  const { data: stats, isLoading: loadingStats } = useQuery({
    queryKey: ['dashboard-stats'],
    queryFn: async () => {
      const { data } = await api.get<DashboardStats>('/api/admin/reports/dashboard')
      return data
    },
  })

  const { data: todayAppointments, isLoading: loadingAppointments } = useQuery({
    queryKey: ['today-appointments'],
    queryFn: async () => {
      const { data } = await api.get<CalendarEvent[]>('/api/admin/appointments', {
        params: { startDate, endDate },
      })
      return data
    },
  })

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

  if (loadingStats) {
    return (
      <div className="flex items-center justify-center h-64">
        <Loader2 className="h-8 w-8 animate-spin" />
      </div>
    )
  }

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold">Dashboard</h1>
        <p className="text-slate-500">
          {format(today, "EEEE, d 'de' MMMM 'de' yyyy", { locale: ptBR })}
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Agendamentos Hoje</CardTitle>
            <Calendar className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.todayAppointments || 0}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Agendamentos Semana</CardTitle>
            <Users className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.weekAppointments || 0}</div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">Faturamento Semana</CardTitle>
            <DollarSign className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">
              {formatCurrency(stats?.weekRevenue || 0)}
            </div>
          </CardContent>
        </Card>

        <Card>
          <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
            <CardTitle className="text-sm font-medium">No-shows Semana</CardTitle>
            <AlertTriangle className="h-4 w-4 text-muted-foreground" />
          </CardHeader>
          <CardContent>
            <div className="text-2xl font-bold">{stats?.noShowsThisWeek || 0}</div>
          </CardContent>
        </Card>
      </div>

      {/* Today's Appointments */}
      <Card>
        <CardHeader>
          <CardTitle>Agendamentos de Hoje</CardTitle>
          <CardDescription>
            {todayAppointments?.length || 0} agendamentos
          </CardDescription>
        </CardHeader>
        <CardContent>
          {loadingAppointments ? (
            <div className="flex justify-center py-8">
              <Loader2 className="h-6 w-6 animate-spin" />
            </div>
          ) : todayAppointments && todayAppointments.length > 0 ? (
            <div className="space-y-3">
              {todayAppointments
                .sort((a, b) => new Date(a.start).getTime() - new Date(b.start).getTime())
                .map((appointment) => (
                  <div
                    key={appointment.id}
                    className="flex items-center justify-between p-3 rounded-lg border"
                  >
                    <div className="flex items-center gap-4">
                      <div className="text-center">
                        <div className="text-lg font-bold">
                          {format(new Date(appointment.start), 'HH:mm')}
                        </div>
                        <div className="text-xs text-slate-500">
                          {format(new Date(appointment.end), 'HH:mm')}
                        </div>
                      </div>
                      <div>
                        <p className="font-medium">{appointment.clientName}</p>
                        <p className="text-sm text-slate-500">
                          {appointment.serviceName} - {appointment.barberName}
                        </p>
                      </div>
                    </div>
                    <div className="flex items-center gap-2">
                      {getStatusBadge(appointment.status)}
                    </div>
                  </div>
                ))}
            </div>
          ) : (
            <p className="text-center text-slate-500 py-8">
              Nenhum agendamento para hoje
            </p>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
