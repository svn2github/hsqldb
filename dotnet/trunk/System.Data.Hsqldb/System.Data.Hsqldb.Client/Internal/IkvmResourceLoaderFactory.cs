#region licence

/* Copyright (c) 2001-2008, The HSQL Development Group
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this
 * list of conditions and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice,
 * this list of conditions and the following disclaimer in the documentation
 * and/or other materials provided with the distribution.
 *
 * Neither the name of the HSQL Development Group nor the names of its
 * contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL HSQL DEVELOPMENT GROUP, HSQLDB.ORG,
 * OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL,
 * EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO,
 * PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#endregion

#region Using
using System;
using System.Collections.Generic;
using System.Diagnostics;
using System.Globalization;
using System.Reflection;

using ClassLoader = java.lang.ClassLoader;
using HashSet = java.util.HashSet;
using Set = java.util.Set;
using URL = java.net.URL;
using URLEncoder = java.net.URLEncoder;
using URLClassLoader = java.net.URLClassLoader; 
#endregion

namespace System.Data.Hsqldb.Client.Internal
{
    #region ResourceLoaderFactory
    
    /// <summary>
    /// A helper class for HSQLDB res: protocol database instances.
    /// </summary>
    /// <remarks>
    /// <para>
    /// Implements a facility to wrap the closure of the assemblies provided
    /// by an initial list in a <c>URLClassLoader</c> that may then be used to
    /// retrieve IKVM protocol resources.
    /// </para>
    /// <para>
    /// Note that a current compile-time limitation of .NET/IKVM is that
    /// declared assembly references are not recorded for runtime use
    /// unless there is also at least one line of code in the referencing
    /// assembly that makes an actual, explicit reference to code in the
    /// referenced assembly.
    /// </para>
    /// </remarks>   
    internal static class IkvmResourceLoaderFactory
    {
        #region CreateLoader(List<Assembly>)
        /// <summary>
        /// Creates a new <c>java.net.URLClassLoader</c> that wraps the
        /// resource URL closure of the given starting assembly list.
        /// </summary>
        /// <param name="startingList">The starting list.</param>
        /// <returns>A new <c>java.net.URLClassLoader</c></returns>
        //[CLSCompliant(false)]
        public static ClassLoader CreateLoader(List<Assembly> startingList)
        {
            URL[] urls = IkvmResourceLoaderFactory.FindResourceURLClosure(
                startingList);

            return new URLClassLoader(urls);
        } 
        #endregion

        #region FindResourceURLClosure(List<Assembly>)
        /// <summary>
        /// <para>
        /// Finds the resource URL closure for the given starting assembly list.
        /// </para>
        /// The closure is computed recursively from the referenced assemblies;
        /// satellite assemblies for the current culture are included also.
        /// </summary>
        /// <remarks>
        /// Referenced System, IKVM, Hsqldb core and NUnit assemblies are excluded.
        /// </remarks>
        /// <param name="startingList">The starting assembly list.</param>
        /// <returns>
        /// An array of <c>ikvmres:</c> protocol URLs describing the useful portion
        /// of the reference closure for the starting list.
        /// </returns>
        //[CLSCompliant(false)]
        public static URL[] FindResourceURLClosure(List<Assembly> startingList)
        {
            if (startingList == null)
            {
                throw new ArgumentNullException("startingList");
            }

            Set included = new HashSet();
            Set encountered = new HashSet();

            foreach (Assembly entry in startingList)
            {
                AddAssembly(encountered, included, entry);
            }

            Assembly[] assemblies = new Assembly[included.size()];

            included.toArray(assemblies);

            URL[] urls = new URL[included.size()];

            for (int i = 0; i < assemblies.Length; i++)
            {
                Assembly entry = assemblies[i];
                string entryName = URLEncoder.encode(entry.FullName, "UTF-8");
                string entryUrl = string.Concat("ikvmres://", entryName, "/");

                try
                {
                    urls[i] = new URL(entryUrl);
                }
                catch (Exception ex)
                {
#if DEBUG
                    Debug.WriteLine("URL: " + entryUrl);
                    Debug.WriteLine(ex);
#endif
                }
            }

            return urls;
        } 
        #endregion

        #region AddAssembly(Set,Set,Assembly)
        /// <summary>
        /// Adds the given assembly to the included set, if and only
        /// if it is not contained by the encountered set and it
        /// is not a system assembly; a core HSQLDB assembly; an
        /// IKVM assembly; an NUnit assembly or a TestDriven.NET
        /// assembly.
        /// </summary>
        /// <remarks>
        /// The exclusion list could be made much larger to produce
        /// a higher quality included set, but at what price?
        /// </remarks>
        /// <param name="encountered">The assemblies encountered so far.</param>
        /// <param name="included">The assemblies included so far.</param>
        /// <param name="entry">The assembly to add.</param>
        internal static void AddAssembly(
            Set encountered,
            Set included,
            Assembly entry)
        {
            if (entry == null || encountered.contains(entry))
            {
                return;
            }
            else
            {
                encountered.add(entry);
            }

            string simpleName = entry.GetName().Name;

            // ignored (performance optimization)
            if (simpleName == "System"
                || simpleName == "mscorlib"
                || simpleName == "Org.Hsqldb"
                || simpleName.StartsWith("System.")
                || simpleName.StartsWith("IKVM.")
                || simpleName.StartsWith("nunit.")
                || simpleName.StartsWith("TestDriven."))
            {
                return;
            }
            else
            {
                included.add(entry);
            }

            try
            {
                Assembly satellite =
                    entry.GetSatelliteAssembly(CultureInfo.CurrentCulture);

                IkvmResourceLoaderFactory.AddAssembly(
                    encountered,
                    included,
                    satellite);
            }
            catch (Exception ex)
            {
#if DEBUG
                Debug.WriteLine(ex);
#endif
            }

            AssemblyName[] referencedAssemblies
                = entry.GetReferencedAssemblies();

            for (int i = 0; i < referencedAssemblies.Length; i++)
            {
                AssemblyName assemblyName = referencedAssemblies[i];

                try
                {
                    Assembly referencedAssembly
                        = Assembly.Load(assemblyName);

                    IkvmResourceLoaderFactory.AddAssembly(
                        encountered,
                        included,
                        referencedAssembly);
                }
                catch (System.Exception ex)
                {
#if DEBUG
                    Debug.WriteLine(ex);
#endif
                }
            }
        } 
        #endregion
    }

    #endregion
}
