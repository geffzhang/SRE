terraform {
   required_providers {
    docker = {
       source  = "kreuzwerker/docker"
       version = "2.11.0"
    }
  }
}

provider "docker" {
    host = "unix:///var/run/docker.sock"
}

resource "docker_image" "nginx" {
  name = "nginx:latest"
}

resource "docker_container" "nginx-server" {
  name = "nginx-server"
  image = "${docker_image.nginx.latest}"
  ports {
    internal = 8080
    external = 80
  }
  volumes {
    container_path  = "/usr/share/nginx/html"
    host_path = "/etc/nginx/sites-enabled"
    read_only = true
  }
}
