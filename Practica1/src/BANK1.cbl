       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK1.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TARJETAS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS TNUM
           FILE STATUS IS FST.

           SELECT INTENTOS ASSIGN TO DISK
           ORGANIZATION IS INDEXED
           ACCESS MODE IS DYNAMIC
           RECORD KEY IS INUM
           FILE STATUS IS FSI.


       DATA DIVISION.
       FILE SECTION.
       FD TARJETAS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "tarjetas.ubd".
       01 TAJETAREG.
           02 TNUM      PIC 9(16).
           02 TPIN      PIC  9(4).

       FD INTENTOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "intentos.ubd".
       01 INTENTOSREG.
           02 INUM      PIC 9(16).
           02 IINTENTOS PIC 9(1).


       WORKING-STORAGE SECTION.
       77 FST                      PIC  X(2).
       77 FSI                      PIC  X(2).

       78 BLACK   VALUE 0.
       78 BLUE    VALUE 1.
       78 GREEN   VALUE 2.
       78 CYAN    VALUE 3.
       78 RED     VALUE 4.
       78 MAGENTA VALUE 5.
       78 YELLOW  VALUE 6.
       78 WHITE   VALUE 7.

       01 CAMPOS-FECHA.
           05 FECHA.
               10 ANO              PIC  9(4).
               10 MES              PIC  9(2).
               10 DIA              PIC  9(2).
           05 HORA.
               10 HORAS            PIC  9(2).
               10 MINUTOS          PIC  9(2).
               10 SEGUNDOS         PIC  9(2).
               10 MILISEGUNDOS     PIC  9(2).
           05 DIF-GMT              PIC S9(4).

       01 KEYBOARD-STATUS           PIC 9(4).
           88 ENTER-PRESSED          VALUE 0.
           88 PGUP-PRESSED        VALUE 2001.
           88 PGDN-PRESSED        VALUE 2002.
           88 UP-ARROW-PRESSED    VALUE 2003.
           88 DOWN-ARROW-PRESSED  VALUE 2004.
           88 ESC-PRESSED         VALUE 2005.

       77 PRESSED-KEY              PIC  9(4).
       77 PIN-INTRODUCIDO          PIC  9(4).
       77 CHOICE                   PIC  9(1).
       77 TECLA                    PIC  X(1).


      *    LECTURA DE CLAVES CIFRA A CIFRA ("____" -> "*___" -> ... ->
      *    "****"). SIN RETROCESO (GNUCOBOL NO LO AVISA EN UN CAMPO DE
      *    UNA POSICION): ENTER CON LA CLAVE INCOMPLETA LA BORRA.
       77 FILA-CLAVE               PIC  9(2).
       77 COL-CIFRA                PIC  9(2).
       77 POS-CIFRA                PIC  9(4).
       77 NUM-CIFRAS               PIC  9(1).
       77 CIFRA-X                  PIC  X(1).
       77 CIFRA                    PIC  9(1).
       77 CLAVE-LEIDA              PIC  9(4).
       77 CLAVE-CANCELADA          PIC  9(1).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 DATA-ACCEPT.
           05 TARJETA-ACCEPT BLANK ZERO AUTO LINE 08 COL 50
               PIC 9(16) USING TNUM.
      *    EL PIN SE LEE CIFRA A CIFRA CON LEER-CLAVE.

       01 CAMPO-CIFRA.
           05 FILLER AUTO NO-ECHO UNDERLINE
               LINE FILA-CLAVE COL COL-CIFRA PIC X(1) USING CIFRA-X.



       PROCEDURE DIVISION.
       IMPRIMIR-CABECERA.

           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'
           SET ENVIRONMENT 'COB_SCREEN_ESC'        TO 'Y'

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


       P1.
           DISPLAY "Bienvenido a UnizarBank" AT 0828.
           DISPLAY "Por favor, introduzca la tarjeta para operar"
               AT 1018.

           DISPLAY "Enter - Aceptar" AT 2433.

       P1-ENTER.
           ACCEPT TECLA AT 2480 WITH NO-ECHO ON EXCEPTION
           IF ENTER-PRESSED
               GO TO P2
           ELSE
               GO TO P1-ENTER.


       P2.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "ESC - Salir" AT 2433.
           INITIALIZE TNUM.
           INITIALIZE PIN-INTRODUCIDO.
           INITIALIZE TPIN.
           DISPLAY "Numero de tarjeta:" AT 0815.
           DISPLAY "Inserte el pin de tarjeta:" AT 0915.
           DISPLAY "____" AT 0950.
           ACCEPT DATA-ACCEPT ON EXCEPTION
               IF ESC-PRESSED
                   GO TO IMPRIMIR-CABECERA
               END-IF
               IF NOT ENTER-PRESSED
                   GO TO P2
               END-IF
           END-ACCEPT.

      *    PIN CIFRA A CIFRA: "____" -> "*___" -> ... (LEER-CLAVE).
           MOVE 9 TO FILA-CLAVE.
           PERFORM LEER-CLAVE THRU LEER-CLAVE-FIN.
           IF CLAVE-CANCELADA = 1
               GO TO IMPRIMIR-CABECERA
           END-IF.
           MOVE CLAVE-LEIDA TO PIN-INTRODUCIDO.

           OPEN I-O TARJETAS.
           IF FST NOT = 00
               GO TO PSYS-ERR.
           READ TARJETAS INVALID KEY GO TO PSYS-ERR.

           OPEN I-O INTENTOS.
           IF FSI NOT = 00
               GO TO PSYS-ERR.
           MOVE TNUM TO INUM.

           READ INTENTOS INVALID KEY GO TO PSYS-ERR.

           IF IINTENTOS = 0
               GO TO PINT-ERR.

           IF PIN-INTRODUCIDO NOT = TPIN
               GO TO PPIN-ERR.

           PERFORM REINICIAR-INTENTOS THRU REINICIAR-INTENTOS.

       PMENU.
           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "1 - Consultar saldo" AT 0815.
           DISPLAY "2 - Consultar movimientos" AT 0915.
           DISPLAY "3 - Retirar efectivo" AT 1015.
           DISPLAY "4 - Ingresar efectivo" AT 1115.
           DISPLAY "5 - Ordenar transferencia" AT 1215.
           DISPLAY "6 - Listado de transferencias" AT 1315.
           DISPLAY "7 - Comprar entradas de espectaculos" AT 1415.
           DISPLAY "8 - Cambiar clave" AT 1615.
           DISPLAY "ESC - Salir" AT 2434.

       PMENUA1.
           ACCEPT CHOICE
               WITH AUTO
               NO-ECHO
               SIZE 1
               ON EXCEPTION
                   IF ESC-PRESSED
                       GO TO IMPRIMIR-CABECERA
                   ELSE
                       GO TO PMENUA1.


           IF CHOICE = 1
               CALL "BANK2" USING TNUM
               GO TO PMENU.

           IF CHOICE = 2
               CALL "BANK3" USING TNUM
               GO TO PMENU.

           IF CHOICE = 3
               CALL "BANK4" USING TNUM
               GO TO PMENU.

           IF CHOICE = 4
               CALL "BANK5" USING TNUM
               GO TO PMENU.

           IF CHOICE = 5
               CALL "BANK6" USING TNUM
               GO TO PMENU.

           IF CHOICE = 6
               CALL "BANK9" USING TNUM
               GO TO PMENU.

           IF CHOICE = 7
               CALL "BANK7" USING TNUM
               GO TO PMENU.

           IF CHOICE = 8
               CALL "BANK8" USING TNUM
               GO TO PCOMPROBAR-BLOQUEO.

           GO TO PMENU.


      *    TRAS CAMBIAR CLAVE: SI BANK8 HA BLOQUEADO LA TARJETA
      *    (INTENTOS = 0) SE CIERRA LA SESION Y SE VUELVE A LA
      *    PANTALLA DE BIENVENIDA.
       PCOMPROBAR-BLOQUEO.
           OPEN INPUT INTENTOS.
           IF FSI NOT = 00
               GO TO PSYS-ERR.
           MOVE TNUM TO INUM.
           READ INTENTOS INVALID KEY GO TO PSYS-ERR.
           CLOSE INTENTOS.

           IF IINTENTOS = 0
               GO TO IMPRIMIR-CABECERA.

           GO TO PMENU.


       PSYS-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ha ocurrido un error interno" AT 0925
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Vuelva mas tarde" AT 1132
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar" AT 2433.
           GO TO PINT-ERR-ENTER.


       PINT-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Se ha sobrepasado el numero de intentos" AT 0920
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Por su seguridad se ha bloqueado la tarjeta" AT 1118
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Acuda a una sucursal" AT 1230
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar" AT 2433.

       PINT-ERR-ENTER.
           ACCEPT TECLA AT 2480 WITH NO-ECHO ON EXCEPTION
           IF ENTER-PRESSED
               GO TO IMPRIMIR-CABECERA
           ELSE
               GO TO PINT-ERR-ENTER.


       PPIN-ERR.
           SUBTRACT 1 FROM IINTENTOS.
           REWRITE INTENTOSREG INVALID KEY GO TO PSYS-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "El codigo PIN es incorrecto" AT 0926
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Le quedan " AT 1130
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY IINTENTOS AT 1140
               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.
           DISPLAY " intentos" AT 1141

               WITH FOREGROUND-COLOR IS WHITE HIGHLIGHT
                    BACKGROUND-COLOR IS RED.

           DISPLAY "Enter - Aceptar" AT 2401.
           DISPLAY "ESC - Cancelar" AT 2465.

       PPIN-ERR-ENTER.
           ACCEPT TECLA AT 2480 WITH NO-ECHO ON EXCEPTION
           IF ENTER-PRESSED
               GO TO P2
           ELSE
               IF ESC-PRESSED
                   GO TO IMPRIMIR-CABECERA
               ELSE
                   GO TO PPIN-ERR-ENTER.


       REINICIAR-INTENTOS.
           MOVE 3 TO IINTENTOS.
           REWRITE INTENTOSREG INVALID KEY GO TO PSYS-ERR.


      *    LEE UNA CLAVE DE 4 CIFRAS EN LA FILA FILA-CLAVE, COLUMNAS
      *    50-53. DEVUELVE CLAVE-LEIDA; CLAVE-CANCELADA = 1 SI ESC.
       LEER-CLAVE.
           MOVE 0 TO CLAVE-LEIDA.
           MOVE 0 TO NUM-CIFRAS.
           MOVE 0 TO CLAVE-CANCELADA.
           COMPUTE POS-CIFRA = FILA-CLAVE * 100 + 50.
           DISPLAY "____" AT POS-CIFRA.

       LEER-CIFRA.
           COMPUTE COL-CIFRA = 50 + NUM-CIFRAS.
           COMPUTE POS-CIFRA = FILA-CLAVE * 100 + COL-CIFRA.
           MOVE SPACE TO CIFRA-X.
           ACCEPT CAMPO-CIFRA ON EXCEPTION
               IF ESC-PRESSED
                   MOVE 1 TO CLAVE-CANCELADA
                   GO TO LEER-CLAVE-FIN
               END-IF
           END-ACCEPT.

      *    ENTER (CAMPO VACIO): SE BORRA LA CLAVE Y SE EMPIEZA DE NUEVO.
           IF CIFRA-X = SPACE
               GO TO LEER-CLAVE
           END-IF.
           IF CIFRA-X IS NOT NUMERIC
               GO TO LEER-CIFRA
           END-IF.

           MOVE CIFRA-X TO CIFRA.
           COMPUTE CLAVE-LEIDA = CLAVE-LEIDA * 10 + CIFRA.
           DISPLAY "*" AT POS-CIFRA.
           ADD 1 TO NUM-CIFRAS.
           IF NUM-CIFRAS < 4
               GO TO LEER-CIFRA
           END-IF.

       LEER-CLAVE-FIN.
           EXIT.
