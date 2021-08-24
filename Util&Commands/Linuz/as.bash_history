sudo su -
hrlm
kubectl
sudo su -
curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
chmod +x ./kubectl
sudo mv ./kubectl /usr/local/bin/kubectl
sudo su - 
curl -LO https://storage.googleapis.com/kubernetes-release/release/`curl -s https://storage.googleapis.com/kubernetes-release/release/stable.txt`/bin/linux/amd64/kubectl
chmod +x ./kubectl
sudo mv ./kubectl /usr/local/bin/kubectl
kubectl version --client
export KUBECONFIG=/home/valdis/kube.conf 
vim
vim /home/valdis/kube.conf 
cd /home/
ls
mkdir valdis
ls l-ha
ls -lha
sudo touch /home/valdis/kube.conf
sudo mkdir -P /home/valdis/kube.conf
sudo mkdir -R /home/valdis/kube.conf
sudo mkdir -p /home/valdis/kube.conf
cd valdis/
ls
cd kube.conf/
cd ..
rmdir kube.conf/
sudo rmdir kube.conf/
vim kube.conf
cd ..
sudo rmdir -p valdis/
ls -lhar
cd jmuniz/
ls
touch kube.config
vim kube.config 
export KUBECONFIG=/home/jmuniz/kube.conf 
set
set|grep kube
export KUBECONFIG=/home/jmuniz/kube.conf 
set|grep kube
unset KUBECONFIG=/home/jmuniz/kube.conf 
set|grep kube
kubectl cluster-info
cat /home/jmuniz/kube.conf
set|grep kube
ls
cd ..
ls
ls -lhanrp
cat /home/jmuniz/kube.conf
ls jmuniz/
cd jmuniz/
mv kube.config kube.conf
kubectl cluster-info
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
touch test
vim test
ls
pwd
curl -LO "https://dl.k8s.io/release/$(curl -L -s https://dl.k8s.io/release/stable.txt)/bin/linux/amd64/kubectl"
sudo install -o root -g root -m 0755 kubectl /usr/local/bin/kubectl
kubectl version --client
kubecofig
kube config
kube onfig
kube
kube config
kubectl config
sudo apt-get update
sudo apt-get install -y apt-transport-https ca-certificates curl
sudo curl -fsSLo /usr/share/keyrings/kubernetes-archive-keyring.gpg https://packages.cloud.google.com/apt/doc/apt-key.gpg
echo "deb [signed-by=/usr/share/keyrings/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list
sudo apt-get update
sudo apt-get install -y kubectl
kubectl
wget https://get.helm.sh/helm-v3.4.1-linux-amd64.tar.gz
tar xvf helm-v3.4.1-linux-amd64.tar.gz
sudo mv linux-amd64/helm /usr/local/bin
helm version
rm helm-v3.4.1-linux-amd64.tar.gz
rm -rf linux-amd64
kubectl get pod -A |grep post
kubectl config get-contexts
helm
kubectl
history
ls -lahr
cat bash_history
cat .bash_history
code .
