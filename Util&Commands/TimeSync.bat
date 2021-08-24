REM @ECHO OFF
ECHO PARANDO SERVICO
net stop w32time 
TIMEOUT 3
ECHO INICIANDO SERVICO
net start w32time
TIMEOUT 3
ECHO SINCRONIZANDO A HORA
w32tm /config /update /manualpeerlist:"pool.ntp.org time.windows.com time.apple.com time.google.com"
w32tm /resync
w32tm /query /peers
ECHO  !!!!!!HORA SINCRONIZADA!!!!!!!!
TIMEOUT 15
EXIT