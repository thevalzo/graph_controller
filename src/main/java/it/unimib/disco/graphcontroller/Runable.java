
package it.unimib.disco.graphcontroller;

import it.unimib.disco.janusgraphaccesscontrol.GraphAccessControl;
import java.util.*;
import java.util.stream.Collectors;

import javax.script.ScriptEngine;

import java.io.*;

import org.apache.tinkerpop.gremlin.groovy.jsr223.GremlinGroovyScriptEngine;
import org.apache.tinkerpop.gremlin.jsr223.JavaTranslator;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.GraphTraversalSource;
import org.apache.tinkerpop.gremlin.process.traversal.dsl.graph.__;
import org.apache.tinkerpop.gremlin.process.traversal.strategy.decoration.*;
//import org.apache.tinkerpop.gremlin.process.traversal.Bindings;
import org.apache.tinkerpop.gremlin.process.traversal.*;
import com.datastax.*;
import com.tinkerpop.pipes.util.iterators.*;
import com.tinkerpop.gremlin.java.*;
import com.tinkerpop.pipes.*;
import org.apache.tinkerpop.gremlin.structure.Graph;
import org.apache.tinkerpop.gremlin.structure.Vertex;
import org.apache.tinkerpop.gremlin.structure.Edge;
import org.apache.tinkerpop.gremlin.structure.io.graphml.*;

import org.apache.tinkerpop.gremlin.tinkergraph.structure.*;
import org.apache.tinkerpop.gremlin.util.*;
import org.janusgraph.core.JanusGraphFactory;

public class Runable {
public static void main(String[] args) {
		// TODO Auto-generated method stub
				//Graph graph = JanusGraphFactory.open("conf/janusgraph-hbase-es.properties");
		        try{

			        Graph graph =  TinkerGraph.open();
			        graph.io(GraphMLIo.build()).readGraph("C:\\Users\\marco\\Documents\\Workspaces\\janusgraphaccesscontrol\\data\\users-privileges-civilian.graphml");
			        GraphTraversalSource g=graph.traversal();   
			        
			        ScriptEngine engine = new GremlinGroovyScriptEngine();
			        javax.script.Bindings bindings = engine.createBindings();
			        bindings.put("g", graph.traversal());
			        Traversal<Vertex, ?> gremlin = (Traversal<Vertex, ?>) engine.eval("g.V().has(\"label_vp\",\"resource\")", bindings);
			        Bytecode traversalByteCode1 = gremlin.asAdmin().getBytecode();
	        		try {
	        		printWithHeadline("Result", String.join(System.lineSeparator(),JavaTranslator.of(graph.traversal()).translate(traversalByteCode1).toStream().map(Object::toString).collect(Collectors.toList())));
	        		}catch(Exception e){e.printStackTrace();}
	        		//System.out.println("traversal profile : "+ gremlin.profile().toList());
	                System.out.println(g.V().next(20).toString());

			        	
			        //System.out.println("gremlin " +pipe.iterator());

			        
			        String query = "SELECT * WHERE { ?a v:label_vp 'resource' }";
	                Traversal<Vertex, ?> sparq_sg = SparqlToGremlinCompiler.convertToGremlinTraversal(graph, query);
	        		Bytecode traversalByteCode = sparq_sg.asAdmin().getBytecode();
	        		try {
	        		printWithHeadline("Result", String.join(System.lineSeparator(),JavaTranslator.of(graph.traversal()).translate(traversalByteCode).toStream().map(Object::toString).collect(Collectors.toList())));
	        		}catch(Exception e){e.printStackTrace();}
	        		//System.out.println("traversal profile : "+ sparq_sg.profile().toList());
	                System.out.println(g.V().next(20).toString());

	                
	                GraphAccessControl gac = new GraphAccessControl(graph);
	                
	                GraphTraversalSource sg = gac.getCivilianStrategy("Rita Levi Montalcini");
	                 query = "SELECT * WHERE { ?a v:label_vp 'resource' }";
	                sparq_sg = SparqlToGremlinCompiler.convertToGremlinTraversal(sg, query);
	        		 traversalByteCode = sparq_sg.asAdmin().getBytecode();
	        		try {
	        		printWithHeadline("Result", String.join(System.lineSeparator(),JavaTranslator.of(graph.traversal()).translate(traversalByteCode).toStream().map(Object::toString).collect(Collectors.toList())));
	        		}catch(Exception e){e.printStackTrace();}
	        		//System.out.println("traversal profile : "+ sparq_sg.profile().toList());
	                System.out.println(sg.V().next(10).toString());
	                
	                Graph graphm =  TinkerGraph.open();
			        graphm.io(GraphMLIo.build()).readGraph("C:\\Users\\marco\\Documents\\Workspaces\\janusgraphaccesscontrol\\data\\users-privileges-military.graphml");   
			        GraphAccessControl gacm = new GraphAccessControl(graphm);
	                
			        GraphTraversalSource sgm = gacm.getMilitaryStrategy("Rita Levi Montalcini");
	                System.out.println(sgm.V().next(10).toString());
	                
	                
		        }catch (Exception e){
		        e.printStackTrace();
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
