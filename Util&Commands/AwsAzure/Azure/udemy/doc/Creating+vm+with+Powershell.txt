Step1 : $cred = Get-Credential


Step2  :  $vm = New-AzVMConfig -VMName myVM -VMSize Standard_D1

Step3 : 
$vm = Set-AzVMOperatingSystem `
-VM $vm `
-Windows `
-ComputerName myVM `
-Credential $cred `
-ProvisionVMAgent -EnableAutoUpdate

Step4 :
$vm = Set-AzVMSourceImage `
-VM $vm `
-PublisherName MicrosoftWindowsServer `
-Offer WindowsServer `
-Skus 2016-Datacenter `
-Version latest

Step 5 :

$vm = Set-AzVMOSDisk `
-VM $vm `
-Name myOsDisk `
-DiskSizeInGB 128 `
-CreateOption FromImage `
-Caching ReadWrite

Before you get into step 6, Make sure you have a network card created in UI / Portal. This is shown in the video . 
Next, you need to copy the ID of the network card from properties of it. This is mentioned in the video at 6:20 . Please check that section carefully. 

The $nic.ID is replaced with ID property that you copied earlier. 

Step 6 :
$vm = Add-AzVMNetworkInterface -VM $vm -Id <$nic.Id>

Step 7 :
New-AzVM -ResourceGroupName myResourceGroupVM -Location EastUS -VM $vm

------------------------------------------------------------------------
Or your can also use the following script to create a VM 


# create a resource group
New-AzResourceGroup -Name rg-devteam -Location EastUS
# create the virtual machine
# when prompted, provide a username and password to be used as the logon
credentials for the VM.

New-AzVm `
-ResourceGroupName "rg-devteam" `
-Name "WebServer"`
-Location "East US" `
-VirtualNetworkName "myVnet" `
-SubnetName "mySubnet" `
-SecurityGroupName "myNetworkSecurityGroup" `
-PublicIpAddressName "myPublicIpAddress" `
-OpenPorts 80,3389
