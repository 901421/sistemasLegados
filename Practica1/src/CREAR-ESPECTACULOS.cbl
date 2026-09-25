       IDENTIFICATION DIVISION.
       PROGRAM-ID. CREAR-ESPECTACULOS.

      *    DATOS DE PRUEBA: LOS 4 ESPECTACULOS DEL FICHERO ORIGINAL
      *    (MISMO NUMERO, HORA, DESCRIPCION, ENTRADAS Y PRECIO) CON
      *    FECHAS FUTURAS CALCULADAS A PARTIR DE HOY, PARA QUE LA
      *    OPCION 6 LOS MUESTRE. EL espectaculos.ubd ORIGINAL (2015-16)
      *    SE CONSERVA EN original/.

       ENVIRONMENT DIVISION.
       INPUT-OUTPUT SECTION.
       FILE-CONTROL.

           SELECT F-ESPECTACULOS ASSIGN TO DISK
               ORGANIZATION IS INDEXED
               ACCESS MODE IS DYNAMIC
               RECORD KEY IS ESP-NUM
               FILE STATUS IS FSE.

       DATA DIVISION.
       FILE SECTION.

       FD F-ESPECTACULOS
           LABEL RECORD STANDARD
           VALUE OF FILE-ID IS "espectaculos.ubd".

       01 ESPECTACULO-REG.
           02 ESP-NUM               PIC 9(4).
           02 ESP-ANO               PIC 9(4).
           02 ESP-MES               PIC 9(2).
           02 ESP-DIA               PIC 9(2).
           02 ESP-HOR               PIC 9(2).
           02 ESP-MIN               PIC 9(2).
           02 ESP-DESCR             PIC X(40).
           02 ESP-DISP              PIC 9(7).
           02 ESP-PRECIO-ENT        PIC 9(4).
           02 ESP-PRECIO-DEC        PIC 9(2).

       WORKING-STORAGE SECTION.

       77 FSE                       PIC X(2).
       77 HOY                       PIC 9(8).
       77 DIAS-HOY                  PIC 9(7).
       77 DIAS-DESDE-HOY            PIC 9(3).
       77 FECHA-ESP                 PIC 9(8).

       PROCEDURE DIVISION.

       INICIO.
           MOVE FUNCTION CURRENT-DATE(1:8) TO HOY.
           COMPUTE DIAS-HOY = FUNCTION INTEGER-OF-DATE(HOY).

           OPEN OUTPUT F-ESPECTACULOS.

           IF FSE <> "00"
               DISPLAY "ERROR AL CREAR ESPECTACULOS.UBD"
               DISPLAY "FILE STATUS: " FSE
               STOP RUN
           END-IF.

      *    ESPECTACULO 1 - DENTRO DE 30 DIAS
           MOVE 1 TO ESP-NUM.
           MOVE 30 TO DIAS-DESDE-HOY.
           PERFORM CALCULAR-FECHA.
           MOVE 22 TO ESP-HOR.
           MOVE 30 TO ESP-MIN.
           MOVE "Julio Iglesias: El Truan" TO ESP-DESCR.
           MOVE 20000 TO ESP-DISP.
           MOVE 300 TO ESP-PRECIO-ENT.
           MOVE 0 TO ESP-PRECIO-DEC.
           PERFORM ESCRIBIR.

      *    ESPECTACULO 2 - DENTRO DE 45 DIAS
           MOVE 2 TO ESP-NUM.
           MOVE 45 TO DIAS-DESDE-HOY.
           PERFORM CALCULAR-FECHA.
           MOVE 18 TO ESP-HOR.
           MOVE 30 TO ESP-MIN.
           MOVE "Los Reyes Magos en Concierto" TO ESP-DESCR.
           MOVE 1000 TO ESP-DISP.
           MOVE 30 TO ESP-PRECIO-ENT.
           MOVE 0 TO ESP-PRECIO-DEC.
           PERFORM ESCRIBIR.

      *    ESPECTACULO 3 - DENTRO DE 60 DIAS
           MOVE 3 TO ESP-NUM.
           MOVE 60 TO DIAS-DESDE-HOY.
           PERFORM CALCULAR-FECHA.
           MOVE 21 TO ESP-HOR.
           MOVE 20 TO ESP-MIN.
           MOVE "David Bisbal: el pelo rizado" TO ESP-DESCR.
           MOVE 23490 TO ESP-DISP.
           MOVE 20 TO ESP-PRECIO-ENT.
           MOVE 99 TO ESP-PRECIO-DEC.
           PERFORM ESCRIBIR.

      *    ESPECTACULO 4 - DENTRO DE 75 DIAS
           MOVE 4 TO ESP-NUM.
           MOVE 75 TO DIAS-DESDE-HOY.
           PERFORM CALCULAR-FECHA.
           MOVE 22 TO ESP-HOR.
           MOVE 10 TO ESP-MIN.
           MOVE "La Pantoja: Entre Rejas" TO ESP-DESCR.
           MOVE 9998 TO ESP-DISP.
           MOVE 200 TO ESP-PRECIO-ENT.
           MOVE 0 TO ESP-PRECIO-DEC.
           PERFORM ESCRIBIR.

           CLOSE F-ESPECTACULOS.

           DISPLAY "ESPECTACULOS.UBD CREADO CORRECTAMENTE."
           DISPLAY "4 ESPECTACULOS DE PRUEBA CON FECHAS FUTURAS."

           STOP RUN.

       CALCULAR-FECHA.
           COMPUTE FECHA-ESP = FUNCTION DATE-OF-INTEGER(
               DIAS-HOY + DIAS-DESDE-HOY).
           MOVE FECHA-ESP(1:4) TO ESP-ANO.
           MOVE FECHA-ESP(5:2) TO ESP-MES.
           MOVE FECHA-ESP(7:2) TO ESP-DIA.

       ESCRIBIR.
           WRITE ESPECTACULO-REG.

           IF FSE <> "00"
               DISPLAY "ERROR ESCRIBIENDO ESPECTACULO " ESP-NUM
               DISPLAY "FILE STATUS: " FSE
               CLOSE F-ESPECTACULOS
               STOP RUN
           END-IF.
