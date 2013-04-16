/**
 * Copyright 2009 Humboldt University of Berlin, INRIA.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 *
 */
package de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.eclipse.emf.common.util.URI;

import de.hu_berlin.german.korpling.saltnpepper.pepperModules.tigerModules.exceptions.InvalidPropertyException;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.SSpan;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCommon.sDocumentStructure.STYPE_NAME;
import de.hu_berlin.german.korpling.saltnpepper.salt.saltCore.SRelation;
import de.hu_berlin.german.korpling.tiger2.Edge;
import de.hu_berlin.german.korpling.tiger2.Segment;

/**
 * This class provides simple methods to access properties to customize a mapping of a <tiger2/> model to Salt or a Salt model to
 * a <tiger2/> model. This class also contains the names of all available properties. 
 * 
 * @author Florian Zipser
 *
 */
public class Tiger2Properties 
{
	public static final String PREFIX_PROP= "pepperModules.tigerModules.";
	public static final String PREFIX_IMPORTER_PROP= PREFIX_PROP+ "importer.";
	
	/**
	 * This flag determines if a SSpan object shall be created for each segment.
	 * Must be mappable to a {@link Boolean} value.
	 */
	public static final String PROP_CREATE_SSPAN= PREFIX_IMPORTER_PROP+"createSSpan4Segment";
	
	/**
	 * Property to determine, which {@link Edge} type shall be mapped to which kind of {@link SRelation}.
	 * This is just a prefix of the real property, which has a suffix specifying the {@link Edge} type. For instance
	 * {@value #PROP_EDGE_2_SRELATION}.dep or {@value #PROP_EDGE_2_SRELATION}.prim.
	 */
	public static final String PROP_EDGE_2_SRELATION= PREFIX_IMPORTER_PROP+"map";
	
	/**
	 * Creates a {@link Tiger2Properties} object and initializes it with the given {@link Properties} object. All properties
	 * contained in the given {@link Properties} object will be loaded and be available via simple accessors.
	 * All properties will be checked if they are valid.
	 * 
	 * @param props
	 * @return
	 */
	public static Tiger2Properties createTiger2Properties(File file)
	{
		if (file== null)
			throw new InvalidPropertyException("Cannot create a '"+Tiger2Properties.class.getSimpleName()+"' property object, because the given filename to load properties from is empty.");
		if (!file.exists())
			throw new InvalidPropertyException("Cannot create a '"+Tiger2Properties.class.getSimpleName()+"' property object, because the given file '"+file.getAbsolutePath()+"' to load properties from does not exist.");
		
		Properties props= new Properties();
		FileInputStream in= null;
		try {
			in= new FileInputStream(file);
			props= new Properties();
			props.load(in);
		} catch (FileNotFoundException e) {
			throw new InvalidPropertyException("Cannot create a '"+Tiger2Properties.class.getSimpleName()+"' property object, because the property file '"+file.getAbsolutePath()+"' does not exist.",e);
		} catch (IOException e) {
			throw new InvalidPropertyException("Cannot create a '"+Tiger2Properties.class.getSimpleName()+"' property object, because the property file '"+file.getAbsolutePath()+"' does not exist.",e);
		}
		finally
		{
			if (in!= null)
			{
				try {
					in.close();
				} catch (IOException e) 
				{
					throw new InvalidPropertyException("Cannot close stream to cosmat-properties.", e);
				}
			}
		}
		return(createTiger2Properties(props));
	}
	
	/**
	 * Creates a {@link Tiger2Properties} object and initializes it with the given {@link Properties} object. All properties
	 * contained in the given {@link Properties} object will be loaded and be available via simple accessors.
	 * All properties will be checked if they are valid.
	 * 
	 * @param props
	 * @return
	 */
	public static Tiger2Properties createTiger2Properties(URI uri)
	{
		if (uri== null)
			throw new InvalidPropertyException("Cannot create a '"+Tiger2Properties.class.getSimpleName()+"' property object, because the uri pointing to a file to load properties from is empty.");
		
		File file= new File(uri.toFileString());
		return(createTiger2Properties(file));
	}
	
	/**
	 * Creates a {@link Tiger2Properties} object and initializes it with the given {@link Properties} object. All properties
	 * contained in the given {@link Properties} object will be loaded and be available via simple accessors.
	 * All properties will be checked if they are valid.
	 * 
	 * @param props
	 * @return
	 */
	public static Tiger2Properties createTiger2Properties(Properties props)
	{
		return(new Tiger2Properties(props));
	}
	
	/**
	 * The {@link Properties} object where properties are stored.
	 */
	private Properties props= null;
	
	/**
	 * Returns the {@link Properties} object where properties are stored.
	 * @return
	 */
	public Properties getProps() {
		return props;
	}

	/**
	 * Sets the {@link Properties} object where properties are stored.
	 * @param props
	 */
	public void setProps(Properties props) {
		this.props = props;
	}

	/**
	 * Creates a {@link Tiger2Properties} object and initializes it with the given {@link Properties} object. All properties
	 * contained in the given {@link Properties} object will be loaded and be available via simple accessors.
	 * All properties will be checked if they are valid.
	 * 
	 * @param props
	 * @return
	 */
	private Tiger2Properties(Properties props)
	{
		this.checkProperties(props);
		this.setProps(props);
	}
	
	/**
	 * Checks if all given properties have valid values.
	 * @param props
	 * @return true, if all values are valid
	 */
	public boolean checkProperties(Properties props)
	{
		return(true);
	}
	
	/**
	 * Returns if all {@link Segment} objects shall be mapped to {@link SSpan} objects. If the value {@value #PROP_CREATE_SSPAN} is
	 * not set, the default is <code>true</code>. 
	 * @return
	 */
	public boolean propCreateSSpan4Segment()
	{
		String valueStr= this.getProps().getProperty(PROP_CREATE_SSPAN);
		if (valueStr!= null)
		{
			if ("true".equalsIgnoreCase(valueStr))
				return(true);
			else if ("false".equalsIgnoreCase(valueStr))
				return(false);
			else
			{// an unreadable value, a warning should be thrown
				return(true);
			}
		}
		else return(true);
	}
	
	/**
	 * Returns a list containing all mappings from {@link Edge} types to derivates of {@link SRelation}. 
	 * @return
	 */
	public Map<String, STYPE_NAME> propEdge2SRelation()
	{
		Map<String, STYPE_NAME> retVal= Collections.synchronizedMap(new Hashtable<String, STYPE_NAME>());
		
		Set<Object> keys= this.getProps().keySet();
		
		for (Object key :keys)
		{
			if (	(key != null)&&
					(key instanceof String))
			{
				if (key.toString().startsWith(PROP_EDGE_2_SRELATION))
				{
					STYPE_NAME saltType = STYPE_NAME.get(this.getProps().getProperty(key.toString()));
					String edgeType= key.toString().replace(PROP_EDGE_2_SRELATION+".", "");
					if (	(saltType!= null)&&
							(edgeType!= null))
						retVal.put(edgeType, saltType);
				}
			}
		}
		return(retVal);
	}
}
