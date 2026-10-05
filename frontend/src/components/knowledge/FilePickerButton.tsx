import { Button, type ButtonProps } from '@mui/material'
import { useRef, type ReactNode } from 'react'

/** A real button that opens the file chooser (keyboard accessible, no role on a label). */
export function FilePickerButton({ accept, onFile, children, ...button }: Omit<ButtonProps, 'onClick'> & {
  accept?: string; onFile: (file: File) => void; children: ReactNode
}) {
  const input = useRef<HTMLInputElement>(null)
  return (
    <>
      <Button {...button} onClick={() => input.current?.click()}>{children}</Button>
      <input ref={input} type="file" hidden accept={accept} tabIndex={-1} aria-hidden
        onChange={(e) => { const f = e.target.files?.[0]; e.target.value = ''; if (f) onFile(f) }} />
    </>
  )
}
