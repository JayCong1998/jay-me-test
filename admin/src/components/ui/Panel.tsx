import * as React from 'react'
import { cn } from '@/lib/utils'

export function Panel({ className, ...props }: React.HTMLAttributes<HTMLDivElement>) {
  return (
    <section
      className={cn('rounded-lg border border-border bg-white p-4 shadow-sm', className)}
      {...props}
    />
  )
}
