#region licence

/* Copyright (c) 2001-2009, The HSQL Development Group
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
using Microsoft.Win32;
using System;
using System.Collections;
using System.ComponentModel;
using System.Configuration.Install;
using System.IO;
using System.Reflection;
using System.Xml;
#endregion

namespace System.Data.Hsqldb.Client
{
    #region HsqlInstaller
    /// <summary>
    /// <para>
    /// Registers the HSQLDB ADO.NET Data Provider in
    /// the <c>machine.config</c> file.
    /// </para>
    /// <img src="/ClassDiagrams/System.Data.Hsqldb.Client.HsqlInstaller.png"
    ///      alt="HsqlInstaller Class Diagram"/>
    /// </summary>
    [RunInstaller(true)]
    public class HsqlInstaller : Installer
    {
        #region Method Overrides

        #region Install(IDictionary)
        /// <summary>
        /// Performs the installation.
        /// </summary>
        /// <param name="stateSaver">
        /// An <see cref="IDictionary"/> used to save information
        /// needed to perform a commit, rollback, or uninstall
        /// operation.
        /// </param>
        /// <exception cref="ArgumentException">
        /// When the stateSaver parameter is null.
        /// </exception>
        /// <exception cref="Exception">
        /// When an exception occurrs in the <c>BeforeInstall</c>
        /// event handler of one of the installers in the collection.
        /// -or-
        /// An exception occurred in the <c>AfterInstall</c>
        /// event handler of one of the installers in the collection.
        /// </exception>
        public override void Install(IDictionary stateSaver)
        {
#if DEBUG
            Console.WriteLine("Installing System.Data.Hsqldb.Client...");
#endif
            base.Install(stateSaver);
        } 
        #endregion

        #region OnCommitted(IDictionary)
        /// <summary>
        /// Overridden to add assembly to the desired machine.config file(s).
        /// </summary>
        /// <param name="savedState">
        /// An <see cref="IDictionary"/> that contains the state of the computer
        /// after all the installers in the <see cref="Installer.Installers"/>
        /// property run.</param>
        protected override void OnCommitted(IDictionary savedState)
        {
            base.OnCommitted(savedState);
#if DEBUG
            Console.WriteLine("Adding HSQLDB to the system.data/DbProviderFactories section of the machine configuration file(s)...");
#endif
            AddProviderToMachineConfig();
#if DEBUG
            Console.WriteLine("Finished adding HSQLDB to the system.data/DbProviderFactories section of the machine configuration file(s).");
#endif
        } 
        #endregion

        #region Uninstall(IDictionary)
        /// <summary>
        /// Overridden to remove assembly from the desired
        /// <c>machine.config</c> file(s).
        /// </summary>
        /// <param name="savedState"/>
        public override void Uninstall(IDictionary savedState)
        {
#if DEBUG
            Console.WriteLine("Uninstalling System.Data.Hsqldb.Client...");
#endif
            base.Uninstall(savedState);
#if DEBUG
            Console.WriteLine("Removing HSQLDB from the system.data/DbProviderFactories section of the machine configuration file(s)...");            
#endif
            RemoveProviderFromMachineConfig();
#if DEBUG
            Console.WriteLine("Finished removing HSQLDB from the system.data/DbProviderFactories section of the machine configuration file(s).");
#endif
        }
        #endregion

        #endregion

        #region Static Methods

        #region GetInstallRoot()
        private static string GetInstallRoot()
        {
            object value = Registry.GetValue(
                @"HKEY_LOCAL_MACHINE\Software\Microsoft\.NETFramework\",
                "InstallRoot",
                null);

            if (value == null)
            {
                throw new NullReferenceException(
                    "Unable to retrieve install root for .NET framework");
            }

            return value.ToString();
        }
        #endregion

        #region ToInstallRoot64(string)
        private static string ToInstallRoot64(string path)
        {
            return string.Format(
                "{0}64{1}",
                path.Substring(0, path.Length - 1),
                Path.DirectorySeparatorChar);
        }
        #endregion

        #region GetConfigPath(string)
        private static string GetConfigPath(string installRoot)
        {
            return string.Format(
                @"{0}v2.0.50727\CONFIG\machine.config",
                installRoot);
        }
        #endregion

        #region ReadConfig(string)
        private static XmlDocument ReadConfig(string path)
        {
            XmlDocument doc = new XmlDocument();

            using (StreamReader sr = new StreamReader(path))
            {
                doc.LoadXml(sr.ReadToEnd());
            }

            return doc;
        }
        #endregion

        #region WriteConfig(string,XmlDocument)
        private static void WriteConfig(string path, XmlDocument doc)
        {
            using (XmlTextWriter writer = new XmlTextWriter(path, null))
            {
                writer.Formatting = Formatting.Indented;

                doc.Save(writer);
            }
        }
        #endregion

        #region GetFactoryType()
        private static string GetFactoryType()
        {
            return string.Format(
                "{0}, {1}",
                typeof(HsqlProviderFactory).FullName,
                typeof(HsqlProviderFactory).Assembly.FullName);
        }
        #endregion

        #region AddProviderToMachineConfig()
        private static void AddProviderToMachineConfig()
        {
            string installRoot = GetInstallRoot();

            AddProviderToMachineConfigInDir(installRoot);

            string installRoot64 = ToInstallRoot64(installRoot);

            if (Directory.Exists(installRoot64))
            {
                AddProviderToMachineConfigInDir(installRoot64);
            }
        }
        #endregion

        #region AddProviderToMachineConfigInDir(string)
        private static void AddProviderToMachineConfigInDir(string path)
        {
            string configPath = GetConfigPath(path);
            string factoryType = GetFactoryType();
            XmlDocument doc = ReadConfig(configPath);
            
            // TODO:  The following validation is weak, because it does not actually
            //        assert that the XPath absolutely must be:
            //        /configuration/configSections/system.data/DbProviderFactories

            XmlNodeList systemDataNodes = doc.GetElementsByTagName("system.data");

            if (systemDataNodes.Count != 1)
            {
                throw new InvalidOperationException(
                    "Machine config file is malformed.\n"
                    + "Single system.data element not found in:\n" 
                    + configPath); 
            }

            XmlNode systemDataNode = systemDataNodes[0];

            if (systemDataNode.ChildNodes.Count != 1 ||
                systemDataNode.ChildNodes[0].Name != "DbProviderFactories")
            {
                throw new InvalidOperationException(
                    "Machine config file is malformed.\n"
                    + "Single system.data/DbProviderFactories element not found in:\n"
                    + configPath); 
            }

            XmlNode bbProviderFactoriesNode = systemDataNode.ChildNodes[0];
            XmlNodeList factoryNodes = bbProviderFactoriesNode.ChildNodes;
            
            bool found = false;

            foreach (XmlNode factoryNode in factoryNodes)
            {
                XmlAttributeCollection attributes = factoryNode.Attributes;
                
                XmlAttribute typeAttribute = attributes["type"];

                if (typeAttribute == null)
                {
                    // malformed, but not *our* fault...
                    continue;
                }

                string typeAttributeValue = typeAttribute.Value;

                if (typeAttributeValue == factoryType)
                {
                    found = true;
                    // TODO: detect and handle case where there is >1 element with factoryType
                    // CHECKME: policy? {e.g. abort with exception, silently ignore all but first, other check(s)?}
                    break;
                }
            }

            if (!found)
            {
                XmlElement node = (XmlElement)doc.CreateNode(
                    XmlNodeType.Element, 
                    "add", 
                    "");

                node.SetAttribute("name", HsqlProviderFactory.FactoryName);
                node.SetAttribute("invariant", HsqlProviderFactory.FactoryInvariant);
                node.SetAttribute("description", HsqlProviderFactory.FactoryDescription);
                node.SetAttribute("type", factoryType);

                bbProviderFactoriesNode.AppendChild(node);

                WriteConfig(configPath, doc);
            }
        }
        #endregion

        #region RemoveProviderFromMachineConfig()
        private static void RemoveProviderFromMachineConfig()
        {
            string installRoot = GetInstallRoot();

            RemoveProviderFromMachineConfigInDir(installRoot);

            string installRoot64 = ToInstallRoot64(installRoot);

            if (Directory.Exists(installRoot64))
            {
                RemoveProviderFromMachineConfigInDir(installRoot64);
            }
        }
        #endregion

        #region RemoveProviderFromMachineConfigInDir(string)
        private static void RemoveProviderFromMachineConfigInDir(string path)
        {
            string configPath = GetConfigPath(path);
            string factoryType = GetFactoryType();
            XmlDocument doc = ReadConfig(configPath);

            // TODO:  The following validation is weak, because it does not actually
            //        assert that the XPath absolutely must be:
            //        /configuration/configSections/system.data/DbProviderFactories
            
            XmlNodeList systemDataNodes = doc.GetElementsByTagName("system.data");

            if (systemDataNodes.Count != 1)
            {
                throw new InvalidOperationException(
                    "Machine config file is malformed.\n"
                    + "Single system.data element not found in:\n"
                    + configPath);
            }

            XmlNode systemDataNode = systemDataNodes[0];

            if (systemDataNode.ChildNodes.Count != 1 ||
                systemDataNode.ChildNodes[0].Name != "DbProviderFactories")
            {
                throw new InvalidOperationException(
                    "Machine config file is malformed.\n"
                    + "Single system.data/DbProviderFactories element not found in:\n"
                    + configPath);
            }

            XmlNode bbProviderFactoriesNode = systemDataNode.ChildNodes[0];
            XmlNodeList factoryNodes = bbProviderFactoriesNode.ChildNodes;
            
            bool found = false;

            foreach (XmlNode factoryNode in factoryNodes)
            {
                XmlAttributeCollection attributes = factoryNode.Attributes;
                XmlAttribute typeAttribute = attributes["type"];

                if (typeAttribute == null)
                {
                    // malformed, but not *our* fault...
                    continue;
                }

                string typeAttributeValue = typeAttribute.Value;

                if (typeAttributeValue == factoryType)
                {
                    found = true;
                    bbProviderFactoriesNode.RemoveChild(factoryNode);
                    // TODO: detect and handle case where there is >1 element with factoryType
                    // CHECKME: policy? {e.g. abort with exception, silently remove all, ???}
                    break;
                }
            }

            if (found)
            {
                WriteConfig(configPath, doc);
            }
        }
        #endregion

        #endregion
    }
    #endregion
}
