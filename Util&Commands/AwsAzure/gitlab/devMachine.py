nome = jeff
age  = 25
peso = 75.8


nome = input(' Qual é seu nome?')
age  = input(' Qual é sua idade?')
peso = input(' Qual é seu peso?')

print (nome, age, peso)


msg = ('Variável')
print msg


nome = input('Digite seu nome:')
print('Prazer te conhecer, {}!'.format(nome))

#Por padrão o input trata a entrada como  string 
n1 = input('Digite um número')
n2 = input('Digite mais um número')
#Soma ai inves de concatenar como acima
n1 = int(input('Digite um número'))
n2 = int(input('Digite mais um número'))
 
s = n1+n2
print ( 'A soma entre', n1, 'e', n2, 'Resulta em:')

#Usando o método
print('Resultado foi{}'.format(s))

print('A soma entre {} e {} Resulta em {}'.format(n1, n2, s))

#Métodos que validam as propriedades de uma string, por padrão o input trata a entrada como  string 
n = input('Digite um número')
print(type(n))
print(n.isalpha())
print(n.isupper())
print(n.isalnum())


#outro exemplo



a = input('Digita algo: ')
print('O tipo primitivo do valor é: ', type(a))













