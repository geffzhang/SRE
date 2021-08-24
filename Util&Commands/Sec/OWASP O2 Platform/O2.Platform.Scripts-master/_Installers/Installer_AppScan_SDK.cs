﻿using System.Diagnostics;
using FluentSharp.CoreLib;

//O2File:Tool_API.cs

namespace O2.XRules.Database.APIs
{
	public class Installer_AppScan_SDK_Test
	{
		public void test()
		{
			new Installer_AppScan_SDK().start();
		}
	}
	public class Installer_AppScan_SDK : Tool_API 
	{			
		
		public Installer_AppScan_SDK()
		{
			config("AppScanSDK", 
				   "http://public.dhe.ibm.com/software/dw/rational/zip/AppScanSDK.zip".uri(),
				   "AppScanSDK.chm");
			install();		
		}
		
		
		public bool install()
		{
			if (isInstalled().isFalse())
			{
				"Installing {0}".info(ToolName);
				return installFromZip_Web(); 						
			}
			return true;
		}
		
		public Process start()
		{
			if (install())
				return Executable.startProcess();
			return null;
		}		
	}
}