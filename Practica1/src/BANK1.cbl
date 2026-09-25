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


       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.

       01 DATA-ACCEPT.
           05 TARJETA-ACCEPT BLANK ZERO AUTO LINE 08 COL 50
               PIC 9(16) USING TNUM.
           05 PIN-ACCEPT BLANK ZERO AUTO SECURE LINE 09 COL 50
               PIC 9(4) USING PIN-INTRODUCIDO.



       PROCEDURE DIVISION.
       IMPRIMIR-CABECERA.

           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'
           SET ENVIRONMENT 'COB_SCREEN_ESC'        TO 'Y'

           DISPLAY BLANK-SCREEN.

           DISPLAY "Cajero Automatico UnizarBank" AT 0226
               WITH FOREGROUND-COLOR IS BLUE.

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
           ACCEPT CHOICE AT 2480 ON EXCEPTION
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
           ACCEPT DATA-ACCEPT ON EXCEPTION
               IF ESC-PRESSED
                   GO TO IMPRIMIR-CABECERA
               ELSE
                   GO TO P2.

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
           DISPLAY "6 - Comprar entradas de espectaculos" AT 1315.
           DISPLAY "7 - Cambiar clave" AT 1515.
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
               CALL "BANK7" USING TNUM
               GO TO PMENU.

           IF CHOICE = 7
               CALL "BANK8" USING TNUM
               GO TO PMENU.

           GO TO PMENU.


       PSYS-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ha ocurrido un error interno" AT 0925
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Vuelva mas tarde" AT 1132
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar" AT 2433.
           GO TO PINT-ERR-ENTER.


       PINT-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "(9 20) Se ha sobrepasado el numero de intentos"
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Por su seguridad se ha bloqueado la tarjeta" AT 1118
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Acuda a una sucursal" AT 1230
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar" AT 2433.

       PINT-ERR-ENTER.
           ACCEPT CHOICE AT 2480 ON EXCEPTION
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
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Le quedan " AT 1130
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY IINTENTOS AT 1140
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY " intentos" AT 1142

               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.

           DISPLAY "Enter - Aceptar" AT 2401.
           DISPLAY "ESC - Cancelar" AT 2465.

       PPIN-ERR-ENTER.
           ACCEPT CHOICE AT 2480 ON EXCEPTION
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
