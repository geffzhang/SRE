
create database cadastro
#Define o idioma do banco
DEFAULT CHARACTER set utf8
#Define o template de banco de dados a ser criado
default collate utf8_general_ci;

use cadastro
create table pessoas (

id int NOT NULL AUTO_INCREMENT,

nome varchar(30) not null,
born date,
sexo enum('M','F'),
peso DECIMAL(5,2)
altura DECIMAL(3,2)
nacional varchar(20) default 'Brazil'

PRIMARY KEY (id)

) default charset = utf8;


####

insert into pessoas
#Essa linha pode ser omitida se a ordem dos campos for seguida na linha que implementa os dados
(id,nome,born,sexo,peso,altura,nacional)
VALUES
('1','anja','2000-12-31','M','65,7','1.67','Brasil');
#Com valores DEFAULT
('bia','2000-01-15','M','65,7','1.67');
(default,'Carla','1985-02-27','M','65,7','1.67',default);

#Ou seja:
INSERT INTO pessoas VALUES
(default,'Joao','1985-01-29','F','75,7','1.77',default),
(default,'Carla','1985-02-27','M','65,7','1.67',EUA),
('Carlos','1985-02-27','M','65,7','1.67',Portugal);
('Carla','1985-02-27','M','65,7','1.67');



