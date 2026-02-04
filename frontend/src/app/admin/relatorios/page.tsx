'use client'

import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { api } from '@/lib/api'
import { formatCurrency } from '@/lib/utils'
import type { PeriodReport } from '@/types'
import { format, subDays, startOfMonth, endOfMonth } from 'date-fns'

import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Loader2, Calendar, DollarSign, Users, XCircle, AlertTriangle } from 'lucide-react'

export default function RelatoriosPage() {
  const [period, setPeriod] = useState<'week' | 'month' | 'custom'>('week')
  const [startDate, setStartDate] = useState(format(subDays(new Date(), 7), 'yyyy-MM-dd'))
  const [endDate, setEndDate] = useState(format(new Date(), 'yyyy-MM-dd'))

  const getDateRange = () => {
    const now = new Date()
    switch (period) {
      case 'week':
        return {
          start: format(subDays(now, 7), 'yyyy-MM-dd'),
          end: format(now, 'yyyy-MM-dd'),
        }
      case 'month':
        return {
          start: format(startOfMonth(now), 'yyyy-MM-dd'),
          end: format(endOfMonth(now), 'yyyy-MM-dd'),
        }
      default:
        return { start: startDate, end: endDate }
    }
  }

  const dateRange = getDateRange()

  const { data: report, isLoading } = useQuery({
    queryKey: ['report', dateRange.start, dateRange.end],
    queryFn: async () => {
      const { data } = await api.get<PeriodReport>('/api/admin/reports/period', {
        params: {
          startDate: dateRange.start,
          endDate: dateRange.end,
        },
      })
      return data
    },
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-2xl font-bold">Relatórios</h1>
        <div className="flex gap-2">
          <Button
            variant={period === 'week' ? 'default' : 'outline'}
            onClick={() => setPeriod('week')}
          >
            7 dias
          </Button>
          <Button
            variant={period === 'month' ? 'default' : 'outline'}
            onClick={() => setPeriod('month')}
          >
            Este mês
          </Button>
        </div>
      </div>

      {isLoading ? (
        <div className="flex justify-center py-12">
          <Loader2 className="h-8 w-8 animate-spin" />
        </div>
      ) : report ? (
        <>
          {/* Summary Cards */}
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Total Agendamentos</CardTitle>
                <Calendar className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{report.totalAppointments}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Concluídos</CardTitle>
                <Users className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{report.completedAppointments}</div>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">Cancelados</CardTitle>
                <XCircle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{report.cancelledAppointments}</div>
                <p className="text-xs text-muted-foreground">
                  {report.cancellationRate.toFixed(1)}% de cancelamento
                </p>
              </CardContent>
            </Card>

            <Card>
              <CardHeader className="flex flex-row items-center justify-between space-y-0 pb-2">
                <CardTitle className="text-sm font-medium">No-shows</CardTitle>
                <AlertTriangle className="h-4 w-4 text-muted-foreground" />
              </CardHeader>
              <CardContent>
                <div className="text-2xl font-bold">{report.noShowAppointments}</div>
                <p className="text-xs text-muted-foreground">
                  {report.noShowRate.toFixed(1)}% de no-show
                </p>
              </CardContent>
            </Card>
          </div>

          {/* Revenue Card */}
          <Card>
            <CardHeader>
              <CardTitle>Faturamento</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="grid gap-6 md:grid-cols-2">
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Faturamento Real</p>
                  <p className="text-3xl font-bold text-green-600">
                    {formatCurrency(report.actualRevenue)}
                  </p>
                  <p className="text-xs text-muted-foreground mt-1">
                    Baseado em agendamentos concluídos
                  </p>
                </div>
                <div>
                  <p className="text-sm text-muted-foreground mb-1">Faturamento Estimado</p>
                  <p className="text-3xl font-bold">
                    {formatCurrency(report.estimatedRevenue)}
                  </p>
                  <p className="text-xs text-muted-foreground mt-1">
                    Se todos agendamentos fossem concluídos
                  </p>
                </div>
              </div>
            </CardContent>
          </Card>

          {/* Period Info */}
          <Card>
            <CardHeader>
              <CardTitle>Período Analisado</CardTitle>
            </CardHeader>
            <CardContent>
              <p className="text-muted-foreground">
                {format(new Date(report.startDate), 'dd/MM/yyyy')} até{' '}
                {format(new Date(report.endDate), 'dd/MM/yyyy')}
              </p>
            </CardContent>
          </Card>
        </>
      ) : (
        <p className="text-center text-slate-500">Nenhum dado disponível</p>
      )}
    </div>
  )
}
