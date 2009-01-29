#region Using

using System.Reflection;
using System.Runtime.CompilerServices;
using System.Runtime.InteropServices;
using System;

#endregion

// Information about this assembly is defined by the following
// attributes.
//
// change them to the information which is associated with the assembly
// you compile.

[assembly: AssemblyTitle("System.Data.Hsqldb.Client")]
[assembly: AssemblyDescription("IKVM-based HSQLDB Data Provider for ADO.NET 2.0")]
#if DEBUG
[assembly: AssemblyConfiguration("debug")]
#else
[assembly: AssemblyConfiguration("retail")]
#endif
[assembly: AssemblyCompany("The HSQL Development Group")]
[assembly: AssemblyProduct("HSQLDB ADO.NET 2.0 Data Provider")]
[assembly: AssemblyCopyright("Copyright © 2001-2009, The HSQL Development Group")]
[assembly: AssemblyTrademark("HSQLDB")]
[assembly: AssemblyCulture("")]

// This sets the default COM visibility of types in the assembly to invisible.
// If you need to expose a type to COM, use [ComVisible(true)] on that type.
[assembly: ComVisible(false)]

[assembly: CLSCompliant(true)]

// The assembly version has following format :
//
// Major.Minor.Build.Revision
//
// You can specify all values by your own or you can build default build and revision
// numbers with the '*' character (the default):
[assembly: AssemblyVersion("1.8.0.10")]
[assembly: AssemblyFileVersionAttribute("1.8.0.10")]
