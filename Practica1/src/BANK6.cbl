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

           SELECT F-TRANSFERENCIAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TRANS-NUM
           FILE STATUS IS FSTR.


       DATA DIVISION.
       FILE SECTION.
       FD TARJETAS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "tarjetas.ubd".
       01 TAJETAREG.
           02 TNUM-E      PIC 9(16).
           02 TPIN-E      PIC  9(4).
       FD F-MOVIMIENTOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "movimientos.ubd".
       01 MOVIMIENTO-REG.
           02 MOV-NUM              PIC  9(35).
           02 MOV-TARJETA          PIC  9(16).
           02 MOV-ANO              PIC   9(4).
           02 MOV-MES              PIC   9(2).
           02 MOV-DIA              PIC   9(2).
           02 MOV-HOR              PIC   9(2).
           02 MOV-MIN              PIC   9(2).
           02 MOV-SEG              PIC   9(2).
           02 MOV-IMPORTE-ENT      PIC  S9(7).
           02 MOV-IMPORTE-DEC      PIC   9(2).
           02 MOV-CONCEPTO         PIC  X(35).
           02 MOV-SALDOPOS-ENT     PIC  S9(9).
           02 MOV-SALDOPOS-DEC     PIC   9(2).

      *    UNA FILA POR TRANSFERENCIA ORDENADA (D13). TIPO: 1 INMEDIATA,
      *    2 PUNTUAL, 3 MENSUAL. FECHA: INMEDIATA = HOY, PUNTUAL = LA
      *    INDICADA, MENSUAL = SOLO EL DIA (ANO Y MES A CERO).
       FD F-TRANSFERENCIAS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "transferencias.ubd".
       01 TRANSFERENCIA-REG.
           02 TRANS-NUM            PIC  9(35).
           02 TRANS-ORIGEN         PIC  9(16).
           02 TRANS-DESTINO        PIC  9(16).
           02 TRANS-IMPORTE-ENT    PIC   9(7).
           02 TRANS-IMPORTE-DEC    PIC   9(2).
           02 TRANS-TIPO           PIC   9(1).
           02 TRANS-ANO            PIC   9(4).
           02 TRANS-MES            PIC   9(2).
           02 TRANS-DIA            PIC   9(2).
           02 TRANS-ALTA-ANO       PIC   9(4).
           02 TRANS-ALTA-MES       PIC   9(2).
           02 TRANS-ALTA-DIA       PIC   9(2).


       WORKING-STORAGE SECTION.
       77 FST                      PIC   X(2).
       77 FSM                      PIC   X(2).
       77 FSTR                     PIC   X(2).

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
               10 ANO              PIC   9(4).
               10 MES              PIC   9(2).
               10 DIA              PIC   9(2).
           05 HORA.
               10 HORAS            PIC   9(2).
               10 MINUTOS          PIC   9(2).
               10 SEGUNDOS         PIC   9(2).
               10 MILISEGUNDOS     PIC   9(2).
           05 DIF-GMT              PIC  S9(4).

       01 KEYBOARD-STATUS          PIC  9(4).
           88 ENTER-PRESSED      VALUE     0.
           88 PGUP-PRESSED       VALUE  2001.
           88 PGDN-PRESSED       VALUE  2002.
           88 UP-ARROW-PRESSED   VALUE  2003.
           88 DOWN-ARROW-PRESSED VALUE  2004.
           88 ESC-PRESSED        VALUE  2005.

       77 TECLA                  PIC   X(1).

       77 LAST-MOV-NUM             PIC  9(35).
       77 LAST-USER-ORD-MOV-NUM    PIC  9(35).
       77 LAST-USER-DST-MOV-NUM    PIC  9(35).

       77 EURENT-USUARIO           PIC  S9(7).
       77 EURDEC-USUARIO           PIC   9(2).
       77 CUENTA-DESTINO           PIC  9(16).
       77 NOMBRE-DESTINO           PIC  X(35).

      *    SALDOS EN CENTIMOS CON 11 CIFRAS: EL MISMO TAMANO QUE EL
      *    SALDO GUARDADO (S9(9) EUROS + 2 DECIMALES), COMO BANK4-5-7.
       77 CENT-SALDO-ORD-USER      PIC  S9(11).
       77 CENT-SALDO-DST-USER      PIC  S9(11).
       77 CENT-IMPOR-USER          PIC  S9(9).

       77 TIPO-TRF                 PIC   9(1).
       77 LAST-TRANS-NUM           PIC  9(35).
       77 DIA-TRF                  PIC   9(2).
       77 MES-TRF                  PIC   9(2).
       77 ANO-TRF                  PIC   9(4).
       77 DIA-MENSUAL              PIC   9(2).
       77 FECHA-TRF-N              PIC   9(8).
       77 FECHA-HOY-N              PIC   9(8).

       77 MSJ-ORD                  PIC  X(35) VALUE "Transferimos".
       77 MSJ-DST                  PIC  X(35) VALUE "Nos transfieren".

       LINKAGE SECTION.
       77 TNUM                     PIC  9(16).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 FILTRO-CUENTA.
           05 FILLER BLANK WHEN ZERO AUTO UNDERLINE
               LINE 12 COL 54 PIC 9(16) USING CUENTA-DESTINO.
           05 FILLER AUTO UNDERLINE
               LINE 14 COL 54 PIC X(15) USING NOMBRE-DESTINO.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 16 COL 54 PIC 9(7) USING EURENT-USUARIO.
           05 FILLER BLANK ZERO UNDERLINE
               LINE 16 COL 63 PIC 9(2) USING EURDEC-USUARIO.

       01 SALDO-DISPLAY.
           05 FILLER SIGN IS LEADING SEPARATE
               LINE 10 COL 33 PIC -9(9) FROM MOV-SALDOPOS-ENT.
           05 FILLER LINE 10 COL 43 VALUE ",".
           05 FILLER LINE 10 COL 44 PIC 99 FROM MOV-SALDOPOS-DEC.
           05 FILLER LINE 10 COL 47 VALUE "EUR".

       01 FORM-FECHA-TRF.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 14 COL 29 PIC 9(2) USING DIA-TRF.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 14 COL 32 PIC 9(2) USING MES-TRF.
           05 FILLER BLANK ZERO UNDERLINE
               LINE 14 COL 35 PIC 9(4) USING ANO-TRF.

       01 FORM-DIA-MENSUAL.
           05 FILLER BLANK ZERO UNDERLINE
               LINE 14 COL 34 PIC 9(2) USING DIA-MENSUAL.


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
           INITIALIZE TIPO-TRF.
           INITIALIZE DIA-TRF.
           INITIALIZE MES-TRF.
           INITIALIZE ANO-TRF.
           INITIALIZE DIA-MENSUAL.

       IMPRIMIR-CABECERA.
           DISPLAY BLANK-SCREEN.
           DISPLAY "Cajero Automatico UnizarBank" AT 0226
               WITH FOREGROUND-COLOR IS BLUE HIGHLIGHT.

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
           IF FSM <> "00" THEN
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

           DISPLAY "ESC - Cancelar" AT 2466.

           IF LAST-USER-ORD-MOV-NUM = 0 THEN
               GO TO NO-MOVIMIENTOS
           END-IF.

           MOVE LAST-USER-ORD-MOV-NUM TO MOV-NUM.

           PERFORM MOVIMIENTOS-OPEN THRU MOVIMIENTOS-OPEN.
           READ F-MOVIMIENTOS INVALID KEY GO PSYS-ERR.
           DISPLAY SALDO-DISPLAY.
           CLOSE F-MOVIMIENTOS.

      *    TIPO DE TRANSFERENCIA (OBJ. 2): INMEDIATA, PUNTUAL O MENSUAL.
       ELEGIR-TIPO.
           DISPLAY "1 - Transferencia inmediata" AT 1219.
           DISPLAY "2 - Transferencia puntual (en una fecha)" AT 1319.
           DISPLAY "3 - Transferencia mensual (un dia de cada mes)"
               AT 1419.
           DISPLAY "Seleccione el tipo de transferencia:" AT 1619.

       ACEPTAR-TIPO.
           ACCEPT TIPO-TRF AT 1656
               WITH AUTO
               NO-ECHO
               SIZE 1
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   ELSE
                       GO TO ACEPTAR-TIPO
                   END-IF
           END-ACCEPT.

           IF TIPO-TRF < 1 OR TIPO-TRF > 3
               GO TO ACEPTAR-TIPO
           END-IF.

       PANTALLA-CTA-DST.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ordenar Transferencia" AT 0830.
           DISPLAY "Saldo Actual:" AT 1019.
           DISPLAY SALDO-DISPLAY.
           DISPLAY "Enter - Confirmar" AT 2402.
           DISPLAY "ESC - Cancelar" AT 2466.

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

           IF CENT-IMPOR-USER = 0 THEN
               DISPLAY " " AT 2001 WITH ERASE EOL
               DISPLAY "Indique una cantidad mayor que cero" AT 2019
                   WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO INDICAR-CTA-DST
           END-IF.

      *    MINIMO 1 EUR (VER BANK4): CON PARTE ENTERA 0 EL CARGO
      *    QUEDARIA EN POSITIVO EN LOS MOVIMIENTOS.
           IF CENT-IMPOR-USER < 100 THEN
               DISPLAY " " AT 2001 WITH ERASE EOL
               DISPLAY "El importe minimo es 1 EUR" AT 2019
                   WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO INDICAR-CTA-DST
           END-IF.

           IF CENT-IMPOR-USER > CENT-SALDO-ORD-USER THEN
                   DISPLAY " " AT 2001 WITH ERASE EOL
                   DISPLAY "Indique una cantidad menor!!" AT 2019
                    WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                         BACKGROUND-COLOR IS RED
                   GO TO INDICAR-CTA-DST
           END-IF.

      *    A LA PROPIA CUENTA NO: EL ORIGINAL CALCULABA EL ABONO SOBRE
      *    EL SALDO ANTERIOR AL CARGO Y EL SALDO AUMENTABA.
           IF CUENTA-DESTINO = TNUM THEN
               DISPLAY " " AT 2001 WITH ERASE EOL
               DISPLAY "La cuenta destino debe ser distinta de la suya"
                   AT 2019 WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO INDICAR-CTA-DST
           END-IF.

           IF TIPO-TRF = 2
               GO TO PEDIR-FECHA-TRF
           END-IF.
           IF TIPO-TRF = 3
               GO TO PEDIR-DIA-MENSUAL
           END-IF.

           GO TO REALIZAR-TRF-VERIFICACION.

      *    PUNTUAL: FECHA REAL Y NO ANTERIOR A HOY (D4, D5).
       PEDIR-FECHA-TRF.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Transferencia puntual" AT 0830.
           DISPLAY "Indique la fecha en que se ejecutara" AT 1219.
           DISPLAY "Fecha:" AT 1419.
           DISPLAY "/" AT 1431.
           DISPLAY "/" AT 1434.
           DISPLAY "(dd/mm/aaaa)" AT 1441.
           DISPLAY "Enter - Confirmar" AT 2402.
           DISPLAY "ESC - Cancelar" AT 2466.

       ACEPTAR-FECHA-TRF.
           ACCEPT FORM-FECHA-TRF ON EXCEPTION
               IF ESC-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO ACEPTAR-FECHA-TRF
               END-IF
           END-ACCEPT.

           COMPUTE FECHA-TRF-N = ANO-TRF * 10000 + MES-TRF * 100
                                 + DIA-TRF.
           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.
           COMPUTE FECHA-HOY-N = ANO * 10000 + MES * 100 + DIA.

           IF FUNCTION TEST-DATE-YYYYMMDD(FECHA-TRF-N) NOT = 0
               DISPLAY " " AT 1801 WITH ERASE EOL
               DISPLAY "Fecha incorrecta" AT 1819
                   WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO ACEPTAR-FECHA-TRF
           END-IF.

           IF FECHA-TRF-N < FECHA-HOY-N
               DISPLAY " " AT 1801 WITH ERASE EOL
               DISPLAY "La fecha no puede ser anterior a hoy" AT 1819
                   WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO ACEPTAR-FECHA-TRF
           END-IF.

           GO TO REALIZAR-TRF-VERIFICACION.

      *    MENSUAL: DIA 1-31 (29-31 EN MESES CORTOS: ULTIMO DIA, D6).
       PEDIR-DIA-MENSUAL.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Transferencia mensual" AT 0830.
           DISPLAY "Indique el dia de cada mes en que se ejecutara"
               AT 1219.
           DISPLAY "Dia del mes:" AT 1419.
           DISPLAY "(1-31)" AT 1437.
           DISPLAY "Enter - Confirmar" AT 2402.
           DISPLAY "ESC - Cancelar" AT 2466.

       ACEPTAR-DIA-MENSUAL.
           ACCEPT FORM-DIA-MENSUAL ON EXCEPTION
               IF ESC-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO ACEPTAR-DIA-MENSUAL
               END-IF
           END-ACCEPT.

           IF DIA-MENSUAL < 1 OR DIA-MENSUAL > 31
               DISPLAY "Dia incorrecto" AT 1819
                   WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO ACEPTAR-DIA-MENSUAL
           END-IF.

           GO TO REALIZAR-TRF-VERIFICACION.

       NO-MOVIMIENTOS.
           DISPLAY "Enter - Confirmar" AT 2402.
           DISPLAY "0" AT 1051.
           DISPLAY "," AT 1052.
           DISPLAY "00" AT 1053.
           DISPLAY "EUR" AT 1056.

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
            WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                 BACKGROUND-COLOR IS RED.

           GO TO NO-MOVIMIENTOS.

       REALIZAR-TRF-VERIFICACION.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ordenar Transferencia" AT 0830.
           DISPLAY "Va a transferir:" AT 1119.
           DISPLAY EURENT-USUARIO AT 1138.
           DISPLAY "," AT 1145.
           DISPLAY EURDEC-USUARIO AT 1146.
           DISPLAY "EUR de su cuenta" AT 1149.
           DISPLAY "a la cuenta cuyo titular es" AT 1219.
           DISPLAY NOMBRE-DESTINO AT 1248.

           IF TIPO-TRF = 2
               DISPLAY "Fecha de ejecucion:" AT 1419
               DISPLAY DIA-TRF AT 1439
               DISPLAY "-" AT 1441
               DISPLAY MES-TRF AT 1442
               DISPLAY "-" AT 1444
               DISPLAY ANO-TRF AT 1445
           END-IF.
           IF TIPO-TRF = 3
               DISPLAY "Todos los meses, el dia" AT 1419
               DISPLAY DIA-MENSUAL AT 1443
           END-IF.

           DISPLAY "Enter - Confirmar" AT 2402.
           DISPLAY "ESC - Cancelar" AT 2466.

       ENTER-VERIFICACION.
      *    CON UN CAMPO, ENTER TAMBIEN LLEGA COMO EXCEPCION (3.C).
           ACCEPT TECLA AT 2480 WITH NO-ECHO ON EXCEPTION
           IF ESC-PRESSED THEN
               EXIT PROGRAM
           END-IF
           IF NOT ENTER-PRESSED THEN
               GO TO ENTER-VERIFICACION
           END-IF.

       VERIFICACION-CTA-CORRECTA.
           OPEN I-O TARJETAS.
           IF FST <> "00"
              GO TO PSYS-ERR.

           MOVE CUENTA-DESTINO TO TNUM-E.
           READ TARJETAS INVALID KEY GO TO USER-BAD.
           CLOSE TARJETAS.

      *    LAS PROGRAMADAS SOLO SE REGISTRAN: LAS EJECUTA EL BANCO (D3).
           IF TIPO-TRF NOT = 1
               GO TO REGISTRAR-TRF
           END-IF.

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

      *    SALDO NUEVO DEL DESTINO ANTES DE ESCRIBIR NADA: SI NO CABE,
      *    SE RECHAZA SIN DEJAR UN CARGO SIN SU ABONO.
           ADD CENT-IMPOR-USER TO CENT-SALDO-DST-USER
               ON SIZE ERROR GO TO PSYS-ERR
           END-ADD.

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

           COMPUTE MOV-SALDOPOS-ENT = (CENT-SALDO-DST-USER / 100).
           MOVE FUNCTION MOD(CENT-SALDO-DST-USER, 100)
               TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG INVALID KEY GO TO PSYS-ERR.

           CLOSE F-MOVIMIENTOS.

      *    TODAS LAS TRANSFERENCIAS SE ANOTAN EN TRANSFERENCIAS.UBD
      *    PARA EL LISTADO (D10, D13).
       REGISTRAR-TRF.
           OPEN I-O F-TRANSFERENCIAS.
           IF FSTR <> "00"
               GO TO PSYS-ERR
           END-IF.
           MOVE 0 TO LAST-TRANS-NUM.

       LEER-TRANSFERENCIAS.
           READ F-TRANSFERENCIAS NEXT RECORD
               AT END GO TO ESCRIBIR-TRF
           END-READ.
           IF TRANS-NUM > LAST-TRANS-NUM
               MOVE TRANS-NUM TO LAST-TRANS-NUM
           END-IF.
           GO TO LEER-TRANSFERENCIAS.

       ESCRIBIR-TRF.
           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.
           ADD 1 TO LAST-TRANS-NUM.

           MOVE LAST-TRANS-NUM  TO TRANS-NUM.
           MOVE TNUM            TO TRANS-ORIGEN.
           MOVE CUENTA-DESTINO  TO TRANS-DESTINO.
           MOVE EURENT-USUARIO  TO TRANS-IMPORTE-ENT.
           MOVE EURDEC-USUARIO  TO TRANS-IMPORTE-DEC.
           MOVE TIPO-TRF        TO TRANS-TIPO.
           MOVE ANO             TO TRANS-ALTA-ANO.
           MOVE MES             TO TRANS-ALTA-MES.
           MOVE DIA             TO TRANS-ALTA-DIA.

           IF TIPO-TRF = 1
               MOVE ANO         TO TRANS-ANO
               MOVE MES         TO TRANS-MES
               MOVE DIA         TO TRANS-DIA
           END-IF.
           IF TIPO-TRF = 2
               MOVE ANO-TRF     TO TRANS-ANO
               MOVE MES-TRF     TO TRANS-MES
               MOVE DIA-TRF     TO TRANS-DIA
           END-IF.
           IF TIPO-TRF = 3
               MOVE 0           TO TRANS-ANO
               MOVE 0           TO TRANS-MES
               MOVE DIA-MENSUAL TO TRANS-DIA
           END-IF.

           WRITE TRANSFERENCIA-REG INVALID KEY GO TO PSYS-ERR.
           CLOSE F-TRANSFERENCIAS.

       P-EXITO.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.

           DISPLAY "Ordenar transferencia" AT 0830.
           IF TIPO-TRF = 1
               DISPLAY "Transferencia realizada correctamente!" AT 1119
           ELSE
               DISPLAY "Transferencia programada correctamente!"
                   AT 1119
           END-IF.
           DISPLAY "Enter - Aceptar" AT 2433.

           GO TO EXIT-ENTER.

       PSYS-ERR.
           CLOSE TARJETAS.
           CLOSE F-MOVIMIENTOS.
           CLOSE F-TRANSFERENCIAS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ha ocurrido un error interno" AT 0925
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Vuelva mas tarde" AT 1132
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar" AT 2433.

       EXIT-ENTER.
           ACCEPT TECLA AT 2480 WITH NO-ECHO
           IF ENTER-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO EXIT-ENTER.

       USER-BAD.
           CLOSE TARJETAS.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "La cuenta introducida es incorrecta" AT 0922
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Salir" AT 2433.
           GO TO EXIT-ENTER.
