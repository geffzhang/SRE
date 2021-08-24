alias aws="/usr/local/aws-cli/v2/2.2.5/bin/aws"
export AWS_CLI=/usr/local/aws-cli/v2/2.2.5
export PATH=$AWS_CLI/bin:$PATH
export EC2_AMITOOL_HOME=/usr/local/ec2/ec2-ami-tools-1.5.7
export PATH=$EC2_AMITOOL_HOME/bin:$PATH



bucket_name="s3migratebuck"
vm_image_name="VM-Import/bitnami-tinytinyrss-disk001.vmdk"

aws iam create-role --role-name vmimport --assume-role-policy-document "file://trust-policy.json"
aws iam put-role-policy --role-name vmimport   


aws ec2 import-image --description "centos7" --disk-containers "file://centos7.json"
aws ec2 import-image --description "windows10" --disk-containers "file://win10.json"


aws ec2 describe-import-image-tasks --import-task-ids "import-ami-03b2e8acc5853e77c"

sudo apt-get update 
sudo apt-get upgrade
sudo apt install npm
sudo apt install python3.8
sudo npm install -g wscat
sudo apt  install unzip -y
sudo apt  install gunzip -y
sudo apt  install vim* -y

wscat -c wss://3w0zg1oeh0.execute-api.us-east-2.amazonaws.com/production
{"action":"word"}

unzip awscliv2.zip
sudo ./aws/install
alias aws="/usr/local/aws-cli/v2/2.2.5/bin/aws" 
export AWS_CLI=/usr/local/aws-cli/v2/2.2.5
export PATH=$AWS_CLI/bin:$PATH
export EC2_AMITOOL_HOME=/usr/local/ec2/ec2-ami-tools-1.5.7
export PATH=$EC2_AMITOOL_HOME/bin:$PATH



bucket_name="s3migratebuck"
vm_image_name="VM-Import/bitnami-tinytinyrss-disk001.vmdk"

cat trust-policy.json
aws iam create-role --role-name vmimport --assume-role-policy-document "file://trust-policy.json"

cat role-policy.json
aws iam put-role-policy --role-name vmimport \
                        --policy-name vmimport \
                        --policy-document "file://role-policy.json"


aws ec2 import-image --description "centosv7" --disk-containers "file://win10.json"
aws ec2 describe-import-image-tasks --import-task-ids "import-ami-0d6db3a35d431e4e3"
aws ec2 describe-import-image-tasks









#create files

cat > "trust-policy.json" << "EOF"
{
   "Version": "2012-10-17",
   "Statement": [
      {
         "Effect": "Allow",
         "Principal": { "Service": "vmie.amazonaws.com" },
         "Action": "sts:AssumeRole",
         "Condition": {
            "StringEquals":{
               "sts:Externalid": "vmimport"
            }
         }
      }
   ]
}
EOF



cat > "role-policy.json" << "EOF"
{
   "Version":"2012-10-17",
   "Statement":[
      {
         "Effect":"Allow",
         "Action":[
            "s3:GetBucketLocation",
            "s3:GetObject",
            "s3:ListBucket" 
         ],
         "Resource":[
            "arn:aws:s3:::s3migratebuck",
            "arn:aws:s3:::s3migratebuck/*"
         ]
      },
      {
         "Effect":"Allow",
         "Action":[
            "ec2:ModifySnapshotAttribute",
            "ec2:CopySnapshot",
            "ec2:RegisterImage",
            "ec2:Describe*"
         ],
         "Resource":"*"
      }
   ]
}
EOF




