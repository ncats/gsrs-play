1. [Create Azure account](https://azure.microsoft.com)
2. [Create resource group](https://docs.microsoft.com/en-us/azure/azure-resource-manager/manage-resource-groups-portal)
	- This will be the resource that you will select when you create all the needed resources
	- Note the selected region and make sure to select the same region for all resources that you will create 
3. [Create virtual network](https://docs.microsoft.com/en-us/azure/virtual-network/quick-create-portal#create-a-virtual-network)
	- Make sure to only follow steps for creating virtual network, not for creating virtual machine
	- Where it says __Service endpoints__, select __Enabled__ and check the following option:
		- Microsoft.Sql
		- Microsoft.Storage
4. [Create MySQL database](https://docs.microsoft.com/en-us/azure/mysql/quickstart-create-mysql-server-database-using-azure-portal#create-an-azure-database-for-mysql-server)
	- Make sure to save the password somewhere safe since you won't be able to access after you create the resource. If you lose it, you can only reset it
	- Select either __General Purpose__ or __Memory Optimized__ pricing tier so you have the option to add your database server to the virtual network
5. After the database server has deployed, [make sure to add your local IP address to your database server](https://docs.microsoft.com/en-us/azure/mysql/quickstart-create-mysql-server-database-using-azure-portal#create-an-azure-database-for-mysql-server) and then [follow the steps to connect to MySQL and create a database (running the query ```
CREATE DATABASE gsrsdb;```)](https://docs.microsoft.com/en-us/azure/mysql/quickstart-create-mysql-server-database-using-azure-portal#connect-to-mysql-by-using-the-mysql-command-line-tool)
6. [Add the database to the virtual network](https://docs.microsoft.com/en-us/azure/mysql/howto-manage-vnet-using-portal) you created in step 3
7. Increase the database server's __wait_timeout__ setting by going to __Server parameters__ (on the left) and increasing the value to the maximum (2147483)
8. [Create a Linux virtual machine](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/quick-create-portal)
	- Select Ubuntu Server 18.04 LTS as the image
	- Select a size with enough resources to support GSRS
	- Go to the __Advanced__ tab, then copy and paste the following code in  __Cloud init__, but make sure to make the following substitutions:
		- {AZURE_VM_USERNAME} - the same value entered in __Username__ under the __Administrator account__ section
		- {AZURE_APPLICATION_HOST} - this will be the URL host that you're going to use for your application. If you're planning to use the default given by Azure, it will be in this format: {AnyAvailableNameYouChoose}.{SelectedRegion}.cloudapp.azure.com (example: gsrs.eastus.cloudapp.azure.com)
		- {AZURE_APPLICATION_HOST_WITH_HTTPS} - same as above but prepend with "https://" (example: https://gsrs.eastus.cloudapp.azure.com)
		- {AZURE_DB_SERVER} - MySQL database server name created in step 4
		- {AZURE_DB_NAME} - MySQL database name created in step 4 (not the same as the server name unless you implicitly created both with the same name)
		- {AZURE_DB_DEFAULT_USER} - MySQL server admin login name created in step 4
		-  {AZURE_DB_DEFAULT_PASSWORD} - Password for the MySQL server admin login name created in step 4
```yaml
#cloud-config
package_upgrade: true
packages:
  - nginx
  - openjdk-8-jdk
  - unzip
write_files:
  - owner: www-data:www-data
    path: /etc/nginx/sites-available/default
    content: |
      server {
        listen 80;
        listen 443 ssl;
        server_name {AZURE_APPLICATION_HOST};
        ssl_protocols TLSv1.1 TLSv1.2;
        ssl_prefer_server_ciphers on;
        ssl_ciphers "EECDH+AESGCM:EDH+AESGCM:AES256+EECDH:AES256+EDH";
        ssl_ecdh_curve secp384r1;
        ssl_session_timeout 1d;
        ssl_session_cache shared:SSL:50m;
        ssl_stapling on;
        ssl_stapling_verify on;
        location / {
          proxy_pass http://localhost:9000;
          proxy_http_version 1.1;
          proxy_set_header Upgrade $http_upgrade;
          proxy_set_header Connection keep-alive;
          proxy_set_header Host $host;
          proxy_cache_bypass $http_upgrade;
        }
      }
  - owner: {AZURE_VM_USERNAME}:{AZURE_VM_USERNAME}
    path: /home/{AZURE_VM_USERNAME}/azureconf/azure.conf
    content: |
      include "ginas.conf"
      application.host="{AZURE_APPLICATION_HOST_WITH_HTTPS}"
      db.default.driver=com.mysql.jdbc.Driver
      db.default.url="jdbc:mysql://{AZURE_DB_SERVER}.mysql.database.azure.com:3306/{AZURE_DB_NAME}?useSSL=true&requireSSL=false"
      db.default.user="{AZURE_DB_DEFAULT_USER}@{AZURE_DB_SERVER}"
      db.default.password="{AZURE_DB_DEFAULT_PASSWORD}"
      ix.home="/home/{AZURE_VM_USERNAME}/ginasix"
      evolutionplugin=disabled
runcmd:
  - sudo mkdir /ginasix
  - cd /home/{AZURE_VM_USERNAME}
  - sudo wget https://tripod.nih.gov/ginas/releases/gsrs2_4_empty_h2.zip
  - sudo unzip gsrs2_4_empty_h2.zip
```
9. __(OPTIONAL)__ Create storage account and mount it to the VM to make the application more modular
	1. [Create storage account](https://docs.microsoft.com/en-us/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal)
		- Before you click on __Review + create__, follow these steps:
			1. click on the __Networking__ tab
			2. In __Connectivity method__, select __Public endpoint (selected networks)__
			3. Select the virtual network you created in step 3 and the default subnet
	2. After the storage account has deployed, [create a blob container](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-quickstart-blobs-portal#create-a-container)
	3. [Mount Blob storage to VM](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-how-to-mount-container-linux)
		- You can SSH to your virtual machine by accessing your cloud shell the same way you did in step nine and typing the following command: `ssh {AZURE_VM_USERNAME}@{IP_ADDRESS}`
		- You can get the IP address from the overview section of your virtual machine in the Azure portal
		- You can omit the step to [Create an empty directory for mounting](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-how-to-mount-container-linux)
		- The empty directory is already created and it's in this path: /ginasix
		- You will need that path to do the final step of [mounting the drive](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-how-to-mount-container-linux#mount)
		- Also [during the final step of mounting the drive](https://docs.microsoft.com/en-us/azure/storage/blobs/storage-how-to-mount-container-linux#mount), you'll need to add `-o allow_other` to the command (so the full command will be `sudo blobfuse /ginasix --tmp-path=/mnt/blobfusetmp --config-file=/path/to/fuse_connection.cfg -o attr_timeout=240 -o entry_timeout=240 -o negative_timeout=120 -o allow_other`
10. Create database tables
	- This step will run evolutions and create table structure in your database. This is intended to run only on a new database. If you run this step on an existing database, it will wipe out all the data and create new tables. On a new database, this process will lead to warnings regarding unfound tables which can safely be ignored.
	- To do this, SSH to your virtual machine, CD to the admin's home directory, then CD into the __gsrs2_4_empty_h2__ directory, and run the following command `
sudo java -cp "lib/*" -Dconfig.file=../azureconf/azure.conf ix.ginas.utils.Evolution
`
11. [Set up a domain name](https://docs.microsoft.com/en-us/azure/virtual-machines/linux/portal-create-fqdn)
12. [Allow inbound internet traffic to your virtual machine](https://blogs.msdn.microsoft.com/pkirchner/2016/02/02/allow-incoming-web-traffic-to-web-server-in-azure-vm/)
	- You're going to want to open port 80, 8080 for HTTP and 443 for HTTPS
13. [Install certbot](https://certbot.eff.org/lets-encrypt/ubuntubionic-nginx) to make sure your application can handle secure communication via HTTPS
14. Run application
	1. SSH to your virtual machine
	2. CD to the admin's home directory, then CD into the __gsrs2_4_empty_h2__ directory
	3. Run this command `sudo nohup ./bin/ginas -mem 8068 -Dconfig.file=../azureconf/azure.conf -Djava.awt.headless=true -Devolutionplugin=disabled &`
