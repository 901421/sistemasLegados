       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK8.

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
           02 TNUM                  PIC  9(16).
           02 TPIN                  PIC   9(4).

       FD INTENTOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "intentos.ubd".
       01 INTENTOSREG.
           02 INUM                  PIC  9(16).
           02 IINTENTOS             PIC   9(1).

       WORKING-STORAGE SECTION.
       77 FST                       PIC   X(2).
       77 FSI                       PIC   X(2).

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
           88 ENTER-PRESSED         VALUE    0.
           88 ESC-PRESSED           VALUE 2005.

       77 PIN-ACTUAL-USER           PIC   9(4).
       77 PIN-NUEVO-1               PIC   9(4).
       77 PIN-NUEVO-2               PIC   9(4).
       77 CHOICE                    PIC   X(1).

       LINKAGE SECTION.
       77 TNUM-LINK                 PIC   9(16).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN.

       01 FORMULARIO-CLAVE.
           05 CLAVE-ACTUAL-ACCEPT BLANK ZERO AUTO SECURE 
               LINE 10 COL 40 PIC 9(4) USING PIN-ACTUAL-USER.
           05 CLAVE-NUEVA-1-ACCEPT BLANK ZERO AUTO SECURE 
               LINE 11 COL 40 PIC 9(4) USING PIN-NUEVO-1.
           05 CLAVE-NUEVA-2-ACCEPT BLANK ZERO AUTO SECURE 
               LINE 12 COL 40 PIC 9(4) USING PIN-NUEVO-2.

       PROCEDURE DIVISION USING TNUM-LINK.

       INICIO.
           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'.
           SET ENVIRONMENT 'COB_SCREEN_ESC'        TO 'Y'.

       IMPRIMIR-CABECERA.
           DISPLAY BLANK-SCREEN.
           DISPLAY "Cajero Automatico UnizarBank" LINE 2 COL 26.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           DISPLAY DIA LINE 4 COL 32.
           DISPLAY "-" LINE 4 COL 34.
           DISPLAY MES LINE 4 COL 35.
           DISPLAY "-" LINE 4 COL 37.
           DISPLAY ANO LINE 4 COL 38.
           DISPLAY HORAS LINE 4 COL 44.
           DISPLAY ":" LINE 4 COL 46.
           DISPLAY MINUTOS LINE 4 COL 47.

       SOLICITAR-DATOS.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.

           INITIALIZE PIN-ACTUAL-USER.
           INITIALIZE PIN-NUEVO-1.
           INITIALIZE PIN-NUEVO-2.

           DISPLAY "Cambio de clave personal" LINE 8 COL 20.
           DISPLAY "Introduzca clave actual:" LINE 10 COL 15.
           DISPLAY "Introduzca nueva clave:" LINE 11 COL 15.
           DISPLAY "Repita la nueva clave:" LINE 12 COL 15.

           DISPLAY "Enter - Confirmar" LINE 24 COL 2.
           DISPLAY "ESC - Cancelar" LINE 24 COL 65.

           ACCEPT FORMULARIO-CLAVE ON EXCEPTION
               IF ESC-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO SOLICITAR-DATOS.

       VERIFICAR-DATOS.
           OPEN I-O TARJETAS.
           IF FST NOT = "00" GO TO PSYS-ERR.

           MOVE TNUM-LINK TO TNUM.
           READ TARJETAS INVALID KEY GO TO PSYS-ERR.

           OPEN I-O INTENTOS.
           IF FSI NOT = "00" GO TO PSYS-ERR.

           MOVE TNUM-LINK TO INUM.
           READ INTENTOS INVALID KEY GO TO PSYS-ERR.

           IF IINTENTOS = 0
               GO TO PINT-ERR.

           IF PIN-ACTUAL-USER NOT = TPIN
               GO TO PPIN-ERR.

           IF PIN-NUEVO-1 NOT = PIN-NUEVO-2
               GO TO PCOINCIDENCIA-ERR.

       GUARDAR-NUEVA-CLAVE.
           MOVE PIN-NUEVO-1 TO TPIN.
           REWRITE TAJETAREG INVALID KEY GO TO PSYS-ERR.

           MOVE 3 TO IINTENTOS.
           REWRITE INTENTOSREG INVALID KEY GO TO PSYS-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

       EXITO.
           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.

           DISPLAY "Cambio de clave personal" LINE 8 COL 20.
           DISPLAY "La clave se ha cambiado correctamente" LINE 12 COL 20.
           DISPLAY "Enter - Aceptar" LINE 24 COL 33.

       EXITO-ENTER.
           ACCEPT CHOICE LINE 24 COL 80 ON EXCEPTION
               IF ENTER-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO EXITO-ENTER.

       PPIN-ERR.
           SUBTRACT 1 FROM IINTENTOS.
           REWRITE INTENTOSREG INVALID KEY GO TO PSYS-ERR.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           IF IINTENTOS = 0
               GO TO PINT-ERR.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "La clave actual es incorrecta" LINE 9 COL 25.
           DISPLAY "Le quedan " LINE 11 COL 30.
           DISPLAY IINTENTOS LINE 11 COL 40.
           DISPLAY " intentos" LINE 11 COL 42.

           DISPLAY "Enter - Reintentar" LINE 24 COL 2.
           DISPLAY "ESC - Cancelar" LINE 24 COL 65.

       PPIN-ERR-ENTER.
           ACCEPT CHOICE LINE 24 COL 80 ON EXCEPTION
               IF ENTER-PRESSED
                   GO TO SOLICITAR-DATOS
               ELSE
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   ELSE
                       GO TO PPIN-ERR-ENTER.

       PCOINCIDENCIA-ERR.
           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Las nuevas claves introducidas no coinciden" 
               LINE 10 COL 18.

           DISPLAY "Enter - Reintentar" LINE 24 COL 2.
           DISPLAY "ESC - Cancelar" LINE 24 COL 65.

       PCOINCIDENCIA-ENTER.
           ACCEPT CHOICE LINE 24 COL 80 ON EXCEPTION
               IF ENTER-PRESSED
                   GO TO SOLICITAR-DATOS
               ELSE
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   ELSE
                       GO TO PCOINCIDENCIA-ENTER.

       PINT-ERR.
           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Se ha sobrepasado el numero de intentos" LINE 9 COL 20.
           DISPLAY "Por su seguridad se ha bloqueado la tarjeta" LINE 11 COL 18.
           DISPLAY "Acuda a una sucursal" LINE 12 COL 30.

           DISPLAY "Enter - Aceptar" LINE 24 COL 33.

       PINT-ERR-ENTER.
           ACCEPT CHOICE LINE 24 COL 80 ON EXCEPTION
               IF ENTER-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO PINT-ERR-ENTER.

       PSYS-ERR.
           CLOSE TARJETAS.
           CLOSE INTENTOS.

           PERFORM IMPRIMIR-CABECERA THRU IMPRIMIR-CABECERA.
           DISPLAY "Ha ocurrido un error interno" LINE 9 COL 25.
           DISPLAY "Vuelva mas tarde" LINE 11 COL 32.

           DISPLAY "Enter - Aceptar" LINE 24 COL 33.

       PSYS-ERR-ENTER.
           ACCEPT CHOICE LINE 24 COL 80 ON EXCEPTION
               IF ENTER-PRESSED
                   EXIT PROGRAM
               ELSE
                   GO TO PSYS-ERR-ENTER.