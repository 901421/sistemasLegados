       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK5.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

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
           02 MOV-NUM               PIC  9(35).
           02 MOV-TARJETA           PIC  9(16).
           02 MOV-ANO               PIC   9(4).
           02 MOV-MES               PIC   9(2).
           02 MOV-DIA               PIC   9(2).
           02 MOV-HOR               PIC   9(2).
           02 MOV-MIN               PIC   9(2).
           02 MOV-SEG               PIC   9(2).
           02 MOV-IMPORTE-ENT       PIC  S9(7).
           02 MOV-IMPORTE-DEC       PIC   9(2).
           02 MOV-CONCEPTO          PIC  X(35).
           02 MOV-SALDOPOS-ENT     PIC  S9(9).
           02 MOV-SALDOPOS-DEC     PIC   9(2).
       
       WORKING-STORAGE SECTION.
       77 FSM                       PIC   X(2).
       
       78 BLACK                     VALUE      0.
       78 BLUE                      VALUE      1.
       78 GREEN                     VALUE      2.
       78 CYAN                      VALUE      3.
       78 RED                       VALUE      4.
       78 MAGENTA                   VALUE      5.
       78 YELLOW                    VALUE      6.
       78 WHITE                     VALUE      7.
       
       01 CAMPOS-FECHA.
           05 FECHA.
               10 ANO               PIC   9(4).
               10 MES               PIC   9(2).
               10 DIA               PIC   9(2).
           05 HORA.
               10 HORAS             PIC   9(2).
               10 MINUTOS           PIC   9(2).
               10 SEGUNDOS          PIC   9(2).
               10 MILISEGUNDOS      PIC   9(2).
           05 DIF-GMT               PIC  S9(4).

       01 KEYBOARD-STATUS           PIC   9(4).

           88 ENTER-PRESSED         VALUE      0.
           88 PGUP-PRESSED          VALUE   2001.
           88 PGDN-PRESSED          VALUE   2002.
           88 UP-ARROW-PRESSED      VALUE   2003.
           88 DOWN-ARROW-PRESSED    VALUE   2004.
           88 ESC-PRESSED           VALUE   2005.
       
       77 LAST-USER-MOV-NUM         PIC   9(35).
       77 LAST-MOV-NUM              PIC   9(35).
       
       77 EURENT_USUARIO            PIC    9(7).
       77 EURDEC_USUARIO            PIC    9(2).
       77 SALDO_USUARIO_ENT         PIC   S9(9).
       77 SALDO_USUARIO_DEC         PIC    9(2).
       77 CENT_SALDO_USER           PIC  S9(11).
       
       77 BILLETES_10               PIC 9(3) VALUE 0.
       77 BILLETES_20               PIC 9(3) VALUE 0.
       77 BILLETES_50               PIC 9(3) VALUE 0.
       77 CENT_IMPOR_USER           PIC    9(9).
       77 CENT_ACUMULADOR           PIC   9(11).
       
       77 CON                       PIC   X(35) VALUE "Ingreso".
       
       LINKAGE SECTION.
       77 TNUM                      PIC  9(16).
       
       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 COL 1 BLANK SCREEN BACKGROUND-COLOR BLACK.
       
       01 ENTRADA_USUARIO.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 13 COL 41 PIC 9(7) USING EURENT-USUARIO.
           05 FILLER BLANK ZERO UNDERLINE
               LINE 13 COL 49 PIC 9(2) USING EURDEC-USUARIO.

       01 SALDO-DISPLAY.
           05 FILLER SIGN IS LEADING SEPARATE
               LINE 10 COL 33 PIC -9(7) FROM SALDO-USUARIO-ENT.
           05 FILLER LINE 10 COL 41 VALUE ".".
           05 FILLER LINE 10 COL 42 PIC 99 FROM SALDO-USUARIO-DEC.
           05 FILLER LINE 10 COL 45 VALUE "EUR".

       01 SALDO-DISPLAY-FINAL.
           05 FILLER SIGN IS LEADING SEPARATE
               LINE 11 COL 44 PIC -9(7) FROM SALDO-USUARIO-ENT.
           05 FILLER LINE 11 COL 52 VALUE ".".
           05 FILLER LINE 11 COL 53 PIC 99 FROM SALDO-USUARIO-DEC.
           05 FILLER LINE 11 COL 56 VALUE "EUR".
       
       01 INGRESO_BILLETES.
           05 FILLER LINE 10 COL 20 VALUE "Introduzca billetes de 10: ".
           05 BILLETES_10_IN PIC 9(3) BLANK WHEN ZERO LINE 10 COL 50
              USING BILLETES_10.
       
           05 FILLER LINE 11 COL 20 VALUE "Introduzca billetes de 20: ".
           05 BILLETES_20_IN PIC 9(3) BLANK WHEN ZERO LINE 11 COL 50
              USING BILLETES_20.
       
           05 FILLER LINE 12 COL 20 VALUE "Introduzca billetes de 50: ".
           05 BILLETES_50_IN PIC 9(3) BLANK WHEN ZERO LINE 12 COL 50
              USING BILLETES_50.
       
           05 FILLER LINE 14 COL 20 VALUE 
             "Flechas/TAB - Moverse, Enter - Aceptar, ESC - Cancelar".
       
       PROCEDURE DIVISION USING TNUM.
       IMPRIMIR-CABECERA.

           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'.

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
       
       CONSULTA-ULTIMO-MOVIMIENTO SECTION.

           INITIALIZE CENT-ACUMULADOR.

           OPEN I-O F-MOVIMIENTOS.
           IF FSM <> "00"
              GO TO PSYS-ERR.

           MOVE 0 TO LAST-MOV-NUM.

       LEER-ULTIMO-MOV-READ.
           READ F-MOVIMIENTOS NEXT RECORD AT END GO TO LAST-MOV-FOUND.

           IF MOV-NUM > LAST-MOV-NUM
               MOVE MOV-NUM TO LAST-MOV-NUM.

           GO TO LEER-ULTIMO-MOV-READ.

       LAST-MOV-FOUND.
           CLOSE F-MOVIMIENTOS.




       CONSULTA-SALDO-USUARIO SECTION.
           OPEN INPUT F-MOVIMIENTOS.
           IF FSM <> "00"
               GO TO PSYS-ERR.

           MOVE 0 TO LAST-USER-MOV-NUM.
           MOVE 0 TO MOV-NUM.


       LECTURA-MOV-USER.
           READ F-MOVIMIENTOS NEXT RECORD
              AT END GO LAST-USER-MOV-FOUND.

              IF MOV-TARJETA = TNUM
                  IF LAST-USER-MOV-NUM < MOV-NUM
                      MOVE MOV-NUM TO LAST-USER-MOV-NUM.
              GO LECTURA-MOV-USER.

       LAST-USER-MOV-FOUND.
           CLOSE F-MOVIMIENTOS.

           IF LAST-USER-MOV-NUM = 0 THEN
               MOVE 0 TO SALDO_USUARIO_ENT
               MOVE 0 TO SALDO_USUARIO_DEC
               MOVE 0 TO CENT_SALDO_USER
               GO TO INICIO-PANTALLA-INGRESO
           END-IF.

           MOVE LAST-USER-MOV-NUM TO MOV-NUM.

           OPEN INPUT F-MOVIMIENTOS.
           IF FSM <> "00"
               GO TO PSYS_ERR.
       
           READ F-MOVIMIENTOS INVALID KEY GO TO PSYS_ERR.
       
           MOVE MOV-SALDOPOS-ENT TO SALDO_USUARIO_ENT.
           MOVE MOV-SALDOPOS-DEC TO SALDO_USUARIO_DEC.
           COMPUTE CENT_SALDO_USER = (SALDO_USUARIO_ENT * 100)
                                     + SALDO_USUARIO_DEC.
       
           CLOSE F-MOVIMIENTOS.
       
       PANTALLA_INGRESO SECTION.
       INICIO-PANTALLA-INGRESO.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           MOVE 0 TO BILLETES_10.
           MOVE 0 TO BILLETES_20.
           MOVE 0 TO BILLETES_50.
           
           DISPLAY INGRESO_BILLETES.
       
       ACEPTAR_BILLETES.
           ACCEPT INGRESO_BILLETES ON EXCEPTION
               IF ESC-PRESSED THEN
                   GO TO EXIT_ENTER 
               ELSE
                   GO TO ACEPTAR_BILLETES
               END-IF
           END-ACCEPT.
       
           COMPUTE CENT_IMPOR_USER =
               BILLETES_10 * 10
               + BILLETES_20 * 20
               + BILLETES_50 * 50.
           
           COMPUTE CENT_IMPOR_USER =
               CENT_IMPOR_USER * 100.
       
           COMPUTE EURENT_USUARIO = CENT_IMPOR_USER / 100.

           MOVE FUNCTION MOD(CENT_IMPOR_USER, 100) TO EURDEC_USUARIO.
       
           ADD CENT_IMPOR_USER TO CENT_ACUMULADOR.
       
           PERFORM INSERTAR_MOVIMIENTO.
       
           GO TO INICIO-PANT.
       
       INSERTAR_MOVIMIENTO SECTION.
           OPEN I-O F-MOVIMIENTOS.
           IF FSM <> "00"
              GO TO PSYS-ERR.

           ADD CENT-IMPOR-USER TO CENT-SALDO-USER
               ON SIZE ERROR GO TO PSYS-ERR.
           COMPUTE SALDO-USUARIO-ENT = (CENT-SALDO-USER / 100).
           MOVE FUNCTION MOD(CENT-SALDO-USER, 100)
               TO SALDO-USUARIO-DEC.


       ESCRITURA.
           ADD 1 TO LAST-MOV-NUM.

           MOVE LAST-MOV-NUM            TO MOV-NUM.
           MOVE TNUM                    TO MOV-TARJETA.
           MOVE ANO                     TO MOV-ANO.
           MOVE MES                     TO MOV-MES.
           MOVE DIA                     TO MOV-DIA.
           MOVE HORAS                   TO MOV-HOR.
           MOVE MINUTOS                 TO MOV-MIN.
           MOVE SEGUNDOS                TO MOV-SEG.

           MOVE EURENT-USUARIO          TO MOV-IMPORTE-ENT.
           MOVE EURDEC-USUARIO          TO MOV-IMPORTE-DEC.

           MOVE CON                     TO MOV-CONCEPTO.

           MOVE SALDO-USUARIO-ENT       TO MOV-SALDOPOS-ENT.
           MOVE SALDO-USUARIO-DEC       TO MOV-SALDOPOS-DEC.

           WRITE MOVIMIENTO-REG INVALID KEY GO TO PSYS-ERR.
           CLOSE F-MOVIMIENTOS.
       
       PANT SECTION.
       INICIO-PANT.
           COMPUTE EURENT_USUARIO = (CENT_ACUMULADOR / 100).
           MOVE FUNCTION MOD(CENT_ACUMULADOR, 100)
               TO EURDEC_USUARIO.
       
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ingresar efectivo" AT 0830.
           DISPLAY "Se han recibido correctamente:" AT 1019.
           DISPLAY EURENT_USUARIO AT 1050.
           DISPLAY EURDEC_USUARIO AT 1058.
           DISPLAY "." AT 1057.
           DISPLAY "EUR" AT 1061.
           DISPLAY "El saldo resultante es de:" AT 1119.
       
           DISPLAY SALDO_DISPLAY_FINAL.
       
           DISPLAY "Enter - Aceptar" AT 2433.
       
           GO TO EXIT_ENTER.
       
       PSYS_ERR.
           CLOSE F-MOVIMIENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ha ocurrido un error interno" AT 0925
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Vuelva mas tarde" AT 1132
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.

           DISPLAY "Enter - Aceptar" AT 2433.
       
       EXIT_ENTER.
           ACCEPT OMITTED AT 2480.
           IF ENTER-PRESSED
               EXIT PROGRAM
           ELSE
               GO TO EXIT_ENTER.
			   