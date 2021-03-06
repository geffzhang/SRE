//O2Tag:SkipGlobalCompilation
using System;
using System.Collections.Generic;
using System.Linq;
using System.Windows.Forms;
using System.Reflection;
using System.IO;

namespace O2.AutoGeneratedExe
{
    class Program_O2_Created
    {
        /// <summary>
        /// The main entry point for the application.
        /// </summary>
        
        static void Main(string[] args)
        {
            setAssemblyResolver();                        
            new Program_O2_Created().invokeMain(args);
            
        }

        public void invokeMain(string[] args)
        {            
            mainInvoker.Main(args);
        }

        public static void setAssemblyResolver()
        {
            AppDomain.CurrentDomain.AssemblyResolve += new ResolveEventHandler(AssemblyResolve);			
        }

        public static Assembly AssemblyResolve(object sender, ResolveEventArgs args)
        {
            var nameToFind = args.Name;
            if (nameToFind.IndexOf(",") > -1)
                nameToFind = nameToFind.Substring(0,nameToFind.IndexOf(","));
            var targetAssembly = Assembly.GetExecutingAssembly();           
            foreach (var resourceName in targetAssembly.GetManifestResourceNames())
                if (resourceName.Contains(nameToFind))
                {
                    var assemblyStream = targetAssembly.GetManifestResourceStream(resourceName);
                    byte[] data = new BinaryReader(assemblyStream).ReadBytes((int)assemblyStream.Length);
                    return Assembly.Load(data);
                }
            return null;
        }        
    }
}
