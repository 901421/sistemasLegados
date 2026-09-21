       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK6.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TARJETAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TNUM-E
           FILE STATUS IS FST.

           SELECT F-MOVIMIENTOS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS MOV-NUM
           FILE STATUS IS FSM.


       DATA DIVISION.
       FILE SECTION.
       FD TARJETAS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "tarjetas.ubd".
       01 TAJETAREG.
           02 TNUM-E      PIC S9(16).
           02 TPIN-E      PIC  S9(4).
       FD F-MOVIMIENTOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "movimientos.ubd".
       01 MOVIMIENTO-REG.
           02 MOV-NUM              PIC  S9(35).
           02 MOV-TARJETA          PIC  S9(16).
           02 MOV-ANO              PIC   S9(4).
           02 MOV-MES              PIC   S9(2).
           02 MOV-DIA              PIC   S9(2).
           02 MOV-HOR              PIC   S9(2).
           02 MOV-MIN              PIC   S9(2).
           02 MOV-SEG              PIC   S9(2).
           02 MOV-IMPORTE-ENT      PIC  S9(7).
           02 MOV-IMPORTE-DEC      PIC   S9(2).
           02 MOV-CONCEPTO         PIC  X(35).
           02 MOV-SALDOPOS-ENT     PIC  S9(9).
           02 MOV-SALDOPOS-DEC     PIC   S9(2).


       WORKING-STORAGE SECTION.
       77 FST                      PIC   X(2).
       77 FSM                      PIC   X(2).

       78 BLACK                  VALUE      0.
       78 BLUE                   VALUE      1.
       78 GREEN                  VALUE      2.
       78 CYAN                   VALUE      3.
       78 RED                    VALUE      4.
       78 MAGENTA                VALUE      5.
       78 YELLOW                 VALUE      6.
       78 WHITE                  VALUE      7.

       01 CAMPOS-FECHA.
           05 FECHA.
               10 ANO              PIC   S9(4).
               10 MES              PIC   S9(2).
               10 DIA              PIC   S9(2).
           05 HORA.
               10 HORAS            PIC   S9(2).
               10 MINUTOS          PIC   S9(2).
               10 SEGUNDOS         PIC   S9(2).
               10 MILISEGUNDOS     PIC   S9(2).
           05 DIF-GMT              PIC  S9(4).

       01 KEYBOARD-STATUS          PIC  S9(4).
           88 ENTER-PRESSED      VALUE     0.
           88 PGUP-PRESSED       VALUE  2001.
           88 PGDN-PRESSED       VALUE  2002.
           88 UP-ARROW-PRESSED   VALUE  2003.
           88 DOWN-ARROW-PRESSED VALUE  2004.
           88 ESC-PRESSED        VALUE  2005.

       77 PRESSED-KEY              PIC   S9(4).

       77 LAST-MOV-NUM             PIC  S9(35).
       77 LAST-USER-ORD-MOV-NUM    PIC  S9(35).
       77 LAST-USER-DST-MOV-NUM    PIC  S9(35).

       77 EURENT-USUARIO           PIC  9(7).
       77 EURDEC-USUARIO           PIC   S9(2).
       77 CUENTA-DESTINO           PIC  9(16).
       77 NOMBRE-DESTINO           PIC  X(35).

       77 CENT-SALDO-ORD-USER      PIC  S9(9).
       77 CENT-SALDO-DST-USER      PIC  S9(9).
       77 CENT-IMPOR-USER          PIC  S9(9).

       77 MSJ-ORD                  PIC  X(35) VALUE "Transferimos".
       77 MSJ-DST                  PIC  X(35) VALUE "Nos transfieren".

       LINKAGE SECTION.
       77 TNUM                     PIC  S9(16).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 FILTRO-CUENTA.
           05 FILLER AUTO UNDERLINE
               LINE 12 COL 54 PIC S9(16) USING CUENTA-DESTINO.
           05 FILLER AUTO UNDERLINE
               LINE 14 COL 54 PIC X(15) USING NOMBRE-DESTINO.
           05 FILLER AUTO UNDERLINE
               SIGN IS LEADING SEPARATE
               LINE 16 COL 54 PIC S9(7) USING EURENT-USUARIO.
           05 FILLER UNDERLINE
               LINE 16 COL 63 PIC S9(2) USING EURDEC-USUARIO.

       01 SALDO-DISPLAY.
           05 FILLER SIGN IS LEADING SEPARATE
               LINE 10 COL 33 PIC S9(7) FROM MOV-SALDOPOS-ENT.
           05 FILLER LINE 10 COL 41 VALUE ",".
           05 FILLER LINE 10 COL 42 PIC S99 FROM MOV-SALDOPOS-DEC.
           05 FILLER LINE 10 COL 45 VALUE "EUR".


       PROCEDURE DIVISION USING TNUM.
       INICIO.
           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'.

           INITIALIZE CUENTA-DESTINO.
           INITIALIZE NOMBRE-DESTINO.
           INITIALIZE EURENT-USUARIO.
           INITIALIZE EURDEC-USUARIO.
           INITIALIZE LAST-MOV-NUM.
           INITIALIZE LAST-USER-ORD-MOV-NUM.
           INITIALIZE LAST-USER-DST-MOV-NUM.

       IMPRIMIR-CABECERA.
           DISPLAY BLANK-SCREEN.
           DISPLAY "Cajero Automatico UnizarBank" AT 0226
               WITH FOREGROUND-COLOR IS 1.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           DISPLAY DIA AT 0432.
           DISPLAY "-" AT 0434.
           DISPLAY MES AT 0435.
           DISPLAY "-" AT 0437.
           DISPLAY ANO AT 0438.
           DISPLAY HORAS AT 0444.
           DISPLAY ":" AT 0446.
           DISPLAY MINUTOS AT 0447.

       MOVIMIENTOS-OPEN.
           OPEN I-O F-MOVIMIENTOS.
           IF FSM <> 30 THEN
               GO TO PSYS-ERR
           END-IF.

       LECTURA-MOVIMIENTOS.
           READ F-MOVIMIENTOS NEXT RECORD AT END GO TO ORDENACION-TRF.
           IF MOV-TARJETA = TNUM THEN
               IF LAST-USER-ORD-MOV-NUM < MOV-NUM THEN
                   MOVE MOV-NUM TO LAST-USER-ORD-MOV-NUM
               END-IF
           END-IF.
           IF LAST-MOV-NUM < MOV-NUM THEN
               MOVE MOV-NUM TO LAST-MOV-NUM
           END-IF.
           GO TO LECTURA-MOVIMIENTOS.

       ORDENACION-TRF.
           CLOSE F-MOVIMIENTOS.

           DISPLAY "Ordenar Transferencia" AT 0830.
           DISPLAY "Saldo Actual:" AT 1019.

           DISPLAY "Enter - Confirmar" 2402.
           DISPLAY "ESC - Cancelar" 2466.

           IF LAST-USER-ORD-MOV-NUM = 0 THEN
               GO TO NO-MOVIMIENTOS
           END-IF.

           MOVE LAST-USER-ORD-MOV-NUM TO MOV-NUM.

           PERFORM MOVIMIENTOS-OPEN THRU MOVIMIENTOS-OPEN.
           READ F-MOVIMIENTOS INVALID KEY GO PSYS-ERR.
           DISPLAY SALDO-DISPLAY.
           CLOSE F-MOVIMIENTOS.

       INDICAR-CTA-DST.
           DISPLAY "Indica la cuenta destino" AT 1219.
           DISPLAY "y nombre del titular" AT 1419.
           DISPLAY "Indique la cantidad a transferir" AT 1619.
           DISPLAY "," AT 1661.
           DISPLAY "EUR" AT 1666.

           COMPUTE CENT-SALDO-ORD-USER = (MOV-SALDOPOS-ENT * 100)
                                         + MOV-SALDOPOS-DEC.

           ACCEPT FILTRO-CUENTA ON EXCEPTION
           IF ESC-PRESSED THEN
               EXIT PROGRAM
           ELSE
               GO TO INDICAR-CTA-DST
           END-IF.

           COMPUTE CENT-IMPOR-USER = (EURENT-USUARIO * 100)
                                     + EURDEC-USUARIO.

           IF CENT-IMPOR-USER > CENT-SALDO-ORD-USER THEN
                   DISPLAY "Indique una cantidad menor!!" AT 2019
                    WITH BACKGROUND-COLOR RED
                   GO TO INDICAR-CTA-DST
           END-IF.

           GO TO REALIZAR-TRF-VERIFICACION.

       NO-MOVIMIENTOS.
           DISPLAY "0" AT 1051.
           DISPLAY "." AT 1052.
           DISPLAY "00" AT 1053.
           DISPLAY "EUR" AT 1054.

           DISPLAY "Indica la cuenta destino " AT 1219.
           DISPLAY "y nombre del titular" AT 1419.
           DISPLAY "Indique la cantidad a transferir" AT 1619.
           DISPLAY "," AT 1661.
           DISPLAY "EUR" AT 1666.

           ACCEPT FILTRO-CUENTA ON EXCEPTION
           IF ESC-PRESSED THEN
               EXIT PROGRAM
           END-IF.

           DISPLAY "Indique una cantidad menor!!" AT 2019
            WITH BACKGROUND-COLOR RED.

           GO TO NO-MOVIMIENTOS.

       REALIZAR-TRF-VERIFICACION.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ordenar Transferencia" AT 0830.
           DISPLAY "Va a transferir:" AT 1119.
           DISPLAY EURENT-USUARIO AT 1138.
           DISPLAY "." AT 1145.
           DISPLAY EURDEC-USUARIO AT 1146.
           DISPLAY "EUR de su cuenta" AT 1149.
           DISPLAY "a la cuenta cuyo titular es" AT 1219.
           DISPLAY NOMBRE-DESTINO AT 1248.

           DISPLAY "Enter - Confirmar" AT 2402.
           DISPLAY "ESC - Cancelar" AT 2466.

       ENTER-VERIFICACION.
           ACCEPT PRESSED-KEY AT 2480 ON EXCEPTION
           IF ESC-PRESSED THEN
               EXIT PROGRAM
           ELSE
               GO TO ENTER-VERIFICACION
           END-IF.

       VERIFICACION-CTA-CORRECTA.
           OPEN I-O TARJETAS.
           IF FST <> 30
              GO TO PSYS-ERR.

           MOVE CUENTA-DESTINO TO TNUM-E.
           READ TARJETAS INVALID KEY GO TO USER-BAD.
           CLOSE TARJETAS.

           PERFORM MOVIMIENTOS-OPEN THRU MOVIMIENTOS-OPEN.
           MOVE 0 TO MOV-NUM.
           MOVE 0 TO LAST-USER-DST-MOV-NUM.

       LECTURA-SALDO-DST.
           READ F-MOVIMIENTOS NEXT RECORD AT END GO TO GUARDAR-TRF.
           IF MOV-TARJETA = CUENTA-DESTINO THEN
               IF LAST-USER-DST-MOV-NUM < MOV-NUM THEN
                   MOVE MOV-NUM TO LAST-USER-DST-MOV-NUM
               END-IF
           END-IF.

           GO TO LECTURA-SALDO-DST.

       GUARDAR-TRF.
           CLOSE F-MOVIMIENTOS.
           MOVE LAST-USER-DST-MOV-NUM TO MOV-NUM.
           PERFORM MOVIMIENTOS-OPEN THRU MOVIMIENTOS-OPEN.
           READ F-MOVIMIENTOS INVALID KEY GO PSYS-ERR.

           COMPUTE CENT-SALDO-DST-USER = (MOV-SALDOPOS-ENT * 100)
                                         + MOV-SALDOPOS-DEC.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           ADD 1 TO LAST-MOV-NUM.

           MOVE LAST-MOV-NUM   TO MOV-NUM.
           MOVE TNUM           TO MOV-TARJETA.
           MOVE ANO            TO MOV-ANO.
           MOVE MES            TO MOV-MES.
           MOVE DIA            TO MOV-DIA.
           MOVE HORAS          TO MOV-HOR.
           MOVE MINUTOS        TO MOV-MIN.
           MOVE SEGUNDOS       TO MOV-SEG.

           MULTIPLY -1 BY EURENT-USUARIO.
           MOVE EURENT-USUARIO TO MOV-IMPORTE-ENT.
           MULTIPLY -1 BY EURENT-USUARIO.
           MOVE EURDEC-USUARIO TO MOV-IMPORTE-DEC.

           MOVE MSJ-ORD        TO MOV-CONCEPTO.

           SUBTRACT CENT-IMPOR-USER FROM CENT-SALDO-ORD-USER.

           COMPUTE MOV-SALDOPOS-ENT = (CENT-SALDO-ORD-USER / 100).
           MOVE FUNCTION MOD(CENT-SALDO-ORD-USER, 100)
               TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG INVALID KEY GO TO PSYS-ERR.

           ADD 1 TO LAST-MOV-NUM.

           MOVE LAST-MOV-NUM   TO MOV-NUM.
           MOVE CUENTA-DESTINO TO MOV-TARJETA.
           MOVE ANO            TO MOV-ANO.
           MOVE MES            TO MOV-MES.
           MOVE DIA            TO MOV-DIA.
           MOVE HORAS          TO MOV-HOR.
           MOVE MINUTOS        TO MOV-MIN.
           MOVE SEGUNDOS       TO MOV-SEG.

           MOVE EURENT-USUARIO TO MOV-IMPORTE-ENT.
           MOVE EURDEC-USUARIO TO MOV-IMPORTE-DEC.

           MOVE MSJ-DST        TO MOV-CONCEPTO.

           ADD CENT-IMPOR-USER TO CENT-SALDO-DST-USER.
           COMPUTE MOV-SALDOPOS-ENT = (CENT-SALDO-DST-USER / 100).
           MOVE FUNCTION MOD(CENT-SALDO-DST-USER, 100)
               TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG INVALID KEY GO TO PSYS-ERR.

           CLOSE F-MOVIMIENTOS.

       P-EXITO.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.

           DISPLAY "Ordenar transferencia" AT 0830.
           DISPLAY "Transferencia realizada correctamente!" AT 1119.
           DISPLAY "Enter - Aceptar" AT 2433.

           GO TO EXIT-ENTER.

       PSYS-ERR.
           CLOSE TARJETAS.
           CLOSE F-MOVIMIENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ha ocurrido un error interno" AT 0925
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Vuelva mas tarde" AT 1132
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar" AT 2433.

       EXIT-ENTER.
           ACCEPT PRESSED-KEY AT 2480
           IF ENTER-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO EXIT-ENTER.

       USER-BAD.
           CLOSE TARJETAS.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "La cuenta introducida es incorrecta" AT 0922
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Salir" AT 2433.
           GO TO EXIT-ENTER.
