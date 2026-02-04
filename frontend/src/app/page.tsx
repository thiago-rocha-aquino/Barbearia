'use client'

import { useEffect } from 'react'
import { useRouter } from 'next/navigation'
import { Scissors } from 'lucide-react'
import { Button } from '@/components/ui/button'
import Link from 'next/link'

export default function HomePage() {
  return (
    <div className="min-h-screen bg-gradient-to-b from-slate-900 to-slate-800">
      <div className="container mx-auto px-4 py-16">
        <div className="flex flex-col items-center justify-center text-center">
          <div className="mb-8 rounded-full bg-primary/10 p-6">
            <Scissors className="h-16 w-16 text-primary" />
          </div>

          <h1 className="mb-4 text-4xl font-bold text-white md:text-5xl">
            Barbearia
          </h1>

          <p className="mb-8 max-w-md text-lg text-slate-300">
            Agende seu horário online de forma rápida e prática.
            Escolha o serviço, o profissional e o melhor horário para você.
          </p>

          <div className="flex flex-col gap-4 sm:flex-row">
            <Link href="/agendar">
              <Button size="lg" className="w-full sm:w-auto">
                Agendar Agora
              </Button>
            </Link>
            <Link href="/login">
              <Button size="lg" variant="outline" className="w-full sm:w-auto">
                Acesso Admin
              </Button>
            </Link>
          </div>

          <div className="mt-16 grid gap-8 md:grid-cols-3">
            <div className="rounded-lg bg-slate-800/50 p-6">
              <h3 className="mb-2 text-lg font-semibold text-white">
                Escolha o Serviço
              </h3>
              <p className="text-slate-400">
                Selecione entre corte, barba, combo e mais
              </p>
            </div>
            <div className="rounded-lg bg-slate-800/50 p-6">
              <h3 className="mb-2 text-lg font-semibold text-white">
                Horário Flexível
              </h3>
              <p className="text-slate-400">
                Veja os horários disponíveis em tempo real
              </p>
            </div>
            <div className="rounded-lg bg-slate-800/50 p-6">
              <h3 className="mb-2 text-lg font-semibold text-white">
                Confirmação Imediata
              </h3>
              <p className="text-slate-400">
                Receba confirmação por email instantaneamente
              </p>
            </div>
          </div>
        </div>
      </div>
    </div>
  )
}
