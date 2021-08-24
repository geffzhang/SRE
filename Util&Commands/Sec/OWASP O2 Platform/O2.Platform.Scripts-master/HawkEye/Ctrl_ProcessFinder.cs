// This file is part of the OWASP O2 Platform (http://www.owasp.org/index.php/OWASP_O2_Platform) and is released under the Apache 2.0 License (http://www.apache.org/licenses/LICENSE-2.0)
using System;
using System.Diagnostics;
using System.Windows.Forms;
using FluentSharp.CoreLib;
using FluentSharp.WinForms;
//O2File:WindowFinder.cs

namespace O2.XRules.Database.APIs
{	
	public class test_Ctrl_ProcessFinder
	{
		public void test()
		{
			var propertyGrid = "Process Finder (showing Selected Process)".popupWindow(200,400)
									.add_PropertyGrid();
			propertyGrid.insert_Above_ProcessFinder()
					    .onProcessChange((process)=> propertyGrid.show(process));
		}
	}
    public class Ctrl_ProcessFinder : WindowFinder
    {   
			
		public Ctrl_ProcessFinder()
		{
			
			
		}
		
		public Ctrl_ProcessFinder onProcessSelect(Action<Process> callback)
		{
			if(callback.notNull())
				this.ActiveWindowSelected += (sender, e)=> callback.invoke(this.TargetProcess);
			return this;
		}
		
		public Ctrl_ProcessFinder onProcessChange(Action<Process> callback)
		{
			if(callback.notNull())
				this.ActiveWindowChanged  += (sender, e)=> callback.invoke(this.TargetProcess);	
			return this;
		}
	}
	
	public static class Ctrl_ProcessFinder_ExtensionMethods
	{
		public static Ctrl_ProcessFinder insert_Above_ProcessFinder(this Control control)
		{
			return control.insert_Above(30).add_Control<Ctrl_ProcessFinder>().width(30).fill(false);
		}
		
		public static Ctrl_ProcessFinder add_ProcessFinder(this Control control, Action<Process> onProcessSelect)
		{
			return control.add_ProcessFinder(onProcessSelect, null);
		}
		public static Ctrl_ProcessFinder add_ProcessFinder(this Control control, Action<Process> onProcessSelect, Action<Process> onProcessChange )
		{
			return control.add_Control<Ctrl_ProcessFinder>().width(30).fill(false)
						  .onProcessSelect(onProcessSelect)
						  .onProcessChange(onProcessChange);
		}
		
	}
}