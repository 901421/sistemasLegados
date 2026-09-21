       IDENTIFICATION DIVISION.
       PROGRAM-ID. CREAR-MOVIMIENTO.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT F-MOVIMIENTOS ASSIGN TO DISK
               ORGANIZATION IS INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS MOV-NUM
               FILE STATUS IS FSM.

       DATA DIVISION.
       FILE SECTION.

       FD F-MOVIMIENTOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "movimientos.ubd".

       01 MOVIMIENTO-REG.
           02 MOV-NUM               PIC 9(35).
           02 MOV-TARJETA           PIC 9(16).
           02 MOV-ANO               PIC 9(4).
           02 MOV-MES               PIC 9(2).
           02 MOV-DIA               PIC 9(2).
           02 MOV-HOR               PIC 9(2).
           02 MOV-MIN               PIC 9(2).
           02 MOV-SEG               PIC 9(2).
           02 MOV-IMPORTE-ENT       PIC S9(7).
           02 MOV-IMPORTE-DEC       PIC 9(2).
           02 MOV-CONCEPTO          PIC X(35).
           02 MOV-SALDOPOS-ENT      PIC S9(9).
           02 MOV-SALDOPOS-DEC      PIC 9(2).

       WORKING-STORAGE SECTION.

       77 FSM                       PIC X(2).

       01 CAMPOS-FECHA.
           05 FECHA.
               10 ANO               PIC 9(4).
               10 MES               PIC 9(2).
               10 DIA               PIC 9(2).
           05 HORA.
               10 HORAS             PIC 9(2).
               10 MINUTOS           PIC 9(2).
               10 SEGUNDOS          PIC 9(2).
           05 MILISEGUNDOS          PIC 9(2).
           05 DIF-GMT               PIC S9(4).

       PROCEDURE DIVISION.

       INICIO.
           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           OPEN OUTPUT F-MOVIMIENTOS.

           IF FSM NOT = "00"
               DISPLAY "ERROR AL CREAR MOVIMIENTOS.UBD"
               DISPLAY "FILE STATUS: " FSM
               STOP RUN
           END-IF.

      *    TARJETA 1 - SALDO INICIAL 100 EUR
           INITIALIZE MOVIMIENTO-REG.
           MOVE 1 TO MOV-NUM.
           MOVE 1 TO MOV-TARJETA.
           MOVE ANO TO MOV-ANO.
           MOVE MES TO MOV-MES.
           MOVE DIA TO MOV-DIA.
           MOVE HORAS TO MOV-HOR.
           MOVE MINUTOS TO MOV-MIN.
           MOVE SEGUNDOS TO MOV-SEG.
           MOVE 100 TO MOV-IMPORTE-ENT.
           MOVE 0 TO MOV-IMPORTE-DEC.
           MOVE "Saldo inicial" TO MOV-CONCEPTO.
           MOVE 100 TO MOV-SALDOPOS-ENT.
           MOVE 0 TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG.

           IF FSM NOT = "00"
               DISPLAY "ERROR ESCRIBIENDO MOVIMIENTO 1"
               DISPLAY "FILE STATUS: " FSM
               CLOSE F-MOVIMIENTOS
               STOP RUN
           END-IF.

      *    TARJETA 2 - SALDO INICIAL 200 EUR
           INITIALIZE MOVIMIENTO-REG.
           MOVE 2 TO MOV-NUM.
           MOVE 2 TO MOV-TARJETA.
           MOVE ANO TO MOV-ANO.
           MOVE MES TO MOV-MES.
           MOVE DIA TO MOV-DIA.
           MOVE HORAS TO MOV-HOR.
           MOVE MINUTOS TO MOV-MIN.
           MOVE SEGUNDOS TO MOV-SEG.
           MOVE 200 TO MOV-IMPORTE-ENT.
           MOVE 0 TO MOV-IMPORTE-DEC.
           MOVE "Saldo inicial" TO MOV-CONCEPTO.
           MOVE 200 TO MOV-SALDOPOS-ENT.
           MOVE 0 TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG.

           IF FSM NOT = "00"
               DISPLAY "ERROR ESCRIBIENDO MOVIMIENTO 2"
               DISPLAY "FILE STATUS: " FSM
               CLOSE F-MOVIMIENTOS
               STOP RUN
           END-IF.

      *    TARJETA 3 - SALDO INICIAL 300 EUR
           INITIALIZE MOVIMIENTO-REG.
           MOVE 3 TO MOV-NUM.
           MOVE 3 TO MOV-TARJETA.
           MOVE ANO TO MOV-ANO.
           MOVE MES TO MOV-MES.
           MOVE DIA TO MOV-DIA.
           MOVE HORAS TO MOV-HOR.
           MOVE MINUTOS TO MOV-MIN.
           MOVE SEGUNDOS TO MOV-SEG.
           MOVE 300 TO MOV-IMPORTE-ENT.
           MOVE 0 TO MOV-IMPORTE-DEC.
           MOVE "Saldo inicial" TO MOV-CONCEPTO.
           MOVE 300 TO MOV-SALDOPOS-ENT.
           MOVE 0 TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG.

           IF FSM NOT = "00"
               DISPLAY "ERROR ESCRIBIENDO MOVIMIENTO 3"
               DISPLAY "FILE STATUS: " FSM
               CLOSE F-MOVIMIENTOS
               STOP RUN
           END-IF.

      *    TARJETA 4 - SALDO INICIAL 400 EUR
           INITIALIZE MOVIMIENTO-REG.
           MOVE 4 TO MOV-NUM.
           MOVE 4 TO MOV-TARJETA.
           MOVE ANO TO MOV-ANO.
           MOVE MES TO MOV-MES.
           MOVE DIA TO MOV-DIA.
           MOVE HORAS TO MOV-HOR.
           MOVE MINUTOS TO MOV-MIN.
           MOVE SEGUNDOS TO MOV-SEG.
           MOVE 400 TO MOV-IMPORTE-ENT.
           MOVE 0 TO MOV-IMPORTE-DEC.
           MOVE "Saldo inicial" TO MOV-CONCEPTO.
           MOVE 400 TO MOV-SALDOPOS-ENT.
           MOVE 0 TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG.

           IF FSM NOT = "00"
               DISPLAY "ERROR ESCRIBIENDO MOVIMIENTO 4"
               DISPLAY "FILE STATUS: " FSM
               CLOSE F-MOVIMIENTOS
               STOP RUN
           END-IF.

      *    TARJETA 5 - SALDO INICIAL 500 EUR
           INITIALIZE MOVIMIENTO-REG.
           MOVE 5 TO MOV-NUM.
           MOVE 5 TO MOV-TARJETA.
           MOVE ANO TO MOV-ANO.
           MOVE MES TO MOV-MES.
           MOVE DIA TO MOV-DIA.
           MOVE HORAS TO MOV-HOR.
           MOVE MINUTOS TO MOV-MIN.
           MOVE SEGUNDOS TO MOV-SEG.
           MOVE 500 TO MOV-IMPORTE-ENT.
           MOVE 0 TO MOV-IMPORTE-DEC.
           MOVE "Saldo inicial" TO MOV-CONCEPTO.
           MOVE 500 TO MOV-SALDOPOS-ENT.
           MOVE 0 TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG.

           IF FSM NOT = "00"
               DISPLAY "ERROR ESCRIBIENDO MOVIMIENTO 5"
               DISPLAY "FILE STATUS: " FSM
               CLOSE F-MOVIMIENTOS
               STOP RUN
           END-IF.

           CLOSE F-MOVIMIENTOS.

           DISPLAY "MOVIMIENTOS.UBD CREADO CORRECTAMENTE."
           DISPLAY "5 MOVIMIENTOS INICIALES CREADOS."

           STOP RUN.
           