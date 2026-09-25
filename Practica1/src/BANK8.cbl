       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK8.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.

       INPUT-OUTPUT SECTION.
       FILE-CONTROL.
           SELECT TARJETAS ASSIGN TO "tarjetas.ubd"
               ORGANIZATION IS INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS TNUM
               FILE STATUS IS FST.

           SELECT INTENTOS ASSIGN TO "intentos.ubd"
               ORGANIZATION IS INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS INUM
               FILE STATUS IS FSI.

       DATA DIVISION.

       FILE SECTION.

       FD TARJETAS
           LABEL RECORD STANDARD.
       01 TAJETAREG.
           02 TNUM                    PIC 9(16).
           02 TPIN                    PIC 9(4).

       FD INTENTOS
           LABEL RECORD STANDARD.
       01 INTENTOSREG.
           02 INUM                    PIC 9(16).
           02 IINTENTOS               PIC 9(1).

       WORKING-STORAGE SECTION.

       77 FST                         PIC X(2).
       77 FSI                         PIC X(2).


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
               10 ANO                 PIC 9(4).
               10 MES                 PIC 9(2).
               10 DIA                 PIC 9(2).
           05 HORA.
               10 HORAS               PIC 9(2).
               10 MINUTOS             PIC 9(2).
               10 SEGUNDOS            PIC 9(2).
               10 MILISEGUNDOS        PIC 9(2).
           05 DIF-GMT                 PIC S9(4).

       01 KEYBOARD-STATUS             PIC 9(4).
           88 ENTER-PRESSED           VALUE 0.
           88 ESC-PRESSED             VALUE 2005.

       77 PIN-ACTUAL-USER             PIC 9(4).
       77 PIN-NUEVO-1                 PIC 9(4).
       77 PIN-NUEVO-2                 PIC 9(4).
       77 CHOICE                      PIC X(1).

       LINKAGE SECTION.

       77 TNUM-LINK                   PIC 9(16).

       SCREEN SECTION.

       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN.

       01 FORMULARIO-CLAVE.
           05 CLAVE-ACTUAL-ACCEPT
               BLANK ZERO AUTO SECURE
               LINE 10 COL 40
               PIC 9(4)
               USING PIN-ACTUAL-USER.

           05 CLAVE-NUEVA-1-ACCEPT
               BLANK ZERO AUTO SECURE
               LINE 11 COL 40
               PIC 9(4)
               USING PIN-NUEVO-1.

           05 CLAVE-NUEVA-2-ACCEPT
               BLANK ZERO AUTO SECURE
               LINE 12 COL 40
               PIC 9(4)
               USING PIN-NUEVO-2.

       PROCEDURE DIVISION USING TNUM-LINK.

       INICIO.
           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'.
           SET ENVIRONMENT 'COB_SCREEN_ESC' TO 'Y'.

       IMPRIMIR-CABECERA.
           DISPLAY BLANK-SCREEN.
           DISPLAY "Cajero Automatico UnizarBank"
               AT 0226.

           MOVE FUNCTION CURRENT-DATE TO CAMPOS-FECHA.

           DISPLAY DIA AT 0432.
           DISPLAY "-" AT 0434.
           DISPLAY MES AT 0435.
           DISPLAY "-" AT 0437.
           DISPLAY ANO AT 0438.
           DISPLAY HORAS AT 0444.
           DISPLAY ":" AT 0446.
           DISPLAY MINUTOS AT 0447.

       SOLICITAR-DATOS.
           PERFORM IMPRIMIR-CABECERA.

           INITIALIZE PIN-ACTUAL-USER.
           INITIALIZE PIN-NUEVO-1.
           INITIALIZE PIN-NUEVO-2.

           DISPLAY "Cambio de clave personal"
               AT 0820.
           DISPLAY "Introduzca clave actual:"
               AT 1015.
           DISPLAY "Introduzca nueva clave:"
               AT 1115.
           DISPLAY "Repita la nueva clave:"
               AT 1215.

           DISPLAY "Enter - Confirmar"
               AT 2402.
           DISPLAY "ESC - Cancelar"
               AT 2465.

           ACCEPT FORMULARIO-CLAVE
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   ELSE
                       GO TO SOLICITAR-DATOS
                   END-IF
           END-ACCEPT.

       VERIFICAR-DATOS.
           OPEN I-O TARJETAS.

           IF FST NOT = "00"
               GO TO PSYS-ERR
           END-IF.

           MOVE TNUM-LINK TO TNUM.

           READ TARJETAS
               INVALID KEY
                   CLOSE TARJETAS
                   GO TO PSYS-ERR
           END-READ.

           OPEN I-O INTENTOS.

           IF FSI NOT = "00"
               CLOSE TARJETAS
               GO TO PSYS-ERR
           END-IF.

           MOVE TNUM-LINK TO INUM.

           READ INTENTOS
               INVALID KEY
                   CLOSE TARJETAS
                   CLOSE INTENTOS
                   GO TO PSYS-ERR
           END-READ.

           IF IINTENTOS = 0
               CLOSE TARJETAS
               CLOSE INTENTOS
               GO TO PINT-ERR
           END-IF.

           IF PIN-ACTUAL-USER NOT = TPIN
               GO TO PPIN-ERR
           END-IF.

           IF PIN-NUEVO-1 NOT = PIN-NUEVO-2
               CLOSE TARJETAS
               CLOSE INTENTOS
               GO TO PCOINCIDENCIA-ERR
           END-IF.

       GUARDAR-NUEVA-CLAVE.
           MOVE PIN-NUEVO-1 TO TPIN.

           REWRITE TAJETAREG
               INVALID KEY
                   CLOSE TARJETAS
                   CLOSE INTENTOS
                   GO TO PSYS-ERR
           END-REWRITE.

           MOVE 3 TO IINTENTOS.

           REWRITE INTENTOSREG
               INVALID KEY
                   CLOSE TARJETAS
                   CLOSE INTENTOS
                   GO TO PSYS-ERR
           END-REWRITE.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           GO TO EXITO.

       EXITO.
           PERFORM IMPRIMIR-CABECERA.

           DISPLAY "Cambio de clave personal"
               AT 0820.
           DISPLAY "La clave se ha cambiado correctamente"
               AT 1220.
           DISPLAY "Enter - Aceptar"
               AT 2433.
           
           GO TO EXITO-ENTER.
           
       EXITO-ENTER.
           ACCEPT OMITTED AT 2480
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   END-IF
           END-ACCEPT.

           IF ENTER-PRESSED OR KEYBOARD-STATUS = 0
               EXIT PROGRAM
           ELSE
               GO TO EXITO-ENTER
           END-IF.
           
       PPIN-ERR.
           SUBTRACT 1 FROM IINTENTOS.

           REWRITE INTENTOSREG
               INVALID KEY
                   CLOSE TARJETAS
                   CLOSE INTENTOS
                   GO TO PSYS-ERR
           END-REWRITE.

           CLOSE TARJETAS.
           CLOSE INTENTOS.

           IF IINTENTOS = 0
               GO TO PINT-ERR
           END-IF.

           PERFORM IMPRIMIR-CABECERA.

           DISPLAY "La clave actual es incorrecta"
               AT 0925
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Le quedan"
               AT 1130
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY IINTENTOS
               AT 1140
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY " intentos"
               AT 1142
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           
           DISPLAY "Enter - Reintentar"
               AT 2402.
           DISPLAY "ESC - Cancelar"
               AT 2465.

       PPIN-ERR-ENTER.
           ACCEPT OMITTED AT 2480
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   ELSE
                       GO TO PPIN-ERR-ENTER
                   END-IF
           END-ACCEPT.

           GO TO SOLICITAR-DATOS.

       PCOINCIDENCIA-ERR.
           PERFORM IMPRIMIR-CABECERA.

           DISPLAY "Las nuevas claves introducidas no coinciden"
               AT 1018
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.

           DISPLAY "Enter - Reintentar"
               AT 2402.
           DISPLAY "ESC - Cancelar"
               AT 2465.

       PCOINCIDENCIA-ENTER.
           ACCEPT CHOICE AT 2480
               ON EXCEPTION
                   IF ENTER-PRESSED
                       GO TO SOLICITAR-DATOS
                   ELSE
                       IF ESC-PRESSED
                           EXIT PROGRAM
                       ELSE
                           GO TO PCOINCIDENCIA-ENTER
                       END-IF
                   END-IF
           END-ACCEPT.

       PINT-ERR.
           PERFORM IMPRIMIR-CABECERA.

           DISPLAY "Se ha sobrepasado el numero de intentos"
               AT 0920
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Por su seguridad se ha bloqueado la tarjeta"
               AT 1118
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Acuda a una sucursal"
               AT 1230
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar"
               AT 2433.

       PINT-ERR-ENTER.
           ACCEPT CHOICE AT 2480
               ON EXCEPTION
                   IF ENTER-PRESSED
                       EXIT PROGRAM
                   ELSE
                       GO TO PINT-ERR-ENTER
                   END-IF
           END-ACCEPT.

       PSYS-ERR.
           PERFORM IMPRIMIR-CABECERA.

           DISPLAY "Ha ocurrido un error interno"
               AT 0925
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Vuelva mas tarde"
               AT 1132
               WITH FOREGROUND-COLOR IS BLACK
                    BACKGROUND-COLOR IS RED.
           DISPLAY "Enter - Aceptar"
               AT 2433.

       PSYS-ERR-ENTER.
           ACCEPT CHOICE AT 2480
               ON EXCEPTION
                   IF ENTER-PRESSED
                       EXIT PROGRAM
                   ELSE
                       GO TO PSYS-ERR-ENTER
                   END-IF
           END-ACCEPT.
           
       FIN-BANK8.
           EXIT PROGRAM.
		   