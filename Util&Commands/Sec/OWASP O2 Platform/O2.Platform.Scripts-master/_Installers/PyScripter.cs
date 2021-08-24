﻿using System.Diagnostics;
using FluentSharp.CoreLib;

//O2File:Tool_API.cs

namespace O2.XRules.Database.APIs
{
	public class PyScripter_Test
	{
		public static void test() 
		{
			new PyScripter().start(); 
		}
	}
	
	public class PyScripter : Tool_API 
	{				
		public PyScripter()
		{
			
			config("PyScripter", 
				   "http://pyscripter.googlecode.com/files/PyScripter-v2.5.3.zip".uri(),
				   "PyScripter\\PyScripter.exe");    		
			installFromZip_Web();	       		
		}	
		//http://download.codeplex.com/Download/Release?ProjectName=ironpython&DownloadId=352994&FileTime=129760937686670000&Build=18978
		
		public Process start()
		{
			if (this.isInstalled())
				return this.Executable.startProcess(); 
			return null;
		}		
	}
}