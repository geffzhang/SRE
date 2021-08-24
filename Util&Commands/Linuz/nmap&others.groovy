COMBINAÇÕES PARA UM BOM SAN - NMAP
Bem, como a maioria conhece ou já ouviu falar, o nmap é o melhor scan da atualidade. Abaixo vou citar alguns comandos e as combinações que mais utilizo, me ajuda bastante. =) 

# nmap -sTUR -O -v -p 1-65535 -P0 hostname.domain 

Explicando o comando acima:

s => scan type (tipo de scaneamento)
T => utiliza o protocolo TCP
U => utiliza o protocolo UDP
R => utiliza o protocolo RPC
-O => tenta descobrir o sistema operacional
-v => verbose mode
-p => escaneia as portas de 1 a 65535
-sS => ativa o scan escondido, Scan Stealth

OBS: Sistemas que tiverem algum tipo de firewall instalado farão, na maioria das vezes, que o scan não seja efetuado ou que ele demore muito para terminar. 

Continuando a explicação de cada opção... 

Para fazer o scan de uma maneira mais rápida, substituímos o -p pela flag -F (default). Essa flag fará o scan somente das portas privilegiadas (0-1023) e nas portas mais usadas em serviços conhecidos (1024-49,151). Isso pode ser bem útil ao invés de passar por todas as 65.535 portas!

-P0 => Essa opção diz ao nmap para não pingar o host de destino. Isso é útil também quando se faz um scan em uma máquina que possua firewall. Se o firewall bloqueia pacotes ICMP (o que, pessoalmente, não acho uma boa idéia), o nmap nem vai rodar sobre o host.

A combinação mais viável acaba ficando assim: 

# nmap -sS -O -P0 -v hostname.domain

#################################################################################################################################################


SSH SEM SENHA E SUAS FACILIDADES ADMINISTRATIVAS
Olá pessoal, um recurso muito interessante do SSH é a possibilidade de usarmos chaves no lugar de senhas para acessar outras máquinas. Dããã isso todo mundo já sabe e há toneladas de informações sobre isso na internet. Mas vou demonstrar aqui que esse recurso pode ser muito mais útil do que simplesmente acessar o servidor da sua rede sem o incoveniente de ficar digitando senhas.
Bem, vamos então ao básico, como configurar esse acesso. Logado na máquina que fará o acesso e com a conta do usuário que fará o acesso (isso ficou repetitivo não?) use o comando ssh-keygen:

$ ssh-keygen

Ele fará umas perguntas e mostrará os arquivos onde guardará as chaves, que são:

~/.ssh/id_rsa
~/.ssh/id_rsa.pub

Essa id_rsa.pub é a sua chave pública, é como se fosse a impressão digital da sua máquina, e é ela que usaremos para acessar a outra máquina. Agora você deve copiar o conteúdo desse arquivo para o arquivo ~/.ssh/authorized_keys do usuário na máquina que será acessada.
Como a coisa funciona? Na hora em que você usa o comando ssh para logar em uma máquina o que acontece é mais ou menos isso:

$ ssh infog@debian

Sua Máquina: Tenta conectar à porta 22 da máquina remota.
Máquina Remota: Aceita a conexão.
SM: Verifica se a máquina remota é conhecida, checando o arquivo ~./ssh/known_hosts, se não for ela pergunta se você quer adicioná-la à lista. Se for conhecida ela checa a identidade da máquina remota. Isso é muito útil, imagine que alguém trocou a máquina que você acessava ou está usando o mesmo IP dessa máquina para capturar senhas, o ssh consegue saber se é a mesma máquina ou não e se não for ele te avisa, assim você pode, por exemplo, ligar para o local onde a máquina fica e perguntar o que houve. Continuando… Então rola um papo entre as máquinas:
SM: – Opa, e aí debian tudo bem, eu sou o infog e venho da máquina gnu.
MR: – Olá infog, tudo certo aqui. Ok, deixe-me ver sua impressão digital?
SM: – Ah, claro que sim, aqui está (~/.ssh/id_rsa.pub).
MR: – Obrigado, vou conferir, aguarde por favor. (Compara o id_rsa.pub com o ~/.ssh/authorized_keys, se a impressão estiver no authorized_keys o acesso é permitido):
MR: – Oi, desculpe pela demora, sua impressão confere com minha lista, seja bem vindo!
SM: – Obrigado!

Caso a impressão não esteja na lista o seguinte aconteceria:

MR: Poxa cara você não está na lista, podemos tentar com outra impressão? (Se você tiver outro tipo de chave ela será enviada).
SM: Eu não tenho outra impressão, o que faremos?
MR: Hum, você pode me mandar uma senha e eu comparo com a do usuário aqui. Pode ser? Digite aqui:
password:

Bem daí pra você sabe o que acontece.

Mas quais são as facilidades disso além de nãao digitar a senha? Imagine que você está com um problema em sua máquina e não esteja conseguindo resolver, então aquele cara que você conheceu no irc se oferece para ajudar, mas você não quer dar sua senha para ele e nem quer mudar a senha da máquina para algo como 123 para que ele acesse. Aqui você poderia pedir para ele enviar a chave pública dele, você coloca no seu authorized_keys, ele acessa e te ajuda, e depois disso você remove a chave do authorized_keys. Pronto ele te ajudou e nem sabe do tipo de senha que você usa e você pode autorizá-lo ou desautorizá-lo sempre que precisar.

Lembrem-se crianças, não vão sair adicionando qualquer um no seu authorized_keys e quando o fizer faça com um usuário que tenha permissões apenas para ajudar na solução do seu problema.

Bem, é isso. Boa sorte à todos e lembrem-se de ler o manual para obter mais recursos e aprender sempre mais sobre as ferramentas que você usa no dia-a-dia.

Link retirado dê: http://infog.casoft.info/2007/12/ssh-sem-senha-e-suas-facilidades-administrativas/

#############################################################################################################



USANDO O NFS
Usando o NFS
Enquanto o Samba permite solucionar sem muita dor de cabeça o desafio de interligar máquinas Linux e Windows na mesma rede, o NFS é uma opção para compartilhar sistemas de arquivos entre máquinas Linux, de uma forma prática e estável. Na verdade, você pode perfeitamente usar o Samba para compartilhar arquivos entre máquinas Linux, mas o NFS não deixa de ser um recurso importante, que você não deve deixar de estudar.

Assim como o Samba, o NFS é um servidor que precisa ser habilitado manualmente na maior parte das distribuições. No Mandriva, Fedora e outras distribuições derivadas do Red Hat, procure pelo serviço "nfs". Nas distribuições derivadas do Debian, procure pelo serviço "nfs-kernel-server".

O NFS utiliza um outro serviço, o portmap, para gerenciar as requisições dos clientes. Este serviço precisa estar ativo para que o NFS funcione, ou seja, para inicializar o servidor NFS, você precisa ativar os dois:

# /etc/init.d/portmap start
# /etc/init.d/nfs-common start
# /etc/init.d/nfs-kernel-server start
(ou simplesmente "service portmap start; service nfs start", no Fedora)

A configuração do NFS é feita em um único arquivo, o "/etc/exports", onde vai a configuração dos diretórios compartilhados, um por linha. Originalmente, este arquivo fica vazio, ou contém apenas um comentário. Você precisa apenas abrí-lo num editor de textos e adicionar as pastas que deseja compartilhar. Por exemplo, para compartilhar a pasta "/home/arquivos" como somente leitura, para todos os micros da sua rede local, adicione a linha:

/home/arquivos 192.168.0.*(ro)

Para compartilhar a pasta "/home/trabalhos" com permissão de leitura e escrita, adicione a linha:

/home/trabalhos 192.168.0.*(rw)

Para compartilhar a pasta "/arquivos", de forma que apenas o micro 192.168.0.3 possa acessar:

/arquivos 192.168.0.3(rw)

Outra opção, útil em redes locais, é a "async", que permite que o NFS transfira arquivos de forma assíncrona, sem precisar esperar pela resposta do cliente a cada pacote enviado. Sem a opção async, a taxa de transmissão em uma rede de 100 megabits fica, em geral, em torno de 6 a 7 MB/s, enquanto que, ao ativá-la, sobe para até 11 MB/s, ficando limitada apenas à velocidade da rede e dos HDs no servidor e cliente.

Ao adicioná-la, a linha de compartilhamento ficaria:

/home/trabalhos 192.168.0.*(rw,async)

Você pode usar, ainda, o parâmetro "noacess", que permite que você compartilhe apenas os arquivos dentro do diretório, mas não subdiretórios que eventualmente estejam presentes.

Depois de incluir todos os diretórios que deseja compartilhar, com suas respectivas permissões de acesso, salve o arquivo e reinicie o serviço nfs para que as alterações surtam efeito. Para isso, use o comando:

# /etc/init.d/nfs-kernel-server restart

Sempre que desejar parar o serviço, você pode usar os comandos abaixo, que respectivamente param e inicializam o serviço:

# /etc/init.d/nfs-kernel-server stop
# /etc/init.d/nfs-kernel-server start
(note que no Fedora o serviço não se chama "nfs-kernel-server", mas apenas "nfs").

Embora seja fácil editar diretamente o arquivo "/etc/exports", muitas distribuições incluem ferramentas gráficas para gerenciar os compartilhamentos NFS. O Fedora, por exemplo, inclui o "system-config-nfs" (que se chama "redhat-config-nfs" no Red Hat). O Mandriva inclui um utilitário similar dentro do Mandriva Control Center, enquanto que no Kurumin você encontra um painel de configuração no Iniciar > Redes e Acesso Remoto > NFS.



Ao compartilhar os diretórios, resolvemos apenas metade do problema. Ainda falta acessá-los a partir dos clientes. Assim como no caso das partições, você pode montar os compartilhamentos NFS em qualquer pasta vazia. Muitas empresas utilizam compartilhamentos montados no diretório /home (das estações) para que os arquivos gerados pelos usuários (e armazenados no home) sejam armazenados no compartilhamento do servidor, facilitando os backups, por exemplo.

Caso você monte o compartilhamento em uma pasta que contenha arquivos, estes ficarão momentaneamente inacessíveis, dando lugar aos do compartilhamento. Contudo, depois que o compartilhamento é desativado, eles reaparecem. Nada é perdido.

Para montar o compartilhamento manualmente, use (como root) o comando:

# mkdir /mnt/arquivos
# mount -t nfs 192.168.0.1:/arquivos /mnt/arquivos

Aqui eu comecei criando a pasta "/mnt/arquivos", onde vou montar o compartilhamento. A linha de montagem propriamente dita inclui o sistema de arquivos usado, neste caso o nfs (-t nfs), o endereço IP do servidor, seguido da pasta que ele está compartilhando e, finalmente, a pasta local onde os arquivos ficarão acessíveis.

Ao terminar de acessar o compartilhamento, ou caso precise desligar o servidor, use o comando "umount /mnt/arquivos" (no cliente) para desmontá-lo. É importante desmontar o compartilhamento antes de desligar o servidor, do contrário, o cliente continua tentando acessar o compartilhamento sempre que você acessa a pasta onde ele está montado, o que faz com que os gerenciadores de arquivos e outros programas "parem" ao passar pela pasta, aguardando a resposta do servidor que não está mais lá.

Se você acessa o compartilhamento freqüentemente, pode ganhar tempo inserindo uma entrada referente a ele no arquivo "/etc/fstab". Assim você pode montar o compartilhamento usando o comando simplificado, ou configurar o sistema para montá-lo automaticamente durante o boot. Basta incluir a linha no final do arquivo, deixando sempre uma linha em branco após ela. A linha para o compartilhamento que acabamos de montar seria:

192.168.0.1:/arquivos /mnt/arquivos nfs noauto,users,exec 0 0

Neste exemplo, o "192.168.0.1:/arquivos" é o IP do servidor, seguido pela pasta compartilhada, o "/mnt/arquivos" é a pasta local onde este compartilhamento ficará acessível e o "nfs" é o sistema de arquivos; os mesmos parâmetros que usamos no comando manual.

O "noauto" faz com que o compartilhamento não seja montado automaticamente durante o boot. Você pode montá-lo e desmontá-lo conforme for utilizá-lo, usando os comandos "mount /mnt/arquivos" e "umount /mnt/arquivos". Note que graças à entrada no fstab, você agora precisa especificar apenas a pasta, pois o sistema lê os outros parâmetros a partir da entrada no arquivo.

O parâmetro "users" permite que você monte e desmonte o compartilhamento usando seu login normal, sem precisar usar o root e o "exec", que permite executar programas dentro do compartilhamento. Caso você esteja preocupado com a segurança, pode remover as duas opções.

Você pode facilitar o acesso ao compartilhamento adicionando um ícone para ele no desktop do KDE. Para isso, clique com o botão direito sobre uma área vazia e acesse a opção: "Criar novo > Dispositivo > NFS".

Na janela que se abre, acesse a aba "Dispositivo" e aponte a entrada que foi adicionada ao fstab. A partir daí você monta o compartilhamento clicando sobre o ícone, e pode desmontá-lo clicando com o botão direito e usando a opção "desmontar".



Você pode incluir várias linhas, se desejar montar vários compartilhamentos. Caso o servidor fique sempre ligado e você queira que o compartilhamento seja montado automaticamente durante o boot, retire o "noauto". Neste caso, a linha ficaria:

192.168.0.1:/arquivos /mnt/arquivos nfs users,exec 0 0

Novamente, este é o procedimento manual, muitas distribuições incluem utilitários gráficos para facilitar isso. No Mandriva, por exemplo, você encontra um utilitário de montagem no Centro de Controle (mcc), em "Pontos de Montagem > Pontos de montagem NFS".

Nele você clica no "Servidores de busca" para ver uma lista dos compartilhamentos disponíveis na rede. Ao escolher um compartilhamento, clique no "Ponto de montagem" para definir a pasta local onde ele será acessado e configure as opções adicionais (como o "noauto" e "user") em "Opções". Depois de terminar, clique no "Pronto" e ele pergunta se você quer salvar a configuração no "/etc/fstab".



Mais um comando útil ao utilizar o NFS é o "showmount -a" (só funciona se dado pelo root) que mostra uma lista com os diretórios NFS compartilhados na sua máquina que foram acessados e quais máquinas os acessaram desde o último reboot. Não é muito específico, pois não mostra datas nem horários, mas pelo menos permite descobrir se alguém não autorizado está acessando os compartilhamentos.

Link retirado dê:http://www.hardware.com.br/livros/linux-redes/usando-nfs.html


