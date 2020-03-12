package it.unimib.disco.graphcontroller;
/*
* Licensed to the Apache Software Foundation (ASF) under one
* or more contributor license agreements.  See the NOTICE file
* distributed with this work for additional information
* regarding copyright ownership.  The ASF licenses this file
* to you under the Apache License, Version 2.0 (the
* "License"); you may not use this file except in compliance
* with the License.  You may obtain a copy of the License at
*
*   http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing,
* software distributed under the License is distributed on an
* "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
* KIND, either express or implied.  See the License for the
* specific language governing permissions and limitations
* under the License.
*/



import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.*;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator;
import org.apache.tinkerpop.gremlin.process.traversal.Bytecode;
import org.apache.tinkerpop.gremlin.process.traversal.Traversal;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.io.IoCore;
import org.apache.tinkerpop.gremlin.structure.io.graphml.GraphMLIo;
import org.apache.tinkerpop.gremlin.structure.util.GraphFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerFactory;
import org.apache.tinkerpop.gremlin.tinkergraph.structure.TinkerGraph;
import org.janusgraph.core.JanusGraphFactory;

import com.datastax.SparqlToGremlinCompiler;

import it.unimib.disco.janusgraphaccesscontrol.GraphAccessControl;

class Controller {

   public static void main(final String[] args) throws IOException {
   	//args = "/examples/modern1.sparql";
       final Options options = new Options();
       options.addOption("u", "user", true, "Authenticated user");
       options.addOption("t", "type", true, "language of the query");
       options.addOption("l", "language", true, "language of the query");
       options.addOption("q", "query", true, "query to execute");
       
       Graph graph =  TinkerGraph.open();
       GraphTraversalSource g = graph.traversal();
       graph.io(GraphMLIo.build()).readGraph("C:\\Users\\marco\\Documents\\Workspaces\\janusgraphaccesscontrol\\data\\users-privileges-civilian.graphml");

       

       final CommandLineParser parser = new DefaultParser();
       final CommandLine commandLine;

       try {
           commandLine = parser.parse(options, args);
       } catch (ParseException e) {
           System.out.println(e.getMessage());
           printHelp(1);
           return;
       }

       ScriptEngine engine = new GremlinGroovyScriptEngine();
       javax.script.Bindings bindings = engine.createBindings();

       Long start_time = java.lang.System.currentTimeMillis();
       
       if (commandLine.hasOption("user")) {
    	   
    	   try {
	    	   String user = commandLine.getOptionValue("user");
	           GraphAccessControl gac = new GraphAccessControl(graph);
	           
	           if (commandLine.hasOption("type")) {
	        	   
	        	   String type = commandLine.getOptionValue("type");
	        	   if(type.equals("civilian")) {
	        		   g = gac.getCivilianStrategy(user);
	            	   bindings.put("g", gac.getCivilianStrategy(user));
	        	   }
	        	   else if (type.equals("military")) {
	        		   g = gac.getMilitaryStrategy(user);
	            	   bindings.put("g", gac.getMilitaryStrategy(user));
	        		  
	        	   }
	           }
    	   }
    	   catch(Exception e)
    	   {
    		   e.printStackTrace();
    	   }
    	}
       else {
    	   g = graph.traversal();
    	   bindings.put("g", graph.traversal());
       }
       
       try {
	       String language = "gremlin";
	       String query = "g.V()";

	       Traversal<Vertex, ?> traversal = (Traversal<Vertex, ?>) engine.eval(query, bindings);
	       
	       if (commandLine.hasOption("language")) {
	    	   language = commandLine.getOptionValue("language").toLowerCase();
	    			   }
	       if (commandLine.hasOption("query")) {
	    	   query = commandLine.getOptionValue("query");
	       } 
	       
	       if (language.equals("gremlin") ) {
	    	   
			       
			         traversal = (Traversal<Vertex, ?>) engine.eval(query, bindings);
	
	       }
	       else if (language.equals("sparql")){
	    	   traversal = SparqlToGremlinCompiler.convertToGremlinTraversal(g, query);
	       }

	       Bytecode traversalByteCode = traversal.asAdmin().getBytecode();
	       List<String> result = new ArrayList<String>(); 
	       result=JavaTranslator.of(graph.traversal()).translate(traversalByteCode).toStream().map(Object:: toString).collect(Collectors.toList());
	       Long end_time = java.lang.System.currentTimeMillis();
	       System.out.println(result);
	       System.out.println(end_time-start_time);
	   }
	   catch(Exception e) {
		   e.printStackTrace();
       }
}

   private static void printHelp(final int exitCode) throws IOException {
       final Map<String, String> env = System.getenv();
       final String command = env.containsKey("LAST_COMMAND") ? env.get("LAST_COMMAND") : "sparql-gremlin.sh";
       printWithHeadline("Usage Examples", String.join("\n",
               command + " -f examples/modern1.sparql",
               command + " < examples/modern2.sparql",
               command + " <<< 'SELECT * WHERE { ?a e:knows ?b }'",
               command + " -g crew < examples/crew1.sparql"));
       if (exitCode >= 0) {
           System.exit(exitCode);
       }
   }

   private static void printWithHeadline(final String headline, final Object content) throws IOException {
       final StringReader sr = new StringReader(content != null ? content.toString() : "null");
       final BufferedReader br = new BufferedReader(sr);
       String line;
       System.out.println();
       System.out.println( headline ); 
       System.out.println();
       boolean skip = true;
       while (null != (line = br.readLine())) {
           skip &= line.isEmpty();
           if (!skip) {
               System.out.println("  " + line);
           }
       }
       System.out.println();
       br.close();
       sr.close();
   }
}

