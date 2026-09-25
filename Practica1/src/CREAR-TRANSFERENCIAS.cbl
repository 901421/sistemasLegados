       IDENTIFICATION DIVISION.
       PROGRAM-ID. CREAR-TRANSFERENCIAS.

      *    CREA TRANSFERENCIAS.UBD VACIO (D13). LO EJECUTAN BUILD.BAT Y
      *    BUILD.SH EN LA MAQUINA DONDE SE INSTALA EL CAJERO.

       ENVIRONMENT DIVISION.
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
           02 TRANS-NUM             PIC 9(35).
           02 TRANS-ORIGEN          PIC 9(16).
           02 TRANS-DESTINO         PIC 9(16).
           02 TRANS-IMPORTE-ENT     PIC 9(7).
           02 TRANS-IMPORTE-DEC     PIC 9(2).
           02 TRANS-TIPO            PIC 9(1).
           02 TRANS-ANO             PIC 9(4).
           02 TRANS-MES             PIC 9(2).
           02 TRANS-DIA             PIC 9(2).
           02 TRANS-ALTA-ANO        PIC 9(4).
           02 TRANS-ALTA-MES        PIC 9(2).
           02 TRANS-ALTA-DIA        PIC 9(2).

       WORKING-STORAGE SECTION.

       77 FSTR                      PIC X(2).

       PROCEDURE DIVISION.

       INICIO.
           OPEN OUTPUT F-TRANSFERENCIAS.

           IF FSTR <> "00"
               DISPLAY "ERROR AL CREAR TRANSFERENCIAS.UBD"
               DISPLAY "FILE STATUS: " FSTR
               STOP RUN
           END-IF.

           CLOSE F-TRANSFERENCIAS.

           DISPLAY "TRANSFERENCIAS.UBD CREADO CORRECTAMENTE.".

           STOP RUN.
