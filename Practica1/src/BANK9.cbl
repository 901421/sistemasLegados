       IDENTIFICATION DIVISION.
       PROGRAM-ID. BANK9.

      *    OPCION 6 - LISTADO DE TRANSFERENCIAS.

       ENVIRONMENT DIVISION.
       CONFIGURATION SECTION.
       SPECIAL-NAMES.
           CRT STATUS IS KEYBOARD-STATUS.


       DATA DIVISION.
       WORKING-STORAGE SECTION.

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

       LINKAGE SECTION.
       77 TNUM                     PIC  9(16).

       SCREEN SECTION.
       01 BLANK-SCREEN.
           05 FILLER LINE 1 BLANK SCREEN BACKGROUND-COLOR BLACK.


       PROCEDURE DIVISION USING TNUM.
       IMPRIMIR-CABECERA.

           SET ENVIRONMENT 'COB_SCREEN_EXCEPTIONS' TO 'Y'.
           SET ENVIRONMENT 'COB_SCREEN_ESC'        TO 'Y'.

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

       PLISTADO.
           DISPLAY "Listado de transferencias" AT 0828.
           DISPLAY "ESC - Salir" AT 2434.

       PLISTADO-ESC.
           ACCEPT OMITTED AT 2480
               ON EXCEPTION
                   IF ESC-PRESSED
                       EXIT PROGRAM
                   END-IF
           END-ACCEPT.

           GO TO PLISTADO-ESC.
