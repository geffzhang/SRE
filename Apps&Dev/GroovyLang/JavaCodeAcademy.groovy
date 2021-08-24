JAVASCRIPT - programacao SENAI e PMG
https://www.youtube.com/watch?v=NZ937D7Jzsw&index=5&list=PL1EkVGo1AQ0Hsqhvjm4khfp6innDjpj9J
programacao SENAI e PMG
http://eadsenaies.com.br/logica-de-programacao/
http://eadsenaies.com.br/cursos/mod/scorm/player.php?a=8&currentorg=ORG-D5E0CB9FFCBBA4BC21FE87C5EDEAB116&scoid=16
pmg ACADEMY
http://www.pmgacademy.com/ead/my/
WEB html css js - codecademy.com
http://tableless.com.br/como-usar-gradient-no-css-de-forma-consciente/
jogo java
https://www.youtube.com/watch?v=NZ937D7Jzsw&index=5&list=PL1EkVGo1AQ0Hsqhvjm4khfp6innDjpj9J
http://www.escolafreelancer.com/sites-cursos-e-aplicativos-para-aprender-programacao/
http://www.w3schools.com/css/css_syntax.asp


##Codes for the game
<!doctype html>
<html lang="pt">
	<head>
	<meta charset="utf-8" />
			<title>EnginE</title>
			<style>
				canvas {
					position: absolute;
					top: 0px;
					bottom: 0px
					left: 100px;
					right: 350px;
					margin: auto;
					
					}
			</style>	
	</head>
<body>
	<script> 
			
		var canvas, ctx, ALTURA, LARGURA, frames = 0, maxPulos = 3, velocidade = 6,
		  			chao = {
						y: 500,
						altura: 50,
						cor: "#ffdf70",

						desenha: function (){
						ctx.fillStyle = this.cor;
						ctx.fillRect(0, this.y, LARGURA, this.altura);
					 }
					},
		bloco = {
			x: 50,
			y: 50,
			altura: 50,
			largura: 50,
			cor: "#000000",
			gravidade: 1.5,
			velocidade: 0,
			forcaDopulo: 23,
			qtdPulos: 0,


		atualiza: function(){
						this.velocidade += this.gravidade;
						this.y +=  this.velocidade;

						if (this.y > chao.y - this.altura){
							this.y = chao.y - this.altura;
							this.qtdPulos = 0;
						}
						},
		pula: function(){
				if (this.qtdPulos < maxPulos){
					this.velocidade =- this.forcaDopulo;
					this.qtdPulos ++;		
				}		
				},


		desenha: function(){
							ctx.fillStyle = this.cor;
							ctx.fillRect(this.x, this.y, this.largura, this.altura);
					}
				};	
			

obstaculos = {
	_obs:  [],
	cores: ["#00CC00", "#000066", "#33CCFF", "##4A766E", "4F4F2F", "#9F9F5F", "#FFCCFF", "#66FF99", "#66FFFF", "#FF99FF", "#33ff33", "#33CCFF", "#CC66CC", "#00CC00", "#3366FF", "#CC33CC"],

		insere: function() {
			this._obs.push({
				x: LARGURA,
				largura: 30 + Math.floor(21 * Math.random()),
				altura:  30 + Math.floor(120 * Math.random()),
				cor: this.cores[Math.floor(16 * Math.random())]
							});
						},
				
	atualiza: function() {
		for (var i = 0, tam = this._obs.length; i < tam; i++){
			var obs=this._obs[i];
			obs.x -= velocidade
		}

		},

	desenha: function() {
		for (var i = 0, tam = this._obs.length; i < tam; i++){
			var obs = this._obs[i];
	ctx.fillStyle = obs.cor;
	ctx.fillRect(obs.x, chao.y - obs.altura, obs.largura, obs.altura);
		}
	}
};





			function click(event){
				bloco.pula();
				//alert("clicou")
			}

			function main(){
				ALTURA =  window.innerHeight;
				LARGURA = window.innerWidth;

				if (LARGURA >= 500){
					LARGURA = 600;
					ALTURA = 550;
				}
			canvas = document.createElement("canvas")
				canvas.width = LARGURA;
				canvas.height = ALTURA;
				canvas.style.border = "2px solid #000";
				ctx = canvas.getContext("2d")
				console.log(ctx); // CanvasRenderingContext2D { ... }
				document.body.appendChild(canvas);
				document.addEventListener("mousedown", click);
				roda();
			}
			function roda(){
					atualiza();
					desenha();

					window.requestAnimationFrame(roda);

			}
			function atualiza(){
				frames++;
				bloco.atualiza();
			}
			function desenha(){
			ctx.fillStyle = "#50BEFF";
			ctx.fillRect(0, 0, LARGURA, ALTURA)	;

			chao.desenha();
			obstaculos.desenha();
			bloco.desenha();

			}
			//inicializa o jogo
			main();
		</script>
</body>
</html>





