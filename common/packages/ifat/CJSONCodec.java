/************************************************************
 *                                                          *
 *  Contents of file Copyright (c) Moogsoft Inc 2010        *
 *                                                          *
 *----------------------------------------------------------*
 *                                                          *
 *  WARNING:												*
 *  THIS FILE CONTAINS UNPUBLISHED PROPRIETARY				*
 *  SOURCE CODE WHICH IS THE PROPERTY OF MOOGSOFT INC AND	*
 *  WHOLLY OWNED SUBSIDIARY COMPANIES.						*
 *  PLEASE READ THE FOLLOWING AND TAKE CAREFUL NOTE:		*
 *															*
 *  This source code is confidential and any person who		*
 *  receives a copy of it, or believes that they are viewing*
 *  it without permission is asked to notify Phil Tee		*
 *  on 07734 591962 or email to phil@moogsoft.com.			*
 *  All intellectual property rights in this source code	*
 *  are owned by Moogsoft Inc.  No part of this source		*
 *  code may be reproduced, adapted or transmitted in any	*
 *  form or by any means, electronic, mechanical,			*
 *  photocopying, recording or otherwise.					*
 *															*
 *  You have been warned....so be good for goodness sake...	*
 *															*
 ************************************************************/
package com.moogsoft.ifat;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.JsonStringEncoder;
import com.fasterxml.jackson.databind.ObjectMapper;


//-----------------------------------------------------------------
//
// CJSONCodec
//
// Wrapper around a 3rd party JSON parser - in this case it is jackson
//
//-----------------------------------------------------------------
public class CJSONCodec
{
    private static ObjectMapper msMapper;
    private static JsonFactory msFactory;

    //
    // Static initialization block.
    // ObjectMapper (Databind) for serialization.
    // JsonFactory (Streaming) for deserialization.
    //
    static
    {
        msFactory = new JsonFactory();
        msFactory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        msFactory.enable(JsonParser.Feature.ALLOW_NON_NUMERIC_NUMBERS);
        msFactory.enable(JsonParser.Feature.ALLOW_UNQUOTED_FIELD_NAMES);
        msFactory.enable(JsonParser.Feature.ALLOW_SINGLE_QUOTES);
        msFactory.enable(JsonParser.Feature.ALLOW_NUMERIC_LEADING_ZEROS);
        msMapper = new ObjectMapper(msFactory);
    }

    //
    // Parse a file, assumed to contain JSON, down to a map or a list
    //
    public static Object decodeFile(String fPath)
    {
        Object obj = null;
        // check we have a valid filename
        if( fPath == null || fPath.length() == 0 )
        {
            CLogger.logger().info( "No file name suppplied" );
            return null;
        }

        String jsonStr = getFileContents(fPath);

        if (jsonStr == null)
        {
            CLogger.logger().warning( "Failed to parse file %s, returned null contents", fPath);
        }
        else
        {
            jsonStr = CJSONCodec.removeHashComments(jsonStr);
            obj = CJSONCodec.decode( jsonStr );
            if (obj == null)
            {
                CLogger.logger().warning( "Failed to parse file %s, JSON %s", fPath, jsonStr );
            }
        }
        return obj;
    }

    //
    // Parse a file, assumed to contain JSON, down to a map
    //
	@SuppressWarnings("unchecked")
	public static Map<String, Object> decodeFileToMap(String fPath)
    {
		Map<String, Object> ret = null;
        Object obj = CJSONCodec.decodeFile(fPath);
		if(obj != null)
		{
			if(obj instanceof Map)
			{
				ret = (Map<String, Object>)obj;
			}
		}
		
		return ret;
    }

    //
    // Parse json down to a map only
    //
	public static Map<String,Object> decodeMap(String json)
	{
	    JsonParser jp = null;
	    Map<String,Object> map = null;
	    // ensure we have something to parse...
        if ( ( json == null ) || ( json.length() == 0 ) )
        {
            return null;
        }
        try
        {
            jp = msFactory.createJsonParser(json);
            JsonToken current = jp.nextToken();
            if (current != JsonToken.START_OBJECT)
            {
                throw new IOException("Unexpected token " + current);
            }
            map = CJSONCodec.parseObject(jp);
        }
        catch (IOException e)
        {
            CLogger.logger().warning("Json Decode Error: %s [%s]", e.toString(), json);
        }
        finally
        {
            if (null != jp)
            {
                try
                {
                    jp.close();
                }
                catch (IOException ioe)
                {
                    CLogger.logger().warning("Json Decode Error: %s (Unable to close json parser)", ioe.toString());
                }
            }
        }
        return map;
	}

	//
	// Parse json down to a map or list object
	//
	public static Object decode(String json)
	{
	    JsonParser jp = null;
		Object ret = null;
	    // ensure we have something to parse...
        if ( ( json == null ) || ( json.length() == 0 ) )
        {
            return null;
        }
        try
        {
            jp = msFactory.createJsonParser(json);
			jp.nextToken();
			ret = CJSONCodec.parseAnything(jp);
        }
        catch (IOException e)
        {
            CLogger.logger().warning("Json Decode Error: %s [%s]", e.toString(), json);
			ret = null;
        }
        finally
        {
            if (null != jp)
            {
                try
                {
                    jp.close();
                }
                catch (IOException ioe)
                {
                    CLogger.logger().warning("Json Decode Error: %s (Unable to close json parser)", ioe.toString());
                }
            }
        }
		
		//
		// Since RFC-4627 Support only top level Map or List as JSON text we
		// need to null all other object types. More details in
		// http://stackoverflow.com/questions/19569221/did-the-publication-of-ecma-404-affect-the-validity-of-json-texts-such-as-2-or
		//
		if(!(ret instanceof Map) &&
		   !(ret instanceof List))
		{
			CLogger.logger().warning("Expecting a JSON object (map or list), not [%s]", json);
			ret = null;
		}
        return ret;
	}
	
	
	//
	// Take a map and convert to a JSON string
	//
    public static String encode(Map<String, Object> map)
    {
        String jsonString = null;
        StringWriter strWriter = null;
        //return {} was added for compatibility with the json.org original implementation
        if (map == null)
        {
            return "{}";
        }
        try
        {
            strWriter = new StringWriter();
            msMapper.writeValue(strWriter, map);
            jsonString = strWriter.toString();
        }
        catch (IOException e)
        {
            CLogger.logger().warning("%s: Unable to encode map", e.toString());
        }
        finally
        {
            if (strWriter != null)
            {
                try
                {
                    strWriter.close();
                }
                catch (IOException ioe)
                {
                    CLogger.logger().warning("%s: Unable to close string writer", ioe.toString());
                }
            }
        }
        return jsonString;
    }

	//
	// Take a list and convert to a JSON string
	//
    public static String encode(List<?> list)
    {
        String jsonString = null;
        StringWriter strWriter = null;

        try
        {
            strWriter = new StringWriter();
            msMapper.writeValue(strWriter, list);
            jsonString = strWriter.toString();
        }
        catch (IOException e)
        {
            CLogger.logger().warning("%s: Unable to encode list", e.toString());
        }
        finally
        {
            if (strWriter != null)
            {
                try
                {
                    strWriter.close();
                }
                catch (IOException ioe)
                {
                    CLogger.logger().warning("%s: Unable to close string writer", ioe.toString());
                }
            }
        }
        return jsonString;
    }

    //
    // Produce a quoted & escaped JSON "value string" ready to be used on
    // hand-crafted json strings.
    // e.g.
    //     IN = ' " \n
    //     OUT=  "\' \" \\n"
    //
    public static String quote(String val)
    {
        JsonStringEncoder jsonStringEncoder = JsonStringEncoder.getInstance();
        StringBuilder strBuilder = new StringBuilder("\"");
        strBuilder.append(jsonStringEncoder.quoteAsString(val));
        strBuilder.append('"');
        return strBuilder.toString();
    }

    //
    // Merge two pieces of JSON.  The base json will be overwritten with values
    // from the new json if the key is the same.  If the key/value only exists
    // in the base json it will not be changed.
    //
    // If there is a problem during the merge null will be returned.
    // If successfull then the merged json string will be returned.
    //
    public static String mergeToString(String baseJson, String newJson)
    {
        String mergedJson = null;
        Map<String, Object> mergedMap = CJSONCodec.mergeToMap(baseJson, newJson);
        if (mergedMap != null)
        {
            mergedJson = CJSONCodec.encode(mergedMap);
        }
        return mergedJson;
    }

    //
    // Merge two pieces of JSON.  The base json will be overwritten with values
    // from the new json if the key is the same.  If the key/value only exists
    // in the base json it will not be changed.
    //
    // If there is a problem during the merge null will be returned.
    // If successfull then the merged json map will be returned.
    //
    public static Map<String, Object> mergeToMap(String baseJson, String newJson)
    {
        Map<String, Object> mergedMap = null;
        try
        {
            Map<String, Object> baseMap = CJSONCodec.decodeMap(baseJson);
            Map<String, Object> newMap = CJSONCodec.decodeMap(newJson);

            mergedMap = CJSONCodec.deepMerge(baseMap, newMap);
        }
        catch (Exception e)
        {
            CLogger.logger().warning("Problem merging json: [%s]", e);
        }
        return mergedMap;
    }

    //
    // Recursive helper method to merge two maps
    //
    // Warning: This method typically modifies and returns the baseMap so
    // a deep copy should be passed to this method if that is a concern.
    //
    private static Map<String, Object> deepMerge(Map<String, Object> baseMap, Map<String, Object> newMap)
    {
        Map<String, Object> mergedMap = null;
        if (baseMap == null)
        {
            //
            // If the base map is null then simply set the merged map to be the
            // new map.
            //
            mergedMap = newMap;
        }
        else
        {
            if (newMap != null)
            {
                //
                // If both the base and new maps are not null or then they need
                // to be properly merged.
                //
                for (String key : newMap.keySet())
                {
                    if ((newMap.get(key) instanceof Map) &&
                        (baseMap.get(key) instanceof Map))
                    {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> baseChild = (Map<String, Object>) baseMap.get(key);

                        @SuppressWarnings("unchecked")
                        Map<String, Object> newChild = (Map<String, Object>) newMap.get(key);

                        Map<String, Object> mergedChild = CJSONCodec.deepMerge(baseChild, newChild);

                        baseMap.put(key, mergedChild);
                    }
                    else
                    {
                        baseMap.put(key, newMap.get(key));
                    }
                }
            }

            //
            // If the new map is null then simply set the merged map to be
            // unmodified base map.
            //
            mergedMap = baseMap;
        }
        return mergedMap;
    }

    //
    // For FILES ONLY
    // Strip comments from the JSON string.
    //
    // NOTE:
    // At present we use a VERY naive way of removing # based comments
    // which simply removes from a # to the end of the line.
    // It is fine provided the "value" doesn't contain a #
    //
    private static String removeHashComments(String jsonStr)
    {
        //TODO: Remove this comment method and use jackson parsing instead
        return jsonStr.replaceAll( "#.*\n", "" );
    }

	//
	// Parse any kind of valid JSON object
	//
	private static Object parseAnything(JsonParser jp) throws IOException
	{
		JsonToken current = jp.getCurrentToken();
		if(current == null)
		{
			throw new IOException("Unexpected empty token");
		}
		switch(current)
		{
			case START_ARRAY:
				return CJSONCodec.parseArray(jp);
			case START_OBJECT:
				return CJSONCodec.parseObject(jp);
			default:
				return CJSONCodec.parsePrimitive(jp,current);
		}
	}
    //
    // Streaming reader with conversion to Map
    //
    private static Map<String,Object> parseObject(JsonParser jp) throws IOException
    {
        HashMap<String,Object> map = new HashMap<String,Object>();
        while (jp.nextToken() != JsonToken.END_OBJECT)
        {
			//
			// Making sure that the current token is field name as one would
			// expect within a Map before the value...
			//
			if(jp.getCurrentToken() != JsonToken.FIELD_NAME)
			{
				throw new IOException("Expecting Field name, got " + jp.getCurrentToken() );
			}
			
			//
			// Get the field name
			//
            String fieldName = jp.getCurrentName();
			
			//
			// Get the object
			//
			jp.nextToken();
			Object value = CJSONCodec.parseAnything(jp);
			
			//
			// Adding them to the map
			//
            map.put(fieldName, value);
        }
        return map;
    }

    //
    // Streaming reader with conversion to List
    //
    private static List<Object> parseArray(JsonParser jp) throws IOException
    {
        ArrayList<Object> list = new ArrayList<Object>();
        while (jp.nextToken() != JsonToken.END_ARRAY)
        {
			Object value = CJSONCodec.parseAnything(jp);
			list.add(value);
        }
        return list;
    }

    //
    // Streaming reader with conversion to primitive
    //
    private static Object parsePrimitive(JsonParser jp, JsonToken current) throws IOException
    {
        Object primitiveObject = null;
        if (current == JsonToken.VALUE_STRING)
        {
            primitiveObject = jp.getText();
        }
        else if (current == JsonToken.VALUE_FALSE || current == JsonToken.VALUE_TRUE)
        {
            primitiveObject = jp.getBooleanValue();
        }
        else if (current == JsonToken.VALUE_NUMBER_FLOAT)
        {
            try
            {
                primitiveObject = jp.getDoubleValue();
            }
            catch (Exception e)
            {
                primitiveObject = jp.getText();
            }
        }
        else if (current == JsonToken.VALUE_NUMBER_INT)
        {
            try
            {
                primitiveObject = jp.getLongValue();
            }
            catch (Exception e)
            {
                primitiveObject = jp.getText();
            }
        }
        else if (current == JsonToken.VALUE_NULL)
        {
            primitiveObject = null;
        }
        else
        {
            throw new IOException("Unexpected token " + current);
        }
        return primitiveObject;
    }

    //
    // Get the contents of a file as a string (typically for configuration files)
    //
    private static String getFileContents(String fPath)
    {
        FileInputStream fStrm = null;
        String  fileContents = null;
        try
        {
            // create an input stream to read the file contents
            fStrm = new FileInputStream(fPath);
            int fSize = fStrm.available();
            byte[] buf=new byte[fSize];
            int readed=fStrm.read(buf);
            if (readed != fSize)
            {
                CLogger.logger().info("Unable to read file %s", fPath);
            }
            else
            {
                fileContents = new String(buf);
            }
        }
        catch(IOException e)
        {
            CLogger.logger().info("%s: Unable to open file %s", e.toString(), fPath);
        }
        finally
        {
            if (fStrm != null)
            {
                try
                {
                    fStrm.close();
                }
                catch (IOException ioe)
                {
                    CLogger.logger().warning("%s: Unable to close file stream", ioe.toString());
                }
            }
        }
        return fileContents;
    }

}
