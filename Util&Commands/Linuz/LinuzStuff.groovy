Corrige acessos

rsync -raptugolv



#!/bin/bash
echo -e "# Cria TIMEOUT para todos os usuarios\nTMOUT=1800\nreadonly TMOUT\nexport TMOUT" > /etc/profile.d/tmout.sh
cp --preserve=all /etc/ntp.conf /etc/ntp.conf_orig ; egrep -v ^server /etc/ntp.conf_orig > /etc/ntp.conf
egrep "200.20.186.75" /etc/ntp.conf >/devnull 2>&1
if [ $? -ne 0 ]; then
        echo -e "# Servidores oficiais de padrão de tempo no Brasil" >> /etc/ntp.conf
        echo -e "servers pool.ntp.br\nserver 200.20.186.75\nserver 200.20.186.94" >> /etc/ntp.conf
fi
type systemctl > /dev/null 2>&1
if [ $? -eq 0 ]; then
        systemctl enable ntpd
else
        type chkconfig > /dev/null 2>&1
        if [ $? -eq 0 ]; then
                chkconfig ntpd on
        else
                echo "Nao ha systemctl ou chkconfig, providenciar inicializacao automatica do ntp"
        fi
fi
# Ajuste ssh
echo 'Machina First Law: 


                 UNAUTHORIZED ACCESS WILL BE PROSECUTED

                   TO THE FULLEST EXTENT OF THE LAW!!


                      Somente acesso autorizado!

------------------------------------------------------------------------------
' > /etc/ssh/ssh_banner
cp --preserve=all /etc/issue /etc/issue_orig ; cat /etc/ssh/ssh_banner > /etc/issue
echo -e "\\S\nKernel \\\r on an \\m" >> /etc/issue
#Faz Backup do SSH-CONFIG
grep "\/etc\/ssh\/keys" /etc/ssh/sshd_config  > /dev/null 2>&1
if [ $? -ne 0 ]; then
        cp --preserve=all /etc/ssh/sshd_config /etc/ssh/sshd_config_orig
        #Altera o arquivo de configuração do SSH para Porta 22,SSH-KEYS e desabilita root 
        cat /etc/ssh/sshd_config_orig  | sed  's/^.\{0,4\}Port .*/Port 22\nPort 222/I ;s/^.\{0,4\}ClientAliveInterval.*/ClientAliveInterval 1800/I ;s/^.\{0,4\}IgnoreRhosts.*/IgnoreRhosts yes/I ; s/^.\{0,4\}PermitRootLogin.*/PermitRootLogin no/I; s/^.\{0,4\}PermitEmptyPasswords.*/PermitEmptyPasswords no/I ; s/^.\{0,4\}Banner.*/Banner \/etc\/ssh\/ssh_banner/I; s/^.\{0,4\}AuthorizedKeysFile.*/AuthorizedKeysFile      \/etc\/ssh\/keys\/%u\/authorized_keys/I' > /etc/ssh/sshd_config
fi
#Cria Diretorio onde será armazendo as Chaves SSH
[ -d /etc/ssh/keys ] || mkdir /etc/ssh/keys
# Ajustando diretorios chaves, por causa do backup que tem acesso restrito de uma maquina "Match Address IP \n PermitRootLogin yes"
cd /etc/ssh/keys
for id_dir in $(find /etc/ssh/keys -maxdepth 1 -type d -printf "%P\n" | egrep -v ^$); do 
        id ${id_dir} > /dev/null 2>&1
        if [ $? -eq 0 ]; then
                [ ! -d  /home/${id_dir}/.ssh ] && mkdir /home/${id_dir}/.ssh
        else
                rm -rf /etc/ssh/keys/${id_dir}
                continue
        fi
        cp -rpf /etc/ssh/keys/${id_dir}/* /home/${id_dir}/.ssh/ 
        rm -rf /etc/ssh/keys/${id_dir}
        ln -s /home/${id_dir}/.ssh /etc/ssh/keys/${id_dir}
        chown -R ${id_dir} /home/${id_dir}/.ssh 
        chmod 700 /home/${id_dir}/.ssh 
done
#Criando Grupo Admins
[ "$(groups admins 2> /dev/null )" == "" ] && groupadd -r admins
#Adicionando Grupo Admins no SUDO
grep -v "%admins" /etc/sudoers >/dev/null 2>&1
if [ $? -ne 0 ]; then
        echo -e "# Usuarios admins\n%admins    ALL=(ALL)     ALL" >> /etc/sudoers
fi
##############/CRIANDO USUARIOS/################################################
function usuario() {
        user="$1"
        nome="$2"
        grupo="$3"
        senha="$4"
        chave="$5"
        dias=$(($(date +%s) / 86400 ))
        tmp=$(mktemp)
        chmod 600 ${tmp}
        ask=0
        if [ "$(id ${user} 2> /dev/null )" == "" ]; then
            useradd -G ${grupo} -c "${nome}" -s /bin/bash -m ${user} || usermod -a -G ${grupo} ${user}
            ask=1
        fi
        if [ ${ask} -eq 0 ]; then
            read -p "Alterar a senha atual de ${user} ? [N/y]" resp
            if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
                ask=1
            fi
        fi
        if [ ${ask} -eq 1 ]; then
            pwdshd=$(grep ${user} /etc/shadow)
            grep -v ${user} /etc/shadow > ${tmp}
            cat ${tmp} > /etc/shadow
            echo ${pwdshd} | awk -F':' -v senha=${senha} '{print $1 ":" senha ":" $3 ":" $4 ":"  $5 ":" $6 ":"  $7 ":" $8 ":"}' >> /etc/shadow
        fi
        if [ ! "${chave}" == "" ]; then
            if [ ! -d  /home/${user}/.ssh ]; then
                mkdir -p /home/${user}/.ssh 
                echo "${chave}" > /home/${user}/.ssh/authorized_keys
                ln -s /home/${user}/.ssh /etc/ssh/keys/${user}
            else
                echo "${chave}" > ${tmp}
                diff  ${tmp} /home/${user}/.ssh/authorized_keys > /dev/null 2>&1
                if [ $? -ne 0 ]; then
                    echo "${chave}" > /home/${user}/.ssh/authorized_keys
                fi
            fi
            chmod 700 /home/${user}/.ssh
            chmod 600 /home/${user}/.ssh/authorized_keys
            chown -R ${user} /home/${user}/.ssh
        fi
        rm -f ${tmp}
        
}
function remove_user() {
        user="$1"
        [ "$(id ${user} 2> /dev/null )" == "" ] || userdel -f -r ${user}
}

##############/Fazendo Backup das Senhas Antigas/###############################
cp --preserve=all /etc/shadow /etc/shadow_$(date +%Y%m%d%H%M).bkp
# machina Moreno
usuario machina "Jose machina Moreno Jr" admins '$6$vj44dF6H$swoR0mg1XdrBl.iAAYy6aQCqzH24lUudn0nBU8EGfY300hLa9hS2s/98liJiBjN8u4WUcrvfaZoqpDXzAH/Da0' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCsYNInip7HX4CME6RvJF5Q9PIrRNjSs+Y51Db92QcIT20Ol56UFJ2aYUx0FbS4QsBwc1W1EPmACbintvcaYSfq2KCfeaiaLCQLRa7LvROndAt9SB74rC84iLIf1MSEJHivgn780akT2Eqh3y3vteRlb2z2UYk1DM+t06JMxG9DDtlHtgAbmDd5oiYdmD9CTMj5H91bAf+ThVaEmmq9JwtMLpxDIlcQ2jq+iPJaT5ZznNDg7BFbxoyNDz7fgOYcyXoWVC9oSQIoaFbCxwFwEEMdjvEADDK5sr9fwi3hnRQoA8JMrRdHAxovBZFbWrk2vWx66CyAxpKNY6OfFBBQ/V/J id_rsa_cluster_teste
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJ0DpqOtENJOSzLvr7NDXPlWc5zuq1UWUBR0G4b/uRaXAGu8bl1dZ14DE0AvMVfHT5w2a6hLDESgeG624eMgwRA0SQMuoS1hNr2rMFu6OVRpcQWz/ZUCqqA0fUBQZnt/U5yWsHY3b7mHoybMaGIVEsoKc569nEh/LdGF6KqJSy081hy7qrps86PigopDHPztnkc9wEMMdcA7j0yqv12SGjIriuXURK04Z0rDM6ze91MLXvb5lh3Ln0xuGwrOyV1wEUrBlYOSSfU0sYhzGYKOJ6wDKmEEdAhPwhWRqODd+Kv9xHO+u1460baq3/fVIgFgY+qxXxCV2rJsmrz8EU0763 /home/machina/.ssh/id_rsa
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJ0DpqOtENJOSzLvr7NDXPlWc5zuq1UWUBR0G4b/uRaXAGu8bl1dZ14DE0AvMVfHT5w2a6hLDESgeG624eMgwRA0SQMuoS1hNr2rMFu6OVRpcQWz/ZUCqqA0fUBQZnt/U5yWsHY3b7mHoybMaGIVEsoKc569nEh/LdGF6KqJSy081hy7qrps86PigopDHPztnkc9wEMMdcA7j0yqv12SGjIriuXURK04Z0rDM6ze91MLXvb5lh3Ln0xuGwrOyV1wEUrBlYOSSfU0sYhzGYKOJ6wDKmEEdAhPwhWRqODd+Kv9xHO+u1460baq3/fVIgFgY+qxXxCV2rJsmrz8EU0763 machina@lunar"
usuario machina.moreno "Jose machina Moreno Jr" admins '$6$vj44dF6H$swoR0mg1XdrBl.iAAYy6aQCqzH24lUudn0nBU8EGfY300hLa9hS2s/98liJiBjN8u4WUcrvfaZoqpDXzAH/Da0' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCsYNInip7HX4CME6RvJF5Q9PIrRNjSs+Y51Db92QcIT20Ol56UFJ2aYUx0FbS4QsBwc1W1EPmACbintvcaYSfq2KCfeaiaLCQLRa7LvROndAt9SB74rC84iLIf1MSEJHivgn780akT2Eqh3y3vteRlb2z2UYk1DM+t06JMxG9DDtlHtgAbmDd5oiYdmD9CTMj5H91bAf+ThVaEmmq9JwtMLpxDIlcQ2jq+iPJaT5ZznNDg7BFbxoyNDz7fgOYcyXoWVC9oSQIoaFbCxwFwEEMdjvEADDK5sr9fwi3hnRQoA8JMrRdHAxovBZFbWrk2vWx66CyAxpKNY6OfFBBQ/V/J id_rsa_cluster_teste
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJ0DpqOtENJOSzLvr7NDXPlWc5zuq1UWUBR0G4b/uRaXAGu8bl1dZ14DE0AvMVfHT5w2a6hLDESgeG624eMgwRA0SQMuoS1hNr2rMFu6OVRpcQWz/ZUCqqA0fUBQZnt/U5yWsHY3b7mHoybMaGIVEsoKc569nEh/LdGF6KqJSy081hy7qrps86PigopDHPztnkc9wEMMdcA7j0yqv12SGjIriuXURK04Z0rDM6ze91MLXvb5lh3Ln0xuGwrOyV1wEUrBlYOSSfU0sYhzGYKOJ6wDKmEEdAhPwhWRqODd+Kv9xHO+u1460baq3/fVIgFgY+qxXxCV2rJsmrz8EU0763 /home/machina/.ssh/id_rsa
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDJ0DpqOtENJOSzLvr7NDXPlWc5zuq1UWUBR0G4b/uRaXAGu8bl1dZ14DE0AvMVfHT5w2a6hLDESgeG624eMgwRA0SQMuoS1hNr2rMFu6OVRpcQWz/ZUCqqA0fUBQZnt/U5yWsHY3b7mHoybMaGIVEsoKc569nEh/LdGF6KqJSy081hy7qrps86PigopDHPztnkc9wEMMdcA7j0yqv12SGjIriuXURK04Z0rDM6ze91MLXvb5lh3Ln0xuGwrOyV1wEUrBlYOSSfU0sYhzGYKOJ6wDKmEEdAhPwhWRqODd+Kv9xHO+u1460baq3/fVIgFgY+qxXxCV2rJsmrz8EU0763 machina@lunar"

# Admin
usuario admin "Usuario de Administracao Emergencia" admins '$6$9Y70k9aN$GVmCnbrBSV2Hfp4dTUSsIGKiC.hdjsGyY1ZC7VA/GYNGUqIXdbMSr8a2LTv7A7KktGfJ20euYgKdRKg2fjzqM0'
usuario jefferson.moura "Jefferson Moura" admins '$6$G7KPcYoExJqkJ25i$uqOTRQRJRe.9.iZ/rlTYXl8FioS1S9S82q4BjlxgmP9M/0TCZQ0936Sx50KNFKb36fmguYj8RBbkW0J53.00T0' 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC5U/wJFqE9ZknPa3Kn9Ee2zgl0J6vJsW3/xudJUAMXirU2gyN4cmPvF4hY+QigoG2xEqqIfgDedHAQwj0kTKDMzbYGByZSsfXl+JlXfbAYSORseN/Z69nOk4sMl3a4mQqsmhSVh6TeiwWS7Rlp74+fdZou2CB0DWnKfMZ7nXk7cMh/CGelUs9JCDzcsmCZRDv1NBCv1ZO89smSduAvhmh0EWWpBvW52wB1Ixih8jNRLyOmcoP/ZxjQ3WQe1HQjoD2HbYvTd+6BVg7uHH3Rx0/63QdsvhvW5LwLshK2HnXclhNadAVFzWmhhrxhNJVp4X1WKUJIDeP4xZihvO024NHF jefferson.moura@pc-jeff' 

#Jose Welligton
#usuario jose.silva "Jose Wellington Silva" admins '$6$/VQVEalw$2/gyewZ5Fg3lbhSs84iM/Od9rvMYGvmtBG.T2n58MOg1Ip18JJl7MZ0d5AINLSh6yjq1b9LEIeGPXFCEgUoSmo0' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDSftq/FpM1tCDetD5NImD6suUK1xsZ+I0tfKOMol1OgubkVYMvmlPkdajkDY9awAG3Kjr4x3cmxoHufevwu/1mGJH/CPk1S1zHqQxjwvgd1DyIsAcsRrXCbKF0hTXHW4KLA2vU7lTGOAWgRD2/gk0a6/Sg+ag39W5SFZuX9yrYmoZewDZrEegMQM/yJNUlz6doTaA4zQa10xXJZ3mTjJGHyspxU8YsPPZIHcm5bjzi2VuTt93quvBqNtNzRlgJhcLr5sROSxZEk5g958IBNBYbtu+/YzbOGPdpqlCUd+LPPRZAx+obHPQVYAfZ/VQxGKOPovA0BBalkJ9Ipt048Von wellington@Wellington"

# Cristian
usuario cpn "Cristian P. Nascimento" admins '$6$8f6xV171$9mrYyEZdkmHe1mmlX5qD471b2JLFrpfFEZnYMgYbhel8z05.5qOb2i.D6IWvmb.QxkIJ.dUeU8LyhJgD7DQPQ0' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDZsm5Jg8GG7b9Beo+Mt4TFNPQb+kjXHij41CmCOdWC1rykG81u6ReDUkW7sGPDgysIR/EGhaGhC9NuHhRRKamDmdwOwFy5/BPSkL8DXfjqrnW61YxqgK+LatvJTolbon7dJxiVLJVVAIUX2XL3I2PhRyQuHqdQQbE0sg1ZKjuNpaA/1x5ZxuH4bkFwkht/u+quqxAFavSOesBFbkXFI60XdKepMZ77s6v0qSy7ug45hpi/uP4SuzN1DxqG27caPNWg6I8Lq0Enzj7KfFKtma86yFd75MgQI/4uWDAj3nHuVJrKuDfKHffV9tKlEGSLfz6epLTXYSLHsPIypA6FPTKz cristian@cpn"

# Thiago Sampaio
usuario sampaio "Thiago Sampaio" admins '$6$.fhbrztG$UK/OpyuJAsTIScwyOTJRPFyumDr07tyQjERUGip1WPEEv9FUNVaJlK6iyWMcpbM82L3.bX.66a5sqv7gstr/v1' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDQfmk5ZM6AotWd5WUQdz2DtCQEoF455WQ1XeySRmJwNP+l42B/Z1t5hNNH4Hrc97G9cv1/GV/fhM3+9gaki7kS0dIsxNNu8LePkQbJMjjCavTa1UpCaL0ylXahIOJ+i1H6PNK3sYgtFTfB08NKKgwCCGKU7xrGFGj34Z161b75oYQRL5j6JuhkrA/UFy/yL0o7b80ckg/crXemEYNxC5TegaZKjpe1LlU+XZyx5hVsZrj6ZMs37zYbPR7fQY1dzAFsnzuMu6VdfZK3m33FsT1fTKPaZYeaK8LHjMLGWbwtM8EvS7RhF7aZAokmBlsnZolR+Zg4aHntV2rM9YDUzNod root@sampaio-op01"

# Eduardo Augusto
#usuario eduardo.augusto "Eduardo Augusto Pinto" admins '$6$qvn0KAxw$KI63lm51hcw4uva/B14lbOCnJeWR74pMI.nOmcPBoyzidRX.eqE4sQSrbayTGlgsAIYFYmWjWZI.dJZYwsCFR/' 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDWrihvFLrItYKq40NZxKivY6GTcTTgJWGFZh0YJ4SpcHjp47QnOX4+OzIcVOAvtv07xrtHanG4FjssZt+KY1CvVtgxw7B/0m3VSaZHIxy1m5smFrV1waXKmsH2YqKa2E3GmffTwY3PnUUNcYg8dR07mb02nz/iJP0ac/B8nNX3dIwtTKBjndoxb07hP/UzJQnrk7ryt0mBUf2T/yy12rA/F9nW8R59HWytI+yHVAElQ6aUoz5T0HvzZqWqQZ+CSkiO+bK0eHH2p2Wy4G99EeerZhhLHY3eKE5A3br+3ALfh5BjcO6yiRDr3yyO9Vi+jpShJ/CGDljMq9RLzGqMSFa1 eduardo@eduardo-machine'

remove_user thiago.sampaio
remove_user sysadmin
remove_user robson.nascimento
remove_user jose.silva
remove_user eduardo.augusto
remove_user leandro.mattos

read -p "Acrescentar usuarios de TI ? [N/y]" resp
if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
        [ "$(groups grpti 2> /dev/null )" == "" ] && groupadd -r grpti
        # Eduardo Lourenco
        usuario eduardo.lourenco "Eduardo Lourenco" grpti '$6$qnBW8/Eo$dXeOvmqPkonPTg527FocaDhJmGTv5VIzhB8cbtTo3l/Trh4IuHDpfv5/NTtq/OjS1Pz1Ik3e7n0IvrR9mxODT0' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCxNSaytVm33o5pDA+OHUJtBh1R7h84QElfjYWYV78cCT3TtDL84T7kSpKke3/0CMYSNMM0VQr8DB6jDBPJDr18WwH5XvVWjAq4Yz6LWzv38xdrkexZ+FggMu+T0H/ID7OBJeL7gvwr1w3f73KkYTxZUvNswdB+0pSyH9jhWyUE/GEPKEWXpzYiq05JSNkVne3j9elaHhft+nr/qg8mc7Mg8i0tJQfvNbOCHKJeKYR6gV5uwBj7osqLwADlmesP2CaR0U3dvpkHqP5VUXbZPLxODk0Zne3WaXSvGAmC+VCNuGHGIFoJvbkReEXnJZ4qRq9f/1iEIbh2RVSjmePPR2sd eduardo.lourenco@ti02
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQChfrfkKtNJEP49fkje+D2a3HR/fWJYGzCTveibnVJy4fpdZIYPOzhJzNZHZ+oqjzA8E6+f2dvQi+iHbg+6QzGLrTSktpEJth/BUF6/8dztZhz1mpMZ4MbHJtg4I9a00MON4QQsYnR3gL8xxY50T51+ik8erriLWMy+BYA0uED4gWV0RUfJePZltbKfukcRCfYHZEkC4ZBthpzgueumDOXLonjF6csQd5qeNguvPXwqgv4MjWqyJOMCnhqSh7kDq/pW3ft+DOhfI50V8fdT+BgXSoyPyEXHxag1Smme9n7p3nrKjs+O1kf26bd8wW/F2/mwGUvUJY8+lcpk+LXJmvxF eduardo@notetiedu"
        
        # Vitor Paiva
        usuario vitor.paiva "Vitor Paiva" grpti '$6$PKL/1zSf$PoHN/VtRRl/7NDKhXuJ5lOingWGtumEEty9jGXb6s89z7J6dXnUynCXPoEVXjjSuCdhu6aMu1/Q2A2XIMu3WU.' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC3Ky6062RhQ4g9moHXwPi69MsuKfo577plgCv7lQQTqpyX7B5REisWGCQAtT39GDmRfNq5ygeE/2j7zZWFywOCv3m+lSpgPT94gaU5iESM763yMfzQmHmCKGwpanEaaFedjN1iwu5IoLlotUYKkviANQ+GUDqs8BnQqEVI/kvHHOPAinz5/Sgxk0OUoP3kQO19zWEuAzELFJ3wzmwDVM4TdYpFwi7jAzjDEmzZ6gmjE3XBnA2fphlpgw8qwL8QM/c0y2rOVK/x2lIKxxpg3HqHxrK4BIyXhr/ryUpKBkyApMMxpMTEXywf/MVemHHCGZFrS/grWXyI4iF1TtJ95dRP vitor.paiva@localhost.localdomain"
        
        # Daniel Sachet
        usuario daniel.sachet "Daniel Sachet" grpti '$6$jAOnEd6Q$0IUfsjS439Xm41B4V2i//Yk3mRW.5wYYWD.iOXDbnMhQakYoxodgVO9KFZu/nkCviupwEPCvEOVQ4mpGUznL6.' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC/6rih0WL0GlQtzEQesqouXWvagQNAJpZnv2cWE/UilCHgGZ/Nu4vKRiK3TZvwCAIpyaqdlEYEWJVztSRKFPyZwIE4XrPraz07xkNRJnd2jte0SE2QcwL6aRpnC/oj4zO6JUYvfpCogF5cA12+lvTdTdvx1glIVb6bqcvi1KtCuC0C3EpGCLozVPnGzDG+K/ZnCobqkp2zayVs5URbqWkWUpn7muhjbdRE6tuvuidkfybCFxX2nElE8OSBjWtl3O29jF0ZQx58CUGsFtKs300ILFemMy0XSpy6Wxir6SOeQGbgOtVJeGWa0cYuCNkV42i0NyVTeHKXZWpjqwr6iwzJ daniel@MacDaniel.local"
        read -p "Acrescentar usuarios de TI ao sudo ? [N/y]" resp
        if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
                grep  "%grpti" /etc/sudoers > /dev/null 2>&1
                if [ $? -ne 0 ]; then
                        echo -e "# Usuarios de TI\n%grpti    ALL=(ALL)     ALL" >> /etc/sudoers
                fi
        fi
fi

read -p "Acrescentar usuarios de VOIP ? [N/y]" resp
if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
        [ "$(groups grpti 2> /dev/null )" == "" ] && groupadd -r grpti
        # Joel Junior
        usuario joel.junior "Joel Ferreira Junior" grpvoip '$6$XbANQNoW$esiBEGUTbTSUP9JAb29lQ2McMOWafgceFRSTgR/EdRFc2IniQHWbE29HIoMiIDTyhvIfG2cL5DxUBstEfHdT61' 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC36ZdYyM1jmYC8RX7qtRccR8CSXfIEBHdRf1+i93NNRLX68CxndNklhf4fZ8fCXTpZ+ArMaBTh7WzxJ4rk7+8e+a4yXtpo72j0lA80/nZdD/baN3Clo3vfC/8Wh9gMVmj3xdOxScgYS+bps8Prq/Rf4RwAElFGG+dYvCd+1Cxbb7UpfZKkZaRQC9Nyj7Ln1Gl2M+x6alF6W5N5oKRStjgFC6bM3Qog+/H8Ux7OV3V+I1A/pZB50X7oe31tULG8fdOvb9jFcTqscnK/01tNge+sohRLhpKjVSS48lBH62A1wA9rRvANBX4KkPK4WTjeFlK9uHpLH4wcoNw0zfaJGI2x joel.junior@Hosting-Backup'
        
        read -p "Acrescentar usuarios de VOIP ao sudo ? [N/y]" resp
        if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
                grep  "%grpvoip" /etc/sudoers > /dev/null 2>&1
                if [ $? -ne 0 ]; then
                        echo -e "# Usuarios de VOIP\n%grpvoip    ALL=(ALL)     ALL" >> /etc/sudoers
                fi
        fi
fi
read -p "Acrescentar usuarios de Rede ? [N/y]" resp
if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
        [ "$(groups grpredes 2> /dev/null )" == "" ] && groupadd -r grpredes
        # Adiltone Lima
        remove_user adiltone.lima
        remove_user bruno.pereira
        remove_user charles.rocha
        remove_user luiz.casali
        remove_user leandro

        # Charles Rocha
        #usuario charles.rocha "Charles Rocha" grpredes '$6$VswWNfg3$LgALzoXGJetsLTzpTviNSMc3d00ikK3MBVj0s2PgKfXXWt20VX63wuPNCE//t/yo9aFHS1XTEXlyZJ8RQ8WU61' 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDPMIZvt2WDuOPO+1bybK2M+bBKBQSOxSMS6Avdk85oVFCrqhEO+wkD4MieyhWw4s9HNqwkDci2B85kfOJxMO417xgCtbHGC53nfCDCvRONXT2hDCqma2pHZbPimJPgJaYzxjcF3lCYqwW2OxACEFOs97bmeBFjT+9/I84lv6vR4EoJviheq4wdUGrn2uZpkTZM4Uvbiv9IyY+BMA9Ukgwp2rVKQDBMmOux348NkAqGV1rJPXDuEjqzYh259itf2VJxhPAkzB/c4n0W5Re/yhyHlPbZFMY9BUmu6IFM8ww3DT2CmMEDu9iSSaYOcjqluDQhlDUchuFOcznNviFdfS/f charles.rocha@tacacs'

        # Johnny Merlini
        usuario johnny.merlini "Johnny Merlini" grpredes '$6$LVvnmfub$cnyr3Y4byUnZPktQILBjmZtXA1snGhpDUa0mt6iIuwGFLS1MKVQkv.ZfbArK0pUyoPw/Sw0Eynyd9XVPEqnoK1' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC9RcoGOEvCtJ4BtVJDMlmkD+XvZOGqMx310O3mLZ8cQa9ROCdTSFgwovvDnb0yDYobOrHYn2luv/9MZh146HPO28u3kwWAXsMQzxkJ+Re0ke8bjcCSMMnoSfM5/tqHaYor5RUc1ft7UJnmHTxkGe3VmCDeIO8ohlSzKVrzTNi4lX6dDpKdMR3YAO9bSpecmaKzDNcz564QuhLPrRml751sf0cUXn5aWWg3jAyOPrpBCqSxeahK7bdhEfMlYPpirBJCEaHaIBkXdvbYi+E9riMVHmDl37EUp6qM0Y1JIej4LLipzVrKuVQ6aiRh31b+PdXv2cilmFtOGXZ3Hoe9tp0p"

        # Rogerio Santos
        usuario rogerio.santos "Rogerio Santos" grpredes '$6$qUyOQNxi$IOy.EzcDiy5kzFJKfy.bkwtpoTIEEa1hoAtfAkJf6n.dEjI.z0HHcIrVpVHLDvgsA1/8ABAdXmt4TCbXm1q6H.' 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDKfdpHlCMbIb1TpvLocRiQDuPHK/kDrHXtBD4WF94RKy0OWwUlwZz1z/JHt759QxfN7L5uv+GJl2lqgkS8Jg0mClyJy9nt9p7/JJj8ybeTCloZb/7db0WRE04IXP3xoP2ZiuiAJPZUNqWCMUEBa0/8nY4wBiU4d1iKrTbSX1WLGSV5bScfnLsD/5SLxODqg5ZowIWkPOQzCKczSHpPGtHCfIoeLoHgCN0dsQPOUjirx7KFaTzw9QGaUbuWcX4t9anXs+jqk6P4qdQ+/EXBebtep4J0o09MUXREnKAnDn0ssD6NOKKkpydZ28V3IcGnvCSMbbBNRZQG/RPw3c1azYF/ rogerio@rogerio-desktop'
        
        # Bruno Pereira
        #usuario bruno.pereira "Bruno Pereira" grpredes '$6$1uAqvS3u$3hFOfZU/GAjxl/7.am1j9XemfJpHxTh/dFd61wP11BTNWtRcDhPaq6fyYr6z9hjTsyZ7rA52PrvoGE/8M4KG.0' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDLMhCooHSR5qZg3AFxZlcsu03RTNh0+u2DHR8oLDb8l2SH9TJraLzStwsiz4FOEl+bD4ffTLf74QZcfvQ77a1KKCM/rZVuRGIEWZOdWq8CB69Q/nEn+USGdooiLjh+IPKRKc04GIPKgMffd4RfYdglz9WdEDSFzB+xgfGznhFbpwVmQIlNjOx/LYVMfutRTJ1ewaqwjJs2sxnpjrksf/Wir7nwdFv7BQ5cQ5tjd2RrUhWi8ZsmOlvpgjYCVEX3OPFpu/kV/fjifreabTPvk9WZPQDhFy4VR8OoJgJzTCDJYUyhh+OlaHU75HlqIo4Dv0SG0PFstGUf5Wb+yyBuj7F1 bruno@machina"

        # Luiz Casali
        #usuario luiz.casali "Luiz Casali" grpredes '6$jNmFJlAT$b4BEThkqwEYTg/skaVtDReIbUDkOjfEcOnUJxpwSUiy2f3q73YcIt5RtTHwliO7vET99lZzjG/pqU.liCroKR/' "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDTe+wyo3ottRQWci96S3m3cI6QvbugKSGCWMbNd2rOLS1GCcn8jovKc/aImqY0SSF79ow2fHmW64EmbYdA4JMVzlEsTSKtOn6aQ10E+lNnlxwccvY77CV+p5N04+OKaFnk7xDdR1uwQNzIedxxxoSEaQ6CHpslStkQiP4mv69U/XPnzEduDY7pgQJK0dgSjEx+zM4YQX78UNMkSptOlPOi5p1Hj5TVGrNLn+oNCbiperfH0rqKJDbpY+zUklU53DhhB7s0sMpGa6TtCNmefriLVoTx3gu6hHJM53aoCTpUibs+0BfwoBdOzyO2vWX0BAfW30cioDhVRTy3BXKmnJ2j /home/luiz.casali/.ssh/id_rsa
        #               ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQDTe+wyo3ottRQWci96S3m3cI6QvbugKSGCWMbNd2rOLS1GCcn8jovKc/aImqY0SSF79ow2fHmW64EmbYdA4JMVzlEsTSKtOn6aQ10E+lNnlxwccvY77CV+p5N04+OKaFnk7xDdR1uwQNzIedxxxoSEaQ6CHpslStkQiP4mv69U/XPnzEduDY7pgQJK0dgSjEx+zM4YQX78UNMkSptOlPOi5p1Hj5TVGrNLn+oNCbiperfH0rqKJDbpY+zUklU53DhhB7s0sMpGa6TtCNmefriLVoTx3gu6hHJM53aoCTpUibs+0BfwoBdOzyO2vWX0BAfW30cioDhVRTy3BXKmnJ2j Luiz-Casali@172-15-15-50.lightspeed.sntcca.sbcglobal.net"

        # Robert Alexandrino
        usuario robert.alexandrino "Robert Alexandrino" grpredes '$6$nQPagcvH$1F5pNCMT3Q6UBvcpPDwTOthT5A6zhvedMEX3rOTSAqnapgzfp6zazwH9/28GGlMhgQLLsirl.imnvLvVZQyaY/' 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCfSnnvuv6FRd4J2HAsap58gQ/zTD8jnkjDwr0wyqOmCJAm5lUadwYBgpwK+mLKvwgiP7faml8gfyJewcQ2Jf3+V9pEcrr5+kLY11hw6ua59V4+USuRvqjIYDEUgEkVPt2yp+Ylcqih1wjM4GHvQmLDUnzlAT6ZsR/p9A3bO8ReI+bvctNgRSms5nF0NpKel9Q5NCRcstruItsN7oBWuESNHhhYurakD58FeM0qu8tnbEPQ4W3vZV60rCjsjT9In2A6jz+u/X1TGazZgeCsTz0NZGhaeRHpdErXlGDUZuveTfHFSAaQC5yTfJIEcPiKxe5YxnE4HfbHbCI/+fXQtVfL robert.alexandrino@robert'

        # marcos Pereira Silva
        usuario marcos.silva 'Marcos Pereira Silva' grpredes '$6$Nu7qby91$qAJ9LKZLenXeL7d9.NQVV48NyrcPAGaMJTIPBTf/YgFzHn8R3Q8PoozRDtQH9UOBh5x98/YDqE./u2yphGg/2.' 'ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQCvtNbX1WeeWmnnSDbz5ncFrCyebnev2icDCwAwu/eEWeZTZiWPcCZUc46xVN/TBdSeldUgmV7ffqu6a+xHUQ9WY+vq+uHSlfcwoDf18uZ7ij72aoyEaDQvr/OxaFcnSwEkT0BxV3ZbrU1Hhm7dbPzXooOpNC3tIZfGqBXMd4x1wtB1jsHe2aWomh405soGkbgkX1QXgf3F6H4Z/DNY+afDEhg9sc0QXpfqa57H72qbgzOh7vbtqP6Q+X0YOsLtFD5CKPIxD92o04xxoV7188tZP8aC1YykGy8fXBzjub1/J5T2RT1FcUVN0jRI6Y/EUing8mUzZX0Xrb/feZoQSidR marcos.silva@PC-Marcos'

        read -p "Acrescentar usuarios de Rede RJO ? [N/y]" resp_rjo
        if [ "${resp_rjo}" == "Y" ] || [ "${resp_rjo}" == "y" ]; then
                # Joao Furtado
                usuario joao.furtado "Joao Furtado" grpredes '$6$eoDgMb4T$Nob263ZpgZOGiSCRkNQBY28klD6huBU2Aqt6LkJQMVuYRJZ8YUFba78XvdqDgwI4GSWmjkgvhE4mh0TYj8CEj1' 'ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAQEAsxuP+qyVMVplKifVo+IXinR8mDxS6lHnwdn86lEQtwFxBh0Tul/Yg36aJNDLCdFejqpQW4k23snsOJZSYStYFMI2zs07hrkW9IuadqbxpvGNz32Xskx6se92pWW9wYbMT86rYDQqf71KGjRHpUx3QzIp8xeXB/NgCHpd96oqNUI1EkvXwx9DkaGIoH3ZtNYmfwvMugi6O9McwmWi3UBoWlyfYPnHaa597YCjcmuJ0aHjdLVw9T5ev0MHQBgR5DWRJKplHo3VIuSQw2ewI09IwYy11iqJ/2YJxVARzHzHxe8m7abCkJq/Mqtz4LmVCOCQ7rgkHomiV8du7r0dWYq8hQ== joao.furtado@machina'
                # Francisco Sousa
                usuario francisco.sousa "Francisco Sousa" grpredes '$6$YH76M6jb$7EgSncj9YfC34VRrYnwaobixf1i2lQKcExkDr.M7mnvqS3560.PlFBa0.svaCg4o.rPTFI6CnQbsWVU5YwtOm/' 'ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAQEAljIwxGQdwz2SvsW3Wi9G4v/Ay9P6f6nTrAgm/4j6/uXq8wAqZ996WhUN/WuR5zr7I5f2YsxQittgj7d5Ep5R4mOqdzwP4udB/L95PVfqwtOPpK2yoxm1YZwBU2agDd8E+C9O7wyRw+BRdSN9BP6lUKf99B7rTjVmyNSbe3iP/PIz21CAIPwlLxe+0SmxnZF81r2fMIfYdXuiXWLEOZKUQuiENPNPBGW3FAKT5oe3YYLeVaJVI1hnuuxzgPtTmWlS/0+ChTfl903ivNmpfHnYX6IivlJU3yQ/yBKS7mrIUg9iy7ZRjf5DdOphS4phCfTKrmEftH1aHSlW20PJmt2akw== francisco.sousa@machina.com.br'
        fi
        if [ -f /usr/bin/vtysh ]; then
                read -p "Acrescentar usuarios de Rede a interface vtysh ? [N/y]" resp
                if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
                        for grpid in $(groupmems -l -g grpredes); do
                                usermod -s /usr/bin/vtysh ${grpid}
                                usermod -a -G quaggavt ${grpid} 
                        done
                fi
        fi

        read -p "Acrescentar usuarios de Rede ao sudo ? [N/y]" resp
        if [ "${resp}" == "Y" ] || [ "${resp}" == "y" ]; then
                grep  "%grpredes" /etc/sudoers > /dev/null 2>&1
                if [ $? -ne 0 ]; then
                        echo -e "# Usuarios Rede\n%grpredes    ALL=(ALL)     ALL" >> /etc/sudoers
                fi
        fi
fi
echo "Reiniciar servicos de ssh e ntp, certificar que porta 22 e 222 estao ativa"


machina arquivos
ftpFTP#450#_S


!@n3wf0rth3l0stT3l1um2013


VBoxManage internalcommands createrawvmdk -filename ~/.VirtualBox/HardDisks/usb.vmdk -rawdisk /dev/sdd -register
sudo chmod 666 /dev/sdb*
sudo chown `whoami` ~/.VirtualBox/HardDisks/usb1.vdmk

ls -l is only run when a file is found, try:

find . -mmin -60 -type f -exec ls -l {} +


paginas infectadas php
find ./ -type f -iname *.php -print -exec head -n 1 {} \;

tail -f /home/*/logs/*/


find . -mmin 60 -print0 | xargs -0r ls -l.
 Newline is usually pretty safe except against active attackers find . -mmin 60 | xargs '-rd\n' ls -l The -e


History com hora 
history time
export HISTTIMEFORMAT="%h/%d - %H:%M:%S "




tail -n 1 -f /var/log/virtualmin//*_log |egrep -v "403|png|jpg|css|Geko"


tail -n 1 -f *_log |egrep -v "403|png|jpg|css|Geko"



tail -n 1 -f /var/log/virtualmin//*_log |egrep -v "403|png|jpg|css|Geko"




tail -n 1 -f /var/log/virtualmin//*_log |grep If






cd /etc/apache2/sites-enabled/

for cliente in $(grep Root * | uniq | awk '{print $2}'); do echo $cliente  ;done | wc -l


for cliente in $(grep Root * | uniq | awk '{print $2}'); do  if [ ! -f ${cliente}/.htaccess ]; then  ln -s /etc/apache2/htaccess_ref_bloqueio ${cliente}/.htaccess ; fi; done 



for cliente in $(grep Root * | uniq | awk '{print $2}'); do  if [ ! -f ${cliente}/.htaccess ];
 then  ln -s /etc/apache2/htaccess_ref_bloqueio ${cliente}/.htaccess ; fi; done 



 altera hora
 date -s "15:10" 




rdesktop 10.16.140.20 -u jefferson.moura  -
...

[Message clipped]  View entire message
Displaying 71532192415219.png.









tacacs server
AtomicRocket

Jeff Muniz <jmuniz1985@gmail.com>
Sat, Dec 26, 2020, 2:07 AM
to me

root@tacacs:~# cat /etc/tacacs+/tac_plus.conf |more
# Atualmente existem apenas dois grupos: admin e operacoes.
# Isso infelizmente nao esta separado de forma correta, uma vez que muitos comandos todo o noc consegue executar.
# Tendo em vista o gerenciamento e a seguranca, vou separar o noc em 3 grupos (nocjr, nocpl e nocsr) desta forma conseguiremos dar certos priv
ilegios apenas a quem
# sabe oque esta fazendo.
# -------------------DICAS----------------
# Nas configuracoes, para permitir um comando basta especifica-lo no cmd e espeficar o sub_parametro dentro de conchetes.
# sintaxe cmd = <comando> { permit | deny <sub_parametro> }
# Ex: cmd = show { permit interfaces }
# O comando acima permit o show interfaces
#
# Para liberar um subparametro de um subparametro bloqueado basta usar-lo em aspas duplas e colocar um '.*' no final, como no exemplo:
#/
# cmd = show { permit "running-config int.*" }
#
# no comando acima liberamos APENAS o sh run inte. O comando sh run esta bloqueado
#
#================== ALTERANDO/CRIANDO USUARIOS (SENHAS) ==============================
#
# Para criar uma senha, digite
#
# htpasswd -n -d <usuario>
#
# Lhe sera solicitando uma senha, e uma confirmacao da mesma senha, e logo apos sera gerando um HASH da senha.
# Copie o HASH e cole no neste arquivo
#
# htpasswd -n jbolivia
# New password:
# Re-type new password:
# jbolivia:LT2gZU8PwfiVk
#
# user = jbolivia {
# name = "Jalala Bolivia"
#   login = des LT2gZU8PwfiVk
#   member = windows
# }
#

key = "bR()X3ll@$$"

accounting file = /var/log/tac-plus.acct

#group = admin {
# default service = permit
# name = "Administradores de rede com acesso lv15"
# service = exec { priv-lvl = 15 }
# member = ifxstaff
#}

group = redes {
default service = permit
name = "Administradores de rede com acesso lv15"
service = exec { priv-lvl = 15 }
member = ifxstaff
# cmd = switchport {
# permit "trunk allowed vlan add.*"
# permit "trunk allowed vlan.*"
# permit "trunk allowed vlan remove.*"
# permit "trunk encapsulation.*"
# permit "trunk native vlan.*"
# permit "access.*"
# permit "mode.*"
# permit "nonegotiate.*"
# permit "block.*"
# permit "port-security.*"
# deny .*
# }
}

root@tacacs:~# cat /etc/tacacs+/tac_plus.conf |more
# Atualmente existem apenas dois grupos: admin e operacoes.
# Isso infelizmente nao esta separado de forma correta, uma vez que muitos comandos todo o noc consegue executar.
# Tendo em vista o gerenciamento e a seguranca, vou separar o noc em 3 grupos (nocjr, nocpl e nocsr) desta forma conseguiremos dar certos priv
ilegios apenas a quem
# sabe oque esta fazendo.
# -------------------DICAS----------------
# Nas configuracoes, para permitir um comando basta especifica-lo no cmd e espeficar o sub_parametro dentro de conchetes.
# sintaxe cmd = <comando> { permit | deny <sub_parametro> }
# Ex: cmd = show { permit interfaces }
# O comando acima permit o show interfaces
#
# Para liberar um subparametro de um subparametro bloqueado basta usar-lo em aspas duplas e colocar um '.*' no final, como no exemplo:
#/
# cmd = show { permit "running-config int.*" }
#
# no comando acima liberamos APENAS o sh run inte. O comando sh run esta bloqueado
#
#================== ALTERANDO/CRIANDO USUARIOS (SENHAS) ==============================
#
# Para criar uma senha, digite
#
# htpasswd -n -d <usuario>
#
# Lhe sera solicitando uma senha, e uma confirmacao da mesma senha, e logo apos sera gerando um HASH da senha.
# Copie o HASH e cole no neste arquivo
#
# htpasswd -n jbolivia
# New password:
# Re-type new password:
# jbolivia:LT2gZU8PwfiVk
#
# user = jbolivia {
# name = "Jalala Bolivia"
#   login = des LT2gZU8PwfiVk
#   member = windows
# }
#

key = "bR()X3ll@$$"

accounting file = /var/log/tac-plus.acct

#group = admin {
# default service = permit
# name = "Administradores de rede com acesso lv15"
# service = exec { priv-lvl = 15 }
# member = ifxstaff
#}

group = redes {
default service = permit
name = "Administradores de rede com acesso lv15"
service = exec { priv-lvl = 15 }
member = ifxstaff
# cmd = switchport {
# permit "trunk allowed vlan add.*"
# permit "trunk allowed vlan.*"
# permit "trunk allowed vlan remove.*"
# permit "trunk encapsulation.*"
# permit "trunk native vlan.*"
# permit "access.*"
# permit "mode.*"
# permit "nonegotiate.*"
# permit "block.*"
# permit "port-security.*"
# deny .*
# }
}

group = ifxstaff {
default service = deny
# Este grupo nao esta associado a nenhum outro.
name = "Funcionarios da IFX Networks Brasil"
}

group = backuper {
default service = deny
service=connection {}
name = "BACKUP CONFIGURACOES"
cmd = copy { permit .* }
cmd = write { permit .* }
cmd = terminal { permit "length 0" }
cmd = show { permit .* }
cmd = dir { permit .* }
cmd = more { permit .* }
service = exec { priv-lvl = 15 }
}

#=========== Padrao Para  NOC Estagiario (NOC Bebê/Padawan) ==============
#Group para usuarios do suporte auxiliarem o NOC. Sugestão de Paulo Haga - NOC, configuração realizada por Johnny Merlini - Redes.
group = nocB {
default service = deny
service=connection {}
name = "NOC Bebê"
# message deny="Voce nao tem permissao para utilizar este comando, consulte a equipe de redes"
service = exec { priv-lvl = 15 }
#----CMDs_GERAIS----
cmd = traceroute { permit .* }
cmd = exit { permit .* }
cmd = ping { permit .* }
cmd = ps { permit .* }
cmd = * { deny .* }
#----CMDs_SHOW----
cmd = show {
permit access-lists
permit clock
permit errdisable
permit "interfaces."
permit interfaces
permit "ip access-lists.*"
permit "ip accounting output-packets"
permit "ip arp"
permit "ip bgp summary"
permit "ip interface.*"
permit "ip route.*"
permit "running-config interface.*"    
permit "ip vrf.*"
permit "ipv6 access-list"
permit "ipv6 interface"
permit "ipv6 route"
permit "ipv6 tunnel"
permit "ip nat.*"
permit logging
permit "mac address-table.*"
permit port-security
permit port
permit storm-control
permit system
permit uptime
permit logging
permit "spanning-tree.*"
permit "vlan.*"
permit storm-control
}
}
#=========== Padrão Para NOC (Monitoria) ==============

group = nocmon {
default service = deny
service=connection {}
name = "NOC Monitoria"
service = exec { priv-lvl = 15 }
#----CMDs_GERAIS----
cmd = clear { permit counters permit arp-cache permit mac-address-table permit "ip accounting" permit "ip nbar" }
cmd = telnet { permit .* }
cmd = traceroute { permit .* }
cmd = exit { permit .* }
cmd = ping { permit .* }
cmd = quit { permit .* }
cmd = logout { permit .* }
cmd = ps { permit .* }
#----CMDs_CSIM (VOIP)----
cmd = csim {
permit "start.*"
}
#----CMDs_SHOW----
cmd = show {
permit "ntp.*"
permit access-lists
permit "bgp summary"
permit cam
permit "clns is-neighbors"
permit "clns neighbors"
permit "controllers E1"
permit clock
permit environment
permit env
permit errdisable
permit interf
permit "ip access-lists.*"
permit "ip accounting output-packets"
permit "ip arp"
permit "ip bgp summary"
permit "ip interface.*"
permit "ip nbar protocol-discovery.*"
permit "ip route.*"
permit "running-config interface.*"    
permit "ip vrf.*"
permit "ipv6 access-list"
permit "ipv6 interface"
permit "ipv6 route"
permit "ipv6 tunnel"
permit "isis database"
permit "isis hostname"
permit "isis topology"
permit "controllers vip all proc cpu"
permit version
permit logging
permit "mac address-table"
permit mac-address-table
permit port-security
permit port
permit storm-control
permit system
permit standby
permit uptime
permit logging
permit "voice call summary.*"
permit "dial-peer voice summary.*"
permit spanning-tree
permit vlan
permit processes
permit running-config
permit storm-control
}
#----CMDs_GLOBAIS
cmd = configure {
permit terminal
}
cmd = interface {
permit FastEthernet
permit GigabitEthernet
permit Serial
permit Tunnel
}
cmd = voice-port { permit .* }
cmd = no {
permit shutdown
permit "accounting.*"
permit "nbar.*"
}
cmd = description { permit .* }
cmd = shutdown { permit .* }
cmd = duplex { permit .* }
cmd = speed { permit .* }
}



#=========== Padrao Para NOC (JR) ==============
#Group para usuarios Junior do NOC
group = nocjr {
default service = deny
service=connection {}
name = "NOC JR"
# message deny="Voce nao tem permissao para utilizar este comando, consulte a equipe de redes"
service = exec { priv-lvl = 15 }
#----CMDs_GERAIS----
cmd = clear { permit "counters Ethernet.*"
permit "counters FastEthernet.*"
permit "counters GigabitEthernet.*"
permit "counters Serial.*"
permit arp-cache
permit mac-address-table
permit port-security.*
permit "ip accounting"
permit "ip nbar"
permit "mac address-table dynamic interface.*"
permit "mac address-table dynamic vlan.*"
}
cmd = telnet { permit .* }
cmd = traceroute { permit .* }
cmd = exit { permit .* }
cmd = ping { permit .* }
cmd = quit { permit .* }
cmd = logout { permit .* }
cmd = ps { permit .* }
cmd = duplex { permit .* }
cmd = speed { permit .* }
cmd = copy { permit .* }
cmd = write { permit .* }
#----CMDs_CSIM (VOIP)----
cmd = csim {
permit "start.*"
}
#----CMDs_SHOW----
cmd = show {
permit "ntp.*"
permit access-lists
permit "bgp summary"
permit cam
permit "cpu.*"
permit "clns is-neighbors"
permit "clns neighbors"
permit "controllers E1"
permit clock
--More--
#----CMDs_SHOW----
cmd = show {
permit "ntp.*"
permit access-lists
permit "bgp summary"
permit cam
permit "cpu.*"
permit "clns is-neighbors"
permit "clns neighbors"
permit "controllers E1"
permit clock
permit environment
permit env
permit errdisable
permit "hardware-status.*"
permit "interfaces."
permit interfaces
permit "interfaces description"
permit "ip access-lists.*"
permit "ip accounting output-packets"
permit "ip arp"
permit "ip bgp summary"
permit "ip interface.*"
permit "ip nbar protocol-discovery.*"
permit "ip route.*"
permit "running-config interface.*"
permit "ip sla.*"
permit "ip vrf.*"
permit "ipv6 access-list"
permit "ipv6 interface"
permit "ipv6 route"
permit "ipv6 tunnel"
permit "isis database"
permit "isis hostname"
permit "isis topology"
permit "controllers vip all proc cpu"
permit "ip nat.*"
permit version
permit logging
permit "log.*"
permit mac-address-table
permit "mac address-table"
permit "mpls.*"
permit port-security
permit port
permit storm-control
permit system
permit standby
permit uptime
permit running-config
permit logging
permit "voice call summary.*"
permit "dial-peer voice summary.*"
permit spanning-tree
permit vlan
permit processes
permit storm-control
permit "ip flow.*"
permit "ip cef.*"
permit "mpls l2vpn.*"
}
#----CMDs_GLOBAIS
cmd = configure { permit .* }
cmd = interface {
permit FastEthernet
permit GigabitEthernet
permit Serial
permit Tunnel
}
cmd = no {
permit shutdown
permit description
#message deny "Voce nao tem permissao para utilizar este comando, consulte a equipe de redes."
}
cmd = description { permit .* }
cmd = shutdown { permit .* }
cmd = set {
deny "port disable 3/29"
deny "port disable 3/38"
deny "port disable 4/1"
deny "port disable 4/2"
deny "port disable 5/1"
deny "port disable 5/2"
deny "port disable 5/3"
deny "port disable 5/4"
deny "port disable 5/5"
deny "port disable 5/6"
deny "port disable 5/7"
deny "port disable 5/15"
deny "port disable 5/48"
deny "port disable 6/46"
deny "port disable 6/48"
permit "port disable.*"
permit "port enable.*"
}
}

#=========== Padrão Para NOC (PL) ==============

group = nocpl {
default service = deny
service=connection {}
name = "NOC Pleno"
service = exec { priv-lvl = 15 }
#----CMDs_GERAIS----
cmd = clear {
permit "mac address-table dynamic interface *"
permit counters
permit arp-cache
permit mac-address-table
permit "ip accounting"
permit "ip nbar"
}
cmd = telnet { permit .* }
cmd = traceroute { permit .* }
cmd = exit { permit .* }
cmd = ping { permit .* }
cmd = quit { permit .* }
cmd = logout { permit .* }
cmd = ps { permit .* }
cmd = copy { permit .* }
cmd = write { permit .* }
#----CMDs_CSIM (VOIP)----
cmd = csim {
permit "start.*"
}

#----CMDs_SHOW----
cmd = show {
permit access-lists
permit "bgp summary"
permit cam
permit "clns is-neighbors"
permit "clns neighbors"
permit "controllers E1"
permit clock
permit environment
permit env
permit errdisable
permit interfaces
permit "ip access-lists.*"
permit "ip accounting output-packets"
permit "ip arp"
permit "ip bgp summary"
permit "ip interface.*"
permit "ip nbar protocol-discovery.*"
permit "ip route.*"
permit "running-config interface.*"    
permit "ip vrf.*"
permit "ipv6 access-list"
permit "ipv6 interface"
permit "ipv6 route"
permit "ipv6 tunnel"
permit "ip nat.*"
permit "isis database"
permit "isis hostname"
permit "isis topology"
permit "controllers vip all proc cpu"
permit version
permit logging
permit "mac address-table"
permit mac-address-table
permit port-security
permit port
permit storm-control
permit system
permit standby
permit uptime
permit logging
permit "voice call summary.*"
permit "dial-peer voice summary.*"
permit spanning-tree
permit vlan
permit processes
permit running-config
permit storm-control
permit "ip flow.*"
permit "ip cef.*"
}
#----CMDs_GLOBAIS
cmd = configure {
permit terminal
}
cmd = interface {
permit FastEthernet
permit GigabitEthernet
permit Serial
permit Tunnel
}
cmd = voice-port { permit .* }
cmd = no {
permit shutdown
permit "accounting.*"
permit "nbar.*"
}
cmd = description { permit .* }
cmd = shutdown { permit .* }
cmd = set {
deny "port disable 3/29"
deny "port disable 3/38"
deny "port disable 4/1"
deny "port disable 4/2"
deny "port disable 5/1"
deny "port disable 5/2"
deny "port disable 5/3"
deny "port disable 5/4"
deny "port disable 5/5"
deny "port disable 5/6"
deny "port disable 5/7"
deny "port disable 5/15"
deny "port disable 5/48"
deny "port disable 6/46"
deny "port disable 6/48"
permit "port disable.*"
permit "port enable.*"
}
cmd = banner { permit motd }
cmd = line { permit vty }
cmd = password { permit .* }
cmd = transport { permit output }
cmd = encapsulation {
permit "frame-relay.*"
}
cmd = frame-relay {
permit "lmi-type.*"
permit "interface-dlci.*"
}
#cmd = ip {
# permit "accounting output-packets"
# permit "nbar protocol-discovery"
#}
cmd = duplex { permit .* }
cmd = speed { permit .* }
}

#=========== Padrão Para NOC (SR) ==============

group = nocsr {
default service = deny
service=connection {}
name = "NOC Senior"
service = exec { priv-lvl = 15 }
#----CMDs_GERAIS----
cmd = clear {
permit "mac address-table dynamic interface *"
permit counters
permit arp-cache
permit mac-address-table
permit "ip accounting"
permit "ip nbar"
}
cmd = telnet { permit .* }
cmd = traceroute { permit .* }
cmd = exit { permit .* }
cmd = ping { permit .* }
cmd = quit { permit .* }
cmd = logout { permit .* }
cmd = ps { permit .* }
cmd = copy { permit .* }
cmd = write { permit .* }
#----CMDs_CSIM (VOIP)----
cmd = csim {
permit "start.*"
}
#----CMDs_SHOW----
cmd = show {
permit .*
# permit access-lists
# permit "bgp summary"
# permit cam
# permit "clns is-neighbors"
# permit "clns neighbors"
# permit "controllers E1"
# permit clock
# permit environment
# permit errdisable
# permit interfaces
# permit "ip access-lists.*"
# permit "ip accounting output-packets"
# permit "ip arp"
# permit "ip bgp summary"
# permit "ip interface.*"
# permit "ip nbar protocol-discovery.*"
# permit "ip route.*"
# permit "running-config interface.*"    
# permit "ip vrf.*"
# permit "ipv6 access-list"
# permit "ipv6 interface"
# permit "ipv6 route"
# permit "ipv6 tunnel"
# permit "ip nat.*"
# permit "isis database"
# permit "isis hostname"
# permit "isis topology"
# permit version
# permit logging
# permit "mac address-table"
# permit "controllers vip all proc cpu"
# permit mac-address-table
# permit port-security
# permit port
# permit storm-control
# permit system
# permit standby
# permit uptime
# permit logging
# permit "voice call.*"
# permit "dial-peer voice summary.*"
# permit spanning-tree
# permit vlan
# permit nat
# permit processes
# permit running-config
# permit storm-control
# permit "ip flow.*"
# permit "ip cef.*"
}
#----CMDs_GLOBAIS
cmd = configure {
permit terminal
}
cmd = interface {
permit FastEthernet
permit GigabitEthernet
permit Serial
permit Tunnel
}
cmd = voice-port { permit .* }
cmd = no {
permit shutdown
permit "accounting.*"
permit "nbar.*"
permit "route.*"
permit "route vrf.*"
permit "ip.*"
}
cmd = description { permit .* }
cmd = shutdown { permit .* }
cmd = set {
deny "port disable 3/29"
deny "port disable 3/38"
deny "port disable 4/1"
deny "port disable 4/2"
deny "port disable 5/1"
deny "port disable 5/2"
deny "port disable 5/3"
deny "port disable 5/4"
deny "port disable 5/5"
deny "port disable 5/6"
deny "port disable 5/7"
deny "port disable 5/15"
deny "port disable 5/48"
deny "port disable 6/46"
deny "port disable 6/48"
permit "port disable.*"
permit "port enable.*"
permit "port speed.*"
permit "port duplex.*"
permit "port name.*"
}
cmd = banner { permit motd }
cmd = line { permit vty }
cmd = password { permit .* }
cmd = transport { permit output }
cmd = encapsulation {
permit "frame-relay.*"
}
cmd = frame-relay {
permit "lmi-type.*"
permit "interface-dlci.*"
}
cmd = ip {
permit "accounting output-packets"
permit "nbar protocol-discovery"
permit "route vrf.*"
permit "route .*"
permit access-list
permit address
}
cmd = access-list { permit .* }
cmd = speed { permit .* }
cmd = duplex { permit .* }
cmd = rate-limit { permit .* }
cmd = switchport { permit .* }
cmd = vlan { permit .* }
}

#================ IFX =========================
group = ifxcorp {
default service = deny
service=connection {}
name = "ifxcorp"
cmd = logout { permit .* }
cmd = ping { permit .* }
cmd = quit { permit .* }
cmd = show {
permit interfaces
permit controllers
permit ip
permit running-config
permit startup-config
permit users
permit frame-relay
permit logging
permit version
permit vlan
}
}
group = web {
default service = deny
service=connection {}
name = "Usuários com permissao de acesso Web"
member = ifxstaff
}

group = ratelimit {
default service = deny
service=connection {}
name = "Usuário para robo que muda rate-limit"
cmd = configure { permit terminal }
cmd = interface { permit ratelimit permit FastEthernet permit GigabitEthernet }
cmd = rate-limit { permit .* }
cmd = no { permit rate-limit }
cmd = description { permit .* }
cmd = write { permit memory }
cmd = bandwidth { permit .* }
service = exec { priv-lvl = 15 }
}


#=========== USUARIOS REDES ===============

user = cpn {
name = "Cristian Nascimento - Operacoes"
login = des 4A0c9SXpwmi5U
member = redes
}

user = thiago.sampaio {
name = "Thiago Sampaio - Operacoes"
login = des HeKOPpAOyVe2o
member =  redes
}

user = domingues {
name = "Domingues"
login = des smI30VgjemyDo
member =  redes
}
user = johnny.merlini {
name = "Johnny Merlini - Operacoes SPO"
login = des b.13bqMJLAybg
member =  redes
}

user = robert.alexandrino {
name = "Robert Alexandrino - Redes SPO"
login = des EkySYiH/U5Vhw
member =  redes
}

user = francisco.sousa {
name = "Francisco Sousa - Redes RJO"
login = des fr08z4ILeLmWs
member =  redes
}

user = joao.furtado {
name = "Joao Guilherme Furtado - Redes RJO"
login = des Ha4OcYeAAthIg
member =  redes
}

user = leandro.mattos {
name = "Leandro Mattos - Redes SPO"
login = des 6duoKlw8U3dDU
member =  redes
}

##===== USUARIOS NOC Bebê/Padawan/Estagiario ======
user = padawan01 {
name = "Usuario 01 - NOC/Suporte"
login = des 5UGtRJWUGqGJ2
member =  nocB
}

user = padawan02 {
name = "Usuario 02 - NOC/Suporte"
login = des ngtrFyS3X3Gd6
member =  nocB
}

user = padawan03 {
name = "Usuario 03 - NOC/Suporte"
login = des 4d7hxXuljCrBI
member =  nocB
}

##===== USUARIOS NOCJR ======

user = lucilia.rosa {
name = "Lucilia Rosa - Noc SPO"
login = des 80jIEd/vQdk/o
member =  nocjr
}

user = flaviano.martins {
name = "Flaviano Martins - Noc SPO"
login = des eSUFhH2yWHBAE
member =  nocjr
}

user = leonardo.lemos {
name = "Leonardo Lemos - Noc SPO"
login = des vXnZNIv4dH.d2
member =  nocjr
}


user = mauricio.salema {
name = "Mauricio Salema - Noc SPO"
login = des KmFDu67r4uEig
member =  nocjr
}

user = rogerio.santos {
name = "Rogerio Bisbo dos Santos - Noc SPO"
login = des huKsaqgS8bBCo
member =  nocjr
}

user = leticia.rosa {
name = "Leticia Rosa - NOC"
login = des MO8LyFCxMJ6ck
member =  nocjr
}

user = alexandre.santos {
name = "Alexandre Ferreira dos Santos - NOC"
login = des yv/7ND8atqhjM
member =  nocjr
}

user = luiz.araujo {
name = "Luiz Araujo - NOC"
login = des NuFipTqz9w94k
member = nocjr
}

#user = rafael.oliveira {
# name = "Rafael Oliveira - NOC"
# login = des vCBgPRoUcS2QE
# member =  redes
#}

user = osvaldo.junior {
name = "Osvaldo Junior - VOIP"
login = des XbgMno6lmrGyM
member =  nocjr
}

user = allan.lopes {
name = "Allan Lopes - NOC"
login = des 2a0SyFY1kU0XY
member =  nocjr
}

#user = felippe.silva {
#name = "Felippe Silva - NOC"
#login = des BjcfNT7s0N5WE
#member =  nocjr
#}

user = jonatas.tenorio {
name = "Jonatas Tenorio - NOC"
login = des GSzur6qsWEfzg
member =  nocjr
}

user = milena.jesus {
name = "Milena Jesus - NOC"
login = des 2rJAC65HEiGA2
member =  nocjr
}

user = douglas.guessi {
name = "Douglas Guessi - NOC"
login = des hv59mT1JWjvMY
member =  nocjr
}

user = felipe.sousa {
name = "Felipe Sousa - NOC"
login = des WWZBHUu236E7w
member =  nocjr
}

user = leonardo.teixeira  {
name = "Leonardo Teixeira - NOC"
login = des pxSmwvKI1eie2
member =  nocjr

}

user = thiago.pereira  {
name = "Thiago Batista - NOC"
login = des FC.VdYyH.iMfA
member =  nocjr
}

user = marcos.silva  {
name = "Marcos Silva - NOC"
login = des SkW4h2HLUGJbA
member =  redes
}

user = vitor.pereira  {
name = "Vitor Pereira - NOC"
login = des qaM2WurwOGFKs
member =  nocjr
}

user = lucas.pires  {
name = "Lucas Pires - NOC"
login = des oRFpcCwBH62o6
member =  nocjr
}

user = philippe.araujo {
name = "Philippe Araujo - NOC"
login = des KlRAbdGAoBbQ2
member =  nocjr
}
--More--



##===== USUARIOS NOCPL ======


##===== USUARIOS NOCSR ======


##===== USUARIOS Wireless ======

user = moaci {
name = "Moaci Albuquerque - Wireless"
login = des lP9yy5W.4k71w
member = nocsr
}

##===== USUARIOS LINUX ======
#user = jose.silva {
# name = "Jose Silva - Linux"
# login = des 4YfZ1/gnkIqNQ
# member =  nocjr
#}

user = machina.moreno {
name = "machina Moreno - Linux"
login = des iXrd2fFm5CJ3o
member = nocsr
}

##==== USUARIOS POA (Porto Alegre) ====

user = rafael.witczak {
name = "Rafael Witczak - POA"
login = des bg4nuwZh6VSM6
member =  nocsr
}

user = sergio.moraes {
name = "Sergio Moraes - POA"
login = des GpQm404xwwVPM
member =  nocsr
}


##===== USUARIOS Windows ======

##===== USUARIOS VoIP ======

##===== USUARIOS Diversos ======

user = backup {
name = "Usuario de backup da beetle"
login = des Vxhz.ac/RvHyw
member =  redes
}

user = robo {
name = "Robo"
login = des DT8r5y.Yn6Vd2
member =  ratelimit
}

user = lookingglass {
name = "LG - machina"
login = des XsE6Fd4agnCh6
member =  redes
}

##===== USUARIOS Teste ======


root@tacacs:~#  

Displaying 71532192415219.png.














26 of 122
iptables ftp
AtomicRocket

Jeff Muniz <jmuniz1985@gmail.com>
Sat, Dec 26, 2020, 2:07 AM
to me

root@Paineiras-srv-01:~# cat /etc/iptables/rules.v4
# Firewall configuration written by system-config-firewall
# Manual customization of this file is not recommended.
*filter
:INPUT DROP [0:0]
:FORWARD ACCEPT [0:0]
:OUTPUT DROP [0:0]
:MYSQL - [0:0]
:SSH - [0:0]
:FTP - [0:0]
:FTP_OUT - [0:0]
:SITE - [0:0]
:INTERNO - [0:0]
:INTERNO_OUT - [0:0]
:SMTP - [0:0]
:CONFIAVEIS - [0:0]
:WEB_ALLOW - [0;0]
:Wordpress - [0:0]
:WWW_Interno - [0:0]
:SALT - [0:0]
:MONITORIA - [0:0]
:MONITORIA_OUT - [0:0]
-A INPUT -m state --state ESTABLISHED,RELATED -j ACCEPT
-A INPUT -p icmp -j ACCEPT
-A INPUT -i lo -j ACCEPT
-A INPUT -i eth1 -j ACCEPT
-A INPUT -m state --state NEW -j SITE
-A SITE -m tcp -p tcp --dport 80 -j ACCEPT
-A SITE -m tcp -p tcp --dport 443 -j ACCEPT
-A SITE -m tcp -p tcp --dport 445 -j INTERNO
-A INPUT -m state --state NEW -m tcp -p tcp --dport 22 -j SSH
# Allow FTP connections @ port 21
-A INPUT -p tcp --dport 21 -m state --state NEW,ESTABLISHED,RELATED -j FTP
# Allow Active FTP Connections
-A INPUT -p tcp --sport 20 -m state --state NEW,RELATED -j FTP
#Allow Passive FTP Connections
-A INPUT -p tcp --sport 1024: --dport 1024: -m state --state NEW,RELATED -j FTP
-A INPUT -m tcp -p tcp --dport 25 -m state --state NEW -j SMTP
-A INPUT -p tcp -m state --state NEW -m tcp -m multiport --dports 6556,8970 -j MONITORIA
-A INPUT -p tcp -m state --state NEW -m tcp -m multiport --sports 4505,4506 -j SALT
-A OUTPUT -o lo -j ACCEPT
#-A OUTPUT -o eth1 -j ACCEPT
-A OUTPUT -p icmp -j ACCEPT
-A OUTPUT -m tcp -p tcp --sport 22 -j ACCEPT
-A OUTPUT -m multiport -p tcp --sport 80,443 -j ACCEPT
-A OUTPUT -m multiport -p tcp --dport 80,443 -m state --state NEW,ESTABLISHED -j WEB_ALLOW
-A OUTPUT -m tcp -p tcp -m multiport --sports 6556,8970 -j ACCEPT
-A OUTPUT -m tcp -p tcp -m multiport --dports 4505,4506 -j MONITORIA_OUT
-A OUTPUT -m tcp -p tcp --sport 123 --dport 123 -j ACCEPT
-A OUTPUT -m udp -p udp --sport 123 --dport 123 -j ACCEPT
-A OUTPUT -m tcp -p tcp --dport 53 -j ACCEPT
-A OUTPUT -m udp -p udp --dport 53 -j ACCEPT
-A OUTPUT -m tcp -p tcp --dport 25 -j SMTP
# Allow FTP connections @ port 21
-A OUTPUT -p tcp --sport 21 -m state --state ESTABLISHED -j FTP_OUT
# # Allow Active FTP Connections
-A OUTPUT -p tcp --dport 20 -m state --state NEW -j FTP_OUT
# #Allow Passive FTP Connections
-A OUTPUT -p tcp --sport 1024: --dport 1024: -m state --state ESTABLISHED,RELATED -j FTP_OUT
#
-A FTP -j INTERNO
-A FTP_OUT -j INTERNO_OUT
-A FTP -s 187.51.20.91/32 -j ACCEPT
-A FTP_OUT -d 187.51.20.91/32 -j ACCEPT
-A SSH -j INTERNO
-A SSH -j CONFIAVEIS
#Liberado Para Internet BruteFoce
##-A SSH -m recent --set --name SSH --mask 255.255.255.255 --rsource
##-A SSH -m recent --update --seconds 180 --hitcount 10 --name SSH --mask 255.255.255.255 --rsource -j LOG --log-prefix "Anti SSH-Bruteforce: " --log-level 6
##-A SSH -m recent --update --seconds 180 --hitcount 10 --name SSH --mask 255.255.255.255 --rsource -j DROP
##-A SSH -j ACCEPT
#-A SSH -j DROP
-A INPUT -m state --state NEW -m tcp -p tcp --dport 3306 -j MYSQL
-A INPUT -s 200.155.185.234/32 -p tcp -m state --state NEW -m tcp --dport 6556 -j ACCEPT
-A INPUT -j REJECT --reject-with icmp-host-prohibited
#### IPS CONFIAVES - REDE INTERNA
#Rede Monitoria SPO
-A CONFIAVEIS -s 200.155.185.224/28 -j ACCEPT
#Pooler POA
-A CONFIAVEIS -s 200.155.149.214/32 -j ACCEPT
#Poller RJO
-A CONFIAVEIS -s 200.201.158.198/32 -j ACCEPT
#machina SPO
-A INTERNO -s 200.170.201.2/32 -j ACCEPT
-A INTERNO -s 200.201.133.95/32 -j ACCEPT
-A INTERNO -s 200.170.208.13/32 -j ACCEPT
-A INTERNO_OUT -d 200.170.201.2/32 -j ACCEPT
-A INTERNO_OUT -s 200.201.133.95/32 -j ACCEPT
-A INTERNO_OUT -s 200.170.208.13/32 -j ACCEPT
-A INTERNO_OUT -d 200.170.208.13/32 -j ACCEPT
#RedeServico
-A CONFIAVEIS -s 200.170.235.0/24 -j ACCEPT
#Rede Taccs
-A CONFIAVEIS -s 200.201.131.0/24 -j ACCEPT
#Beta
-A CONFIAVEIS -s 200.201.133.10/32 -j ACCEPT
#Alpha
-A CONFIAVEIS -s 200.201.133.75/32 -j ACCEPT
# Não Identificado
-A CONFIAVEIS -s 200.201.144.12/32 -j ACCEPT
#Delta
-A CONFIAVEIS -s 200.170.235.200/32 -j ACCEPT
#OVH
-A CONFIAVEIS -s 149.56.117.54/32 -j ACCEPT
-A CONFIAVEIS -s 167.114.220.59/32 -j ACCEPT
# Backup Server
-A CONFIAVEIS -s 200.155.155.250/32 -j ACCEPT
-A CONFIAVEIS -s 200.170.208.13/32 -j ACCEPT
# MYSQl
#-A MYSQL -s 200.170.201.146/32 -j ACCEPT
# Middleware
-A MYSQL -s 10.16.144.23/32 -j ACCEPT
# BD do Wordpress
-A MYSQL -s 10.16.144.204/32 -j ACCEPT
-A MYSQL -j DROP
-A WEB_ALLOW -j Wordpress
-A WEB_ALLOW -j WWW_Interno
-A WEB_ALLOW -j LOG --log-prefix "Block WWW Out: " --log-level 6
# Liberar somente se necessário
#-A WEB_ALLOW -j ACCEPT
# Wordpress.org
#-A Wordpress -d 66.155.40.250 -j ACCEPT
#-A Wordpress -d 66.155.40.249 -j ACCEPT
# WWW Interno
-A WWW_Interno -d 200.170.196.34 -j ACCEPT
-A WWW_Interno -d 201.28.68.187 -j ACCEPT
-A WWW_Interno -d 200.201.133.30 -j ACCEPT
-A WWW_Interno -d 200.155.185.234 -j ACCEPT
#-A WWW_Interno -d 200.201.133.244 -j ACCEPT
#-A WWW_Interno -d 200.155.185.232 -j ACCEPT
# Filtro Canit
#-A SMTP -d 200.170.193.32/29 -j ACCEPT
# SMTP C3Systems
#-A SMTP -d 200.155.165.0/24 -j ACCEPT
# SMTP webrelay.machina.net.br
#-A SMTP -d 200.201.133.60/32 -j ACCEPT
# Monitoria
-A SALT -j MONITORIA
-A MONITORIA -s 200.155.149.208/29 -j ACCEPT                      
-A MONITORIA -s 200.155.185.224/28 -j ACCEPT
-A MONITORIA_OUT -d 200.155.149.208/29 -j ACCEPT
-A MONITORIA_OUT -d 200.155.185.224/28 -j ACCEPT
COMMIT

root@Paineiras-srv-01:~#


[root@beta ~]# cat /home/machina/iptables_r-56-VM
# Generated by iptables-save v1.4.14 on Mon May 27 13:59:45 2019
*nat
:PREROUTING ACCEPT [65300146:3706617309]
:INPUT ACCEPT [1388860:107163662]
:OUTPUT ACCEPT [133732:10115187]
:POSTROUTING ACCEPT [7630670:423062194]
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.133:20-21
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.133:20-21
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 20:21 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 20:21 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.133:22
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.133:22
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 22 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 22 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.133:80
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.133:80
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 80 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 80 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.133:443
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.133:443
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 443 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 443 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 3306 -j DNAT --to-destination 10.1.1.133:3306
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 3306 -j DNAT --to-destination 10.1.1.133:3306
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 3306 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 3306 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.133:8970
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.133:8970
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 8970 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 8970 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 6556 -j DNAT --to-destination 10.1.1.133:6556
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 6556 -j DNAT --to-destination 10.1.1.133:6556
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 6556 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 6556 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.133:20-21
-A OUTPUT -d 200.170.218.13/32 -p udp -m udp --dport 20:21 -m conntrack --ctstate NEW,ESTABLISHED -j DNAT --to-destination 10.1.1.133:20-21
-A OUTPUT -d 200.170.218.18/32 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m state --state RELATED,ESTABLISHED -j DNAT --to-destination 10.1.1.133
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.133:22
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.133:80
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.133:443
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 3306 -j DNAT --to-destination 10.1.1.133:3306
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.133:8970
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 6556 -j DNAT --to-destination 10.1.1.133:6556
-A POSTROUTING -o eth2 -j SNAT --to-source 200.170.218.13
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 20:21 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p udp -m udp --dport 20:21 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 1024:65535 -m state --state RELATED,ESTABLISHED -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 22 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 80 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 443 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 3306 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 8970 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 6556 -j SNAT --to-source 10.1.1.1
COMMIT
# Completed on Mon May 27 13:59:45 2019
# Generated by iptables-save v1.4.14 on Mon May 27 13:59:45 2019
*mangle
:PREROUTING ACCEPT [157391454:109573001792]
:INPUT ACCEPT [17954846:1140913249]
:FORWARD ACCEPT [187045355:118244074881]
:OUTPUT ACCEPT [17392994:1401541529]
:POSTROUTING ACCEPT [204438349:119645616410]
:FIREWALL_200.170.218.13 - [0:0]
:VPN_200.170.218.13 - [0:0]
-A PREROUTING -d 200.170.218.13/32 -j FIREWALL_200.170.218.13
-A PREROUTING -d 200.170.218.13/32 -j VPN_200.170.218.13
-A PREROUTING -m state --state RELATED,ESTABLISHED -j CONNMARK --restore-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -i eth2 -m state --state NEW -j CONNMARK --set-xmark 0x2/0xffffffff
-A POSTROUTING -p udp -m udp --dport 68 -j CHECKSUM --checksum-fill
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 8970 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 443 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 3306 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 22 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 80 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 20:21 -j RETURN
-A FIREWALL_200.170.218.13 -p udp -m udp --dport 20:21 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.13 -s 200.155.185.224/28 -p tcp -m tcp --dport 8970 -j RETURN
-A FIREWALL_200.170.218.13 -s 200.155.185.224/28 -p tcp -m tcp --dport 6556 -j RETURN
-A FIREWALL_200.170.218.13 -p icmp -m icmp --icmp-type 8/0 -j ACCEPT
-A FIREWALL_200.170.218.13 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.13 -j DROP
-A VPN_200.170.218.13 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A VPN_200.170.218.13 -j RETURN
COMMIT
# Completed on Mon May 27 13:59:45 2019
# Generated by iptables-save v1.4.14 on Mon May 27 13:59:45 2019
*filter
:INPUT DROP [534134:39218636]
:FORWARD DROP [0:0]
:OUTPUT ACCEPT [17392995:1401541613]
:FW_EGRESS_RULES - [0:0]
:FW_OUTBOUND - [0:0]
:NETWORK_STATS - [0:0]
-A INPUT -i eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i eth2 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 443 -m state --state NEW -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 53 -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p udp -m udp --dport 53 -j ACCEPT
-A INPUT -i eth0 -p udp -m udp --dport 67 -j ACCEPT
-A INPUT -j NETWORK_STATS
-A INPUT -d 224.0.0.18/32 -j ACCEPT
-A INPUT -d 225.0.0.50/32 -j ACCEPT
-A INPUT -p icmp -j ACCEPT
-A INPUT -i lo -j ACCEPT
-A INPUT -i eth1 -p tcp -m tcp --dport 3922 -m state --state NEW,ESTABLISHED -j ACCEPT
-A INPUT -i eth0 -j ACCEPT
-A INPUT -i eth0 -p udp -m udp --dport 67 -j ACCEPT
-A INPUT -s 10.1.1.0/24 -i eth0 -p udp -m udp --dport 53 -j ACCEPT
-A INPUT -s 10.1.1.0/24 -i eth0 -p tcp -m tcp --dport 53 -j ACCEPT
-A INPUT -i eth0 -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT
-A INPUT -i eth0 -p tcp -m tcp --dport 8080 -m state --state NEW -j ACCEPT
-A FORWARD -j NETWORK_STATS
-A FORWARD -i eth2 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth0 -o eth2 -j FW_OUTBOUND
-A FORWARD -i eth0 -o eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth0 -o eth0 -m state --state NEW -j ACCEPT
-A FORWARD -i eth0 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 20:21 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p udp -m udp --dport 20:21 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 22 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 80 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 443 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 3306 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 8970 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 6556 -m state --state NEW,ESTABLISHED -j ACCEPT
-A OUTPUT -j NETWORK_STATS
-A FW_EGRESS_RULES -j ACCEPT
-A FW_EGRESS_RULES -j DROP
-A FW_OUTBOUND -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FW_OUTBOUND -j ACCEPT
-A FW_OUTBOUND -j FW_EGRESS_RULES
-A NETWORK_STATS -i eth0 -o eth2
-A NETWORK_STATS -i eth2 -o eth0
-A NETWORK_STATS ! -i eth0 -o eth2 -p tcp
-A NETWORK_STATS -i eth2 ! -o eth0 -p tcp
COMMIT
# Completed on Mon May 27 13:59:45 2019
[root@beta ~]# vim /home/machina/iptables_r-56-VM
[root@beta ~]# vim teste
[root@beta ~]# vim /home/machina/iptables_r-56-VM
[root@beta ~]# vim /home/machina/iptables_r-56-VM
[root@beta ~]# cat /home/machina/iptables_r-56-VM
# Generated by iptables-save v1.4.14 on Mon May 27 13:59:45 2019
*nat
:PREROUTING ACCEPT [65300146:3706617309]
:INPUT ACCEPT [1388860:107163662]
:OUTPUT ACCEPT [133732:10115187]
:POSTROUTING ACCEPT [7630670:423062194]
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.133:20-21
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.133:20-21
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 20:21 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 20:21 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.133:22
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.133:22
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 22 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 22 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.133:80
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.133:80
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 80 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 80 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.133:443
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.133:443
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 443 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 443 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 3306 -j DNAT --to-destination 10.1.1.133:3306
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 3306 -j DNAT --to-destination 10.1.1.133:3306
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 3306 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 3306 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.133:8970
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.133:8970
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 8970 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 8970 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 6556 -j DNAT --to-destination 10.1.1.133:6556
-A PREROUTING -d 200.170.218.13/32 -i eth0 -p tcp -m tcp --dport 6556 -j DNAT --to-destination 10.1.1.133:6556
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 6556 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.13/32 -i eth2 -p tcp -m tcp --dport 6556 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.133:20-21
-A OUTPUT -d 200.170.218.13/32 -p udp -m udp --dport 20:21 -m conntrack --ctstate NEW,ESTABLISHED -j DNAT --to-destination 10.1.1.133:20-21
-A OUTPUT -d 200.170.218.18/32 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m state --state RELATED,ESTABLISHED -j DNAT --to-destination 10.1.1.133
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.133:22
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.133:80
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.133:443
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 3306 -j DNAT --to-destination 10.1.1.133:3306
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.133:8970
-A OUTPUT -d 200.170.218.13/32 -p tcp -m tcp --dport 6556 -j DNAT --to-destination 10.1.1.133:6556
-A POSTROUTING -o eth2 -j SNAT --to-source 200.170.218.13
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 20:21 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p udp -m udp --dport 20:21 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 1024:65535 -m state --state RELATED,ESTABLISHED -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 22 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 80 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 443 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 3306 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 8970 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.133/32 -o eth0 -p tcp -m tcp --dport 6556 -j SNAT --to-source 10.1.1.1
COMMIT
# Completed on Mon May 27 13:59:45 2019
# Generated by iptables-save v1.4.14 on Mon May 27 13:59:45 2019
*mangle
:PREROUTING ACCEPT [157391454:109573001792]
:INPUT ACCEPT [17954846:1140913249]
:FORWARD ACCEPT [187045355:118244074881]
:OUTPUT ACCEPT [17392994:1401541529]
:POSTROUTING ACCEPT [204438349:119645616410]
:FIREWALL_200.170.218.13 - [0:0]
:VPN_200.170.218.13 - [0:0]
-A PREROUTING -d 200.170.218.13/32 -j FIREWALL_200.170.218.13
-A PREROUTING -d 200.170.218.13/32 -j VPN_200.170.218.13
-A PREROUTING -m state --state RELATED,ESTABLISHED -j CONNMARK --restore-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -i eth2 -m state --state NEW -j CONNMARK --set-xmark 0x2/0xffffffff
-A POSTROUTING -p udp -m udp --dport 68 -j CHECKSUM --checksum-fill
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 8970 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 443 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 3306 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 22 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 80 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --dport 20:21 -j RETURN
-A FIREWALL_200.170.218.13 -p udp -m udp --dport 20:21 -j RETURN
-A FIREWALL_200.170.218.13 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.13 -s 200.155.185.224/28 -p tcp -m tcp --dport 8970 -j RETURN
-A FIREWALL_200.170.218.13 -s 200.155.185.224/28 -p tcp -m tcp --dport 6556 -j RETURN
-A FIREWALL_200.170.218.13 -p icmp -m icmp --icmp-type 8/0 -j ACCEPT
-A FIREWALL_200.170.218.13 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.13 -j DROP
-A VPN_200.170.218.13 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A VPN_200.170.218.13 -j RETURN
COMMIT
# Completed on Mon May 27 13:59:45 2019
# Generated by iptables-save v1.4.14 on Mon May 27 13:59:45 2019
*filter
:INPUT DROP [534134:39218636]
:FORWARD DROP [0:0]
:OUTPUT ACCEPT [17392995:1401541613]
:FW_EGRESS_RULES - [0:0]
:FW_OUTBOUND - [0:0]
:NETWORK_STATS - [0:0]
-A INPUT -i eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i eth2 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 443 -m state --state NEW -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 53 -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p udp -m udp --dport 53 -j ACCEPT
-A INPUT -i eth0 -p udp -m udp --dport 67 -j ACCEPT
-A INPUT -j NETWORK_STATS
-A INPUT -d 224.0.0.18/32 -j ACCEPT
-A INPUT -d 225.0.0.50/32 -j ACCEPT
-A INPUT -p icmp -j ACCEPT
-A INPUT -i lo -j ACCEPT
-A INPUT -i eth1 -p tcp -m tcp --dport 3922 -m state --state NEW,ESTABLISHED -j ACCEPT
-A INPUT -i eth0 -j ACCEPT
-A INPUT -i eth0 -p udp -m udp --dport 67 -j ACCEPT
-A INPUT -s 10.1.1.0/24 -i eth0 -p udp -m udp --dport 53 -j ACCEPT
-A INPUT -s 10.1.1.0/24 -i eth0 -p tcp -m tcp --dport 53 -j ACCEPT
-A INPUT -i eth0 -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT
-A INPUT -i eth0 -p tcp -m tcp --dport 8080 -m state --state NEW -j ACCEPT
-A FORWARD -j NETWORK_STATS
-A FORWARD -i eth2 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth0 -o eth2 -j FW_OUTBOUND
-A FORWARD -i eth0 -o eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth0 -o eth0 -m state --state NEW -j ACCEPT
-A FORWARD -i eth0 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 20:21 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p udp -m udp --dport 20:21 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 22 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 80 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 443 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 3306 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 8970 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 6556 -m state --state NEW,ESTABLISHED -j ACCEPT
-A OUTPUT -j NETWORK_STATS
-A FW_EGRESS_RULES -j ACCEPT
-A FW_EGRESS_RULES -j DROP
-A FW_OUTBOUND -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FW_OUTBOUND -j ACCEPT
-A FW_OUTBOUND -j FW_EGRESS_RULES
-A NETWORK_STATS -i eth0 -o eth2
-A NETWORK_STATS -i eth2 -o eth0
-A NETWORK_STATS ! -i eth0 -o eth2 -p tcp
-A NETWORK_STATS -i eth2 ! -o eth0 -p tcp
COMMIT
# Completed on Mon May 27 13:59:45 2019
[root@beta ~]# 
--
Att.
Jeff Muniz, seu amigo de sempre :D





[root@beta ~]# cat /home/machina/iptables_r-147-VM
# Generated by iptables-save v1.6.0 on Mon May 27 14:03:27 2019
*raw
:PREROUTING ACCEPT [1264964760:3060723066797]
:OUTPUT ACCEPT [483898:97914320]
-A PREROUTING -p tcp -m tcp --dport 21 -j CT --helper ftp
COMMIT
# Completed on Mon May 27 14:03:27 2019
# Generated by iptables-save v1.6.0 on Mon May 27 14:03:27 2019
*nat
:PREROUTING ACCEPT [9835:597082]
:INPUT ACCEPT [1219:89058]
:OUTPUT ACCEPT [20:1496]
:POSTROUTING ACCEPT [1666:96189]
-A PREROUTING -m state --state RELATED,ESTABLISHED -j CONNMARK --restore-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.124:20-21
-A PREROUTING -d 200.170.218.18/32 -i eth0 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.124:20-21
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 20:21 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 20:21 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p udp -m udp --dport 20 -j DNAT --to-destination 10.1.1.124:20
-A PREROUTING -d 200.170.218.18/32 -i eth0 -p udp -m udp --dport 20 -j DNAT --to-destination 10.1.1.124:20
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p udp -m udp --dport 20 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p udp -m udp --dport 20 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.124:22
-A PREROUTING -d 200.170.218.18/32 -i eth0 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.124:22
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 22 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 22 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.124:8970
-A PREROUTING -d 200.170.218.18/32 -i eth0 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.124:8970
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 8970 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.18/32 -i eth2 -p tcp -m tcp --dport 8970 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.124:20-21
-A PREROUTING -d 200.170.218.19/32 -i eth0 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.124:20-21
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 20:21 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 20:21 -m state --state NEW,RELATED,ESTABLISHED -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p udp -m udp --dport 20 -j DNAT --to-destination 10.1.1.124:20
-A PREROUTING -d 200.170.218.19/32 -i eth0 -p udp -m udp --dport 20 -j DNAT --to-destination 10.1.1.124:20
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p udp -m udp --dport 20 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p udp -m udp --dport 20 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.124:22
-A PREROUTING -d 200.170.218.19/32 -i eth0 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.124:22
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 22 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 22 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.124:80
-A PREROUTING -d 200.170.218.19/32 -i eth0 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.124:80
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 80 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 80 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.124:443
-A PREROUTING -d 200.170.218.19/32 -i eth0 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.124:443
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 443 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 443 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 4321 -j DNAT --to-destination 10.1.1.124:1234
-A PREROUTING -d 200.170.218.19/32 -i eth0 -p tcp -m tcp --dport 4321 -j DNAT --to-destination 10.1.1.124:1234
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 4321 -j MARK --set-xmark 0x2/0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 4321 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -d 200.170.218.19/32 -i eth2 -p tcp -m tcp --dport 20:21 -m state --state NEW -j CONNMARK --save-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A OUTPUT -d 200.170.218.18/32 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.124:20-21
-A OUTPUT -d 200.170.218.18/32 -p tcp -m tcp --dport 20:21 -m conntrack --ctstate NEW,ESTABLISHED -j DNAT --to-destination 10.1.1.124:20-21
-A OUTPUT -d 200.170.218.18/32 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m state --state RELATED,ESTABLISHED -j DNAT --to-destination 10.1.1.124
-A OUTPUT -d 200.170.218.18/32 -p udp -m udp --dport 20 -j DNAT --to-destination 10.1.1.124:20
-A OUTPUT -d 200.170.218.18/32 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.124:22
-A OUTPUT -d 200.170.218.18/32 -p tcp -m tcp --dport 8970 -j DNAT --to-destination 10.1.1.124:8970
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --dport 20:21 -m conntrack --ctstate NEW,ESTABLISHED -j DNAT --to-destination 10.1.1.124:20-21
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --sport 21 -m state --state ESTABLISHED -j DNAT --to-destination 10.1.1.124
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m state --state RELATED,ESTABLISHED -j DNAT --to-destination 10.1.1.124
-A OUTPUT -d 200.170.218.19/32 -p udp -m udp --dport 20 -j DNAT --to-destination 10.1.1.124:20
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --dport 22 -j DNAT --to-destination 10.1.1.124:22
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --dport 80 -j DNAT --to-destination 10.1.1.124:80
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --dport 443 -j DNAT --to-destination 10.1.1.124:443
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --dport 4321 -j DNAT --to-destination 10.1.1.124:1234
-A OUTPUT -d 200.170.218.19/32 -p tcp -m tcp --dport 20:21 -j DNAT --to-destination 10.1.1.124:20-21
-A POSTROUTING -o eth2 -j SNAT --to-source 200.170.218.18
-A POSTROUTING -o eth2 -j SNAT --to-source 200.170.218.19
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --sport 20 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p udp -m udp --dport 20 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 22 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 8970 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 80 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 443 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 1234 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 20:21 -j SNAT --to-source 10.1.1.1
-A POSTROUTING -s 10.1.1.0/24 -d 10.1.1.124/32 -o eth0 -p tcp -m tcp --dport 1024:65535 -m state --state RELATED,ESTABLISHED -j SNAT --to-source 10.1.1.1
COMMIT
# Completed on Mon May 27 14:03:27 2019
# Generated by iptables-save v1.6.0 on Mon May 27 14:03:27 2019
*mangle
:PREROUTING ACCEPT [1760208:8442584520]
:INPUT ACCEPT [2663:200243]
:FORWARD ACCEPT [3173777:8553621647]
:OUTPUT ACCEPT [1811:218715]
:POSTROUTING ACCEPT [3175588:8553840362]
:FIREWALL_200.170.218.18 - [0:0]
:FIREWALL_200.170.218.19 - [0:0]
:VPN_200.170.218.18 - [0:0]
:VPN_200.170.218.19 - [0:0]
-A PREROUTING -d 200.170.218.19/32 -j FIREWALL_200.170.218.19
-A PREROUTING -d 200.170.218.19/32 -j VPN_200.170.218.19
-A PREROUTING -d 200.170.218.18/32 -j FIREWALL_200.170.218.18
-A PREROUTING -d 200.170.218.18/32 -j VPN_200.170.218.18
-A PREROUTING -m state --state RELATED,ESTABLISHED -j CONNMARK --restore-mark --nfmask 0xffffffff --ctmask 0xffffffff
-A PREROUTING -i eth2 -m state --state NEW -j CONNMARK --set-xmark 0x2/0xffffffff
-A POSTROUTING -p udp -m udp --dport 68 -j CHECKSUM --checksum-fill
-A FIREWALL_200.170.218.18 -s 200.170.201.2/32 -p tcp -m tcp --dport 22 -j RETURN
-A FIREWALL_200.170.218.18 -p tcp -m tcp --dport 20:21 -j RETURN
-A FIREWALL_200.170.218.18 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.18 -p tcp -m tcp --dport 80 -j RETURN
-A FIREWALL_200.170.218.18 -p tcp -m tcp --dport 443 -j RETURN
-A FIREWALL_200.170.218.18 -p udp -m udp --dport 20 -j RETURN
-A FIREWALL_200.170.218.18 -p tcp -m tcp --dport 22 -j RETURN
-A FIREWALL_200.170.218.18 -p udp -m udp --dport 500 -j RETURN
-A FIREWALL_200.170.218.18 -p udp -m udp --dport 1161:1162 -j RETURN
-A FIREWALL_200.170.218.18 -s 200.155.185.230/32 -p tcp -m tcp --dport 8970 -j RETURN
-A FIREWALL_200.170.218.18 -p icmp -m icmp --icmp-type 8/0 -j ACCEPT
-A FIREWALL_200.170.218.18 -p tcp -m tcp --dport 22022 -j RETURN
-A FIREWALL_200.170.218.18 -p udp -m udp --dport 4500 -j RETURN
-A FIREWALL_200.170.218.18 -p tcp -m tcp --dport 2222 -j RETURN
-A FIREWALL_200.170.218.18 -p udp -m udp --dport 1701 -j RETURN
-A FIREWALL_200.170.218.18 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.18 -j DROP
-A FIREWALL_200.170.218.19 -p tcp -m tcp --dport 20 -j RETURN
-A FIREWALL_200.170.218.19 -p tcp -m tcp --dport 21 -j RETURN
-A FIREWALL_200.170.218.19 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.19 -p icmp -m icmp --icmp-type 8/0 -j ACCEPT
-A FIREWALL_200.170.218.19 -p tcp -m tcp --dport 22 -j RETURN
-A FIREWALL_200.170.218.19 -p tcp -m tcp --dport 80 -j RETURN
-A FIREWALL_200.170.218.19 -p tcp -m tcp --dport 443 -j RETURN
-A FIREWALL_200.170.218.19 -p udp -m udp --dport 20 -j RETURN
-A FIREWALL_200.170.218.19 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FIREWALL_200.170.218.19 -j DROP
-A VPN_200.170.218.18 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A VPN_200.170.218.18 -j RETURN
-A VPN_200.170.218.19 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A VPN_200.170.218.19 -j RETURN
COMMIT
# Completed on Mon May 27 14:03:27 2019
# Generated by iptables-save v1.6.0 on Mon May 27 14:03:27 2019
*filter
:INPUT DROP [794:41888]
:FORWARD DROP [0:0]
:OUTPUT ACCEPT [1811:218715]
:FW_EGRESS_RULES - [0:0]
:FW_OUTBOUND - [0:0]
:NETWORK_STATS - [0:0]
-A INPUT -i eth0 -j ACCEPT
-A INPUT -i eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -i eth2 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 443 -m state --state NEW -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p tcp -m tcp --dport 53 -j ACCEPT
-A INPUT -d 10.1.1.1/32 -i eth0 -p udp -m udp --dport 53 -j ACCEPT
-A INPUT -i eth0 -p udp -m udp --dport 67 -j ACCEPT
-A INPUT -j NETWORK_STATS
-A INPUT -d 224.0.0.18/32 -j ACCEPT
-A INPUT -d 225.0.0.50/32 -j ACCEPT
-A INPUT -p icmp -j ACCEPT
-A INPUT -i lo -j ACCEPT
-A INPUT -i eth1 -p tcp -m tcp --dport 3922 -m state --state NEW,ESTABLISHED -j ACCEPT
-A INPUT -i eth0 -j ACCEPT
-A INPUT -i eth0 -p udp -m udp --dport 67 -j ACCEPT
-A INPUT -s 10.1.1.0/24 -i eth0 -p udp -m udp --dport 53 -j ACCEPT
-A INPUT -s 10.1.1.0/24 -i eth0 -p tcp -m tcp --dport 53 -j ACCEPT
-A INPUT -i eth0 -p tcp -m tcp --dport 80 -m state --state NEW -j ACCEPT
-A INPUT -i eth0 -p tcp -m tcp --dport 8080 -m state --state NEW -j ACCEPT
-A FORWARD -j NETWORK_STATS
-A FORWARD -i eth0 -o eth1 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth0 -o eth0 -m state --state NEW -j ACCEPT
-A FORWARD -i eth0 -o eth0 -m state --state RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth0 -o eth2 -j FW_OUTBOUND
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 22 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 8970 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 80 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 443 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 1234 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p tcp -m tcp --dport 20:21 -m state --state NEW,ESTABLISHED -j ACCEPT
-A FORWARD -i eth2 -o eth0 -p udp -m udp --dport 20 -m state --state NEW,ESTABLISHED -j ACCEPT
-A OUTPUT -j NETWORK_STATS
-A FW_EGRESS_RULES -j ACCEPT
-A FW_EGRESS_RULES -j DROP
-A FW_OUTBOUND -p tcp -m tcp --sport 1024:65535 --dport 1024:65535 -m conntrack --ctstate RELATED,ESTABLISHED -j ACCEPT
-A FW_OUTBOUND -j ACCEPT
-A FW_OUTBOUND -j FW_EGRESS_RULES
-A NETWORK_STATS -i eth0 -o eth2
-A NETWORK_STATS -i eth2 -o eth0
-A NETWORK_STATS ! -i eth0 -o eth2 -p tcp
-A NETWORK_STATS -i eth2 ! -o eth0 -p tcp
COMMIT
# Completed on Mon May 27 14:03:27 2019
[root@beta ~]# 




Cloud
Tvdent-srv-01
 i-11-25-VM
Router
 r-147-VM
Public IP Address   200.170.218.18


paineiras
router
  r-56-vm
host
Paineiras-srv-01    i-6-5-VM    10.1.1.133

Displaying 71532192415219.png.


programa fnx
AtomicRocket

Jeff Muniz <jmuniz1985@gmail.com>
Sat, Dec 26, 2020, 2:07 AM
to me

read from clipboard
xclip
DISPLAY=:0 xclip -sel clip < beta.hosts 

#!/bin/bash

read -p "Valor conta Luz : " 		luz
echo " "
read -p "Valor conta Máquina : " 	maq \n
echo " "
read -p "Valor conta Internet : " 	net
echo " "
read -p "Valor conta Água : " 		agu
echo " "

luzdiv=$((luz/4))
echo -e "Valor da Luz dividido por 4 pessoas : 		$luzdiv \n" 			
maqdiv=$((maq/4))
echo "Valor da Máquina dividido por 4 pessoas : 	$maqdiv \n" 		
netdiv=$((net/3))
echo "Valor da Internet dividido por 3 pessoas : 	$luzdiv \n"	
agudiv=$((agu/4))
echo "Valor da Água dividido por 4 pessoas : 		$agudiv \n" 			

echo " "
echo "Rapha pagou conta de Luz: $luz" 						
echo "Mãe pagou conta de Água: $agu" 						
echo "Jeff pagou conta de Internet: $net" 					
 
totraf=$((agudiv+netdiv+maqdiv))
echo "Rapha deve Internet Água Máquina = $totraf" 
echo " "

totmae=$((luzdiv+netdiv+maqdiv))
echo "Mãe deve Internet Luz Máquina = $totmae" 
echo " "

totjef=$((luzdiv+agudiv+maqdiv))
echo "Jeff deve Água+Luz+Máquina = $totjef" 
echo " "


tnettv=$((netdiv+15))
echo -e " Rapha deve pro Jeff  Internet e TV = $tnettv  \n"

tluzmaq=$((luzdiv+maqdiv-tnettv))
echo -e " Jeff deve pro Rapha  Luz e Máquina = $tluzmaq  \n"
tjr=$((tnettv-tluzmaq))
echo -e "Jeff paga para Rapha $tjr \n"

tjm=$((netdiv-$agudiv))
echo -e " Mãe deve pro Jeff Internet = $netdiv  \n"
echo -e " Jeff deve pra Mãe  Água = $agudiv  \n"
echo -e "Mãe  paga para Jeff $tjm \n"



tluzmaqmae=$((luzdiv+maqdiv))
echo -e " Mãe deve pro Rapha  Luz e Máquina = $tluzmaqmae  \n"
trm=$((luzdiv+maqdiv-agudiv))
echo -e " Mãe paga para Rapha  = $trm  \n"
echo " "







	
	
	
	
Displaying 71532192415219.png.


SRE

https://landing.google.com/sre/sre-book/chapters/production-environment/  





10Some readers may be more familiar with Borg’s descendant, Kubernetes—an open source Container Cluster orchestration framework started by Google in 2014; 



see http://kubernetes.io and [Bur16]. For more details on the similarities between Borg and Apache Mesos, see [Ver15].

https://en.wikipedia.org/wiki/Apache_Mesos  

11See http://grpc.io.



12Protocol buffers are a language-neutral, platform-neutral extensible mechanism for serializing structured data. For more details, 

see https://developers.google.com/protocol-buffers/.



Displaying 71532192415219.png.



while true ; do free -m ; echo Este FREE ;sleep 5 ; done

4 Linux - LPI 450



Skip to content
Using Gmail with screen readers
in:sent 
Meet
Hangouts

25 of 125
4 Linux - LPI 450
AtomicRocket

Jeff Muniz <jmuniz1985@gmail.com>
Sun, Sep 23, 2018, 9:50 PM
to me

http://www.linux-praxis.de/lpisim/lpi101sim/main.php3


netstat -putanl
-p PID
-u -t UDP e TCP
a - alll listenn e non lsten
-n numeric IP ADD
-l only listenning soccket

# rpm -qpR htop-1.0.3-1.el5.rf.i386.rpm

warning: htop-1.0.3-1.el5.rf.i386.rpm: Header V3 DSA/SHA1 Signature, key ID 6b8d79e6: NOKEY

libc.so.6  
libc.so.6(GLIBC_2.0)  

libc.so.6(GLIBC_2.1)  estas parecem no rpm -qa | glibc

os abaixo nao
libncursesw.so.5  
rpmlib(CompressedFileNames) <= 3.0.4-1
rpmlib(PayloadFilesHavePrefix) <= 4.0-1
rtld(GNU_HASH)  


dependencia do programa era a libc6 versao 2.9
no sostema esta a versao 1.5 mas o mesmo dizia estar

na ultima versao ao rodar apt-get
resolvi com ao rodar apt-get -f install
Media change: please insert the disc labeled
 'Debian GNU/Linux 7.5.0 _Wheezy_ - Official i386 DVD Binary-1 20140426-12:26'

como funciona sysstat

instalei no terminal como  achar no grafico

sudo e su diferença

professor daniel
dzmlinux no viva o linux
daniel.manzano@4linux.com.br



ghost of the wire


estudar os capitulos
6 8 9 10 13


**** dicas
unknownsec.wordpress.com/


http://www.armbrust.eti.br/
aurelio.net
vimtutor
cmatrix
Limpar a tela no meio de um comando
ctrl+L
/usr = unix shared resources

http://www.tldp.org/


EAD
cesso: http://mais.4linux.com.br/
Login: jmuniz1985@gmail.com
senha
Pistols@01

user / senha maquina
noturno / 4linux

192.168.1.1/450/slides

Servidor na nuvem
200.100.1.79


######
@ubuntu:~# dpkg -S /usr/bin/top
procps: /usr/bin/top
 rpm -qf /usr/bin/top
procps-3.2.8-25.el6.x86_64


comando egrep expressiogrep
dpkg -l |egrep "libc6|libncur"

comando tar para outro direorio

tar zxvf /home/arquivo.tar.gz -C /seu/dir/destino

VIM comandos VI
set number
set nonumber

utilizando mias de um comando
com1 ; com2

com1 && com2
o comando dois sera executado se o um for com sucesso
com1 || com2
Executa o um se OK nao executa o segundo, caso o um falhe executa  o dois

 system-config-keyboard



Versao versoes
/etc/os_release
/proc/version
lsb_release -a

Kernel

uname -r
uname -i


Desligar
shutdown -h now
halt
poweroff
init 0

espaco em disco
df -hT

usuarios da maquina
/etc/passwd

UID no CENT OS
usuario administrador
 o UID sempre 0
Usuario de sistema 
1 a 499
Usuario comum
500 a 65 535



Debian
usuario administrador
 o UID sempre 0
Usuario de sistema 
1 - 999
Usuario comum
1000 a 65 535

awk -F: '($3 > 999) {print$1}' /etc/passwd
suporte
helpdesk



comando w

noturno$ w
 19:39:54 up 50 min,  3 users,  load average: 0,18, 0,12, 0,14
USER     TTY      FROM             LOGIN@   IDLE   JCPU   PCPU WHAT
noturno  tty7     :0               18:50   50:06  32.69s  0.06s gdm-session-worker [pam/gdm3]
noturno  pts/0    :0.0             18:50   31:14   0.15s  0.05s ssh root@200.100.1.79
noturno  pts/1    :0.0             19:13    2.00s  0.16s  0.00s w



comandos de rede

ip a
ip r
route -n

saber o tipo de um arquivo
file

touch
serve para alterar metadados do arquivo
stat mostra os metadados

criar arquivo vazio no  linux
>nome_do_arquivo

ou
comando 
echo 'algumafrase' > echo.txt

find / -iname "*vimrc*"

touch arq{1..30}.txt

figlet Linux 
| (_)_ __  _   ___  __
| | | '_ \| | | \ \/ /
| | | | | | |_| |>  < 
|_|_|_| |_|\__,_/_/\_\

mudando barra invertida no VIM
:% s/\\/\\\\/g


history historico
executar comandos do history

! e numero do comando ou as primeiras letras
!! o ultimo comando e executado
navegar sem setas
CTRL+p 
CTRL+n

Busca reversa
CTRL+r
digita as letras do comando
CTRL+n

resolver problemas com alias
colocar uma contra barra impede que o sistema verifique se trata de um alias
ex.

\alias
ou
\unalias unalias

/
variaveis variavel
Locall e configurada informando no bash
global usando o comando export

env visualiza local - environment

set visualiza locais e globais - todas variaveis do shel segundo man
set [--abefhkmnptuvxBCEHPT] [-o option] [arg ...]
       set [+abefhkmnptuvxBCEHPT] [+o option] [arg ...]
              Without options, the name and value of each shell  variable  are
              displayed in a format that can be reused as input for setting or
              resetting the currently-set variables.

root@ubuntu:/home/jeff# echo $variloc
local
root@ubuntu:/home/jeff# cat set | grep variloc
variloc=local
root@ubuntu:/home/jeff# cat env | grep variloc
root@ubuntu:/home/jeff# 

Variavel local de enviroment

env

TERMO=valor
NUM=36
echo $NUM

Variavel global
criada com export TERMO=valor

sao visualizadas com

set


Aspas duplas converte as variaveis 
Aspas simples mostra o valor literal escrita EX. $NUM 

Usando o resultado de um comando como variavel

VAR3=$(which shutdown)
echo $VAR3
/sbin/shutdown


echo $SHLVL

Variavel Global
Para tornar global uma variavel ja criada
export NUM 

Ou criar e estabelecer valor com o export

export NUMBER=123



Para remover uma variavel

unset NUM
ou
NUM=


variavel $PATH caminho separado por dois pontos
 echo $PATH
/usr/lib/qt-3.3/bin:/usr/local/sbin:

Ao executar um comando o sistema busca um alias nao encontrando ele busca nos caminhos do $PATH

para agregar usar PATH=$PATH:novocaminho

for i in $(seq 1 10); do echo $i; done

$PS1
echo $PS1
[\u@\h \W]\$ = [root@hostname /bin]$

onde
\u = user
@
\h = hostname

\W = localizacao
\$ = credencial

PS1="[\u@\h \w]\n\$" = [root@localhost /usr/share]
                                    $

$TMOUT
Controla o tempo que a maquina trava e pede senha
desloga o usuario
timeout de usuario


sequencia de leitua dos arquivos
 /etc/profile
 ~/.bash_profile 
 ~/.bash_login
 ~/.profile 
/etc/bashrc e lido por todos os usuarios que fazem login

Para usuario por usuario usar o arquivo /home/.bashrc

para recarregar os arquivos modificados
apos alterar o arquivo /etc/bashrc

source /etc/bashrc


yum install bash-completion 
depois editar o /etc/bash
comando sl


no debian descomentar no /etc/bash.bash
o enable bash completion

subshell
ao usar um um comaando dentro de uma subshell
COMANDO 1 $(COMANDO2)
kill -9 $(pgrep firefox)


REDES
comandos de rede cabo desconectado

mii-tool eth0 CENTOS
mii-tool DEBIAN

ethtool

route add -net 0/0 gw 192.168.201.254 dev eth0
route add default gw 192.168.201.254 
route del

ifconfig eth0 down

ifdown eth0
ifup eth0

dhclient - eth0 release do ip
dhclient -v eth0

ifconfig interface real:interface virtual endereco

echo 'redhat.forlinux' >/proc/sys/kernel/hostname

arquivos
red hat

/etc/sysconfig/network-scripts/ifcfg-eth0
/etc/sysconfig/network
cat /etc/sysconfig/network
NETWORKING=yes
HOSTNAME=redhat.forlinux
GATEWAY=192.168.201.254




Debian

/etc/hostname
service hostname.sh start
/etc/network/interfaces
/etc/resolf.conf

Em ambas
/etc/hosts
/etc/nsswitch.conf
/etc/networks


SSH

No Red Hat comentar as opcoes e ficar como abaixo
No Debian vem por default

vim /etc/ssh/sshd_config

# GSSAPI options
#GSSAPIAuthentication no
#GSSAPIAuthentication yes
#GSSAPICleanupCredentials yes
#GSSAPICleanupCredentials yes
#GSSAPIStrictAcceptorCheck yes
#GSSAPIKeyExchange no


ssh-key
duas chaves sao geradas publica e privadas
as chaves publicas tem extensao 
.pub


usando chaves com nome diferente
ssh-copy-id -i diff_id.pub "-p2222 root@192.168.201.150"
noturno@maddog15:~/.ssh$ pwd
/home/noturno/.ssh
noturno@maddog15:~/.ssh$ cat config 
IdentityFile /home/noturno/.ssh/diff_id

ssh-copyid -i para funcionar o tab



Tunel tunelamento
ssh -f -N -L12345:192.168.201.150:80 -p2222 root@192.168.201.150

onde -f faz o prompt ser liberado
e -N para nao executar comando remotamente 

-L12345 = porta local 
192.168.201.150 ip de destino 
:80 porta do servico que sera alocado em Localhost:porta local
127.0.0.1:12345

Usuarios permitidos no sshd_config
AllowUsers noturno id_user

Arquivos
sshd_config = configuracao do daemon - servidor
ssh_config = configuracao do client



TCP Wrappers

/etc/hosts.allow configuração de acessos permitidos
/etc/hosts.deny
lidos em tempo de excucao
sintaxe
servico : endereco_ip : opcoes

no allow
sshd:192.168.201.* EXCEPT 192.168.201.20


duas regras importantes
O que colocado no allow apenas a maquina inserida vai ser liberada
Ordem dos arquivos lidos:
Primeiro ALLOW e lido
depois o DENY 

Nao tendo a regra em nenhum dos arquivos tudo funciona
tendo nos dois eh permitido pois o ALLOW eh lido primeiro

Verificar se o ssh possui suporte/biblioteca ao tcp wrrapper

ldd $(which sshd) | grep wrap
	libwrap.so.0 => /lib/libwrap.so.0



no hosts.deny
ALL:ALL:spawn /bin/echo "$(date) - Conexao Recusada - SSH %a">> /var/log/tcpwrrapers_deny.log

o ALL:ALL todos de toas redes
spawn sxecuta o binario a frente echo no caso


%a 
A MAQUINA de origem



alias sshdeb="ssh root@192.168.201.249"
alias sshred="ssh root@192.168.201.150"


interface de rede nao eh hardware nao tratado como arquivo
Para que nao seja capturado o trafego

outros componentes sao detectados pelos programas
devfs 
udev

tipos de arquivo de hardware
bloco = HD pendrive
caracter = fluxo de texto
pseudo-disposito = devices que nao existem = /dev/null
/dev/full
 cat arq > /dev/random 


Com o dd e possivel gravar dados vindo desses pseudo-devices
dd if=/dev/full of=/root/arq1 bs=1024 count=1024

o diretorio 
/dev/input
contem os dispositivos de entrada


o diretorio /proc eh um diretorio virtual onde o hardware eh utilizado em forma de arquivos em tempo real pelo kernel

Particao primaria recebe dados e tem limite de 4 por disco

Exemplo

/dev/sda1
/dev/sda2
/dev/sda3
/dev/sda4


Particoes extendidas contem outras particoes
Conta como uma particao primaria
E apenas uma particao extendida por disco

Ex.
/dev/sda1
/dev/sda2
/dev/sda3
/dev/sda4 Extendida

Particao Logica
Nao tem limite
Comecam a partir do numero 5

sda1 primaria
sda2 extendida

sda5 a Logica


A logica eh a mesma coisa que a primaria mas fica dentro das extendidas



Menor particao para o /
pois eles contem arvores de diretorio

Maior particao para a home 


Para apliaccao squid por exemplo
o diretorio /var contem log e gera dados


separar o /boot eh uma pratica antiga

Dispositivo Boot      Start         End      Blocks   Id  System
/dev/sdb1               1         132     1060258+  83  Linux
/dev/sdb4             133         652     4176900    5  Estendida
/dev/sdb5             133         264     1060258+  83  Linux
/dev/sdb6             265         396     1060258+  83  Linux
/dev/sdb7             397         461      522081   83  Linux
/dev/sdb8             462         652     1534176   83  Linux


O ReiserFS era mantido por uma unica pessoa
Com a morte da sua mulher ele foi descontinuado

para verificar as particoes
blkid

mkdir /{backup,srv1}


dd if=/dev/zero of=/swap bs=1024 count 10000
mkswap /swap
swapon /swap

swapon -s



Diretorios que podemos particionar
/etc
/bin
/dev
/proc
/lib
/sbin
/sys

Quais nao podem
/
/var
/usr
/home
/boot
/tmp
/srv
/sys


/etc/fstab
/dev/sdb1        /backup                       ext3    defaults        0     0
disco             ponto de montagem       tipo     opcoes         primeiro zero se precisa fazer bakup com o dump
Segundo zero se deve ser verificado a todo boot


/etc/mtab

usar o tune2fs para criaro lable e preencher o /etc/fstab:
                          
LABEL=titulo_particao     /home/seu_diretorio     ext4   defaults 0 0

mount no debian
mount -t cd9660 -o -e /dev/cdrom /mnt/cd
tipo cd9660
-o passa opções e pode ser omitido
-e ainda nao sei

PROGRAMAS
instalação de programas script 

Do codigo fonte resumo
readme/install
./configure
make
make install

make 


Debian
apt-get update 
apt-cache search "log color"
apt-cache show ccze
apt-get install ccze

gerador repositorio
http://debgen.simplylinux.ch
http://pkgs.repoforge.org/
http://pkgs.org/
http://rpm.pbone.net/

debian.org
se usar o apt-get nao usar o apttitude
simon programa de voz



Administrando processos
Processos
Sao tarefas que precisam utilizar o kernel
o comando eh digitado no teclado
obash interpreta e executa o binario
que pede recursos ao kernel
que fornece um PID e recursos ao proprocesso
no diretorio /proc eh mostrado
processos em andamento e todos os recursos e parametros 

Existem diretorios com numeros onde cada numero eh um processo

comandos sao utilzados para interpretar estes diretorios e processos



O processo Init e o primeiro processo a ser criado e te sempre PID 1
Processos PAI criam processos FILHO


Se o user ou o sistema terminar um processo filho
o processo PAI fica intacto
Matando o processo PAI todos filhos morrem

Logo o INIT e o PAI de todos os outros filhos


Pode acontecer de um processo fantasma ou zombie
que deveria ter morrido mas nao morrer

Caso tipico de matar o processo PAI e o processo FILHO ficar vivo

Esses processos fantasmas tem efeitos indesejados no sistema

comandos para verificacao dos processos em tempo real
process snapshot
ps

ps aux 
-a mostra informacoes de terminal
-u 

pstree -pa
-p PID e -a CAMINHO


Na pratica

pgrep -l crond
Faz uma busca apenas no valor informado e lista o nome

pidof
pidof crond


kill PID do processp

kiil -l lista o numero de sinais que podem ser enviados

CTRL+C igual ao 15)SIGTERM

os mais usados sao 

9     MATAR - SIGKILL
O processo e terminado grosseiramente antes mesmo de terminar um a lista de tarefas
Dados nao salvos podem ser perdidos

15   TERMINAR - SIGTERM
Envia sinal de terminar e aguarda oprocesso terminar a lista de tarefas para encerra-lo



kill sem argumento equivale a 
kill -15 PID SIGTERM

matando varios processos
subshell

kill -9 $(pgrep firefox)
o mesmo que 
killall crond

killall usa correspondencia exata

pkill cron
vai matar todos processos com cron




Comandos que mostram processos com um certo intervalo

top
barra de espaco atualiza
a tecla 1 mostra o numero de cpus

htop top melhorado


executando processos em segundo plano

comando &

o comando jobs processos em segundo plano e parado

abrir o vim e apertar CONTROL Z

 jobs
[1]+  Parado                  vim

trazer para primeiro plano
fg ID do jobs

fg 1

kill %ID do jobs

ping 8.8.8.8 &> /root/ping &

bg traz para background com o ID do jobs

nohup 
O processo em segundo plano vai funcionar enquanto a a secao estiver aberta
Com o nohup o processo se torna independente do logout

nohup ping localhost > /dev/null &


Vai iniciar o processo em segundo plano
e continuar mesmo depois da secao ser deslogada
Porem o usuario que iniciou o processo vai ser dono dele ate o fim

comando yes mostra uma string repetitivamente


Prioridade dos processos

Na saida do TOP

Avaliar o valor do nice (NI)
O campo PR (priority) eh uma contrapartida

Menor prioridade recebe atencao maior  e mais tempo de processamento do kernel
os processos podem ir de -20 +19 onde
-20 e a maior prioridade possivel
+19 a menor prioridade possivel

Todo processo iniciado por root tera o valor
Todo processo iniciado por user comum tera o valor 10
o ROOT pode aumentar a priodade dele ou de qualquer outro processo
USER comum apenas pode aumentar o numero
do valor DEZ para +19
ou seja tornar a tarefa dele menos importante




no top
a letra r de renice


nice --20 vim & ou com positivo
nice -+20 vim &
renice -2 3442



Servidor X

O suporte a geracao de imagens eh preciso do servidor grafico
Porem o servidor X apenas vai gerar a aplicacao grafica
Entao precisamos iniciar um terminal de texto e nele iniciar o terminal grafico


A forma mais simples seria transitar do terminal de texto para grafico
O Display Manager vai gerenciar esse login e inicio de sessoes

Gnome 3 e Unity sao parecidos
Gnome fallback parece com o gnome 2

Mate=Gnome 3
Cinnamon=Gnome 2

XFCE e LXDE sao as leves
FluxBox mais pobre

Endereco para interfaces

http://xwinman.org lista de IG

As versoes
Window Manager
Apenas oferecem o gerenciador de janelas

Suite Desktop contem o criador de janelas e aplicacoes proprias da interface gedit gpaint

todos esses sao Window Manager

Display Manager
Gerencia o login do Windows Manager das sessoes

XDM GDM KDM lightDM


apt-get install xorg xdm
X -version
O comando acima instala o servidor X

apt-get install xfce4 lightdm
No comando acima o xfce4 que eh um display manager jah inclui o servidor X

Comandos uteis do Xorg Server

O servidor X configura todo o hardware automaticamente
ON THE FLY e nao cria arquivos

Para passarmos um novo drive de video por exemplo precisamos precisamos criar o

Xorg -configure
gera o arquivo
cat /root/xorg.conf.new

cp /root/xorg.conf.new /etc/X11/xorg.conf

startx


BIBLIOTECAS 

ESTATICAS
cada programa roda uma instancia dela
COMPARTILHADA
Varios programas usando a mesma instancia carregada  na memoria


boas praticas práticas
etc bashrc
alias rm="rm -i"

yum install -y rrdtool-devel python-devel openssl-devel geoip-devel gdbm-devel zlib-devel subversion libpcap-develyum install libtool automake autoconf wget gcc gcc-c++ make

yum groupinstall 'Development Tools'

$ wget -r -l1 -A.bz2 http://aaa.com/directory
In the above example, "-r" and "-l1" options together enable 1-level deep recursive retrieval, and "-A" option specifies lists of file name suffixes to accept during recursive download (".bz2" in this case).


yum provides * /lsb_release
yum install redhat-lsb





Squid
#### Regra Temporaria IS### Remover apos 13-12-2017
iptables -I FORWARD -s 172.16.1.96 -j ACCEPT
iptables -t nat -I PREROUTING -s 172.16.1.96 -j ACCEPT
iptables -t mangle -I PREROUTING -s 172.16.1.96 -j MARK --set-mark 2
iptables -t mangle -I  OUTPUT  -s 172.16.1.96 -j MARK --set-mark 2
iptables -t mangle -I FORWARD -s 172.16.1.96 -j MARK --set-mark 2
iptables -t mangle -I POSTROUTING -s 172.16.1.96 -j MARK --set-mark 2


https://docs.oracle.com/cd/E19122-01/j4500.array/820-3163/bcghjife.html

https://docs.oracle.com/cd/E52668_01/E54669/html/ol7-s20-storage.html


http://www.haproxy.org/


database linux

https://books.google.com.br/books?id=TszUjeyeo8cC&pg=PA192&lpg=PA192&dq=linux+with+large+databases&source=bl&ots=kcIdcbiK6y&sig=Ck2H-AFkKkc5IRH0TUDRLI7rdC8&hl=pt-BR&sa=X&ved=0ahUKEwjTiOOM1urXAhUGhpAKHSR9CP8Q6AEIcjAJ#v=onepage&q=linux%20with%20large%20databases&f=false
  
2

Jeff Muniz
Sat, Apr 20, 2019, 9:27 AM
RASCUNHO

Jeff Muniz <jmuniz1985@gmail.com>
Sat, Dec 26, 2020, 2:09 AM
to me

curAndroid_File_Transfer-5bf7652-x86_64.AppImage


ANDROID
https://www.omgubuntu.co.uk/2017/11/android-file-transfer-app-linux
https://github.com/whoozle/android-file-transfer-linux/releases/download/v3.7/Android_File_Transfer-5bf7652-x86_64.AppImage


Projeto do Netdata:
https://github.com/firehol/netdata

Site do Netdata:
https://my-netdata.io

Projeto Giropops-Monitoring:
https://github.com/badtuxx/giropops-m... 



 

DISPLAY=:0 xclip -sel clip < beta.hosts  
...

[Message clipped]  View entire message
Compose:
New Message
MinimizePop-outClose
Displaying 71532192415219.png.







LPI - 451 - backupdexter.sh - 4 Linux 451



﻿#!/bin/bash
 
# Senha do Usuário do MySQL.
USUARIO="suporte"
 
#Senha do Usuário MySQL.
SQLPW="4linux"
 
DIRBKP="/tmp"
 
DIA=$(date +"%Y-%m-%d")
 
# Variavel que imprime o inicio do Backup.
INICIO=$(date +"%H:%M:%S")
 
# Execução do Backup.
/bin/tar -czf ${DIRBKP}/${1}-${DIA}-${INICIO}.tar.gz ${1} > /dev/null 2>&1
 
# Variavel que exibe o fim do Backup.
FIM=$(date +"%H:%M:%S")

         if [ -e "${DIRBKP}/${1}-${DIA}-${INICIO}.tar.gz" ]; then
                 /usr/bin/mysql -u${USUARIO} -p${SQLPW} -e "INSERT INTO backup.log (inicio,fim,server,arquivo,status) VALUES ('${DIA} ${INICIO}','${    DIA} ${FIM}','${HOSTNAME}','${DIRBKP}/${1}-${DIA}-${INICIO}.tar.gz','OK')" > /dev/null 2>&1
         else
                 /usr/bin/mysql -u${USUARIO} -p${SQLPW} -e "INSERT INTO backup.log (inicio,fim,server,arquivo,status) VALUES ('${DIA} ${INICIO}','${    DIA} ${FIM}','${HOSTNAME}','${DIRBKP}/${1}-${DIA}-${INICIO}.tar.gz','FAIL')" > /dev/null 2>&1
          fi



############




23. Qual comando abaixo pode ser usado para instalar o pacote sudo_1.7.4p4-5_i386.deb ? 

 apt-get install sudo_1.7.4p4-5_i386.deb
 dpkg -i sudo_1.7.4p4-5_i386.deb ok
 apt-install sudo_1.7.4p4-5_i386.deb
 aptitude sudo_1.7.4p4-5_i386.deb
PID 0 There are two tasks with specially distinguished process IDs: swapper or sched has process ID 0 


Em um sistema típico IDE, indique partições e dispositivos que são considerados válidos. 

 /dev/hdd6
 /dev/hde2
 /dev/hda4
 /dev/hdc

 find / -perm +4000 -exec ls -ld {} \;


Qual resultado do comando abaixo ? 

[daniel@lpi ~]$ sort -ur lista.txt | tr 'a-z' 'A-Z' 


28. Qual dos comandos abaixo podem ser usados para checar possíveis problemas em um sistema de arquivos do tipo Ext2 ? 

 e2fsck
 ext2.fsck
 fsck
 fsck.ext2

Qual a correta sintaxe do comando man quando desejamos exibir uma página de manual que pertence a seção de número 6 ? 

 man -s 6 games
 man -w 6 games
 man -D 6 games
 man -k 6 games

grep -v '^#' /etc/services
sed -e “/^#/d” /etc/services
egrep '^[^#]' /etc/services


Contém uma lista de diretórios para procura de bibliotecas ELF em tempo de execução

 Link simbólicos quando criados possuem a permissão rwxrwxrwx e podem ser apagados por qualquer usuário do sistema.

 Qual deve ser o valor de umask quando desejamos criar novos diretórios com a permissão inicial 0700 ?

 /bin/ls -l /etc >&1

34. Qual o comando que é possível alterar o modo de edição do shell Bash para o modo de edição no estilo vi ? 


GTK+ esta em ligtk como saber disso?

como chegou ao autogen?
cd /usr/local/src/ntop<TAB>
3#less INSTALL
4#./autogen.sh
You must have libtoo

estudar xdump
e
od

compilacao


ronaldo s souza@gmail.com

30/07 Inicio 451

servidor da apostila
http://192.168.1.1

alias
servidor cloud
alias sshcloud="ssh root@200.100.1.42"
alias sshred="ssh root@192.168.201.190"
alias sshaudit="ssh root@192.168.201.191"
alias sshfile="ssh root@192.168.201.192"
export EDITOR=vim

DICAS
john t riper quebrar senha /etc/passwd
http://caspian.dotconf.net/menu/Software/SendEmail/

CAI na LPI
ordem dos campos no /etc/shadow


##################################3

Compactadores empacotadores 

find /etc/|cpio -ov>/backup/bkp_etc.cpio

cpio -o empacota -v verbose
cpio -t < /backup/bkp_etc.cpio
cpio -iv --list < /backup/bkp_etc.cpio
mv /etc/ /tmp
cpio -iv </backup/bkp_etc.cpio

cpio -iuv
opcao i extrair e 
-u sobrescreve

tar 
copiar apenas arquivos modificados

tar -tf bkp_home.tar 
opcao -t lista os conteudo do pacote tar


Empacotar
time tar -cvf bkp_etc.tar /etc
time mostra o tempo
c cria arquivo
v verbose
f arquivo


Compactar
time tar -cvzf bkp_etc.tar /etc/
gzip
time tar -cvjf bkp_etc.tar /etc/
bzip2

Descompatar outra pasta
 tar xvf bkp_etc.tar -C /supasta/aqui

gzip bkp_home.tar   41  30-06-2014 20:27- gunzip bkp_home.tar.gz

bzip2 bkp_home.tar 
bunzip2 -v bkp_home.tar.bz2 

hostname -s
date +%d-%m-%y

tar cvfj /tmp/bkp_etc_$(hostname -s)_$(date +%d-%m-%y).tar.bz2 /etc/

ls /tmp
bkp_etcwebserverinterno_30-06-14.tar.bz2


Agendamento de tarefas

CRON
e
AT

/etc/cronatb
crontab do sistema

crontab -e cria o crontab do usuario
os valores coringas do crontab caem na LPI

lista de valores com virgula
1,3,9

intervalos com hifen
1 - 15 todas horas do dia 
0 - 23 dia todo

pulos de iontervalo com barra /
*/4
4 8 12 16

Exemplo doarquivo

* * * * * root /bin/ls
minuto
hora
dia do mes
mes
dia da semana
usuario
comando

date 
Ter Jul  1 19:05:03 BRT 2014
at 19:20 07/01/2014
at> tar cjf /backup/etc.bkp.tar.bz2 /etc/
at> CTRL+D
job 1 at 2014-07-01 19:20
atq


at -rm

/etc/cron.allow
/etc/cron.deny


SHELL SCRIPTS

comando
A=10
B=20
expr $A + $B
operacoes matematicas simples
a mesma coisa
echo "resultado de a mais b" $(($A + $B))
Resultado 30

script1
1 #!/bin/bash
  2 USUARIO=aluno
  3 echo $USUARIO
  4 A=10
  5 B=20
  6 echo "SOMA eh `expr $A + $B`" # usa a crase como subshell
  7 echo "soma eh $(expr $A + $B)" #usa $()  para subshel
  8 echo 'soma eh $(expr $A + $B)'  # mostra literal o valor dentro de aspas simples
root# ./script1.sh 
aluno
SOMA eh 30
soma eh 30
soma eh $(expr $A + $B)


script2
 #!/bin/bash
  2 LISTA=$(awk -F: '$3 >= 500 {print $1}' /etc/passwd)
  3 TODAY=$(date +%d-%m-%y)
  4 #User com UID maior que 500
  5 tar cjf /backup/backup-home-$TODAY.tar.bz2 /home


Variaveis especiais

$0 guarda o nome do script 
e de $1 em diante e o parametro do comando


#!/bin/bash
echo "Os parametros passados sao $*"
echo "A quantidade de parametros passados eh $#"
echo "O nome do script e $0"

 ./script4.sh jeff muniz moura
Os parametros passados sao jeff muniz moura
A quantidade de parametros passados eh 3
O nome do script e ./script4.sh


Exit status
$?
 echo $?
0
root@webserverinterno:/etc# 4linux
-bash: 4linux: comando não encontrado
root@webserverinterno:/etc# echo $?
127

No man bash 127 e o comando nao encontrado

test "cruso" = "cruso" ; echo $?
test 7 -eq 7 ; echo $?

Ambos sao iguais uma para literal outro para numeral

condicional IF
1 #!/bin/bash
  2 echo "Digite o nome do userpara consulta:"
  3 read USUARIO
  4 RESULTADO=$(getent passwd|grep $USUARIO | cut -d: -f1 | grep -v operator)
  5 test -z $RESULTADO
  6 if [ $? -eq 0 ] ;then
  7 echo "O usuario $USUARIO nao existe!"
  8 else
  9 echo "O usuario existe"
 10 fi

onde getent eh o mesmo que cat
cut -d:
delimitador eh o dois pontos
cut -f1
delimitaa no campo um filed=1

grep -v operator
acha qualquer coisa menos o valor operator

Condicional FOR

cat seq.sh 
#!/bin/bash
for i in $(seq 1 10);do
echo $i
done


Padrao C++
 1 #!/bin/bash
  2 echo "for Estilo SHELL"
  3 for i in $(seq 1 10);do
  4 echo $i
  5 done
Padrao C++
  6 echo -e "\nFor estilo C:"
  7 for ((i=0; i<=10; i++)); do
  8 echo $i
  9 done


Condicional case

root@webserverinterno:~/shell# cat case
#!/bin/bash
Start (){
	echo "Starting servic JEFF"
}
Stop (){
	echo "Stop servico JEFF"
}
case $1 in
	start) Start ;;
	stop) Stop ;;
	restart)Stop && Start ;;
	*) echo -e "execute: /etc/init.d/$0 {start|stop|restart}"
esac
root@webserverinterno:~/shell# ./case restart JEFF
Stop servico JEFF
Starting servic JEFF

WHILE


cat while
#!/bin/bash
i=1
while [ $i -le 10 ]; do
clear
echo $i
sleep 1
i=$((i+1))
done




 
Capacity plain

comando 
uptime


Os tres ultimos campos com 0.00
significam
carga no ultimo 1  minuto
carga no ultimo  5 minuto
carga no ultimo  15 minuto

O valor mostrado deve ser sempre igual ou menor ao numero de processadores

vmstat -s
vmstat 1 10

10 linhas de um em um segundo


pidstat -p $(pgrep cron)
pgrep = ps aux com grep


habilitar sysstat no debian
vim /etc/default/sysstat 
para true

sar -u 5 3
3 linhas 
em 15 segndos

Teste de stress
ferramenta stress
people.seas.harvard.edu/ap/stress

CPU
iostat -c;uptime
stress --cpu 2k --timeout 30
iostat -c;uptime


watch -n 1 vmstat
repete o comando vmstat de um em um segundo

MEMORIA
stress --vm 2 --timeout 15



Administracao de usuario
pwunconv -nativo Linux
desativa o /etc/shadow e manda o cache da senha para o 
/etc/passwd
pwconv 
habilita o /etc/shadow


adduser --force-badname bryan.leah
userdel -r bryan.leah

modificar usuarios
usermod -m -d /srv/homes/suporte suporte
-m cria o diretorio
-d indica que e o /home do usuario

Criando pastas padrao
mkdir /etc/skel/{Documentos, Downloads, Imagens}

adduser --force-badname --home /srv/homes/bryan.leah bryan.leah

scripting para adicionar usuarios

cat funcionarios.txt 

Casey Milo;Diretoria
Annie Dee;Vendas
Grace Kenny;Vendas
Antony Brooks;Financeiro
Fox Bennett;Financeiro
Harry Rosemberg;Infra
Voce Sobrenome;Infra


cd /root

root@fileserver:~# cat createuser.sh 
#!/bin/bash
# Script para Criar os Usuarios da Dexter

#Checa se o Arquivo de Funcionarios Existe
test -e /root/funcionarios.txt || exit 

while read FUNCIONARIO
do
  NOME=$(echo $FUNCIONARIO | cut -d";" -f1 | tr [:upper:] [:lower:] | tr " " ".")
  DEPARTAMENTO=$(echo $FUNCIONARIO | cut -d";" -f2)

  useradd -m -b /srv/homes -c "$FUNCIONARIO" -s /bin/bash $NOME
  echo -e "dexter\ndexter" | passwd $NOME

done < /root/funcionarios.txt

  echo dexter | tr "e" "&"
cat funcionarios.txt |tr ";" "\n"
 cat funcionarios.txt |tr ";" "\W"
  cat funcionarios.txt |tr ";" "\t"
 cat funcionarios.txt |tr ";" "\t" >funcionarios.txt 
 tr [:upper:] [:lower:] < funcionarios.txt 
 cut -d";" -f1 funcionarios.txt 
cut -d "\t" -f1 funcionarios.txt 
 tr " " ";" < funcionarios.txt 
   tr "\t" ";" < funcionarios.txt 
 cat funcionarios.txt | tr "\t" ";" > funcionarios2.txt 
    vim funcionarios2.txt 
    mv funcionarios2.txt funcionarios.txt 
  cut -d ";" -f1 funcionarios.txt 


    cut -d ";" -f1 funcionarios.txt | tr [:upper:] [:lower:] | tr " " "."


finger bryan.leah
chage -l bryan.leah

chage -M 30 -m 1 bryan.leah 
chage -l bryan.leah 

-M significaos dias que expira a senha
-m significa o numero de dias que ele nao pode efetuar a troca da senha apos ter alterado 
-L  Bloqueia o user
-U desbloqueia


passwd -l
LOCK no user
passwd -u 
UNLOCK no user

Adicionando user ao grupo
gpasswd -a grace.kenny vendedores

chwon bryan.leah Diretoria
Deixa o user bryan como dono da pasta Diretoria

chgrp diretores Diretoria
deixa o grupo diretores como dono da pasta Diretoria

Permissoes

4 Leitura 
2 Escrita
1 Execucao



Todas as pastas vao ter
d rwx rwx ---
chmod u=rwx,g=rwx,o= Diretoria
chmod g+w,o-rx
chmod g+w,o-rx Financeiro/
chmod g+w,o-rx Financeiro
chmod ug=rwx,o= Infra
chmod 770 Vendas

Para diretorios o direito de execucao (x)
para poder acessar, mesmo tendo R e W


umask 0022
permissao total                                     0777
umask                                                 -0022
Permissao padaro de dir                        0755
para arquivos tira 1 (x) dos impares        -1111
Permissao padrao de arquivos                0644  


Permissoes de arquivos no UMASK
sempre  numeros pares


arquiqvo
pasta

 0421
\

777
022

755
644



 
PAM

classes sao os tipos

account 
authentication
password 
session 

 vim /srv/homes/antony.brooks/.profile 
vim /etc/skel/.profile 
 touch arq.1
    for FUNCIONARIO in $(ls /srv/homes/)
 do cp -v /etc/skel/.profile /srv/homes/$FUNCIONARIO/; done

 chmod 2770 Financeiro Infra Vendas   13  07-07-2014 19:42- chmod g+s Diretori
 chmod g+s Diretoria
chmod 2770 *

chmod 2770 * -R
cat /srv/homes/antony.brooks/.profile |grep umask

chmod o+t Diretoria   26  07-07-2014 19:56- chmod 3770 Financeiro 
 chmod 3770 Vendas/ Infra/
chmod u+s /sbin/ifconfig 
 chmod u-s /sbin/ifconfig 
gpasswd -a dexter diretores
 gpasswd -a dexter financeiro 
 gpasswd -a dexter vendedores 
 gpasswd -a dexter analista
 gpasswd -a dexter analistas

 ls /etc/skel/
ldd pam_vbox.so 
 ldd /bin/login
ldd /bin/login|grep pam
vim /etc/security/time.confman pam_time
vim +9 /etc/pam.d/sshd
 rm -rf /etc/nologin 
 vim /etc/security/time.conf 




FORK BOMB
bomb () { bomb | bomb & } ; bomb


Controle de acesso com sudo



vim +24 /etc/sudoers

# Allow members of group sudo to execute any command
%sudo   ALL=(ALL:ALL) ALL
%analistas ALL=(root) NOPASSWD: ALL



QUOTA de DISCOS
Cai na LPI a ordem dos comandos no fstab
/srv	ext4	defaults,usrjquota=aquota.user,grpjquota=aquota.group,jqfmt=vfsv0 0 0
quotaon 
quotaoff 


INODE

Cada arquivo tem um INODE unico
HARD LINK tem o mesmo INODE do ARQUIVO REAL
SYM LINK tem um INODE gerado para o LINK

criacao de link simbolico
ln -s




/etc/adduser.conf



NTP
ntpdate-debian


rsyslog
cai na lpi

servidor de logs

last
binario le o wtmp
lastlog
binario leo lastlog
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$
$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$$


sudo su -
hrlm
kubectl
sudo su -