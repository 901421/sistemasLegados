       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK9.

      *    OPCION 6 - LISTADO DE TRANSFERENCIAS (OBJ. 2).
      *    MUESTRA ENTRE DOS FECHAS LAS TRANSFERENCIAS ENVIADAS POR LA
      *    TARJETA (TODAS) Y LAS RECIBIDAS YA EJECUTADAS (D10),
      *    INDICANDO SI SON PUNTUALES O PERIODICAS, SI ESTAN EJECUTADAS
      *    O PENDIENTES (D14) Y SI SE ENVIAN O SE RECIBEN. LAS MENSUALES
      *    DAN UNA FILA POR MES (D6, D8). SE CARGAN EN MEMORIA LAS
      *    TRANSFERENCIAS DE LA TARJETA Y SE RECORRE EL INTERVALO MES A
      *    MES; LAS FILAS SE ORDENAN POR FECHA Y SE MUESTRAN DE 15 EN 15

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT F-TRANSFERENCIAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TRANS-NUM
           FILE STATUS IS FSTR.


       DATA DIVISION.
       FILE SECTION.
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
       77 FSTR                     PIC   X(2).

       78 BLACK                   VALUE      0.
       78 BLUE                    VALUE      1.
       78 GREEN                   VALUE      2.
       78 CYAN                    VALUE      3.
       78 RED                     VALUE      4.
       78 MAGENTA                 VALUE      5.
       78 YELLOW                  VALUE      6.
       78 WHITE                   VALUE      7.

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

       01 KEYBOARD-STATUS           PIC  9(4).
           88 ENTER-PRESSED       VALUE     0.
           88 PGUP-PRESSED        VALUE  2001.
           88 PGDN-PRESSED        VALUE  2002.
           88 ESC-PRESSED         VALUE  2005.

      *    FECHAS DEL FORMULARIO (COMO EN BANK3).
       77 DIA1-USUARIO             PIC   9(2).
       77 MES1-USUARIO             PIC   9(2).
       77 ANO1-USUARIO             PIC   9(4).
       77 DIA2-USUARIO             PIC   9(2).
       77 MES2-USUARIO             PIC   9(2).
       77 ANO2-USUARIO             PIC   9(4).

       77 FECHA-INI                PIC   9(8).
       77 FECHA-FIN                PIC   9(8).
       77 FECHA-HOY                PIC   9(8).
       77 FECHA-OCU                PIC   9(8).
       77 FECHA-CORTE              PIC   9(8).
       77 MES-OCU                  PIC   9(6).
       77 MES-ACTUAL               PIC   9(6).

      *    RECORRIDO MES A MES DEL INTERVALO.
       77 ANO-M                    PIC   9(4).
       77 MES-M                    PIC   9(2).
       77 ANO-FIN                  PIC   9(4).
       77 MES-FIN                  PIC   9(2).
       77 SIGUIENTE-MES            PIC   9(8).
       77 ULTIMO-DIA               PIC   9(2).
       77 DIA-OCU                  PIC   9(2).
       77 FECHA-AUX                PIC   9(8).
       77 FILAS-ANTES              PIC   9(3).

      *    TRANSFERENCIAS DE LA TARJETA, CARGADAS EN MEMORIA.
       78 MAX-TRF                 VALUE    200.
       77 NUM-TRF                  PIC   9(3) VALUE 0.
       77 T                        PIC   9(3).
       01 TABLA-TRF.
           05 TRF OCCURS 1 TO 200 DEPENDING ON NUM-TRF.
               10 T-NUM             PIC  9(35).
               10 T-SENTIDO         PIC   X(1).
               10 T-CUENTA          PIC  9(16).
               10 T-IMP-ENT         PIC   9(7).
               10 T-IMP-DEC         PIC   9(2).
               10 T-TIPO            PIC   9(1).
               10 T-FECHA           PIC   9(8).
               10 T-DIA             PIC   9(2).
               10 T-ALTA            PIC   9(8).

      *    FILAS DEL LISTADO. DESBORDE = 1: NO CABEN TODAS Y SE MUESTRA
      *    HASTA FECHA-CORTE (ULTIMO MES COMPLETO). DESBORDE-TRF = 1:
      *    LA TARJETA TIENE MAS DE MAX-TRF TRANSFERENCIAS.
       78 MAX-FILAS               VALUE    500.
       77 NUM-FILAS                PIC   9(3) VALUE 0.
       77 DESBORDE                 PIC   9(1).
       77 DESBORDE-TRF             PIC   9(1).
       01 TABLA-FILAS.
           05 FILA OCCURS 1 TO 500 DEPENDING ON NUM-FILAS.
               10 FILA-FECHA        PIC   9(8).
               10 FILA-NUM          PIC  9(35).
               10 FILA-CUENTA       PIC  9(16).
               10 FILA-IMP-ENT      PIC   9(7).
               10 FILA-IMP-DEC      PIC   9(2).
               10 FILA-TIPO         PIC   X(9).
               10 FILA-ESTADO       PIC   X(9).
               10 FILA-SENTIDO      PIC   X(8).

      *    PAGINACION.
       78 FILAS-PAGINA            VALUE     15.
       77 PAGINA                   PIC   9(3).
       77 TOTAL-PAGINAS            PIC   9(3).
       77 PAGINA-ED                PIC   ZZ9.
       77 TOTAL-ED                 PIC   ZZ9.
       77 TEXTO-PAGINA             PIC  X(20).
       01 CORTE-TXT.
           05 CT-ANO                PIC   9(4).
           05 CT-MES                PIC   9(2).
           05 CT-DIA                PIC   9(2).
       77 AVISO-CORTE              PIC  X(45).
       77 FILA-ACTUAL              PIC   9(3).
       77 FILA-FIN-PAG             PIC   9(3).
       77 LINEA-ACTUAL             PIC   9(2).
       77 MODULO-LINEA             PIC   9(1).

      *    FILA QUE SE PINTA.
       01 FILA-FECHA-TXT.
           05 FT-ANO                PIC   9(4).
           05 FT-MES                PIC   9(2).
           05 FT-DIA                PIC   9(2).

       LINKAGE SECTION.
       77 TNUM                     PIC  9(16).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 FILTRO-FECHAS.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 13 COL 37 PIC 9(2) USING DIA1-USUARIO.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 13 COL 40 PIC 9(2) USING MES1-USUARIO.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 13 COL 43 PIC 9(4) USING ANO1-USUARIO.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 13 COL 50 PIC 9(2) USING DIA2-USUARIO.
           05 FILLER BLANK ZERO AUTO UNDERLINE
               LINE 13 COL 53 PIC 9(2) USING MES2-USUARIO.
           05 FILLER BLANK ZERO UNDERLINE
               LINE 13 COL 56 PIC 9(4) USING ANO2-USUARIO.

       01 FILA-PAR.
           05 FILLER LINE LINEA-ACTUAL COL 02
               FOREGROUND-COLOR YELLOW PIC 99 FROM FT-DIA.
           05 FILLER LINE LINEA-ACTUAL COL 04
               FOREGROUND-COLOR YELLOW VALUE "-".
           05 FILLER LINE LINEA-ACTUAL COL 05
               FOREGROUND-COLOR YELLOW PIC 99 FROM FT-MES.
           05 FILLER LINE LINEA-ACTUAL COL 07
               FOREGROUND-COLOR YELLOW VALUE "-".
           05 FILLER LINE LINEA-ACTUAL COL 08
               FOREGROUND-COLOR YELLOW PIC 9(4) FROM FT-ANO.
           05 FILLER LINE LINEA-ACTUAL COL 13
               FOREGROUND-COLOR YELLOW VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 15
               FOREGROUND-COLOR YELLOW PIC 9(16)
               FROM FILA-CUENTA(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 32
               FOREGROUND-COLOR YELLOW VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 34
               FOREGROUND-COLOR YELLOW PIC 9(7)
               FROM FILA-IMP-ENT(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 41
               FOREGROUND-COLOR YELLOW VALUE ",".
           05 FILLER LINE LINEA-ACTUAL COL 42
               FOREGROUND-COLOR YELLOW PIC 99
               FROM FILA-IMP-DEC(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 45
               FOREGROUND-COLOR YELLOW VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 47
               FOREGROUND-COLOR YELLOW PIC X(9)
               FROM FILA-TIPO(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 57
               FOREGROUND-COLOR YELLOW VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 59
               FOREGROUND-COLOR YELLOW PIC X(9)
               FROM FILA-ESTADO(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 69
               FOREGROUND-COLOR YELLOW VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 71
               FOREGROUND-COLOR YELLOW PIC X(8)
               FROM FILA-SENTIDO(FILA-ACTUAL).

       01 FILA-IMPAR.
           05 FILLER LINE LINEA-ACTUAL COL 02
               PIC 99 FROM FT-DIA.
           05 FILLER LINE LINEA-ACTUAL COL 04 VALUE "-".
           05 FILLER LINE LINEA-ACTUAL COL 05
               PIC 99 FROM FT-MES.
           05 FILLER LINE LINEA-ACTUAL COL 07 VALUE "-".
           05 FILLER LINE LINEA-ACTUAL COL 08
               PIC 9(4) FROM FT-ANO.
           05 FILLER LINE LINEA-ACTUAL COL 13 VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 15
               PIC 9(16) FROM FILA-CUENTA(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 32 VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 34
               PIC 9(7) FROM FILA-IMP-ENT(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 41 VALUE ",".
           05 FILLER LINE LINEA-ACTUAL COL 42
               PIC 99 FROM FILA-IMP-DEC(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 45 VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 47
               PIC X(9) FROM FILA-TIPO(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 57 VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 59
               PIC X(9) FROM FILA-ESTADO(FILA-ACTUAL).
           05 FILLER LINE LINEA-ACTUAL COL 69 VALUE "|".
           05 FILLER LINE LINEA-ACTUAL COL 71
               PIC X(8) FROM FILA-SENTIDO(FILA-ACTUAL).


       PROCEDURE DIVISION USING TNUM.
       INICIO.
           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'.
           SET ENVIRONMENT 'COB_SCREEN_ESC'        TO 'Y'.

           INITIALIZE DIA1-USUARIO.
           INITIALIZE MES1-USUARIO.
           INITIALIZE ANO1-USUARIO.
           INITIALIZE DIA2-USUARIO.
           INITIALIZE MES2-USUARIO.
           INITIALIZE ANO2-USUARIO.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           GO TO PFILTRO.

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

      *    ---- FORMULARIO DE FECHAS ----
       PFILTRO.
           DISPLAY "Listado de transferencias" AT 0828.
           DISPLAY "Indique el intervalo de fechas (ambas incluidas)"
               AT 1016.
           DISPLAY "Entre las fechas   /  /     y   /  /    " AT 1320.
           DISPLAY "Enter - Aceptar" AT 2401.
           DISPLAY "ESC - Cancelar" AT 2465.

       PFILTRO-ACEPTAR.
           ACCEPT FILTRO-FECHAS ON EXCEPTION
               IF ESC-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO PFILTRO-ACEPTAR
               END-IF
           END-ACCEPT.

           COMPUTE FECHA-INI = ANO1-USUARIO * 10000
                               + MES1-USUARIO * 100 + DIA1-USUARIO.
           COMPUTE FECHA-FIN = ANO2-USUARIO * 10000
                               + MES2-USUARIO * 100 + DIA2-USUARIO.

           IF FUNCTION TEST-DATE-YYYYMMDD(FECHA-INI) NOT = 0
              OR FUNCTION TEST-DATE-YYYYMMDD(FECHA-FIN) NOT = 0
               DISPLAY " " AT 1601 WITH ERASE EOL
               DISPLAY "Fecha incorrecta" AT 1632
                   WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO PFILTRO-ACEPTAR
           END-IF.

           IF FECHA-INI > FECHA-FIN
               DISPLAY " " AT 1601 WITH ERASE EOL
               DISPLAY "La fecha inicial es posterior a la final"
                   AT 1620 WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                        BACKGROUND-COLOR IS RED
               GO TO PFILTRO-ACEPTAR
           END-IF.

      *    ---- CALCULO DE LAS FILAS ----
       PCALCULO.
           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.
           COMPUTE FECHA-HOY = ANO * 10000 + MES * 100 + DIA.
           MOVE 0 TO NUM-FILAS.
           MOVE 0 TO NUM-TRF.
           MOVE 0 TO DESBORDE.
           MOVE 0 TO DESBORDE-TRF.

           OPEN INPUT F-TRANSFERENCIAS.
           IF FSTR <> "00"
               GO TO PSYS-ERR
           END-IF.

      *    1) TRANSFERENCIAS ENVIADAS O RECIBIDAS POR ESTA TARJETA
      *    (D10). DE CADA UNA SE GUARDA LA CUENTA DE LA OTRA PARTE.
       CARGAR-TRANSFERENCIA.
           READ F-TRANSFERENCIAS NEXT RECORD
               AT END GO TO FIN-CARGA
           END-READ.

           IF TRANS-ORIGEN NOT = TNUM AND TRANS-DESTINO NOT = TNUM
               GO TO CARGAR-TRANSFERENCIA
           END-IF.

           IF NUM-TRF = MAX-TRF
               MOVE 1 TO DESBORDE-TRF
               GO TO FIN-CARGA
           END-IF.

           ADD 1 TO NUM-TRF.
           MOVE TRANS-NUM         TO T-NUM(NUM-TRF).
           IF TRANS-ORIGEN = TNUM
               MOVE "E"           TO T-SENTIDO(NUM-TRF)
               MOVE TRANS-DESTINO TO T-CUENTA(NUM-TRF)
           ELSE
               MOVE "R"           TO T-SENTIDO(NUM-TRF)
               MOVE TRANS-ORIGEN  TO T-CUENTA(NUM-TRF)
           END-IF.
           MOVE TRANS-IMPORTE-ENT TO T-IMP-ENT(NUM-TRF).
           MOVE TRANS-IMPORTE-DEC TO T-IMP-DEC(NUM-TRF).
           MOVE TRANS-TIPO        TO T-TIPO(NUM-TRF).
           COMPUTE T-FECHA(NUM-TRF) = TRANS-ANO * 10000
                                      + TRANS-MES * 100 + TRANS-DIA.
           MOVE TRANS-DIA         TO T-DIA(NUM-TRF).
           COMPUTE T-ALTA(NUM-TRF) = TRANS-ALTA-ANO * 10000
                                     + TRANS-ALTA-MES * 100
                                     + TRANS-ALTA-DIA.
           GO TO CARGAR-TRANSFERENCIA.

       FIN-CARGA.
           CLOSE F-TRANSFERENCIAS.

           IF NUM-TRF = 0
               GO TO PLISTADO
           END-IF.

      *    2) RECORRIDO MES A MES DE FECHA-INI A FECHA-FIN.
           COMPUTE ANO-M   = FECHA-INI / 10000.
           COMPUTE MES-M   = FUNCTION MOD(FECHA-INI / 100, 100).
           COMPUTE ANO-FIN = FECHA-FIN / 10000.
           COMPUTE MES-FIN = FUNCTION MOD(FECHA-FIN / 100, 100).

       RECORRER-MES.
           IF ANO-M = 9999 AND MES-M = 12
               MOVE 31 TO ULTIMO-DIA
           ELSE
               IF MES-M = 12
                   COMPUTE SIGUIENTE-MES = (ANO-M + 1) * 10000 + 101
               ELSE
                   COMPUTE SIGUIENTE-MES = ANO-M * 10000
                                           + (MES-M + 1) * 100 + 1
               END-IF
               COMPUTE FECHA-AUX = FUNCTION DATE-OF-INTEGER(
                   FUNCTION INTEGER-OF-DATE(SIGUIENTE-MES) - 1)
               COMPUTE ULTIMO-DIA = FUNCTION MOD(FECHA-AUX, 100)
           END-IF.

           COMPUTE MES-ACTUAL = ANO-M * 100 + MES-M.
           MOVE NUM-FILAS TO FILAS-ANTES.
           MOVE 1 TO T.

       RECORRER-TRF.
           IF T-TIPO(T) = 3
      *        MENSUAL: SI EL MES NO TIENE ESE DIA, EL ULTIMO (D6);
      *        SOLO DESDE LA FECHA DE ALTA (D8).
               IF T-DIA(T) > ULTIMO-DIA
                   MOVE ULTIMO-DIA TO DIA-OCU
               ELSE
                   MOVE T-DIA(T) TO DIA-OCU
               END-IF
               COMPUTE FECHA-OCU = MES-ACTUAL * 100 + DIA-OCU
               IF FECHA-OCU >= T-ALTA(T)
                   PERFORM ANADIR-FILA THRU ANADIR-FILA-FIN
               END-IF
           ELSE
      *        INMEDIATA O PUNTUAL: SI SU FECHA CAE EN ESTE MES.
               COMPUTE MES-OCU = T-FECHA(T) / 100
               IF MES-OCU = MES-ACTUAL
                   MOVE T-FECHA(T) TO FECHA-OCU
                   PERFORM ANADIR-FILA THRU ANADIR-FILA-FIN
               END-IF
           END-IF.

      *    SI EL MES NO CABE ENTERO SE DESCARTA Y SE CORTA AHI.
           IF DESBORDE = 1
               MOVE FILAS-ANTES TO NUM-FILAS
               COMPUTE FECHA-CORTE = FUNCTION DATE-OF-INTEGER(
                   FUNCTION INTEGER-OF-DATE(MES-ACTUAL * 100 + 1) - 1)
               GO TO FIN-CALCULO
           END-IF.

           IF T < NUM-TRF
               ADD 1 TO T
               GO TO RECORRER-TRF
           END-IF.

           IF ANO-M = ANO-FIN AND MES-M = MES-FIN
               GO TO FIN-CALCULO
           END-IF.

           IF MES-M = 12
               MOVE 1 TO MES-M
               ADD 1 TO ANO-M
           ELSE
               ADD 1 TO MES-M
           END-IF.

           GO TO RECORRER-MES.

       FIN-CALCULO.
           IF NUM-FILAS > 1
               SORT FILA ON ASCENDING KEY FILA-FECHA FILA-NUM
           END-IF.

           GO TO PLISTADO.

      *    ANADE LA OCURRENCIA FECHA-OCU DE LA TRANSFERENCIA T SI CAE
      *    ENTRE LAS FECHAS PEDIDAS. ESTADO SEGUN D14. DE LAS RECIBIDAS
      *    SOLO SE MUESTRAN LAS YA EJECUTADAS (D10).
       ANADIR-FILA.
           IF FECHA-OCU < FECHA-INI OR FECHA-OCU > FECHA-FIN
               GO TO ANADIR-FILA-FIN
           END-IF.

           IF T-SENTIDO(T) = "R" AND T-TIPO(T) NOT = 1
              AND FECHA-OCU NOT < FECHA-HOY
               GO TO ANADIR-FILA-FIN
           END-IF.

           IF NUM-FILAS = MAX-FILAS
               MOVE 1 TO DESBORDE
               GO TO ANADIR-FILA-FIN
           END-IF.

           ADD 1 TO NUM-FILAS.
           MOVE FECHA-OCU    TO FILA-FECHA(NUM-FILAS).
           MOVE T-NUM(T)     TO FILA-NUM(NUM-FILAS).
           MOVE T-CUENTA(T)  TO FILA-CUENTA(NUM-FILAS).
           MOVE T-IMP-ENT(T) TO FILA-IMP-ENT(NUM-FILAS).
           MOVE T-IMP-DEC(T) TO FILA-IMP-DEC(NUM-FILAS).

           IF T-TIPO(T) = 3
               MOVE "Periodica" TO FILA-TIPO(NUM-FILAS)
           ELSE
               MOVE "Puntual"   TO FILA-TIPO(NUM-FILAS)
           END-IF.

           IF T-TIPO(T) = 1 OR FECHA-OCU < FECHA-HOY
               MOVE "Ejecutada" TO FILA-ESTADO(NUM-FILAS)
           ELSE
               MOVE "Pendiente" TO FILA-ESTADO(NUM-FILAS)
           END-IF.

           IF T-SENTIDO(T) = "E"
               MOVE "Enviada"   TO FILA-SENTIDO(NUM-FILAS)
           ELSE
               MOVE "Recibida"  TO FILA-SENTIDO(NUM-FILAS)
           END-IF.

       ANADIR-FILA-FIN.
           EXIT.

      *    ---- LISTADO PAGINADO ----
       PLISTADO.
           IF NUM-FILAS = 0
               PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA
               DISPLAY "Listado de transferencias" AT 0628
               DISPLAY "No hay transferencias entre esas fechas"
                   AT 1221
               IF DESBORDE = 1 OR DESBORDE-TRF = 1
                   PERFORM MOSTRAR-AVISO-CORTE THRU MOSTRAR-AVISO-CORTE
               END-IF
               DISPLAY "ESC - Salir" AT 2434
               GO TO ESPERAR-SALIR
           END-IF.

           COMPUTE TOTAL-PAGINAS = (NUM-FILAS + FILAS-PAGINA - 1)
                                   / FILAS-PAGINA.
           MOVE 1 TO PAGINA.

       MOSTRAR-PAGINA.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Listado de transferencias" AT 0628.
           DISPLAY "FECHA" AT 0705.
           DISPLAY "|" AT 0713.
           DISPLAY "CUENTA" AT 0720.
           DISPLAY "|" AT 0732.
           DISPLAY "IMPORTE" AT 0736.
           DISPLAY "|" AT 0745.
           DISPLAY "TIPO" AT 0750.
           DISPLAY "|" AT 0757.
           DISPLAY "ESTADO" AT 0760.
           DISPLAY "|" AT 0769.
           DISPLAY "SENTIDO" AT 0771.

           COMPUTE FILA-ACTUAL = (PAGINA - 1) * FILAS-PAGINA + 1.
           COMPUTE FILA-FIN-PAG = FILA-ACTUAL + FILAS-PAGINA - 1.
           IF FILA-FIN-PAG > NUM-FILAS
               MOVE NUM-FILAS TO FILA-FIN-PAG
           END-IF.
           MOVE 8 TO LINEA-ACTUAL.

       MOSTRAR-FILA.
           MOVE FILA-FECHA(FILA-ACTUAL) TO FILA-FECHA-TXT.
           MOVE FUNCTION MOD(LINEA-ACTUAL, 2) TO MODULO-LINEA.
           IF MODULO-LINEA = 0
               DISPLAY FILA-PAR
           ELSE
               DISPLAY FILA-IMPAR
           END-IF.

           IF FILA-ACTUAL < FILA-FIN-PAG
               ADD 1 TO FILA-ACTUAL
               ADD 1 TO LINEA-ACTUAL
               GO TO MOSTRAR-FILA
           END-IF.

           MOVE PAGINA TO PAGINA-ED.
           MOVE TOTAL-PAGINAS TO TOTAL-ED.
           MOVE SPACES TO TEXTO-PAGINA.
           STRING "Pagina " FUNCTION TRIM(PAGINA-ED)
                  " de " FUNCTION TRIM(TOTAL-ED)
                  DELIMITED BY SIZE INTO TEXTO-PAGINA.
           DISPLAY TEXTO-PAGINA AT 2302.
           IF DESBORDE = 1 OR DESBORDE-TRF = 1
               PERFORM MOSTRAR-AVISO-CORTE THRU MOSTRAR-AVISO-CORTE
           END-IF.

           DISPLAY "Re. pag - Anteriores" AT 2402.
           DISPLAY "ESC - Salir" AT 2434.
           DISPLAY "Av. pag - Siguientes" AT 2460.

       ESPERAR-TECLA.
           ACCEPT OMITTED AT 2480
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   END-IF
                   IF PGDN-PRESSED AND PAGINA < TOTAL-PAGINAS
                       ADD 1 TO PAGINA
                       GO TO MOSTRAR-PAGINA
                   END-IF
                   IF PGUP-PRESSED AND PAGINA > 1
                       SUBTRACT 1 FROM PAGINA
                       GO TO MOSTRAR-PAGINA
                   END-IF
           END-ACCEPT.

           GO TO ESPERAR-TECLA.

       ESPERAR-SALIR.
           ACCEPT OMITTED AT 2480
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   END-IF
           END-ACCEPT.

           GO TO ESPERAR-SALIR.

       PSYS-ERR.
           CLOSE F-TRANSFERENCIAS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ha ocurrido un error interno" AT 0925
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Vuelva mas tarde" AT 1132
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "ESC - Salir" AT 2434.
           GO TO ESPERAR-SALIR.

      *    AVISO CUANDO NO CABE TODO EL LISTADO.
       MOSTRAR-AVISO-CORTE.
           MOVE SPACES TO AVISO-CORTE.
           IF DESBORDE = 1
               MOVE FECHA-CORTE TO CORTE-TXT
               STRING "Solo hasta el " CT-DIA "-" CT-MES "-" CT-ANO
                      ": acote las fechas"
                      DELIMITED BY SIZE INTO AVISO-CORTE
           ELSE
               MOVE "Demasiadas transferencias: listado incompleto"
                   TO AVISO-CORTE
           END-IF.
           DISPLAY AVISO-CORTE AT 2330
           WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                BACKGROUND-COLOR IS RED.
