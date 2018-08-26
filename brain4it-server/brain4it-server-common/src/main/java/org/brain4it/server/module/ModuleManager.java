/*
 * Brain4it
 * 
 * Copyright (C) 2018, Ajuntament de Sant Feliu de Llobregat
 * 
 * This program is licensed and may be used, modified and redistributed under 
 * the terms of the European Public License (EUPL), either version 1.1 or (at 
 * your option) any later version as soon as they are approved by the European 
 * Commission.
 * 
 * Alternatively, you may redistribute and/or modify this program under the 
 * terms of the GNU Lesser General Public License as published by the Free 
 * Software Foundation; either  version 3 of the License, or (at your option) 
 * any later version. 
 *   
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT 
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. 
 *   
 * See the licenses for the specific language governing permissions, limitations 
 * and more details.
 *   
 * You should have received a copy of the EUPL1.1 and the LGPLv3 licenses along 
 * with this program; if not, you may find them at: 
 *   
 *   https://joinup.ec.europa.eu/software/page/eupl/licence-eupl
 *   http://www.gnu.org/licenses/ 
 *   and 
 *   https://www.gnu.org/licenses/lgpl.txt
 */

package org.brain4it.server.module;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.brain4it.lang.BList;
import org.brain4it.lang.Function;
import org.brain4it.lib.CoreLibrary;
import org.brain4it.lib.Library;
import org.brain4it.lib.LibraryFactory;
import org.brain4it.lib.ModuleLibrary;
import org.brain4it.server.store.Entry;
import org.brain4it.server.store.Store;
import static org.brain4it.server.store.Store.*;
import static org.brain4it.server.ServerConstants.*;

/**
 *
 * @author realor
 */
public class ModuleManager
{
  private final String name;
  private final Store store;
  private final ModuleMap modules;
  private final ArrayList<Library> libraries = new ArrayList<Library>();
  private final Map<String, Function> functions;
  private final boolean multiTenant;
  private String accessKey;
  private final File accessKeyFile;
  private final Properties accessKeys;
  private long accessKeyFileLastRead;
  private long accessKeyFileLastModified;

  private static final Logger LOGGER = Logger.getLogger("ModuleManager");

  public ModuleManager(String name, Store store)
  {
    this(name, store, false, null, null);
  }

  public ModuleManager(String name, Store store, String accessKey)
  {
    this(name, store, false, accessKey, null);
  }
  
  public ModuleManager(String name, Store store, boolean multiTenant, 
    String accessKey, File accessKeyFile)
  {
    this.name = name;
    this.libraries.add(new CoreLibrary());
    this.libraries.add(new ModuleLibrary());
    this.functions = Collections.synchronizedMap(
      new HashMap<String, Function>());
    this.multiTenant = multiTenant;
    this.modules = multiTenant ? 
      new MultiTenantModuleMap() : new SingleTenantModuleMap();
    this.store = store;
    this.accessKey = accessKey;
    this.accessKeyFile = accessKeyFile;
    this.accessKeys = new Properties();

    LOGGER.log(Level.INFO, "Store: {0}", store.getClass().getName());
    LOGGER.log(Level.INFO, "Multi-tenant mode: {0}", multiTenant);          
    LOGGER.log(Level.INFO, "Access key: {0}", accessKeyFile != null);
    LOGGER.log(Level.INFO, "Access key file: {0}", accessKeyFile);
  }

  public String getName()
  {
    return name;
  }
  
  public boolean isMultiTenant()
  {
    return multiTenant;
  }
 
  public Logger getLogger()
  {
    return LOGGER;
  }
  
  public List<Library> getLibraries()
  {
    return libraries;
  }
  
  public String getAccessKey()
  {
    return accessKey;
  }
  
  public synchronized String getAccessKey(String tenant) throws IOException
  {
    if (tenant == null)
      throw new IOException("Undefined tenant");

    if (accessKeyFile == null) return accessKey;
    
    long now = System.currentTimeMillis();
    if (now - accessKeyFileLastRead > 5000) // look for changes every 5 seconds 
    {
      accessKeyFileLastRead = now;
      if (accessKeyFile.lastModified() != accessKeyFileLastModified)
      {
        accessKeys.clear();
        if (accessKeyFile.exists())
        {
          FileInputStream is = new FileInputStream(accessKeyFile);
          try
          {
            LOGGER.log(Level.INFO, "Loading access key file {0}...", 
              accessKeyFile.getAbsolutePath());             
            accessKeys.load(is);
            accessKeyFileLastModified = accessKeyFile.lastModified();
          }
          finally
          {
            is.close();
          }
        }
      }
    }
    return accessKeys.getProperty(tenant);
  }
  
  public void addLibraries(String libraryClassNames)
  {
    if (libraryClassNames == null) return;
    String[] libraryClassNamesArray = libraryClassNames.split(",");
    for (String libraryClassName : libraryClassNamesArray)
    {
      try
      {
        Library library = LibraryFactory.createLibrary(libraryClassName);
        libraries.add(library);
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.SEVERE, "Can not add library {0}...", 
          libraryClassName);
      }
    }
  }

  public Map<String, Function> getFunctions()
  {
    return functions;
  }

  public Store getStore()
  {
    return store;
  }

  public void init() throws IOException
  {
    store.open();
    loadLibraries();
    loadModules();
  }

  public void destroy() throws IOException
  {
    saveModules();
    unloadLibraries();
    store.close();
  }

  public void loadLibraries()
  {
    LOGGER.log(Level.INFO, "Loading libraries...");
    
    functions.clear();
    for (int i = 0; i < libraries.size(); i++)
    {
      Library library = (Library)libraries.get(i);
      LOGGER.log(Level.INFO, "Loading library {0}", library.getName());
      library.load();
      functions.putAll(library.getFunctions());
    }
  }

  public void unloadLibraries()
  {
    LOGGER.log(Level.INFO, "Unloading libraries...");
    functions.clear();
    for (int i = 0; i < libraries.size(); i++)
    {
      Library library = (Library)libraries.get(i);
      LOGGER.log(Level.INFO, "Unloading library {0}", library.getName());
      library.unload();
    }
  }

  public void loadModules() throws IOException
  {
    LOGGER.log(Level.INFO, "Loading modules...");
    
    if (multiTenant)
    {
      List<Entry> entries = store.listEntries("", null);
      for (Entry entry : entries)
      {
        if (entry.isDirectory())
        {
          String tenant = entry.getPath();
          try
          {
            loadModules(tenant);
          }
          catch (IOException ex)
          {
            LOGGER.log(Level.SEVERE, "[{0}]: {1}", 
              new Object[]{tenant == null ? "" : tenant, ex.getMessage()});
          }
        }
      }
    }
    else
    {
      loadModules(null);
    }
  }
  
  public void loadModules(String tenant) throws IOException
  {
    if (multiTenant && tenant == null)
      throw new IOException("Undefined tenant");
    
    String path = tenant == null ? "" : tenant;
 
    List<Entry> entries = store.listEntries(path, null);

    for (Entry entry : entries)
    {
      if (entry.isDirectory())
      {
        String moduleName = entry.getName();
        String fullName = tenant == null ?
          moduleName : tenant + PATH_SEPARATOR + moduleName;
        Module module = new Module(this, tenant, moduleName);
        try
        {
          module.loadSnapshot();
          modules.putModule(module);
          LOGGER.log(Level.INFO, "Module {0} loaded successfully.", fullName);
          try
          {
            module.start();          
          }
          catch (Exception ex)
          {
            LOGGER.log(Level.SEVERE, "Module {0} start failed: {1}", 
              new Object[]{fullName, ex.toString()});
          }
        }
        catch (Exception ex)
        {
          LOGGER.log(Level.SEVERE, "Module {0} load failed: {1}", 
           new Object[]{fullName, ex.toString()});
          // continue loading tenantModules
        }
      }
    }
  }

  public void saveModules() throws IOException
  {
    LOGGER.log(Level.INFO, "Saving modules...");

    if (multiTenant)
    {
      for (String tenant : modules.getTenants())
      {
        try
        {
          saveModules(tenant);
        }
        catch (IOException ex)
        {
          LOGGER.log(Level.SEVERE, "[{0}]: {1}", 
            new Object[]{tenant == null ? "" : tenant, ex.getMessage()});            
        }
      }
    }
    else
    {
      saveModules(null);
    }
  }
  
  public void saveModules(String tenant) throws IOException
  {
    for (Module module : modules.getModules(tenant, false))
    {
      String fullName = module.getFullName();
      try
      {
        module.stop();
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.INFO, "Module {0} stop failed: {1}", 
          new Object[]{fullName, ex.toString()});
      }
      try
      {
        module.saveSnapshot();     
        LOGGER.log(Level.INFO, "Module {0} saved successfully.", fullName);
      }
      catch (Exception ex)
      {
        LOGGER.log(Level.SEVERE, "Module {0} save failed: {1}", 
          new Object[]{fullName, ex.toString()});
        // continue saving tenantModules
      }
    }
  }

  public BList listModules(String tenant) throws IOException
  {
    if (multiTenant && tenant == null)
      throw new IOException("Undefined tenant");
    
    BList moduleList = new BList();
    
    Collection<Module> tenantModules = modules.getModules(tenant, true);
    for (Module module : tenantModules)
    {
      Object metadata = module.get(MODULE_METADATA_VAR);
      if (metadata instanceof BList)
      {
        BList moduleInfo = new BList(2);
        moduleInfo.add(module.getName());
        moduleInfo.add(metadata);
        moduleList.add(moduleInfo);
      }
      else
      {
        moduleList.add(module.getName());
      }
    }
    return moduleList;
  }
  
  public Module getModule(String tenant, String moduleName, boolean notNull) 
    throws IOException
  {
    if (multiTenant && tenant == null)
      throw new IOException("Undefined tenant");

    Module module = (Module)modules.getModule(tenant, moduleName);
    if (module == null && notNull)
      throw new IOException("Module " + moduleName + " not found.");

    return module;
  }

  public Module createModule(String tenant, String moduleName, BList data) 
    throws IOException
  {
    if (multiTenant && tenant == null)
      throw new IOException("Undefined tenant");
    
    String fullName = multiTenant ? 
      tenant + PATH_SEPARATOR + moduleName : moduleName;
    
    LOGGER.log(Level.INFO, "Creating module {0}...", fullName);

    if (!isValidModuleName(moduleName))
    {
      throw new IOException("Invalid module name: " + moduleName);
    }

    Module module;
    
    synchronized (modules)
    {
      if (modules.getModule(tenant, moduleName) != null)
      {
        throw new IOException("Module " + moduleName + " already exists");
      }
      module = new Module(this, tenant, moduleName);
      modules.putModule(module);
    }

    try
    {
      module.init(data);
      module.saveSnapshot();
      LOGGER.log(Level.INFO, "Module {0} created.", fullName);
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Module {0} creation failed: {1}.", 
        new Object[]{fullName, ex.toString()});
    }
    return module;
  }

  public void destroyModule(String tenant, String moduleName)
    throws IOException
  {
    if (multiTenant && tenant == null)
      throw new IOException("Undefined tenant");

    String fullName = multiTenant ? 
      tenant + PATH_SEPARATOR + moduleName : moduleName;
    
    LOGGER.log(Level.INFO, "Destroying module {0}...", fullName);

    if (modules.getModule(tenant, moduleName) == null)
    {
      throw new IOException("Module does not exist: " + moduleName);
    }

    Module module = modules.removeModule(tenant, moduleName);
    try
    {
      module.stop();
    }
    catch (Exception ex)
    {
      LOGGER.log(Level.SEVERE, "Module {0} can not be stopped.", fullName);
    }
    store.deleteEntry(fullName);

    LOGGER.log(Level.INFO, "Module {0} destroyed.", fullName);
  }

  /* internal classes & functions */
  
  interface ModuleMap
  {
    Collection<String> getTenants();
    Collection<Module> getModules(String tenant, boolean sort);
    Module getModule(String tenant, String moduleName);
    void putModule(Module module);
    Module removeModule(String tenant, String moduleName);     
  }

  class SingleTenantModuleMap extends HashMap<String, Module> 
    implements ModuleMap
  {
    @Override
    public Collection<String> getTenants()
    {
      return Collections.singleton(null);
    }

    @Override
    public synchronized Collection<Module> getModules(String tenant, 
      boolean sort)
    {
      ArrayList<Module> moduleList = new ArrayList<Module>(values());
      if (sort)
      {
        sortModuleList(moduleList);
      }
      return moduleList;
    }

    @Override
    public synchronized Module getModule(String tenant, String moduleName)
    {
      return get(moduleName);
    }

    @Override
    public synchronized void putModule(Module module)
    {
      put(module.getName(), module);
    }

    @Override
    public synchronized Module removeModule(String tenant, String moduleName)
    {
      return remove(moduleName);
    }    
  }
  
  class MultiTenantModuleMap extends HashMap<String, Map<String, Module>> 
    implements ModuleMap
  {
    @Override
    public synchronized Collection<String> getTenants()
    {
      return new ArrayList<String>(keySet());
    }
    
    @Override
    public synchronized Collection<Module> getModules(String tenant, 
       boolean sort)
    {
      Map<String, Module> tenantModules = get(tenant);
      if (tenantModules == null) return Collections.EMPTY_LIST;
      
      ArrayList<Module> moduleList = 
        new ArrayList<Module>(tenantModules.values());
      if (sort)
      {
        sortModuleList(moduleList);
      }
      return moduleList;
    }
    
    @Override
    public synchronized Module getModule(String tenant, String moduleName)
    {
      Map<String, Module> tenantModules = get(tenant);
      return tenantModules == null ? null : tenantModules.get(moduleName);
    }
    
    @Override
    public synchronized void putModule(Module module)
    {
      Map<String, Module> tenantModules = get(module.getTenant());
      if (tenantModules == null)
      {
        tenantModules = Collections.synchronizedMap(
          new HashMap<String, Module>());
        put(module.getTenant(), tenantModules);
      }
      tenantModules.put(module.getName(), module);
    }
    
    @Override
    public synchronized Module removeModule(String tenant, String moduleName)
    {
      Map<String, Module> tenantModules = get(tenant);
      if (tenantModules != null)
      {
        return tenantModules.remove(moduleName);
      }     
      return null;
    }
  }
  
  private void sortModuleList(List<Module> moduleList)
  {
    Collections.sort(moduleList, new Comparator<Module>()
    {
      @Override
      public int compare(Module o1, Module o2)
      {
        return o1.getName().compareTo(o2.getName());
      }
    });    
  }
  
  private boolean isValidModuleName(String moduleName)
  {
    if (moduleName == null || moduleName.length() == 0) return false;
    int i = 0;
    boolean valid = true;
    while (valid && i < moduleName.length())
    {
      char ch = moduleName.charAt(i);
      if (ch == '_' || Character.isLetter(ch) || Character.isDigit(ch)) i++;
      else valid = false;
    }
    return valid;
  }
}
